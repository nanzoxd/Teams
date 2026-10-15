package teams.integrations.placeholder.provider;

import teams.Team;
import teams.integrations.placeholder.IndividualTeamPlaceholderProvider;
import teams.message.MessageManager;

/**
 * %teams_belowname% - the team tag in the team colour, i.e. [APXR].
 * <p>
 * This is made to be used with the TAB plugin (see the tab-example.yml included with the plugin), so the
 * team is displayed on its own line below the player name. The format of the text is placeholder.belowname
 * in messages.yml
 */
public class BelowNamePlaceholderProvider implements IndividualTeamPlaceholderProvider {

	@Override
	public String getPlaceholderForTeam(Team team) {
		return getPlaceholderForTeam(team, false);
	}

	/**
	 * @param owner if the player is an owner of the team, the tag then has the crown in it
	 */
	public String getPlaceholderForTeam(Team team, boolean owner) {
		// the tag (or the team name if it has no tag), without any colour of its own
		return team.getColor().toLegacy() + MessageManager.getMessage("placeholder.belowname", team.getHeadTag(owner));
	}
}
