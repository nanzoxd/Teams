package teams.dialog;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import teams.Main;
import teams.PlayerRank;
import teams.Team;
import teams.TeamColor;
import teams.TeamPlayer;
import teams.gui.GuiManager;
import teams.gui.MainMenu;
import teams.message.MessageManager;
import teams.util.TeamColorUtil;

/** Pick the colour of a team: one button per colour, written with the team name in that colour. */
public final class ColorDialog {

	private ColorDialog() {
	}

	public static void open(Player player, Team team) {
		try {
			TeamColor current = team.getColor();
			List<ActionButton> buttons = new ArrayList<>();
			for (TeamColor color : TeamColor.values()) {
				if (TeamColorUtil.isBanned(color)) {
					continue;
				}
				String name = color.toLegacy() + team.getName();
				Component label = Ui.c(color == current ? Ui.on() + "§l" + Ui.TICK + "§r " + name : name);
				Component tip = Ui.lines(
						Ui.c(color.toLegacy() + color.getDisplayName()),
						Ui.c(Ui.label() + color.getHexWithHash()),
						Ui.c(Ui.text() + "Click to use this colour"));
				buttons.add(Ui.button(label, tip, Ui.BUTTON, (p, v) -> choose(p, color)));
			}
			buttons.add(Ui.button(Ui.back("Back"), null, Ui.BUTTON, (p, v) -> {
				if (GuiManager.isEnabled()) {
					new MainMenu(p).open();
				} else {
					p.closeInventory();
				}
			}));
			buttons.add(Ui.button(Ui.close(), null, Ui.BUTTON, (p, v) -> p.closeInventory()));

			List<DialogBody> body = List.of(DialogBody.plainMessage(
					Ui.header(null, current.toLegacy() + "Now: " + current.getDisplayName(), "Pick a colour for " + team.getName()), 300));
			Dialog dialog = Dialog.create(b -> b.empty()
					.base(DialogBase.builder(Ui.title("Team Colour")).body(body).build())
					.type(DialogType.multiAction(buttons, null, 2)));
			player.showDialog(dialog);
		} catch (Throwable e) {
			Main.plugin.getLogger().warning("Could not open the colour dialog: " + e);
		}
	}

	/** A colour button was clicked (main thread): check everything again, then change the colour */
	private static void choose(Player player, TeamColor color) {
		try {
			Team team = Team.getTeam(player);
			TeamPlayer teamPlayer = team == null ? null : team.getTeamPlayer(player);
			if (team == null || teamPlayer == null || !team.getRank(PlayerRank.OWNER).contains(teamPlayer)) {
				MessageManager.sendMessage(player, "noPerm");
				player.closeInventory();
				return;
			}
			if (TeamColorUtil.isBanned(color)) {
				MessageManager.sendMessage(player, "color.banned");
				open(player, team);
				return;
			}
			team.setColor(color);
			MessageManager.sendMessage(player, "color.success", color.toLegacy() + color.getDisplayName());
			open(player, team);
		} catch (Throwable e) {
			Main.plugin.getLogger().warning("Could not change the team colour: " + e);
		}
	}
}
