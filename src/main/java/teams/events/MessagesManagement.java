package teams.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import teams.customEvents.post.PostCreateTeamEvent;
import teams.customEvents.post.PostDemotePlayerEvent;
import teams.customEvents.post.PostDisbandTeamEvent;
import teams.customEvents.post.PostPlayerJoinTeamEvent;
import teams.customEvents.post.PostPlayerLeaveTeamEvent;
import teams.customEvents.post.PostPromotePlayerEvent;
import teams.util.Notify;

/**
 * Only a sound for the player who did it (see {@code sounds} in config.yml). There are no announcements:
 * nobody else is told when a team is created, joined, left or disbanded.
 */
public class MessagesManagement implements Listener {

	@EventHandler
	public void onPostCreateTeamEvent(@NotNull PostCreateTeamEvent e) {
		Notify.sound(e.getPlayer(), "create");
	}

	@EventHandler
	public void onPostPlayerJoinTeamEvent(@NotNull PostPlayerJoinTeamEvent e) {
		Notify.sound(e.getPlayer().getPlayer(), "join");
	}

	@EventHandler
	public void onPostPlayerLeaveTeamEvent(@NotNull PostPlayerLeaveTeamEvent e) {
		Notify.sound(e.getPlayer().getPlayer(), "leave");
	}

	@EventHandler
	public void onPostDisbandTeamEvent(@NotNull PostDisbandTeamEvent e) {
		Notify.sound(e.getPlayer(), "disband");
	}

	@EventHandler
	public void onPromote(@NotNull PostPromotePlayerEvent e) {
		Player p = e.getTeamPlayer().getPlayer().getPlayer();
		Notify.sound(p, "promote");
	}

	@EventHandler
	public void onDemote(@NotNull PostDemotePlayerEvent e) {
		Player p = e.getTeamPlayer().getPlayer().getPlayer();
		Notify.sound(p, "demote");
	}
}
