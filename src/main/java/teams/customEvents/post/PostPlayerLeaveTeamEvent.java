package teams.customEvents.post;

import teams.Team;
import teams.TeamPlayer;
import teams.customEvents.PlayerLeaveTeamEvent;
import teams.customEvents.TeamPlayerEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * An event which is called after a player has left their team.
 * This event cannot be cancelled since it occurs after the player has left.
 * <p>
 * To modify or cancel the leave action, use {@link PlayerLeaveTeamEvent}.
 *
 * @author svaningelgem
 */
public class PostPlayerLeaveTeamEvent extends TeamPlayerEvent {

	private static final HandlerList HANDLERS = new HandlerList();

	public PostPlayerLeaveTeamEvent(Team team, TeamPlayer teamPlayer) {
		super(team, teamPlayer, false);
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

}
