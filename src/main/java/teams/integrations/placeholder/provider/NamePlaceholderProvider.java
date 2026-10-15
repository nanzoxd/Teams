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
public class NamePlaceholderProvider implements IndividualTeamPlaceholderProvider {

	@Override
	public String getPlaceholderForTeam(Team team) {
		return MessageManager.getMessage("placeholder.name", team.getName());
	}

}
