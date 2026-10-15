/**
 *
 */
package teams.integrations.placeholder.provider;

import teams.Team;
import teams.integrations.placeholder.IndividualTeamPlaceholderProvider;

/**
 * @author booksaw
 */
public class OfflineListPlaceholderProvider implements IndividualTeamPlaceholderProvider {

	@Override
	public String getPlaceholderForTeam(Team team) {
		return team.getMembers().getOfflinePlayersString();
	}

}
