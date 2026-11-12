package teams.commands.team;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import teams.CommandResponse;
import teams.Main;
import teams.commands.SubCommand;
import teams.dialog.DialogSupport;
import teams.dialog.PlayerSettingsDialog;
import teams.gui.Theme;
import teams.message.MessageManager;
import teams.settings.PlayerSettings;

/** /team prefs: the settings a player changes for themselves. Opens a dialog, or works with words when dialogs are off. */
public class PrefsCommand extends SubCommand {

	private static final List<String> NAMES = List.of("notices", "crown", "locator", "chat");

	@Override
	public CommandResponse onCommand(CommandSender sender, String label, String[] args) {
		Player player = (Player) sender;
		UUID id = player.getUniqueId();

		if (args.length == 0 && DialogSupport.enabled()) {
			PlayerSettingsDialog.open(player);
			return new CommandResponse(true);
		}

		if (args.length > 0) {
			switch (args[0].toLowerCase()) {
				case "notices" -> PlayerSettings.set(id, "muteNotices", !PlayerSettings.get(id, "muteNotices", false));
				case "crown" -> {
					PlayerSettings.set(id, "crown", !PlayerSettings.get(id, "crown", true));
					if (Main.plugin.teamManagement != null) {
						Main.plugin.teamManagement.displayBelowName(player);
					}
				}
				case "locator" -> PlayerSettings.set(id, "locator", !PlayerSettings.get(id, "locator", true));
				case "chat" -> {
					String now = PlayerSettings.getString(id, "chatMode", "public");
					String next = now.equals("public") ? "team" : now.equals("team") ? "ally" : "public";
					PlayerSettings.setString(id, "chatMode", next);
				}
				default -> {
					return new CommandResponse(false, "invalidArg");
				}
			}
		}

		MessageManager.sendFullMessage(player, Theme.light() + "Team notices " + onOff(!PlayerSettings.get(id, "muteNotices", false))
				+ Theme.light() + ", owner crown " + onOff(PlayerSettings.get(id, "crown", true))
				+ Theme.light() + ", locator bar " + onOff(PlayerSettings.get(id, "locator", true))
				+ Theme.light() + ", chat on login " + Theme.main() + PlayerSettings.getString(id, "chatMode", "public")
				+ Theme.soft() + "  (/team prefs <notices|crown|locator|chat>)", false);
		return new CommandResponse(true);
	}

	private static String onOff(boolean on) {
		return (on ? Theme.good() + "on" : Theme.bad() + "off");
	}

	@Override
	public String getCommand() {
		return "prefs";
	}

	@Override
	public String getNode() {
		return "prefs";
	}

	@Override
	public String getHelp() {
		return "Your own settings: team notices, chat, crown, locator bar";
	}

	@Override
	public String getArguments() {
		return "[notices|crown|locator|chat]";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public int getMaximumArguments() {
		return 1;
	}

	@Override
	public boolean needPlayer() {
		return true;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			for (String name : NAMES) {
				if (name.startsWith(args[0].toLowerCase())) {
					options.add(name);
				}
			}
		}
	}
}
