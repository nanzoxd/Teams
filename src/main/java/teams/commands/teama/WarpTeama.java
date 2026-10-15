package teams.commands.teama;

import teams.CommandResponse;
import teams.Main;
import teams.Team;
import teams.Warp;
import teams.commands.SubCommand;
import teams.message.ReferencedFormatMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class WarpTeama extends SubCommand {

	@Override
	public CommandResponse onCommand(CommandSender sender, String label, String[] args) {

		Team team = Team.getTeam(args[0]);
		if (team == null) {
			return new CommandResponse("noTeam");
		}

		if (args.length == 1) {

			StringBuilder replace = new StringBuilder();
			for (Warp warp : team.getWarps().get()) {
				replace.append(warp.getName()).append(", ");
			}

			if (replace.length() == 0) { // JDK 8 doesn't have StringBuilder::isEmpty yet
				return new CommandResponse("admin.warps.none");
			}

			replace = new StringBuilder(replace.substring(0, replace.length() - 2));

			return new CommandResponse(new ReferencedFormatMessage("warps.syntax", replace.toString()));
		}

		Warp warp = team.getWarp(args[1]);
		if (warp == null) {
			return new CommandResponse("warp.nowarp");
		}

		// the user is allowed to go to the warp
		try {
			warp.execute((Player) sender);
		} catch (Exception e) {
			return new CommandResponse("warp.world");
		}

		return new CommandResponse(true);

	}

	@Override
	public String getCommand() {
		return "warp";
	}

	@Override
	public String getNode() {
		return "admin.warp";
	}

	@Override
	public String getHelp() {
		return "Teleport to a teams warp";
	}

	@Override
	public String getArguments() {
		return "<team> <warp>";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public int getMaximumArguments() {
		return 2;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			options.add("<team>");
		}
		if (args.length == 2 && Main.plugin.getConfig().getBoolean("teams.allowPassword")) {
			options.add("<warp>");
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
