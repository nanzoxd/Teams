package teams.events;

import teams.Main;
import teams.PlayerRank;
import teams.Team;
import teams.TeamPlayer;
import teams.customEvents.post.PostDemotePlayerEvent;
import teams.customEvents.post.PostPromotePlayerEvent;
import teams.customEvents.BelowNameChangeEvent;
import teams.customEvents.BelowNameChangeEvent.ChangeType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

public class MCTeamManagement implements Listener {

	final Scoreboard board;
	@Getter
	private final BelowNameType type;

	/**
	 * Used to track a list of all listeners
	 *
	 * @param type The type prefixing that should be done
	 */
	public MCTeamManagement(BelowNameType type) {
		this.type = type;

		Objects.requireNonNull(Bukkit.getScoreboardManager()).getMainScoreboard();
		board = Bukkit.getScoreboardManager().getMainScoreboard();

	}

	public void displayBelowNameForAll() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			displayBelowName(p);
		}
	}

	public void displayBelowName(Player player) {
		player.setScoreboard(board);

		// staff have their own scoreboard team (top of the tab list), StaffTab adds their team tag
		if (Main.plugin.staffTab != null && Main.plugin.staffTab.isStaff(player)) {
			Main.plugin.staffTab.refresh(player);
			return;
		}

		Team team = Team.getTeam(player);
		if (team == null) {
			return;
		}

		// checking the player has the correct permission node
		if (!player.hasPermission("teams.teamName")) {
			// player does not have permission to have their team name displayed.
			return;
		}

		BelowNameChangeEvent event = new BelowNameChangeEvent(player, ChangeType.ADD);
		Bukkit.getPluginManager().callEvent(event);

		try {
			// owners get a tag with the crown in it
			TeamPlayer teamPlayer = team.getTeamPlayer(player);
			boolean owner = teamPlayer != null && teamPlayer.getRank() == PlayerRank.OWNER && !Team.getOwnerCrown().isEmpty();
			(owner ? team.getOwnerScoreboardTeam(board) : team.getScoreboardTeam(board)).addEntry(player.getName());
		} catch (IllegalStateException e) {
			Main.plugin.getLogger().severe("Could not register the team name in the tab menu due to a conflict, another plugin (e.g. TAB) is managing the scoreboard teams. error:" + e.getMessage());
		}

	}

	public void removeAll() {
		removeAll(true);
	}

	/**
	 * Used when the plugin is disabled
	 */
	public void removeAll(boolean callEvent) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			remove(p, callEvent);
		}

		// only loaded teams will have a team manager
		for (Entry<UUID, Team> t : Team.getTeamManager().getLoadedTeamListClone().entrySet()) {
			org.bukkit.scoreboard.Team team = t.getValue().getScoreboardTeamOrNull();

			if (team != null) {
				team.unregister();
			}
			org.bukkit.scoreboard.Team ownerTeam = t.getValue().getOwnerScoreboardTeamOrNull();
			if (ownerTeam != null) {
				ownerTeam.unregister();
			}

		}
	}

	public void remove(Player player) {
		remove(player, true);
	}

	/**
	 * Used to remove the prefix / suffix from the specified player
	 *
	 * @param player    the player to remove the prefix/suffix from
	 * @param callEvent if BelowNameChangeEvent should be called
	 */
	public void remove(Player player, boolean callEvent) {

		if (player == null) {
			return;
		}

		Team team = Team.getTeam(player);
		if (team == null) {
			return;
		}

		org.bukkit.scoreboard.Team normal = team.getScoreboardTeamOrNull();
		org.bukkit.scoreboard.Team crowned = team.getOwnerScoreboardTeamOrNull();
		boolean inNormal = normal != null && normal.hasEntry(player.getName());
		boolean inCrowned = crowned != null && crowned.hasEntry(player.getName());
		if (!inNormal && !inCrowned) {
			return;
		}

		try {
			if (inNormal) {
				normal.removeEntry(player.getName());
			}
			if (inCrowned) {
				crowned.removeEntry(player.getName());
			}
		} catch (Exception e) {
			Main.plugin.getLogger().warning(
					"Another plugin is conflicting with the functionality of the Teams.");
			return;
		}

		if (callEvent) {
			BelowNameChangeEvent event = new BelowNameChangeEvent(player, ChangeType.REMOVE);
			Bukkit.getPluginManager().callEvent(event);
		}
	}

	/**
	 * The crown is only for owners, so the tag is refreshed when a rank changes
	 */
	private void refreshRank(TeamPlayer teamPlayer) {
		Player online = teamPlayer.getPlayer().getPlayer();
		if (online != null && online.isOnline()) {
			remove(online, false);
			displayBelowName(online);
		}
	}

	@EventHandler
	public void onPromote(PostPromotePlayerEvent e) {
		refreshRank(e.getTeamPlayer());
	}

	@EventHandler
	public void onDemote(PostDemotePlayerEvent e) {
		refreshRank(e.getTeamPlayer());
	}

	@EventHandler
	public void playerJoinEvent(PlayerJoinEvent e) {
		displayBelowName(e.getPlayer());
	}

	public void setupTeam(org.bukkit.scoreboard.Team scoreboardTeam, String teamName) {
		// setting team name
		if (type == BelowNameType.PREFIX) {
			scoreboardTeam.setPrefix(teamName);
		} else if (type == BelowNameType.SUFFIX) {
			scoreboardTeam.setSuffix(" " + teamName);
		}

		if (!Main.plugin.getConfig().getBoolean("teamOptions.collide", true)) {
			scoreboardTeam.setOption(Option.COLLISION_RULE, OptionStatus.FOR_OWN_TEAM);
		}

		if (Main.plugin.getConfig().getBoolean("teamOptions.privateDeath")) {
			scoreboardTeam.setOption(Option.DEATH_MESSAGE_VISIBILITY, OptionStatus.FOR_OWN_TEAM);
		}

		if (Main.plugin.getConfig().getBoolean("teamOptions.privateName")) {
			scoreboardTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.FOR_OTHER_TEAMS);
		}

		scoreboardTeam.setCanSeeFriendlyInvisibles(Main.plugin.getConfig().getBoolean("teamOptions.canSeeFriendlyInvisibles"));

	}

	public enum BelowNameType {
		PREFIX, SUFFIX, FALSE;

		public static BelowNameType getType(String string) {

			switch (string.toLowerCase()) {
				case "prefix":
				case "true":
					return PREFIX;
				case "suffix":
					return SUFFIX;
				default:
					return FALSE;
			}
		}
	}

}
