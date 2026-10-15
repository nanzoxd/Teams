package teams.integrations.placeholder;

import teams.Team;

/**
 * Interface for placeholders that require additional data
 */
public interface IndividualTeamWithDataPlaceholderProvider {

	String getPlaceholderForTeam(Team team, String data);
}
