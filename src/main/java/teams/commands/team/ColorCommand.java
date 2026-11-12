package teams.commands.team;

import teams.*;
import teams.commands.presets.TeamSubCommand;
import teams.message.ReferencedFormatMessage;
import teams.util.TeamColorUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import teams.dialog.ColorDialog;
import teams.dialog.DialogSupport;
import teams.gui.ColorHelp;

import java.util.List;

public class ColorCommand extends TeamSubCommand {

	@Override
	public CommandResponse onCommand(TeamPlayer teamPlayer, String label, String[] args, Team team) {

		// no colour given: the dialog (or the chat list when dialogs are off)
		if (args.length == 0) {
			Player player = teamPlayer.getPlayer().getPlayer();
			if (player != null) {
				if (DialogSupport.enabled()) {
					ColorDialog.open(player, team);
				} else {
					ColorHelp.send(player);
				}
			}
			return new CommandResponse(true);
		}

		TeamColor color = TeamColor.fromInput(args[0]);
		if (color == null) {
			return new CommandResponse("color.fail");
		}

		if (TeamColorUtil.isBanned(color)) {
			return new CommandResponse("color.banned");
		}

		team.setColor(color);

		return new CommandResponse(true, new ReferencedFormatMessage("color.success", color.toLegacy() + color.getDisplayName()));
	}

	@Override
	public String getCommand() {
		return "color";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public String getNode() {
		return "color";
	}

	@Override
	public String getHelp() {
		return "Change your teams color";
	}

	@Override
	public String getArguments() {
		return "[color]";
	}

	@Override
	public int getMaximumArguments() {
		return 1;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			for (String id : TeamColorUtil.getAllowedIds()) {
				if (id.startsWith(args[0].toLowerCase())) {
					options.add(id);
				}
			}
		}
	}

	@Override
	public PlayerRank getDefaultRank() {
		return PlayerRank.OWNER;
	}

	@Override
	protected boolean runAsync(String[] args) {
		return false;
	}

}
