package teams.dialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import io.papermc.paper.dialog.Dialog;
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

	/** One setting. A slider when max > min, otherwise a toggle. */
	private record Field(String path, String label, float min, float max, float step) {
		boolean toggle() {
			return max <= min;
		}
	}

	private static Field slider(String path, String label, int min, int max, int step) {
		return new Field(path, label, min, max, step);
	}

	private static Field toggle(String path, String label) {
		return new Field(path, label, 0, 0, 0);
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
				toggle("teams.singleOwner", "Only one owner per team"),
				toggle("teams.enforceTag", "A tag is needed to make a team"),
				toggle("teams.allowPassword", "Warp passwords")));
		CATEGORIES.put("Allies", List.of(
				toggle("allies.enabled", "Allies"),
				slider("allies.limit", "Allies per team", 0, 50, 1),
				toggle("allies.chests", "Allies can open chests")));
		CATEGORIES.put("Chat", List.of(
				toggle("display.chat.enabled", "Team tag in chat"),
				toggle("chat.allowToggle", "Players can toggle team / ally chat"),
				toggle("chat.logTeamChat", "Log team chat in console"),
				toggle("chat.notifyTeamPresence", "Tell team when a member logs in / out"),
				toggle("chat.announceTeamCreate", "Announce: team created"),
				toggle("chat.announceTeamJoin", "Announce: player joined"),
				toggle("chat.announceTeamLeave", "Announce: player left"),
				toggle("chat.announceTeamDisband", "Announce: team disbanded"),
				toggle("sounds.enabled", "Sounds")));
		CATEGORIES.put("Tab and names", List.of(
				toggle("display.tab.enabled", "Team tag in tab list"),
				toggle("display.smallCaps", "Small caps tag"),
				toggle("display.colorTeamName", "Coloured team name"),
				toggle("tab.hook", "Use TAB plugin"),
				toggle("tab.autoSetup", "Set up TAB automatically"),
				toggle("staff.enabled", "Staff colour"),
				toggle("staff.chat", "Staff colour in chat")));
		CATEGORIES.put("Combat", List.of(
				toggle("combat.disableCombat", "Members cannot hurt each other"),
				toggle("combat.disablePotions", "Members cannot splash each other"),
				toggle("combat.playerDamageSelf", "Players can hurt themselves")));
		CATEGORIES.put("Score", List.of(
				slider("events.kill.score", "Score for a kill", -10, 10, 1),
				slider("events.kill.spam", "Score for a repeat kill", -10, 10, 1),
				slider("events.death.score", "Score for a death", -10, 10, 1),
				slider("events.death.spam", "Score for a repeat death", -10, 10, 1),
				slider("minScore", "Lowest possible score", -100, 100, 1),
				slider("spamThreshold", "Repeat kill window (seconds)", 0, 600, 5),
				slider("gui.topSize", "Teams in the top", 1, 36, 1)));
		CATEGORIES.put("Teleport", List.of(
				toggle("noTeleport", "Turn off /team home and warps"),
				slider("tpDelay", "Teleport delay (seconds)", 0, 30, 1),
				toggle("noMove", "Cancel teleport if the player moves"),
				slider("maxMove", "Blocks a player may move", 0, 20, 1)));
		CATEGORIES.put("Menus", List.of(
				toggle("gui.enabled", "Menus"),
				toggle("gui.admin", "Admin menu"),
				toggle("gui.dialogs", "Dialogs")));
	}

	private ConfigDialog() {
	}

	public static void open(Player player) {
		List<ActionButton> buttons = new ArrayList<>();
		for (String name : CATEGORIES.keySet()) {
			buttons.add(Ui.button(Theme.light() + name, Theme.soft() + CATEGORIES.get(name).size() + " settings", Ui.BUTTON,
					(p, v) -> category(p, name, CATEGORIES.get(name))));
		}
		ConfigurationSection levels = Main.plugin.getConfig().getConfigurationSection("levels");
		if (levels != null) {
			buttons.add(Ui.button(Theme.light() + "Levels", Theme.soft() + "Price and limits of each level", Ui.BUTTON,
					(p, v) -> levels(p)));
		}
		buttons.add(Ui.button(Theme.accent() + "Reload", Theme.soft() + "Apply everything now", Ui.BUTTON, (p, v) -> {
			Main.plugin.reload();
			p.sendMessage(Ui.c(Theme.good() + "Teams reloaded"));
			open(p);
		}));
		ActionButton close = Ui.button(Theme.bad() + "Close", null, Ui.BUTTON, (p, v) -> p.closeInventory());
		Dialog dialog = Dialog.create(b -> b.empty()
				.base(DialogBase.builder(Ui.title("Team Config"))
						.body(List.of(DialogBody.plainMessage(Ui.c(Theme.soft() + "Pick a page, change the sliders, press Save."), 300)))
						.build())
				.type(DialogType.multiAction(buttons, close, 2)));
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

	private static void category(Player player, String name, List<Field> fields) {
		FileConfiguration cfg = Main.plugin.getConfig();
		List<DialogInput> inputs = new ArrayList<>();
		for (Field f : fields) {
			if (f.toggle()) {
				inputs.add(DialogInput.bool(key(f.path()), Ui.c(Theme.light() + f.label()))
						.initial(cfg.getBoolean(f.path())).build());
			} else {
				float value = Math.max(f.min(), Math.min(f.max(), (float) cfg.getDouble(f.path())));
				inputs.add(DialogInput.numberRange(key(f.path()), Ui.c(Theme.light() + f.label()), f.min(), f.max())
						.initial(value).step(f.step()).width(Ui.WIDE).build());
			}
		}

		ActionButton save = Ui.button(Theme.good() + "Save", Theme.soft() + "Write to config.yml", Ui.BUTTON, (p, v) -> {
			FileConfiguration c = Main.plugin.getConfig();
			for (Field f : fields) {
				if (f.toggle()) {
					Boolean b = v.getBoolean(key(f.path()));
					if (b != null) {
						c.set(f.path(), b);
					}
				} else {
					Float n = v.getFloat(key(f.path()));
					if (n != null) {
						c.set(f.path(), Math.round(n));
					}
				}
			}
			Main.plugin.saveConfig();
			p.sendMessage(Ui.c(Theme.good() + name + " saved. " + Theme.soft() + "Use Reload in /teamconfig for tab and menu changes."));
			open(p);
		});
		ActionButton back = Ui.button(Theme.info() + "Back", null, Ui.BUTTON, (p, v) -> open(p));
		Dialog dialog = Dialog.create(b -> b.empty()
				.base(DialogBase.builder(Ui.title(name)).inputs(inputs).build())
				.type(DialogType.confirmation(save, back)));
		player.showDialog(dialog);
	}
}
