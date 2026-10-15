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
public class TagPlaceholderProvider implements IndividualTeamPlaceholderProvider {

	@Override
	public String getPlaceholderForTeam(Team team) {
		return MessageManager.getMessage("placeholder.tag", team.getTag(false));
	}
}
