package teams.customEvents;

import teams.Team;
import lombok.Getter;
import lombok.Setter;
import teams.TeamColor;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * An event which is called right before the recoloring of a {@link Team}
 */
@Getter
@Setter
public class TeamColorChangeEvent extends TeamEvent {
	private TeamColor newTeamColor;

	public TeamColorChangeEvent(@NotNull Team team,
	                            @NotNull TeamColor newColor) {
		super(team, false);

		this.newTeamColor = newColor;
	}

	private static final HandlerList HANDLERS = new HandlerList();

	@SuppressWarnings("unused")
	public static HandlerList getHandlerList() {
		return HANDLERS;
	}

	@NotNull
	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

}
