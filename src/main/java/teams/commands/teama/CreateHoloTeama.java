package teams.commands.teama;

import teams.CommandResponse;
import teams.commands.SubCommand;
import teams.integrations.hologram.HologramManager;
import teams.integrations.hologram.HologramManager.HologramType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CreateHoloTeama extends SubCommand {

	@Override
	public CommandResponse onCommand(CommandSender sender, String label, String[] args) {
		Player p = (Player) sender;
		if (args[0].equals("score")) {
			HologramManager.holoManager.createHolo(p.getLocation(), HologramType.SCORE);
			return new CommandResponse(true, "admin.holo.create.success");
		}

		return new CommandResponse("help");
	}

	@Override
	public String getCommand() {
		return "create";
	}

	@Override
	public String getNode() {
		return "admin.holo";
	}

	@Override
	public String getHelp() {
		return "Create a hologram at your current location";
	}

	@Override
	public String getArguments() {
		return "<score>";
	}

	@Override
	public int getMinimumArguments() {
		return 1;
	}

	@Override
	public int getMaximumArguments() {
		return 1;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {
		if (args.length == 1) {
			options.add("score");
		}

	}

	@Override
	public boolean needPlayer() {
		return true;
	}

	@Override
	public boolean runAsync(String[] args) {
		return false;
	}

}
