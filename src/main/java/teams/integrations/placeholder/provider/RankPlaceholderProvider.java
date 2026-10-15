/**
 *
 */
package teams.integrations.placeholder.provider;

import teams.Team;
import teams.TeamPlayer;
import teams.integrations.placeholder.IndividualTeamPlayerPlaceholderProvider;
import teams.message.MessageManager;

/**
 * @author booksaw
 */
public class RankPlaceholderProvider implements IndividualTeamPlayerPlaceholderProvider {

	@Override
	public String getPlaceholderForTeamPlayer(Team team, TeamPlayer player) {

		switch (player.getRank()) {
			case ADMIN:
				return MessageManager.getMessage("placeholder.admin");
			case OWNER:
				return MessageManager.getMessage("placeholder.owner");
			default:
				return MessageManager.getMessage("placeholder.default");
		}

	}

}