package teams.integrations.placeholder.provider;

import teams.Team;
import teams.integrations.placeholder.IndividualTeamPlaceholderProvider;
import teams.stats.TeamStats;

/**
 * %teams_kdr% - the kills divided by deaths of the team
 */
public class KdrPlaceholderProvider implements IndividualTeamPlaceholderProvider {

	@Override
	public String getPlaceholderForTeam(Team team) {
		return TeamStats.formatKdr(TeamStats.getKdr(team));
	}
}
