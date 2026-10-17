package teams.commands.teama;

import teams.CommandResponse;
import teams.Team;
import teams.commands.presets.TeamSelectSubCommand;
import teams.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class InviteTeama extends TeamSelectSubCommand {

	@Override
	public CommandResponse onCommand(CommandSender sender, String label, String[] args, Team team) {
		Player toInvite = Bukkit.getPlayer(args[1]);

		if (toInvite == null) {
			return new CommandResponse("noPlayer");
		}

		if (team.isBanned(toInvite)) {
			return new CommandResponse("invite.banned");
		}

		if (Team.getTeam(toInvite) != null) {
			return new CommandResponse("invite.inTeam");
		}

		int limit = team.getTeamLimit();

		if (limit > 0 && limit <= team.getMembers().size() + team.getInvitedPlayers().size()) {
			return new CommandResponse("invite.full");
		}

		// player being invited is not in a team
		team.invite(toInvite.getUniqueId());
		net.kyori.adventure.text.Component component = teams.text.Formatter.absolute().process(MessageManager.getMessage(toInvite, "invite.invite", team.getName()));
		component = component.clickEvent(net.kyori.adventure.text.event.ClickEvent.runCommand("/team join " + team.getName()));
		MessageManager.sendFullMessage(toInvite, component, true);
		teams.util.Notify.sound(toInvite, "invite");
		return new CommandResponse(true, "admin.invite.success");
	}

	@Override
	public String getCommand() {
		return "invite";
	}

	@Override
	public String getNode() {
		return "admin.invite";
	}

	@Override
	public String getHelp() {
		return "Invite a player to the specified team";
	}

	@Override
	public String getArguments() {
		return "<team> <player>";
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
	public boolean needPlayer() {
		return true;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {

		if (args.length == 1) {
			addTeamStringList(options, args[0]);
		} else if (args.length == 2) {
			addPlayerStringList(options, args[1]);
		}
	}

	@Override
	protected boolean runAsync(String[] args) {
		return false;
	}

}
