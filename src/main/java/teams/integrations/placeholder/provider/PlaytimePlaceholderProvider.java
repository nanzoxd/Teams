package teams.integrations.placeholder.provider;

import teams.Team;
import teams.integrations.placeholder.IndividualTeamPlaceholderProvider;
import teams.stats.TeamStats;

/**
 * %teams_playtime% - the total playtime, i.e. 3d 4h of the team
 */
public class PlaytimePlaceholderProvider implements IndividualTeamPlaceholderProvider {

	@Override
	public String getPlaceholderForTeam(Team team) {
		return TeamStats.formatDuration(TeamStats.getPlaytimeSeconds(team));
	}
}
