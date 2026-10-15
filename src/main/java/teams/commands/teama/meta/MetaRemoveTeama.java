package teams.commands.teama.meta;

import teams.CommandResponse;
import teams.Team;
import teams.commands.presets.TeamSelectSubCommand;
import teams.message.ReferencedFormatMessage;
import org.bukkit.command.CommandSender;

import java.util.List;

public class MetaRemoveTeama extends TeamSelectSubCommand {
	@Override
	public CommandResponse onCommand(CommandSender sender, String label, String[] args, Team team) {
		String key = args[1];

		if (!team.getMeta().get().has(key)) {
			return new CommandResponse(false, "admin.meta.get.noexist");
		}

		team.removeAndSaveMeta(key);
		return new CommandResponse(true, new ReferencedFormatMessage("admin.meta.remove.success", key));
	}

	@Override
	public String getCommand() {
		return "remove";
	}

	@Override
	public String getNode() {
		return "admin.meta.remove";
	}

	@Override
	public String getHelp() {
		return "Removes a metadata key from a team";
	}

	@Override
	public String getArguments() {
		return "<team> <key>";
	}

	@Override
	public int getMinimumArguments() {
		return 2;
	}

	@Override
	public int getMaximumArguments() {
		return 2;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			addTeamStringList(options, args[0]);
		} else if (args.length == 2) {
			addMetaStringList(options, Team.getTeam(args[0]), args[1]);
		}
	}
}
