package teams.display;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import teams.Main;
import teams.PlayerRank;
import teams.Team;
import teams.TeamPlayer;
import teams.settings.PlayerSettings;
import teams.text.Formatter;

/**
 * Teams Tab: the header and footer of the player list, and a Teammates view that a player switches to by sneaking
 * (or with /team tab).
 * <ul>
 *   <li>Without the TAB plugin the header and footer are sent with the normal Paper API, and in the Teammates view
 *   everybody who is not in your team is taken off your player list (Player#unlistPlayer, they stay visible in the world).</li>
 *   <li>With the TAB plugin the header and footer go through TAB's HeaderFooterManager (so TAB does not overwrite them).
 *   Everything is done with reflection, so TAB can be missing or another version.</li>
 * </ul>
 * Offline teammates cannot be shown as player list entries without packets, so the header lists the whole team:
 * online in green, offline in red.
 */
public final class TeamsTab implements Listener {

	/** the view is sent again at least this often (in refreshes of one second), TAB forgets it on /tab reload */
	private static final int RESEND_AFTER = 10;

	private final Map<UUID, String> lastSent = new HashMap<>();
	private final Map<UUID, Integer> age = new HashMap<>();
	/** players that THIS plugin took off the player list of a viewer, only those are put back */
	private final Map<UUID, Set<UUID>> unlisted = new HashMap<>();
	private final Map<UUID, Long> lastSneak = new HashMap<>();
	private BukkitTask task;

	// TAB plugin (reflection)
	private Object tabApi;
	private Object tabFooter;
	private Method tabGetPlayer;
	private Method tabSet;

	public void enable() {
		setupTab();
		Bukkit.getPluginManager().registerEvents(this, Main.plugin);
		task = Bukkit.getScheduler().runTaskTimer(Main.plugin, this::refreshAll, 20L, 20L);
	}

	public void disable() {
		HandlerList.unregisterAll(this);
		if (task != null) {
			task.cancel();
			task = null;
		}
		for (Player viewer : Bukkit.getOnlinePlayers()) {
			Set<UUID> ours = unlisted.getOrDefault(viewer.getUniqueId(), Set.of());
			for (UUID id : ours) {
				Player other = Bukkit.getPlayer(id);
				if (other != null) {
					viewer.listPlayer(other);
				}
			}
			if (!sendTab(viewer, null, null)) {
				viewer.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty());
			}
		}
		unlisted.clear();
		lastSent.clear();
		age.clear();
	}

	// ------------------------------------------------------------------ TAB plugin

	private void setupTab() {
		tabApi = null;
		tabFooter = null;
		if (!Main.plugin.getConfig().getBoolean("tablist.useTab", true)) {
			return;
		}
		Plugin tab = Bukkit.getPluginManager().getPlugin("TAB");
		if (tab == null || !tab.isEnabled()) {
			return;
		}
		try {
			ClassLoader loader = tab.getClass().getClassLoader();
			Class<?> apiClass = Class.forName("me.neznamy.tab.api.TabAPI", true, loader);
			Class<?> playerClass = Class.forName("me.neznamy.tab.api.TabPlayer", true, loader);
			Class<?> managerClass = Class.forName("me.neznamy.tab.api.tablist.HeaderFooterManager", true, loader);
			Object api = apiClass.getMethod("getInstance").invoke(null);
			Object manager = apiClass.getMethod("getHeaderFooterManager").invoke(api);
			if (api == null || manager == null) {
				// header-footer is off in the TAB config, then TAB does not touch the header and footer
				return;
			}
			tabGetPlayer = apiClass.getMethod("getPlayer", UUID.class);
			tabSet = managerClass.getMethod("setHeaderAndFooter", playerClass, String.class, String.class);
			tabApi = api;
			tabFooter = manager;
			Main.plugin.getLogger().info("Teams Tab: TAB found, the header and footer are set through TAB.");
		} catch (Throwable e) {
			Main.plugin.getLogger().warning("Teams Tab: could not use the TAB plugin, using the normal header and footer: " + e);
		}
	}

	/** @return true if TAB took it. Null header and footer give the control back to TAB. */
	private boolean sendTab(Player viewer, Component header, Component footer) {
		if (tabFooter == null) {
			return false;
		}
		try {
			Object tabPlayer = tabGetPlayer.invoke(tabApi, viewer.getUniqueId());
			if (tabPlayer == null) {
				return false;
			}
			MiniMessage mm = MiniMessage.miniMessage();
			tabSet.invoke(tabFooter, tabPlayer, header == null ? null : mm.serialize(header),
					footer == null ? null : mm.serialize(footer));
			return true;
		} catch (Throwable e) {
			Main.plugin.getLogger().warning("Teams Tab: TAB did not accept the header and footer, using the normal way: " + e);
			tabFooter = null;
			return false;
		}
	}

	// ------------------------------------------------------------------ views

	/** @return both, everyone or team */
	public static String mode() {
		String mode = Main.plugin.getConfig().getString("tablist.mode", "both");
		return "everyone".equalsIgnoreCase(mode) ? "everyone" : "team".equalsIgnoreCase(mode) ? "team" : "both";
	}

	/** @return true when players can switch the view themselves */
	public static boolean switchable() {
		return mode().equals("both");
	}

	/** @return "team" or "everyone". Players without a team always have everyone. */
	public static String view(Player player) {
		if (Team.getTeam(player) == null) {
			return "everyone";
		}
		String mode = mode();
		if (!mode.equals("both")) {
			return mode;
		}
		String fallback = Main.plugin.getConfig().getString("tablist.defaultView", "everyone");
		return "team".equalsIgnoreCase(PlayerSettings.getString(player.getUniqueId(), "tabView", fallback)) ? "team" : "everyone";
	}

	/** Switches between Everyone and Teammates, @return the new view */
	public String toggle(Player player) {
		if (!switchable()) {
			return view(player);
		}
		String next = view(player).equals("team") ? "everyone" : "team";
		PlayerSettings.setString(player.getUniqueId(), "tabView", next);
		refresh(player);
		return next;
	}

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		if (!event.isSneaking() || !switchable() || !Main.plugin.getConfig().getBoolean("tablist.sneakSwitch", true)
				|| !PlayerSettings.get(player.getUniqueId(), "tabSneak", true) || Team.getTeam(player) == null) {
			return;
		}
		long now = System.currentTimeMillis();
		if (now - lastSneak.getOrDefault(player.getUniqueId(), 0L) < 600) {
			return;
		}
		lastSneak.put(player.getUniqueId(), now);
		toggle(player);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		// everybody gets a new look at the list, the new player may be a teammate
		Bukkit.getScheduler().runTaskLater(Main.plugin, this::refreshAll, 2L);
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		UUID id = event.getPlayer().getUniqueId();
		unlisted.remove(id);
		lastSent.remove(id);
		age.remove(id);
		lastSneak.remove(id);
	}

	private void refreshAll() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			refresh(player);
		}
	}

	private void refresh(Player viewer) {
		try {
			Team team = Team.getTeam(viewer);
			boolean teamView = team != null && view(viewer).equals("team");
			reconcile(viewer, team, teamView);

			FileConfiguration c = Main.plugin.getConfig();
			List<String> header = expand(c.getStringList(teamView ? "tablist.teamHeader" : "tablist.everyoneHeader"), viewer, team, teamView);
			List<String> footer = expand(footerLines(c, teamView), viewer, team, teamView);

			String key = String.join("\n", header) + "\u0000" + String.join("\n", footer);
			int sinceSent = age.merge(viewer.getUniqueId(), 1, Integer::sum);
			if (key.equals(lastSent.get(viewer.getUniqueId())) && sinceSent < RESEND_AFTER) {
				return;
			}
			lastSent.put(viewer.getUniqueId(), key);
			age.put(viewer.getUniqueId(), 0);

			Component headerComponent = join(header);
			Component footerComponent = join(footer);
			if (!sendTab(viewer, headerComponent, footerComponent)) {
				viewer.sendPlayerListHeaderAndFooter(headerComponent, footerComponent);
			}
		} catch (Throwable e) {
			Main.plugin.getLogger().warning("Teams Tab: could not update " + viewer.getName() + ": " + e);
		}
	}

	private static List<String> footerLines(FileConfiguration c, boolean teamView) {
		String key = teamView ? "tablist.teamFooter" : "tablist.everyoneFooter";
		// servers from an older version only have tablist.footer
		return c.contains(key) ? c.getStringList(key) : c.getStringList("tablist.footer");
	}

	private static Component join(List<String> lines) {
		List<Component> parts = new ArrayList<>();
		for (String line : lines) {
			parts.add(Formatter.absolute().process(line));
		}
		return Component.join(JoinConfiguration.newlines(), parts);
	}

	/** In the Teammates view everybody outside the team is taken off the list, in Everyone they come back */
	private void reconcile(Player viewer, Team team, boolean teamView) {
		Set<UUID> ours = unlisted.computeIfAbsent(viewer.getUniqueId(), k -> new HashSet<>());
		for (Player other : Bukkit.getOnlinePlayers()) {
			if (other.equals(viewer)) {
				continue;
			}
			boolean keep = !teamView || (team != null && team.getTeamPlayer(other) != null);
			if (keep) {
				if (ours.remove(other.getUniqueId())) {
					viewer.listPlayer(other);
				}
			} else if (!ours.contains(other.getUniqueId()) && viewer.isListed(other)) {
				viewer.unlistPlayer(other);
				ours.add(other.getUniqueId());
			}
		}
		ours.removeIf(id -> Bukkit.getPlayer(id) == null);
	}

	// ------------------------------------------------------------------ text

	/** Fills in {view} {teamLine} {online} {switch} and turns a line that is only {members} into one line per teammate */
	private List<String> expand(List<String> lines, Player viewer, Team team, boolean teamView) {
		FileConfiguration c = Main.plugin.getConfig();
		String teamLine = team == null ? c.getString("tablist.noTeamLine", "")
				: c.getString("tablist.teamLine", "").replace("{tag}", team.getColor().toAmpersandHex() + team.getHeadTag(false));
		String switchText = c.getBoolean("tablist.sneakSwitch", true) && PlayerSettings.get(viewer.getUniqueId(), "tabSneak", true)
				? c.getString("tablist.switchSneak", "Sneak To Switch") : c.getString("tablist.switchCommand", "/team tab");

		List<String> out = new ArrayList<>();
		boolean canSwitch = switchable();
		for (String line : lines) {
			if (!canSwitch && line.contains("{switch}")) {
				continue;
			}
			if (line.trim().equals("{members}")) {
				out.addAll(memberLines(team));
				continue;
			}
			out.add(line.replace("{view}", teamView ? c.getString("tablist.viewTeam", "Teammates") : c.getString("tablist.viewEveryone", "Everyone"))
					.replace("{teamLine}", teamLine)
					.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
					.replace("{switch}", switchText));
		}
		return out;
	}

	/** One line per teammate, owner first. Online players use onlineColor, offline players offlineColor. */
	private List<String> memberLines(Team team) {
		List<String> lines = new ArrayList<>();
		if (team == null) {
			return lines;
		}
		FileConfiguration c = Main.plugin.getConfig();
		String online = validHex(c.getString("tablist.onlineColor"), "#80ED26");
		String offline = validHex(c.getString("tablist.offlineColor"), "#FF1717");
		int max = Math.max(1, c.getInt("tablist.maxMembers", 12));

		List<TeamPlayer> sorted = new ArrayList<>();
		for (PlayerRank rank : new PlayerRank[]{PlayerRank.OWNER, PlayerRank.ADMIN, PlayerRank.DEFAULT}) {
			sorted.addAll(team.getRank(rank));
		}
		int shown = 0;
		for (TeamPlayer member : sorted) {
			if (shown == max) {
				lines.add("<gray>+" + (sorted.size() - max) + " more");
				break;
			}
			String name = member.getPlayer().getName() == null ? "?" : member.getPlayer().getName();
			String crown = team.hasCrown(member) && !Team.getOwnerCrown().isEmpty() ? Team.getCrownColor() + Team.getOwnerCrown() + " " : "";
			lines.add(crown + "<" + (member.getPlayer().isOnline() ? online : offline) + ">" + name);
			shown++;
		}
		return lines;
	}

	private static String validHex(String value, String def) {
		return value != null && value.trim().matches("#[0-9A-Fa-f]{6}") ? value.trim() : def;
	}
}
