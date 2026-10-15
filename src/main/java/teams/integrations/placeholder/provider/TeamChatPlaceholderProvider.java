package teams.integrations.placeholder.provider;

import teams.Team;
import teams.TeamPlayer;
import teams.integrations.placeholder.IndividualTeamPlayerPlaceholderProvider;
import teams.message.MessageManager;

public class TeamChatPlaceholderProvider implements IndividualTeamPlayerPlaceholderProvider {
	@Override
	public String getPlaceholderForTeamPlayer(Team team, TeamPlayer player) {
		if (player.isInTeamChat()) {
			return MessageManager.getMessage("placeholder.teamChat");
		} else if (player.isInAllyChat()) {
			return MessageManager.getMessage("placeholder.allyChat");
		}
		return MessageManager.getMessage("placeholder.globalChat");
	}
}
