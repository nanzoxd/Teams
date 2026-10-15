package teams.customEvents;

import teams.Team;
import teams.TeamPlayer;
import lombok.Getter;
import org.bukkit.OfflinePlayer;

@Getter
public abstract class TeamPlayerEvent extends TeamEvent {

	protected final TeamPlayer teamPlayer;

	public TeamPlayerEvent(Team team, TeamPlayer teamPlayer, final boolean isAsync) {
		super(team, isAsync);

		this.teamPlayer = teamPlayer;
	}

	public OfflinePlayer getPlayer() {
		return teamPlayer.getPlayer();
	}
}
