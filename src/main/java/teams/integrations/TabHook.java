package teams.integrations;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import teams.Main;
import teams.PlayerRank;
import teams.Team;
import teams.TeamPlayer;
import teams.customEvents.post.PostCreateTeamEvent;
import teams.customEvents.post.PostDemotePlayerEvent;
import teams.customEvents.post.PostDisbandTeamEvent;
import teams.customEvents.post.PostPromotePlayerEvent;
import teams.customEvents.post.PostPlayerJoinTeamEvent;
import teams.customEvents.post.PostPlayerLeaveTeamEvent;
import teams.events.StaffTab;
import teams.integrations.placeholder.provider.BelowNamePlaceholderProvider;
import teams.message.MessageManager;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Used when the TAB plugin is installed. Everything is done through TAB's API with reflection, so Teams
 * also works when TAB is missing or is another version.
 * <ul>
 *   <li>Without TAB: the team tag is in front of the name ("[Niga] mrnanzo"), done with minecraft teams.</li>
 *   <li>With TAB: the name is alone and the tag is on the line under it, using TAB's belowname objective.
 *   The first time, Teams turns that feature on in plugins/TAB/config.yml (a backup is made) and runs /tab reload.</li>
 *   <li>Staff (see {@code staff:} in config.yml) get their colour and the top of the tab list through TAB.</li>
 * </ul>
 */
public class TabHook implements Listener {

	private static final String API = "me.neznamy.tab.api.";
	private static final String PLACEHOLDER = "%teams-below%";
	/** the tag on its own, no colour, so the colour in the TAB config is used ("[Niga]", empty without a team) */
	private static final String TAG_PLACEHOLDER = "%teams-tag%";
	/** the tag without brackets and colour ("ɴɪɢᴀ"), for building your own format in the TAB config */
	private static final String RAW_PLACEHOLDER = "%teams-tagraw%";
	/** the tag without brackets in the team colour ("&#RRGGBBɴɪɢᴀ"), for your own format with white brackets */
	private static final String COLOR_RAW_PLACEHOLDER = "%teams-tagc%";

	private final Plugin tab;
	private final ClassLoader loader;
	/** true if the belowname-objective of TAB is the one made by Teams (so Teams is allowed to manage it) */
	private boolean ours = false;

	public TabHook() {
		this.tab = Bukkit.getPluginManager().getPlugin("TAB");
		this.loader = tab.getClass().getClassLoader();
	}

	public static boolean isInstalled() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("TAB");
		return plugin != null && plugin.isEnabled();
	}

	// ------------------------------------------------------------------ reflection helpers

	private Object call(String type, Object target, String name, Object... args) throws Exception {
		if (target == null) {
			return null;
		}
		Class<?> iface = Class.forName(API + type, true, loader);
		for (Method method : iface.getMethods()) {
			if (!method.getName().equals(name) || method.getParameterCount() != args.length) {
				continue;
			}
			Class<?>[] types = method.getParameterTypes();
			boolean matches = true;
			for (int i = 0; i < types.length; i++) {
				if (args[i] == null ? types[i].isPrimitive() : !wrap(types[i]).isInstance(args[i])) {
					matches = false;
					break;
				}
			}
			if (matches) {
				return method.invoke(target, args);
			}
		}
		throw new NoSuchMethodException(type + "." + name);
	}

	/**
	 * int.class.isInstance(Integer) is false, so primitives are compared by their wrapper class
	 */
	private static Class<?> wrap(Class<?> type) {
		if (!type.isPrimitive()) {
			return type;
		}
		if (type == int.class) {
			return Integer.class;
		}
		if (type == boolean.class) {
			return Boolean.class;
		}
		if (type == long.class) {
			return Long.class;
		}
		if (type == double.class) {
			return Double.class;
		}
		if (type == float.class) {
			return Float.class;
		}
		if (type == short.class) {
			return Short.class;
		}
		if (type == byte.class) {
			return Byte.class;
		}
		return Character.class;
	}

	private Object api() throws Exception {
		return Class.forName(API + "TabAPI", true, loader).getMethod("getInstance").invoke(null);
	}

	private Object manager(String getter) throws Exception {
		return call("TabAPI", api(), getter);
	}

	private Object tabPlayer(Player player) throws Exception {
		return call("TabAPI", api(), "getPlayer", player.getUniqueId());
	}

	// ------------------------------------------------------------------ setup

	public void enable() {
		registerPlaceholder();
		String tabText = tabConfigText();
		// any of the Teams placeholders in the belowname-objective counts (a custom format is allowed)
		ours = tabText.contains("%teams-");

		try {
			boolean belowNameOn = manager("getBelowNameManager") != null;
			if (!belowNameOn && !ours && Main.plugin.getConfig().getBoolean("tab.autoSetup", true)) {
				if (editTabConfig()) {
					ours = true;
					Main.plugin.getLogger().info("TAB found: the team tag is now shown under the player name. "
							+ "Turned on belowname-objective in plugins/TAB/config.yml (backup: config.yml.teams-backup).");
					Bukkit.getScheduler().runTaskLater(Main.plugin, () -> {
						Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tab reload");
						// TAB forgets some API things when it reloads
						Bukkit.getScheduler().runTaskLater(Main.plugin, () -> {
							registerPlaceholder();
							refreshAll();
						}, 80L);
					}, 40L);
					return;
				}
			} else if (belowNameOn && !ours) {
				Main.plugin.getLogger().info("TAB found: its belowname-objective is already used for something else, "
						+ "so Teams leaves it alone. Use " + PLACEHOLDER + " in it to show the team tag.");
			}
		} catch (Exception e) {
			Main.plugin.getLogger().warning("Could not talk to TAB (" + e + "), the team tag is not shown under names");
		}
		Bukkit.getScheduler().runTaskLater(Main.plugin, this::refreshAll, 40L);
	}

	public void disable() {
		try {
			call("placeholder.PlaceholderManager", manager("getPlaceholderManager"), "unregisterPlaceholder", PLACEHOLDER);
			call("placeholder.PlaceholderManager", manager("getPlaceholderManager"), "unregisterPlaceholder", TAG_PLACEHOLDER);
			call("placeholder.PlaceholderManager", manager("getPlaceholderManager"), "unregisterPlaceholder", RAW_PLACEHOLDER);
			call("placeholder.PlaceholderManager", manager("getPlaceholderManager"), "unregisterPlaceholder", COLOR_RAW_PLACEHOLDER);
		} catch (Exception ignored) {
			// TAB may already be disabled
		}
	}

	private void registerPlaceholder() {
		register(PLACEHOLDER, 0);
		register(TAG_PLACEHOLDER, 1);
		register(RAW_PLACEHOLDER, 2);
		register(COLOR_RAW_PLACEHOLDER, 3);
	}

	/**
	 * @param mode 0 = [tag] in the team colour, 1 = [tag] with no colour, 2 = tag only (no brackets, no colour),
	 *             3 = tag only (no brackets) in the team colour
	 */
	private void register(String id, int mode) {
		try {
			Object manager = manager("getPlaceholderManager");
			try {
				call("placeholder.PlaceholderManager", manager, "unregisterPlaceholder", id);
			} catch (Exception ignored) {
				// was not registered yet
			}
			Function<Object, String> text = tabPlayer -> {
				try {
					UUID uuid = (UUID) call("TabPlayer", tabPlayer, "getUniqueId");
					Player player = Bukkit.getPlayer(uuid);
					Team team = player == null ? null : Team.getTeam(player);
					if (team == null) {
						return "";
					}
					TeamPlayer teamPlayer = team.getTeamPlayer(player);
					boolean owner = team.hasCrown(teamPlayer);
					if (mode == 0) {
						return new BelowNamePlaceholderProvider().getPlaceholderForTeam(team, owner);
					}
					if (mode == 2) {
						return team.getHeadTag(owner);
					}
					if (mode == 3) {
						return team.getColor().toAmpersandHex() + team.getHeadTag(owner);
					}
					return MessageManager.getMessage("placeholder.belowname", team.getHeadTag(owner));
				} catch (Exception e) {
					return "";
				}
			};
			call("placeholder.PlaceholderManager", manager, "registerPlayerPlaceholder", id, 1000, text);
		} catch (Exception e) {
			Main.plugin.getLogger().warning("Could not register " + id + " in TAB: " + e);
		}
	}

	private File tabConfig() {
		return new File(tab.getDataFolder(), "config.yml");
	}

	private String tabConfigText() {
		try {
			return Files.readString(tabConfig().toPath(), StandardCharsets.UTF_8);
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * Turns on belowname-objective in the config of TAB, showing %teams-below% under the name.
	 *
	 * @return if the file was changed
	 */
	private boolean editTabConfig() {
		try {
			File file = tabConfig();
			if (!file.exists()) {
				return false;
			}
			List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
			int start = -1;
			for (int i = 0; i < lines.size(); i++) {
				if (lines.get(i).trim().equals("belowname-objective:") && !lines.get(i).startsWith(" ")) {
					start = i;
					break;
				}
			}
			if (start < 0) {
				return false;
			}
			boolean changed = false;
			for (int i = start + 1; i < lines.size(); i++) {
				String line = lines.get(i);
				if (!line.isEmpty() && !line.startsWith(" ") && !line.startsWith("#")) {
					break; // next top level key
				}
				String trimmed = line.trim();
				String replacement = null;
				if (trimmed.startsWith("enabled:")) {
					replacement = "  enabled: true";
				} else if (trimmed.startsWith("fancy-value-default:")) {
					replacement = "  fancy-value-default: \"\"";
				} else if (trimmed.startsWith("fancy-value:")) {
					replacement = "  fancy-value: \"" + PLACEHOLDER + "\"";
				} else if (trimmed.startsWith("value:")) {
					replacement = "  value: \"0\"";
				} else if (trimmed.startsWith("title:")) {
					replacement = "  title: \" \"";
				}
				if (replacement != null) {
					lines.set(i, replacement);
					changed = true;
				}
			}
			if (!changed) {
				return false;
			}
			File backup = new File(file.getParentFile(), "config.yml.teams-backup");
			if (!backup.exists()) {
				Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
			Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
			return true;
		} catch (Exception e) {
			Main.plugin.getLogger().warning("Could not change the TAB config: " + e);
			return false;
		}
	}

	// ------------------------------------------------------------------ per player

	public void refreshAll() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			refresh(player);
		}
	}

	/**
	 * Makes sure the line under the name is shown for everyone, and gives staff their colour and the top of the tab list.
	 */
	public void refresh(Player player) {
		if (!player.isOnline()) {
			return;
		}
		try {
			Object tabPlayer = tabPlayer(player);
			if (tabPlayer == null) {
				return;
			}

			if (ours) {
				// Never turn the line off for a player: TAB's setEnabled hides the whole line for that player as a
				// viewer, so players without a team could not see anyone's line. null = TAB decides (shown).
				call("belowname.BelowNameManager", manager("getBelowNameManager"), "setEnabled", tabPlayer, null);
			}

			boolean staff = Main.plugin.getConfig().getBoolean("staff.enabled", true) && StaffTab.isStaffPlayer(player);
			String color = staff ? "&#" + staffHex() : null;
			String sortName = null;
			if (staff) {
				sortName = "!0" + player.getName();
				if (sortName.length() > 14) {
					sortName = sortName.substring(0, 14);
				}
			}
			// the staff colour is only for the tab list (and chat), never above the head
			call("nametag.NameTagManager", manager("getNameTagManager"), "setPrefix", tabPlayer, null);
			call("tablist.TabListFormatManager", manager("getTabListFormatManager"), "setPrefix", tabPlayer, color);
			call("tablist.SortingManager", manager("getSortingManager"), "forceTeamName", tabPlayer, sortName);
		} catch (Exception e) {
			Main.plugin.getLogger().fine("TAB refresh failed for " + player.getName() + ": " + e);
		}
	}

	private String staffHex() {
		String hex = Main.plugin.getConfig().getString("staff.color", "#D11F3F");
		if (hex == null || !hex.replace("#", "").matches("[0-9a-fA-F]{6}")) {
			return "D11F3F";
		}
		return hex.replace("#", "").toUpperCase();
	}

	private void refreshLater(Player player, long ticks) {
		Bukkit.getScheduler().runTaskLater(Main.plugin, () -> refresh(player), ticks);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		// TAB needs a moment to load the player
		refreshLater(e.getPlayer(), 40L);
		refreshLater(e.getPlayer(), 120L);
	}

	@EventHandler
	public void onTeamCreate(PostCreateTeamEvent e) {
		Bukkit.getScheduler().runTaskLater(Main.plugin, this::refreshAll, 2L);
	}

	@EventHandler
	public void onTeamDisband(PostDisbandTeamEvent e) {
		Bukkit.getScheduler().runTaskLater(Main.plugin, this::refreshAll, 2L);
	}

	@EventHandler
	public void onTeamJoin(PostPlayerJoinTeamEvent e) {
		Bukkit.getScheduler().runTaskLater(Main.plugin, this::refreshAll, 2L);
	}

	@EventHandler
	public void onPromote(PostPromotePlayerEvent e) {
		Bukkit.getScheduler().runTaskLater(Main.plugin, this::refreshAll, 2L);
	}

	@EventHandler
	public void onDemote(PostDemotePlayerEvent e) {
		Bukkit.getScheduler().runTaskLater(Main.plugin, this::refreshAll, 2L);
	}

	@EventHandler
	public void onTeamLeave(PostPlayerLeaveTeamEvent e) {
		Bukkit.getScheduler().runTaskLater(Main.plugin, this::refreshAll, 2L);
	}
}
