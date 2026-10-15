package teams.customEvents;

import teams.Team;
import teams.TeamPlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player joins a team
 *
 * @author booksaw
 */
public class PlayerJoinTeamEvent extends TeamPlayerEvent {

	private static final HandlerList HANDLERS = new HandlerList();

	public PlayerJoinTeamEvent(Team team, TeamPlayer teamPlayer) {
		super(team, teamPlayer, false);
	}

	@SuppressWarnings("unused")
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@Override
	public @NotNull HandlerList getHandlers() {
		return HANDLERS;
	}

}
