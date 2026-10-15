package teams.integrations.placeholder.provider;

import teams.Team;
import teams.integrations.placeholder.IndividualTeamPlaceholderProvider;
import teams.stats.TeamStats;

/**
 * %teams_kills% - the number of kills of the team
 */
public class KillsPlaceholderProvider implements IndividualTeamPlaceholderProvider {

	@Override
	public String getPlaceholderForTeam(Team team) {
		return String.valueOf(TeamStats.getKills(team));
	}
}
