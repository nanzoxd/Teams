package teams.dialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import teams.Main;
import teams.gui.Theme;

/**
 * /teamconfig: every limit and switch of config.yml as sliders and toggles, grouped in pages.
 */
public final class ConfigDialog {

	/** One setting: a slider (max > min), a switch, or a value that cycles through options. */
	private record Field(String path, String label, float min, float max, float step, String tip, List<String> options) {
		boolean toggle() {
			return max <= min && options == null;
		}

		boolean cycle() {
			return options != null;
		}
	}

	private static Field slider(String path, String label, int min, int max, int step) {
		return new Field(path, label, min, max, step, null, null);
	}

	private static Field toggle(String path, String label, String tip) {
		return new Field(path, label, 0, 0, 0, tip, null);
	}

	private static Field cycle(String path, String label, String... options) {
		return new Field(path, label, 0, 0, 0, null, List.of(options));
	}

	private static final Map<String, List<Field>> CATEGORIES = new LinkedHashMap<>();

	static {
		CATEGORIES.put("Team limits", List.of(
				slider("teams.maxTeams", "Max teams (-1 = no limit)", -1, 200, 1),
				slider("teams.maxMembers", "Members per team", 1, 100, 1),
				slider("teams.maxAdmins", "Admins per team (-1 = no limit)", -1, 50, 1),
				slider("teams.maxOwners", "Owners per team", 1, 10, 1),
				slider("teams.maxWarps", "Warps per team", 0, 20, 1),
				slider("teams.maxChests", "Claimed chests per team", 0, 20, 1),
				slider("teams.nameMin", "Shortest team name", 1, 16, 1),
				slider("teams.nameMax", "Longest team name", 2, 32, 1),
				slider("teams.tagMax", "Longest tag", 2, 32, 1),
				slider("teams.titleMax", "Longest player title", 1, 32, 1),
				slider("teams.inviteSeconds", "Invite lasts (seconds)", 10, 600, 10),
				toggle("teams.singleOwner", "Single owner", "Only one owner per team"),
				toggle("teams.enforceTag", "Tag required", "A tag is needed to make a team"),
				toggle("teams.allowPassword", "Warp passwords", "Warp passwords")));
		CATEGORIES.put("Command groups", List.of(
				toggle("features.allies", "Allies", "Allies: ally, neutral, allychat"),
				toggle("features.warps", "Warps", "Warps: warp, warps, setwarp, delwarp"),
				toggle("features.homes", "Homes", "Homes: home, sethome, delhome"),
				toggle("features.chests", "Chests", "Chests: chest, echest")));
		CATEGORIES.put("Allies", List.of(
				toggle("allies.enabled", "Allies on", "Allies"),
				slider("allies.limit", "Allies per team", 0, 50, 1),
				toggle("allies.chests", "Ally chests", "Allies can open chests")));
		CATEGORIES.put("Chat", List.of(
				toggle("chat.allowToggle", "Toggle chats", "Players can toggle team / ally chat"),
				toggle("chat.logTeamChat", "Log chat", "Log team chat in console"),
				toggle("chat.notifyTeamPresence", "Login notice", "Tell team when a member logs in / out"),
				toggle("sounds.enabled", "Sounds", "Sounds")));
		CATEGORIES.put("Design", List.of(
				toggle("belowname.builtin", "Built-in name line", "Line under the name made by Teams (off = TAB plugin or tag in front)"),
				toggle("belowname.showNoTeam", "[NoTeam] line", "Show [NoTeam] under players without a team"),
				toggle("belowname.ping", "Ping", "Ping after the tag under the name"),
				toggle("belowname.hp", "HP", "HP after the tag under the name"),
				toggle("belowname.hearts", "Hearts", "Hearts after the tag under the name"),
				toggle("display.chat.enabled", "Chat tag", "Team tag in front of chat messages"),
				toggle("display.tab.enabled", "Tag before name", "Tag in front of the name (built-in name line off)"),
				toggle("display.smallCaps", "Small caps", "Tag in small capitals"),
				toggle("display.colorTeamName", "Team colours", "Colour the team name with the team colour"),
				toggle("tab.hook", "TAB plugin", "Use the TAB plugin when it is installed"),
				toggle("tab.autoSetup", "TAB auto setup", "Turn on belowname-objective in the TAB config once"),
				toggle("staff.enabled", "Staff colour", "Staff colour in the tab list"),
				toggle("staff.chat", "Staff chat colour", "Staff colour in chat"),
				cycle("display.chat.show", "Chat shows", "name", "tag"),
				cycle("display.tab.show", "Tab shows", "name", "tag"),
				cycle("display.tab.position", "Tag position", "prefix", "suffix")));
		CATEGORIES.put("Combat", List.of(
				toggle("combat.disableCombat", "No team damage", "Members cannot hurt each other"),
				toggle("combat.disablePotions", "No team potions", "Members cannot splash each other"),
				toggle("combat.playerDamageSelf", "Self damage", "Players can hurt themselves"),
				toggle("combat.noTagTeammates", "No team tagging", "CelestCombat: teammates cannot combat tag")));
		CATEGORIES.put("Score", List.of(
				slider("events.kill.score", "Score for a kill", -10, 10, 1),
				slider("events.kill.spam", "Score for a repeat kill", -10, 10, 1),
				slider("events.death.score", "Score for a death", -10, 10, 1),
				slider("events.death.spam", "Score for a repeat death", -10, 10, 1),
				slider("minScore", "Lowest possible score", -100, 100, 1),
				slider("spamThreshold", "Repeat kill window (seconds)", 0, 600, 5),
				slider("gui.topSize", "Teams in the top", 1, 36, 1)));
		CATEGORIES.put("Teleport", List.of(
				toggle("noTeleport", "No teleport", "Turn off /team home and warps"),
				slider("tpDelay", "Teleport delay (seconds)", 0, 30, 1),
				toggle("noMove", "Cancel on move", "Cancel teleport if the player moves"),
				slider("maxMove", "Blocks a player may move", 0, 20, 1)));
		CATEGORIES.put("Menus", List.of(
				toggle("gui.enabled", "Menus", "Menus"),
				toggle("gui.admin", "Admin menu", "Admin menu"),
				toggle("gui.dialogs", "Dialogs", "Dialogs")));
	}

	private ConfigDialog() {
	}

	public static void open(Player player) {
		List<ActionButton> buttons = new ArrayList<>();
		for (String name : CATEGORIES.keySet()) {
			String text = name.equals("Menus") ? Ui.GLOBE + " " + name : name;
			buttons.add(Ui.button(Ui.plain(text), Ui.label() + CATEGORIES.get(name).size() + " settings",
					Ui.BUTTON, (p, v) -> category(p, name, CATEGORIES.get(name))));
		}
		ConfigurationSection levels = Main.plugin.getConfig().getConfigurationSection("levels");
		if (levels != null) {
			buttons.add(Ui.button(Ui.plain("Levels"), Ui.label() + "Price and limits of each level", Ui.BUTTON,
					(p, v) -> levels(p)));
		}
		buttons.add(Ui.button(Ui.plain("Permissions"), Ui.label() + "Which rank can use each /team command", Ui.BUTTON,
				(p, v) -> PermissionsDialog.open(p, 0)));
		buttons.add(Ui.button(Ui.refresh("Reload"), Ui.label() + "Apply everything now", Ui.BUTTON, (p, v) -> {
			Main.plugin.reload();
			p.sendMessage(Ui.c(Ui.on() + "Teams reloaded"));
			open(p);
		}));
		buttons.add(Ui.button(Ui.close(), null, Ui.BUTTON, (p, v) -> p.closeInventory()));
		Dialog dialog = Dialog.create(b -> b.empty()
				.base(DialogBase.builder(Ui.title("Team Config"))
						.body(List.of(DialogBody.plainMessage(Ui.header(null, Ui.text() + "Settings", "Pick a page to change it"), 300)))
						.build())
				.type(DialogType.multiAction(buttons, null, 2)));
		player.showDialog(dialog);
	}

	private static void levels(Player player) {
		ConfigurationSection levels = Main.plugin.getConfig().getConfigurationSection("levels");
		List<Field> fields = new ArrayList<>();
		if (levels != null) {
			for (String id : levels.getKeys(false)) {
				String l = id.toUpperCase();
				fields.add(slider("levels." + id + ".price", l + " score needed", 0, 10000, 50));
				fields.add(slider("levels." + id + ".maxMembers", l + " members", 1, 100, 1));
				fields.add(slider("levels." + id + ".maxChests", l + " chests", 0, 20, 1));
				fields.add(slider("levels." + id + ".maxWarps", l + " warps", 0, 20, 1));
				fields.add(slider("levels." + id + ".maxAdmins", l + " admins", -1, 50, 1));
				fields.add(slider("levels." + id + ".maxOwners", l + " owners", 1, 10, 1));
			}
		}
		category(player, "Levels", fields);
	}

	private static String key(String path) {
		return path.replace('.', '_');
	}

	/** Copies the slider values of an open page into config.yml (not saved to disk yet) */
	private static void applySliders(List<Field> fields, DialogResponseView v) {
		FileConfiguration c = Main.plugin.getConfig();
		for (Field f : fields) {
			if (!f.toggle()) {
				Float n = v.getFloat(key(f.path()));
				if (n != null) {
					c.set(f.path(), Math.round(n));
				}
			}
		}
	}

	private static void saveAndRefresh() {
		Main.plugin.saveConfig();
		if (Main.plugin.getTeamCommand() != null) {
			Main.plugin.getTeamCommand().refreshLive();
		}
	}

	private static String cycleValue(FileConfiguration cfg, Field f) {
		String v = cfg.getString(f.path(), f.options().get(0));
		return v.substring(0, 1).toUpperCase() + v.substring(1).toLowerCase();
	}

	/** Sliders on top, then the grid: switches (tick / cross), value cyclers (⇄), Save and Back */
	private static void category(Player player, String name, List<Field> fields) {
		FileConfiguration cfg = Main.plugin.getConfig();
		List<DialogInput> inputs = new ArrayList<>();
		List<ActionButton> buttons = new ArrayList<>();
		for (Field f : fields) {
			if (f.cycle()) {
				buttons.add(Ui.button(Ui.cycle(f.label(), cycleValue(cfg, f)), Ui.label() + f.path() + "  -  click to change", Ui.BUTTON,
						(p, v) -> {
							applySliders(fields, v);
							FileConfiguration c = Main.plugin.getConfig();
							int next = (f.options().indexOf(c.getString(f.path(), f.options().get(0)).toLowerCase()) + 1) % f.options().size();
							c.set(f.path(), f.options().get(next));
							saveAndRefresh();
							category(p, name, fields);
						}));
			} else if (f.toggle()) {
				boolean on = cfg.getBoolean(f.path());
				buttons.add(Ui.button(Ui.mark(on, f.label()), Ui.label() + f.tip() + "  -  click to turn " + (on ? "off" : "on"),
						Ui.BUTTON, (p, v) -> {
							applySliders(fields, v);
							Main.plugin.getConfig().set(f.path(), !Main.plugin.getConfig().getBoolean(f.path()));
							saveAndRefresh();
							if (f.path().equals("belowname.builtin")) {
								// switching between the built-in line and TAB needs the listeners set up again
								Main.plugin.reload();
							}
							category(p, name, fields);
						}));
			} else {
				float value = Math.max(f.min(), Math.min(f.max(), (float) cfg.getDouble(f.path())));
				inputs.add(DialogInput.numberRange(key(f.path()), Ui.c(Ui.text() + f.label()), f.min(), f.max())
						.initial(value).step(f.step()).width(Ui.WIDE).build());
			}
		}

		if (!inputs.isEmpty()) {
			buttons.add(Ui.button(Ui.save(), Ui.label() + "Write to config.yml", Ui.BUTTON, (p, v) -> {
				applySliders(fields, v);
				saveAndRefresh();
				p.sendMessage(Ui.c(Ui.on() + name + " saved. " + Ui.label() + "Use Reload in /teamconfig for tab and menu changes."));
				open(p);
			}));
		}
		buttons.add(Ui.button(Ui.back("Back"), null, Ui.BUTTON, (p, v) -> open(p)));
		String sub = inputs.isEmpty() ? "Click a setting to switch it" : "Move the sliders, then press Save";
		List<DialogBody> body = List.of(DialogBody.plainMessage(Ui.header(null, Ui.text() + name, sub), 300));
		Dialog dialog = Dialog.create(b -> b.empty()
				.base(DialogBase.builder(Ui.title(name)).body(body).inputs(inputs).build())
				.type(DialogType.multiAction(buttons, null, 2)));
		player.showDialog(dialog);
	}
}
