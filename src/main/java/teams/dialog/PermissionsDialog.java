package teams.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import teams.Main;
import teams.PlayerRank;
import teams.commands.PermissionParentCommand;
import teams.gui.Theme;

/**
 * teampermissions.yml in game: for every /team command, if it is on and which team rank can use it.
 * Rank slider: 0 = member, 1 = admin, 2 = owner.
 */
final class PermissionsDialog {

	private static final int PER_PAGE = 6;

	private PermissionsDialog() {
	}

	private static String id(String command) {
		return command.replaceAll("[^A-Za-z0-9]", "_");
	}

	private static List<String> commands(YamlConfiguration cfg) {
		TreeSet<String> names = new TreeSet<>();
		for (String key : cfg.getKeys(false)) {
			if (cfg.isConfigurationSection(key)) {
				names.add(key);
			}
		}
		return new ArrayList<>(names);
	}

	private static int rankValue(String name) {
		PlayerRank rank = name == null ? null : PlayerRank.getRank(name);
		return rank == null ? 0 : rank.value;
	}

	private static PlayerRank rankOf(int value) {
		return value >= 2 ? PlayerRank.OWNER : value == 1 ? PlayerRank.ADMIN : PlayerRank.DEFAULT;
	}

	static void open(Player player, int page) {
		PermissionParentCommand parent = Main.plugin.getTeamCommand();
		if (parent == null) {
			return;
		}
		YamlConfiguration cfg = parent.getPermissionConfig();
		List<String> all = commands(cfg);
		int pages = Math.max(1, (all.size() + PER_PAGE - 1) / PER_PAGE);
		int current = Math.max(0, Math.min(page, pages - 1));
		List<String> shown = all.subList(current * PER_PAGE, Math.min(all.size(), (current + 1) * PER_PAGE));

		List<DialogInput> inputs = new ArrayList<>();
		for (String cmd : shown) {
			inputs.add(DialogInput.bool("e_" + id(cmd), Ui.c(Ui.P1 + "/team " + cmd + Ui.P5 + "  on"))
					.initial(cfg.getBoolean(cmd + ".enabled", true)).build());
			if (cfg.contains(cmd + ".rank")) {
				inputs.add(DialogInput.numberRange("r_" + id(cmd), Ui.c(Ui.P3 + "   rank needed (0 member, 1 admin, 2 owner)"), 0f, 2f)
						.initial((float) rankValue(cfg.getString(cmd + ".rank"))).step(1f).width(Ui.WIDE).build());
			}
		}

		List<DialogBody> body = List.of(DialogBody.plainMessage(Ui.lines(
				Ui.c(Ui.P1 + "Who can use each /team command"),
				Ui.c(Ui.P5 + "Page " + Ui.P3 + (current + 1) + Ui.P5 + " / " + Ui.P3 + pages),
				Ui.divider()), 300));

		List<ActionButton> buttons = new ArrayList<>();
		buttons.add(Ui.button(Theme.good() + "Save", Ui.P5 + "Saves teampermissions.yml", Ui.BUTTON, (p, v) -> {
			boolean turnedOnOrOff = false;
			for (String cmd : shown) {
				Boolean on = v.getBoolean("e_" + id(cmd));
				if (on != null && on != cfg.getBoolean(cmd + ".enabled", true)) {
					cfg.set(cmd + ".enabled", on);
					turnedOnOrOff = true;
				}
				Float r = v.getFloat("r_" + id(cmd));
				if (r != null && cfg.contains(cmd + ".rank")) {
					PlayerRank rank = rankOf(Math.round(r));
					cfg.set(cmd + ".rank", rank.toString());
					parent.applyRank(cmd, rank);
				}
			}
			parent.savePermissions();
			p.sendMessage(Ui.c(Theme.good() + "Permissions saved." + (turnedOnOrOff ? Ui.P5 + " Use Reload for commands you turned on or off." : "")));
			open(p, current);
		}));
		if (current > 0) {
			buttons.add(Ui.button(Theme.info() + "← Previous", null, Ui.BUTTON, (p, v) -> open(p, current - 1)));
		}
		if (current < pages - 1) {
			buttons.add(Ui.button(Theme.info() + "Next →", null, Ui.BUTTON, (p, v) -> open(p, current + 1)));
		}
		buttons.add(Ui.button(Theme.info() + "Back", null, Ui.BUTTON, (p, v) -> ConfigDialog.open(p)));
		ActionButton close = Ui.button(Theme.bad() + "Close", null, Ui.BUTTON, (p, v) -> p.closeInventory());

		Dialog dialog = Dialog.create(b -> b.empty()
				.base(DialogBase.builder(Ui.title("Permissions")).body(body).inputs(inputs).build())
				.type(DialogType.multiAction(buttons, close, 2)));
		player.showDialog(dialog);
	}
}
