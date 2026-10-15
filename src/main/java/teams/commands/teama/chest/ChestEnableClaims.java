package teams.commands.teama.chest;

import teams.CommandResponse;
import teams.commands.SubCommand;
import teams.events.ChestManagement;
import teams.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ChestEnableClaims extends SubCommand {

	@Override
	public CommandResponse onCommand(CommandSender sender, String label, String[] args) {
		if (ChestManagement.enableClaims) {
			return new CommandResponse("admin.chest.enable.already");
		}

		ChestManagement.enableClaims = true;
		Bukkit.broadcastMessage(MessageManager.getPrefix() + MessageManager.getMessage("admin.chest.enabled.bc"));
		return new CommandResponse("admin.chest.enable.success");
	}

	@Override
	public String getCommand() {
		return "enableclaims";
	}

	@Override
	public String getNode() {
		return "admin.chest.enable";
	}

	@Override
	public String getHelp() {
		return "Re-enable Teams chest Claims";
	}

	@Override
	public String getArguments() {
		return "";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public int getMaximumArguments() {
		return 0;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {

	}

}
