package teams.commands.team;

import teams.CommandResponse;
import teams.PlayerRank;
import teams.Team;
import teams.TeamPlayer;
import teams.commands.presets.TeamSubCommand;
import teams.message.MessageManager;
import teams.message.ReferencedFormatSafeMessage;
import org.bukkit.command.CommandSender;

import java.util.List;

public class DescriptionCommand extends TeamSubCommand {

	public DescriptionCommand() {
		checkRank = false;
	}

	@Override
	public CommandResponse onCommand(TeamPlayer teamPlayer, String label, String[] args, Team team) {

		if (args.length == 0) {
			if (team.getDescription() != null && !team.getDescription().isEmpty()) {
				System.out.println("Description : " + team.getDescription());
				return new CommandResponse(true,
						new ReferencedFormatSafeMessage("description.view", team.getDescription()));
			} else {
				return new CommandResponse("description.noDesc");
			}

		}

		if (teamPlayer.getRank().value < getRequiredRank().value) {

			if (team.getDescription() != null && !team.getDescription().isEmpty()) {
				MessageManager.sendMessage(teamPlayer.getPlayer().getPlayer(), "description.view",
						team.getDescription());
			} else {
				MessageManager.sendMessage(teamPlayer.getPlayer().getPlayer(), "description.noDesc");
			}
			return new CommandResponse("description.noPerm");
		}

		StringBuilder newDescrip = new StringBuilder();
		for (String temp : args) {
			newDescrip.append(temp).append(" ");
		}

		team.setDescription(newDescrip.toString());

		return new CommandResponse(true, "description.success");
	}

	@Override
	public String getCommand() {
		return "description";
	}

	@Override
	public int getMinimumArguments() {
		return 0;
	}

	@Override
	public String getNode() {
		return "description";
	}

	@Override
	public String getHelp() {
		return "View and change your team's description";
	}

	@Override
	public String getArguments() {
		return "[description]";
	}

	@Override
	public int getMaximumArguments() {
		return -1;
	}

	@Override
	public void onTabComplete(List<String> options, CommandSender sender, String label, String[] args) {

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
