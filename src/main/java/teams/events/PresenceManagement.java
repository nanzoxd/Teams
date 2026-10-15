package teams.events;

import teams.Main;
import teams.Team;
import teams.message.Message;
import teams.message.ReferencedFormatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Tells a player's teammates when that player logs in to / out of the server.
 * This is different from joining / leaving the team (see {@link MessagesManagement}).
 */
public class PresenceManagement implements Listener {

	private void notifyTeam(@NotNull Player player, @NotNull String messageKey) {
		if (!Main.plugin.getConfig().getBoolean("chat.notifyTeamPresence", true)) {
			return;
		}

		Team team = Team.getTeam(player);
		if (team == null) {
			return;
		}

		Message message = new ReferencedFormatMessage(messageKey, player.getDisplayName());
		for (Player member : team.getOnlineMembers()) {
			if (!member.equals(player)) {
				message.sendMessage(member);
			}
		}
	}

	@EventHandler
	public void onJoin(@NotNull PlayerJoinEvent e) {
		notifyTeam(e.getPlayer(), "presence.online");
	}

	@EventHandler
	public void onQuit(@NotNull PlayerQuitEvent e) {
		notifyTeam(e.getPlayer(), "presence.offline");
	}
}
