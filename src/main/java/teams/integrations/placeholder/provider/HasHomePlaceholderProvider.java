package teams.integrations.placeholder.provider;

import teams.Team;
import teams.integrations.placeholder.IndividualTeamPlaceholderProvider;
import teams.message.MessageManager;

public class HasHomePlaceholderProvider implements IndividualTeamPlaceholderProvider {
	@Override
	public String getPlaceholderForTeam(Team team) {
		return team.getTeamHome() != null ? MessageManager.getMessage("placeholder.hasHome") : MessageManager.getMessage("placeholder.noHome");
	}
}
