package teams.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import teams.Main;
import teams.Team;
import teams.customEvents.post.PostCreateTeamEvent;
import teams.customEvents.post.PostDisbandTeamEvent;
import teams.customEvents.post.PostPlayerJoinTeamEvent;
import teams.customEvents.post.PostPlayerLeaveTeamEvent;
import teams.events.MCTeamManagement.BelowNameType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Puts staff (OPs and everyone with the permission in {@code staff.permission}) at the top of the tab list,
 * with their name in the staff colour ({@code staff.color}). This works with or without a team and also
 * when {@code display.tab.enabled} is off.
 * <p>
 * The tab list is sorted by the name of the scoreboard team a player is in, and players without a
 * scoreboard team always come first. So staff get their own team named "!0..." and every other player
 * without a team is put in "!~others", which keeps the order: staff, everyone else.
 */
public class StaffTab implements Listener {

	private static final String OTHERS = "!~others";
	private static final String STAFF_PREFIX = "!0";

	private final Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
	/** the scoreboard team of every staff member that is online */
	private final Map<UUID, String> staffTeams = new HashMap<>();

	public static boolean isStaffPlayer(Player player) {
		return player.isOp()
				|| player.hasPermission(Main.plugin.getConfig().getString("staff.permission", "teams.admin"));
	}

	public boolean isStaff(Player player) {
		return isStaffPlayer(player);
	}

	/**
	 * @return the colour code of the staff colour, for example "§x§d§1..."
	 */
	private String colorCode() {
		String hex = Main.plugin.getConfig().getString("staff.color", "#D11F3F");
		if (hex == null || hex.isEmpty()) {
			hex = "#D11F3F";
		}
		if (!hex.startsWith("#")) {
			hex = "#" + hex;
		}
		try {
			return net.md_5.bungee.api.ChatColor.of(hex).toString();
		} catch (Exception e) {
			return org.bukkit.ChatColor.RED.toString();
		}
	}

	private org.bukkit.scoreboard.Team getOrCreate(String name) {
		org.bukkit.scoreboard.Team team = board.getTeam(name);
		return team != null ? team : board.registerNewTeam(name);
	}

	/**
	 * Gives the player their staff team (or their normal place in the tab list if they are not staff).
	 */
	public void refresh(Player player) {
		// with the TAB plugin, TabHook does this through TAB
		if (!player.isOnline() || Main.plugin.tabHook != null) {
			return;
		}
		String entry = player.getName();
		org.bukkit.scoreboard.Team current = board.getEntryTeam(entry);

		if (!isStaff(player)) {
			releaseStaffTeam(player);
			// players without a scoreboard team go in the "others" team, so staff stay above them
			if (board.getEntryTeam(entry) == null) {
				getOrCreate(OTHERS).addEntry(entry);
			}
			return;
		}

		// staff keep the tag of their team in front of / behind their name when display.tab is on
		String prefix = "";
		String suffix = "";
		Team team = Team.getTeam(player);
		MCTeamManagement management = Main.plugin.teamManagement;
		if (team != null && management != null && management.getType() != BelowNameType.FALSE
				&& player.hasPermission("teams.teamName")) {
			org.bukkit.scoreboard.Team teamBoard = team.getScoreboardTeam(board);
			prefix = teamBoard.getPrefix();
			suffix = teamBoard.getSuffix();
		}

		org.bukkit.scoreboard.Team staff = staffTeamOf(player);
		staff.setPrefix(prefix + colorCode());
		staff.setSuffix(suffix);
		if (current == null || !current.getName().equals(staff.getName())) {
			staff.addEntry(entry);
		}
	}

	private org.bukkit.scoreboard.Team staffTeamOf(Player player) {
		String existing = staffTeams.get(player.getUniqueId());
		if (existing != null && board.getTeam(existing) != null) {
			return board.getTeam(existing);
		}
		String base = STAFF_PREFIX + player.getName();
		if (base.length() > 14) {
			base = base.substring(0, 14);
		}
		int attempt = 0;
		while (true) {
			String name = base + (attempt == 0 ? "" : String.valueOf(attempt));
			org.bukkit.scoreboard.Team team = board.getTeam(name);
			if (team == null) {
				team = board.registerNewTeam(name);
				staffTeams.put(player.getUniqueId(), name);
				return team;
			}
			attempt++;
		}
	}

	private void releaseStaffTeam(Player player) {
		String name = staffTeams.remove(player.getUniqueId());
		if (name != null) {
			org.bukkit.scoreboard.Team team = board.getTeam(name);
			if (team != null) {
				team.unregister();
			}
		}
	}

	public void refreshAll() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			refresh(player);
		}
	}

	/**
	 * Removes every team made by this class (plugin disable)
	 */
	public void removeAll() {
		for (String name : staffTeams.values()) {
			org.bukkit.scoreboard.Team team = board.getTeam(name);
			if (team != null) {
				team.unregister();
			}
		}
		staffTeams.clear();
		org.bukkit.scoreboard.Team others = board.getTeam(OTHERS);
		if (others != null) {
			others.unregister();
		}
	}

	private void refreshLater() {
		Bukkit.getScheduler().runTask(Main.plugin, this::refreshAll);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		refreshLater();
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		releaseStaffTeam(e.getPlayer());
		org.bukkit.scoreboard.Team others = board.getTeam(OTHERS);
		if (others != null) {
			others.removeEntry(e.getPlayer().getName());
		}
	}

	@EventHandler
	public void onTeamCreate(PostCreateTeamEvent e) {
		refreshLater();
	}

	@EventHandler
	public void onTeamDisband(PostDisbandTeamEvent e) {
		refreshLater();
	}

	@EventHandler
	public void onTeamJoin(PostPlayerJoinTeamEvent e) {
		refreshLater();
	}

	@EventHandler
	public void onTeamLeave(PostPlayerLeaveTeamEvent e) {
		refreshLater();
	}
}
