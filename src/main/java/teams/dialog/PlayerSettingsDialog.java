package teams.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import teams.Main;
import teams.PlayerRank;
import teams.Team;
import teams.TeamPlayer;
import teams.display.TeamsTab;
import teams.gui.GuiManager;
import teams.gui.MainMenu;
import teams.settings.PlayerSettings;

/** The settings a player changes for themselves. Everything here only affects the player who clicks. */
public final class PlayerSettingsDialog {

	private static final List<String> MODES = List.of("public", "team", "ally");

	private PlayerSettingsDialog() {
	}

	public static void open(Player player) {
		try {
			UUID id = player.getUniqueId();
			Team team = Team.getTeam(player);
			List<ActionButton> buttons = new ArrayList<>();

			boolean notices = !PlayerSettings.get(id, "muteNotices", false);
			buttons.add(Ui.button(Ui.mark(notices, "Team notices"),
					Ui.label() + "Join, leave and login messages of your team  -  click to turn " + (notices ? "off" : "on"), Ui.BUTTON,
					(p, v) -> {
						PlayerSettings.set(id, "muteNotices", notices);
						open(p);
					}));

			String mode = PlayerSettings.getString(id, "chatMode", "public");
			buttons.add(Ui.button(Ui.cycle("Chat on login", mode.substring(0, 1).toUpperCase() + mode.substring(1)),
					Ui.label() + "Which chat you start in: public, team or ally  -  click to change", Ui.BUTTON, (p, v) -> {
						PlayerSettings.setString(id, "chatMode", MODES.get((MODES.indexOf(mode) + 1) % MODES.size()));
						open(p);
					}));

			TeamPlayer self = team == null ? null : team.getTeamPlayer(player);
			if (self != null && self.getRank() == PlayerRank.OWNER) {
				boolean crown = PlayerSettings.get(id, "crown", true);
				buttons.add(Ui.button(Ui.mark(crown, "Owner crown"),
						Ui.label() + "Show your crown next to your team tag  -  click to turn " + (crown ? "off" : "on"), Ui.BUTTON,
						(p, v) -> {
							PlayerSettings.set(id, "crown", !crown);
							if (Main.plugin.teamManagement != null) {
								Main.plugin.teamManagement.displayBelowName(p);
							}
							open(p);
						}));
			}

			if (Main.plugin.getConfig().getBoolean("locator.enabled", false)) {
				boolean locator = PlayerSettings.get(id, "locator", true);
				buttons.add(Ui.button(Ui.mark(locator, "Locator bar"),
						Ui.label() + "Show yourself on your teammates' locator bar  -  click to turn " + (locator ? "off" : "on"), Ui.BUTTON,
						(p, v) -> {
							PlayerSettings.set(id, "locator", !locator);
							open(p);
						}));
			}

			if (Main.plugin.getConfig().getBoolean("tablist.enabled", false) && team != null) {
				boolean sneak = PlayerSettings.get(id, "tabSneak", true);
				buttons.add(Ui.button(Ui.mark(sneak, "Tab sneak switch"),
						Ui.label() + "Sneak to switch the player list between everyone and teammates (/team tab does the same)  -  click to turn " + (sneak ? "off" : "on"),
						Ui.BUTTON, (p, v) -> {
							PlayerSettings.set(id, "tabSneak", !sneak);
							open(p);
						}));
				String view = TeamsTab.view(player);
				buttons.add(Ui.button(Ui.cycle("Tab view", view.equals("team") ? "Teammates" : "Everyone"),
						Ui.label() + "What your player list shows  -  click to change", Ui.BUTTON, (p, v) -> {
							if (Main.plugin.getTeamsTab() != null) {
								Main.plugin.getTeamsTab().toggle(p);
							}
							open(p);
						}));
			}

			if (team != null) {
				buttons.add(Ui.button(Ui.plain("Muted teammates"), Ui.label() + "Hide the team chat of a teammate for yourself",
						Ui.BUTTON, (p, v) -> openMutes(p)));
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
					Ui.header(null, Ui.text() + "My Settings", "Only you see these changes"), 300));
			Dialog dialog = Dialog.create(b -> b.empty()
					.base(DialogBase.builder(Ui.title("My Settings")).body(body).build())
					.type(DialogType.multiAction(buttons, null, 2)));
			player.showDialog(dialog);
		} catch (Throwable e) {
			Main.plugin.getLogger().warning("Could not open the settings dialog: " + e);
		}
	}

	/** Every teammate with a tick (heard) or a cross (muted in your team chat) */
	private static void openMutes(Player player) {
		try {
			Team team = Team.getTeam(player);
			if (team == null) {
				open(player);
				return;
			}
			UUID id = player.getUniqueId();
			List<ActionButton> buttons = new ArrayList<>();
			for (TeamPlayer member : team.getMembers().getClone()) {
				UUID target = member.getPlayer().getUniqueId();
				if (target.equals(id)) {
					continue;
				}
				String name = member.getPlayer().getName() == null ? "?" : member.getPlayer().getName();
				boolean muted = PlayerSettings.isMuted(id, target);
				buttons.add(Ui.button(Ui.mark(!muted, name),
						Ui.label() + (muted ? "Muted in team chat" : "Heard in team chat") + "  -  click to " + (muted ? "unmute" : "mute"),
						Ui.BUTTON, (p, v) -> {
							PlayerSettings.toggleMute(id, target);
							openMutes(p);
						}));
			}
			buttons.add(Ui.button(Ui.back("Back"), null, Ui.BUTTON, (p, v) -> open(p)));
			buttons.add(Ui.button(Ui.close(), null, Ui.BUTTON, (p, v) -> p.closeInventory()));

			String sub = buttons.size() <= 2 ? "No teammates yet" : "A cross means you do not see their team chat";
			List<DialogBody> body = List.of(DialogBody.plainMessage(Ui.header(null, Ui.text() + "Muted teammates", sub), 300));
			Dialog dialog = Dialog.create(b -> b.empty()
					.base(DialogBase.builder(Ui.title("Muted teammates")).body(body).build())
					.type(DialogType.multiAction(buttons, null, 2)));
			player.showDialog(dialog);
		} catch (Throwable e) {
			Main.plugin.getLogger().warning("Could not open the mute list: " + e);
		}
	}
}
