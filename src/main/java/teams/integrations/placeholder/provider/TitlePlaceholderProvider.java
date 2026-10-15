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
public class TitlePlaceholderProvider implements IndividualTeamPlayerPlaceholderProvider {

	@Override
	public String getPlaceholderForTeamPlayer(Team team, TeamPlayer player) {

		if (player.getTitle() == null || player.getTitle().isEmpty()) {
			return MessageManager.getMessage("placeholder.noTitle");
		}

		return player.getTitle();
	}

}
