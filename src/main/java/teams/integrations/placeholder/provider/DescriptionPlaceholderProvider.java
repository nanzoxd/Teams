/**
 *
 */
package teams.integrations.placeholder.provider;

import teams.Team;
import teams.integrations.placeholder.IndividualTeamPlaceholderProvider;
import teams.message.MessageManager;

/**
 * @author booksaw
 */
public class DescriptionPlaceholderProvider implements IndividualTeamPlaceholderProvider {
	@Override
	public String getPlaceholderForTeam(Team team) {
		if (team.getDescription() == null || team.getDescription().isEmpty()) {
			return MessageManager.getMessage("placeholder.noDescription");
		}

		return team.getDescription();
	}
}
