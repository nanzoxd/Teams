package teams.integrations.placeholder;

import teams.Team;
import teams.TeamPlayer;

/**
 * @author booksaw
 */
public interface IndividualTeamPlayerPlaceholderProvider {

	String getPlaceholderForTeamPlayer(Team team, TeamPlayer player);


}
