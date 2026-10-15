package teams.integrations.placeholder.provider;

import teams.Team;
import teams.integrations.placeholder.IndividualTeamPlaceholderProvider;

/**
 * %teams_colorhex% - the hex of the team colour (i.e. #87CEEB)
 */
public class ColorHexPlaceholderProvider implements IndividualTeamPlaceholderProvider {

	@Override
	public String getPlaceholderForTeam(Team team) {
		return team.getColor().getHexWithHash();
	}
}
