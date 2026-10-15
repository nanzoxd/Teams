package teams.events;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import teams.Main;
import teams.Team;
import teams.customEvents.post.PostCreateTeamEvent;
import teams.customEvents.post.PostDisbandTeamEvent;
import teams.customEvents.post.PostPlayerJoinTeamEvent;
import teams.customEvents.post.PostPlayerLeaveTeamEvent;
import teams.message.MessageManager;
import teams.util.Notify;

/**
 * Server wide announcements when a team is created / joined / left / disbanded.
 * Every announcement plays a sound (see {@code sounds} in config.yml) and, when the team still exists,
 * can be clicked to join the team.
 */
public class MessagesManagement implements Listener {

	private enum TeamAnnouncement {
		CREATE("chat.announceTeamCreate", "announce.create", "create", true),
		JOIN("chat.announceTeamJoin", "announce.join", "join", true),
		LEAVE("chat.announceTeamLeave", "announce.leave", "leave", true),
		DISBAND("chat.announceTeamDisband", "announce.disband", "disband", false);

		private final String configKey;
		private final String messageKey;
		private final String soundKey;
		private final boolean clickToJoin;

		TeamAnnouncement(String configKey, String messageKey, String soundKey, boolean clickToJoin) {
			this.configKey = configKey;
			this.messageKey = messageKey;
			this.soundKey = soundKey;
			this.clickToJoin = clickToJoin;
		}
	}

	private void broadcastTeamMessage(@NotNull TeamAnnouncement type, @NotNull Team team, @Nullable String playerName) {
		if (!Main.plugin.getConfig().getBoolean(type.configKey, true)) {
			return;
		}

		String coloredTeamName = team.getColor() + team.getName() + ChatColor.RESET;
		Object[] replacements = playerName != null
				? new Object[]{playerName, coloredTeamName}
				: new Object[]{coloredTeamName};

		for (Player player : Bukkit.getOnlinePlayers()) {
			boolean isMember = team.equals(Team.getTeam(player));

			// join / leave: the team members already get their own "join.notify" / "leave.notify" message
			if (isMember && (type == TeamAnnouncement.JOIN || type == TeamAnnouncement.LEAVE)) {
				Notify.sound(player, type.soundKey);
				continue;
			}

			if (type.clickToJoin && !isMember) {
				Notify.sendClickable(player, type.messageKey, "announce.hover",
						"/team " + Notify.command("join") + " " + team.getName(),
						replacements);
			} else {
				MessageManager.sendMessage(player, type.messageKey, replacements);
			}
			Notify.sound(player, type.soundKey);
		}
	}

	@EventHandler
	public void onPostCreateTeamEvent(@NotNull PostCreateTeamEvent e) {
		broadcastTeamMessage(TeamAnnouncement.CREATE, e.getTeam(), e.getPlayer() == null ? "Console" : e.getPlayer().getName());
	}

	@EventHandler
	public void onPostPlayerJoinTeamEvent(@NotNull PostPlayerJoinTeamEvent e) {
		broadcastTeamMessage(TeamAnnouncement.JOIN, e.getTeam(), e.getPlayer().getName());
	}

	@EventHandler
	public void onPostPlayerLeaveTeamEvent(@NotNull PostPlayerLeaveTeamEvent e) {
		broadcastTeamMessage(TeamAnnouncement.LEAVE, e.getTeam(), e.getPlayer().getName());
	}

	@EventHandler
	public void onPostDisbandTeamEvent(@NotNull PostDisbandTeamEvent e) {
		broadcastTeamMessage(TeamAnnouncement.DISBAND, e.getTeam(), e.getPlayer() == null ? null : e.getPlayer().getName());
	}
}
