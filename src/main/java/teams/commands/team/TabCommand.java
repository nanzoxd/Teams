package teams.commands.team;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import teams.CommandResponse;
import teams.Main;
import teams.Team;
import teams.commands.SubCommand;
import teams.display.TeamsTab;
import teams.gui.Theme;
import teams.message.MessageManager;

/** /team tab: switch the player list between Everyone and Teammates (the same as sneaking) */
public class TabCommand extends SubCommand {

	@Override
	public CommandResponse onCommand(CommandSender sender, String label, String[] args) {
		Player player = (Player) sender;
		TeamsTab tab = Main.plugin.getTeamsTab();
		if (tab == null) {
			MessageManager.sendFullMessage(player, Theme.accent() + "Teams Tab is not turned on on this server.", false);
			return new CommandResponse(true);
		}
		if (!TeamsTab.switchable()) {
			MessageManager.sendFullMessage(player, Theme.accent() + "The tab view is fixed by the server (" + TeamsTab.mode() + " only).", false);
			return new CommandResponse(true);
		}
		if (Team.getTeam(player) == null) {
			MessageManager.sendFullMessage(player, Theme.accent() + "Join a team to switch the tab view.", false);
			return new CommandResponse(true);
		}
		String view = tab.toggle(player);
		MessageManager.sendFullMessage(player, Theme.text() + "Tab view: " + Theme.highlight()
				+ (view.equals("team") ? "Teammates" : "Everyone"), false);
		return new CommandResponse(true);
	}

	@Override
	public String getCommand() {
		return "tab";
	}

	@Override
	public String getNode() {
		return "tab";
	}

	@Override
	public String getHelp() {
		return "Switch the player list between everyone and your teammates";
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
	public boolean needPlayer() {
		return true;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {
	}
}
