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
 * Sounds when a team is created / joined / left / disbanded (see {@code sounds} in config.yml).
 * Server wide announcements are optional and off by default. Only invites are clickable.
 */
public class MessagesManagement implements Listener {

	private enum TeamAnnouncement {
		CREATE("chat.announceTeamCreate", "announce.create", "create", false),
		JOIN("chat.announceTeamJoin", "announce.join", "join", false),
		LEAVE("chat.announceTeamLeave", "announce.leave", "leave", false),
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

	/**
	 * The sound is only for the player who did it. Nobody else is told (the announcements are off by default,
	 * the invite message is the only one that can be clicked).
	 */
	private void announce(@NotNull TeamAnnouncement type, @NotNull Team team, @Nullable Player actor, @Nullable String playerName) {
		if (actor != null) {
			Notify.sound(actor, type.soundKey);
		}
		if (!Main.plugin.getConfig().getBoolean(type.configKey, false)) {
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
				continue;
			}
			MessageManager.sendMessage(player, type.messageKey, replacements);
		}
	}

	@EventHandler
	public void onPostCreateTeamEvent(@NotNull PostCreateTeamEvent e) {
		announce(TeamAnnouncement.CREATE, e.getTeam(), e.getPlayer(), e.getPlayer() == null ? "Console" : e.getPlayer().getName());
	}

	@EventHandler
	public void onPostPlayerJoinTeamEvent(@NotNull PostPlayerJoinTeamEvent e) {
		announce(TeamAnnouncement.JOIN, e.getTeam(), e.getPlayer().getPlayer(), String.valueOf(e.getPlayer().getName()));
	}

	@EventHandler
	public void onPostPlayerLeaveTeamEvent(@NotNull PostPlayerLeaveTeamEvent e) {
		announce(TeamAnnouncement.LEAVE, e.getTeam(), e.getPlayer().getPlayer(), String.valueOf(e.getPlayer().getName()));
	}

	@EventHandler
	public void onPostDisbandTeamEvent(@NotNull PostDisbandTeamEvent e) {
		announce(TeamAnnouncement.DISBAND, e.getTeam(), e.getPlayer(), e.getPlayer() == null ? null : e.getPlayer().getName());
	}
}
