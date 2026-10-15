package teams.integrations.placeholder.provider;

import teams.Team;
import teams.integrations.placeholder.IndividualTeamWithDataPlaceholderProvider;
import teams.message.MessageManager;

public class MetaPlaceholderProvider implements IndividualTeamWithDataPlaceholderProvider {
	@Override
	public String getPlaceholderForTeam(Team team, String data) {
		return team.getMeta().get().get(data).orElse(MessageManager.getMessage("placeholder.noMeta"));
	}
}
