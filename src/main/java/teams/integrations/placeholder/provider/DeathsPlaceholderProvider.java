package teams.integrations.placeholder.provider;

import teams.Team;
import teams.integrations.placeholder.IndividualTeamPlaceholderProvider;
import teams.stats.TeamStats;

/**
 * %teams_deaths% - the number of deaths of the team
 */
public class DeathsPlaceholderProvider implements IndividualTeamPlaceholderProvider {

	@Override
	public String getPlaceholderForTeam(Team team) {
		return String.valueOf(TeamStats.getDeaths(team));
	}
}
