package teams.commands.teama;

import teams.CommandResponse;
import teams.Team;
import teams.commands.presets.TeamSelectSubCommand;
import teams.TeamColor;
import teams.util.TeamColorUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ColorTeama extends TeamSelectSubCommand {

	@Override
	public CommandResponse onCommand(CommandSender sender, String label, String[] args, Team team) {

		TeamColor color = TeamColor.fromInput(args[1]);
		if (color == null) {
			return new CommandResponse("color.fail");
		}

		team.setColor(color);

		return new CommandResponse(true, "admin.color.success");
	}

	@Override
	public String getCommand() {
		return "color";
	}

	@Override
	public int getMinimumArguments() {
		return 2;
	}

	@Override
	public String getNode() {
		return "admin.color";
	}

	@Override
	public String getHelp() {
		return "Change that teams color";
	}

	@Override
	public String getArguments() {
		return "<team> <color>";
	}

	@Override
	public int getMaximumArguments() {
		return 2;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {
		if (args.length == 2) {
			for (String id : TeamColorUtil.getAllowedIds()) {
				if (id.startsWith(args[1].toLowerCase())) {
					options.add(id);
				}
			}
		}
	}

	@Override
	protected boolean runAsync(String[] args) {
		return false;
	}

}
