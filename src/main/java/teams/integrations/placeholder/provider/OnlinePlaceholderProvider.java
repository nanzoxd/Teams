/**
 *
 */
package teams.integrations.placeholder.provider;

import teams.Team;
import teams.integrations.placeholder.IndividualTeamPlaceholderProvider;

/**
 * @author booksaw
 */
public class OnlinePlaceholderProvider implements IndividualTeamPlaceholderProvider {

	@Override
	public String getPlaceholderForTeam(Team team) {
		return Integer.toString(team.getOnlineMembers().size());
	}

}
