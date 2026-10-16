package teams.dialog;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import teams.gui.Theme;

/** /teamconfig, registered straight in the command map (there is no commands block in plugin.yml). */
public final class TeamConfigCommand extends Command {

	private static boolean registered;

	private TeamConfigCommand() {
		super("teamconfig", "Change the team settings", "/teamconfig", List.of("tconfig"));
		setPermission("teams.admin");
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;
		Bukkit.getCommandMap().register("teams", new TeamConfigCommand());
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!sender.hasPermission("teams.admin")) {
			sender.sendMessage(Ui.c(Theme.bad() + "You can not do that"));
			return true;
		}
		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can open the config dialog.");
			return true;
		}
		if (!DialogSupport.available()) {
			player.sendMessage(Ui.c(Theme.bad() + "Dialogs need Paper 1.21.6 or newer"));
			return true;
		}
		ConfigDialog.open(player);
		return true;
	}
}
