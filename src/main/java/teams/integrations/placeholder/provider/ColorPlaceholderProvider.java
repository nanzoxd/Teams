package teams.integrations.placeholder.provider;

import teams.Team;
import teams.integrations.placeholder.IndividualTeamPlaceholderProvider;

/**
 * %teams_color% - the colour of the team as a colour code that can be used in front of any text
 *
 * @author booksaw
 */
public class ColorPlaceholderProvider implements IndividualTeamPlaceholderProvider {

	@Override
	public String getPlaceholderForTeam(Team team) {
		return team.getColor().toLegacy();
	}
}
