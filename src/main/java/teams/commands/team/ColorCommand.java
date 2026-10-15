package teams.commands.team;

import teams.*;
import teams.commands.presets.TeamSubCommand;
import teams.message.ReferencedFormatMessage;
import teams.util.TeamColorUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ColorCommand extends TeamSubCommand {

	@Override
	public CommandResponse onCommand(TeamPlayer teamPlayer, String label, String[] args, Team team) {

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
		return 1;
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
		return "<color>";
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
