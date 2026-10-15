package teams.commands.teama;

import teams.CommandResponse;
import teams.Main;
import teams.Team;
import teams.Warp;
import teams.commands.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SetwarpTeama extends SubCommand {

	@Override
	public CommandResponse onCommand(CommandSender sender, String label, String[] args) {

		Team team = Team.getTeam(args[0]);
		if (team == null) {
			return new CommandResponse("noTeam");
		}

		if (team.getWarp(args[1]) != null) {
			return new CommandResponse("setwarp.exist");
		}

		if (team.getWarps().size() >= team.getMaxWarps()) {
			return new CommandResponse("admin.setwarp.max");
		}

		team.addWarp(new Warp(args[1], ((Player) sender).getLocation(), null));

		return new CommandResponse("admin.setwarp.success");
	}

	@Override
	public String getCommand() {
		return "setwarp";
	}

	@Override
	public String getNode() {
		return "admin.setwarp";
	}

	@Override
	public String getHelp() {
		return "Set a warp for that team";
	}

	@Override
	public String getArguments() {
		return "<team> <name> [password]";
	}

	@Override
	public int getMinimumArguments() {
		return 2;
	}

	@Override
	public int getMaximumArguments() {
		return 3;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			options.add("<team>");
		}
		if (args.length == 2) {
			options.add("<name>");
		}
		if (args.length == 3 && Main.plugin.getConfig().getBoolean("teams.allowPassword")) {
			options.add("[password]");
		}
	}

	@Override
	public boolean needPlayer() {
		return true;
	}

	@Override
	protected boolean runAsync(String[] args) {
		return false;
	}

}
