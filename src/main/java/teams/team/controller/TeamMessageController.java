package teams.team.controller;

import teams.Main;
import teams.Team;
import teams.TeamPlayer;
import teams.customEvents.TeamSendMessageEvent;
import teams.customEvents.post.PostTeamSendMessageEvent;
import teams.message.ChatMessage;
import teams.message.MessageManager;
import teams.text.Formatter;
import teams.text.LegacyTextUtils;
import teams.util.StringUtil;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TeamMessageController {

	public enum TeamMessageType {
		TEAM_CHAT_MESSAGE("chat.syntax", "spy.team",
				(syntax, prefix, team, sender) ->
						Formatter.absolute().process(StringUtil.setPlaceholders(
								syntax.contains("{2}") ? syntax : TEAM_DEFAULT,
								chatTag(team), sender.getName()))),
		ALLY_CHAT_MESSAGE("allychat.syntax", "spy.ally",
				(syntax, prefix, team, sender) ->
						Formatter.absolute().process(StringUtil.setPlaceholders(
								syntax.contains("&f{1}&f: {2}") ? ALLY_DEFAULT : syntax,
								chatTag(team), sender.getName())));

		// used when the server still has the old text in messages.yml (that text has no place for the message)
		private static final String TEAM_DEFAULT = "&#FFFFFF[{0}&#FFFFFF] &#FF2E4A{1} &#FFFFFF\u00BB &#FFFFFF{2}";
		private static final String ALLY_DEFAULT = "&#00C3FF[Ally] &#FFFFFF[{0}&#FFFFFF] &#FF2E4A{1} &#FFFFFF\u00BB &#FFFFFF{2}";

		private static String chatTag(Team team) {
			return team.getColor().toAmpersandHex() + (team.getOriginalTag().isEmpty() ? team.getName() : team.getOriginalTag());
		}

		public final String chatFormat;
		public final String chatSpyFormat;

		public final ChatMessageFormattingReplacementInterface formattingFunction;

		TeamMessageType(String chatFormat, String chatSpyFormat, ChatMessageFormattingReplacementInterface formattingFunction) {
			this.chatFormat = chatFormat;
			this.chatSpyFormat = chatSpyFormat;
			this.formattingFunction = formattingFunction;
		}
	}

	public interface ChatMessageFormattingReplacementInterface {
		Component getTeamMessagePrefix(String syntax, String prefix, Team team, Player sender);
	}

	private final Team team;

	public void sendTeamChatMessage(TeamPlayer sender, String message) {
		Set<TeamPlayer> recipients = team.getMembers().getClone();

		sendTeamMessage(sender, message, recipients, TeamMessageType.TEAM_CHAT_MESSAGE);
	}

	public void sendAllyChatMessage(TeamPlayer sender, String message) {
		Set<TeamPlayer> recipients = team.getMembers().getClone();
		team.getAllies().getClone().stream().map(Team::getTeam).filter(Objects::nonNull)
				.forEach(team -> recipients.addAll(team.getMembers().getClone()));

		sendTeamMessage(sender, message, recipients, TeamMessageType.ALLY_CHAT_MESSAGE);
	}

	private void sendTeamMessage(TeamPlayer sender, String message, Set<TeamPlayer> recipients, TeamMessageType messageType) {
		recipients.removeIf(teamPlayer -> !teamPlayer.getPlayer().isOnline()); // Offline players won't be recipients
		String format = getChatSyntax(sender, messageType);

		// Notify third party plugins that a team message is going to be sent
		TeamSendMessageEvent teamSendMessageEvent = new TeamSendMessageEvent(team, sender, message, format,
				sender.getPlayerPrefix() + getPreviousChatColor(format), recipients, messageType);
		Bukkit.getPluginManager().callEvent(teamSendMessageEvent);

		// Process any updates after the event has been dispatched
		if (teamSendMessageEvent.isCancelled()) {
			Main.plugin.getLogger().log(Level.FINE, "Team send message event is cancelled");
			return;
		}

		message = teamSendMessageEvent.getRawMessage();
		format = teamSendMessageEvent.getFormat();
		String prefix = teamSendMessageEvent.getSenderNamePrefix();
		recipients = teamSendMessageEvent.getRecipients();


		ChatMessage chatMsg = sendApprovedTeamMessage(sender, prefix, message, format, recipients, messageType);

		String fMessage = LegacyTextUtils.serialize(chatMsg.getMessage());
		// Notify third party plugins that a message has been dispatched
		Bukkit.getPluginManager().callEvent(new PostTeamSendMessageEvent(team, sender, fMessage, recipients, messageType));
	}

	/**
	 * Used to get the chat syntax and apply placeholders when possible
	 *
	 * @param sender - The team player who sent the command
	 */

	public String getChatSyntax(TeamPlayer sender, TeamMessageType messageType) {
		if (sender != null && sender.getPlayer() != null && sender.getPlayer().isOnline() && (sender.getPlayer().getPlayer() != null)) {
			return MessageManager.getMessage(sender.getPlayer().getPlayer(), messageType.chatFormat).replace("$name$", "{1}").replace("$message$", "{2}");
		}

		return MessageManager.getMessage(messageType.chatFormat).replace("$name$", "{1}").replace("$message$", "{2}");
	}

	private static @NotNull ChatColor getPreviousChatColor(String toTest) {
		Matcher matcher = Pattern.compile("\\{\\d+}").matcher(toTest);
		if (matcher.find()) {
			int value = matcher.start();
			if (value > 3) {
				for (int i = value; i >= 0; i--) {
					if (toTest.charAt(i) == ChatColor.COLOR_CHAR) {
						ChatColor returnTo = ChatColor.getByChar(toTest.charAt(i + 1));
						if (returnTo != null) {
							return returnTo;
						}
					}
				}
			}
		}

		return ChatColor.RESET;
	}

	private Collection<CommandSender> getOnlineChatSpyPlayers() {
		return Main.plugin.chatManagement.spy.stream()
				.filter(Objects::nonNull)
				.filter(temp -> !(temp instanceof Player && team.getTeamPlayer((Player) temp) != null))
				.collect(Collectors.toList());
	}

	private ChatMessage sendApprovedTeamMessage(TeamPlayer sender, String prefix, String message, String format, Collection<TeamPlayer> recipients, TeamMessageType messageType) {
		Collection<Player> playerRecipients = recipients.stream().map(r -> r.getPlayer().getPlayer())
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		ChatMessage chatMsg = ChatMessage.teamChat(team, sender, prefix, message, format, messageType);
		chatMsg.sendMessage(playerRecipients);
		chatMsg.sendSpyMessage(getOnlineChatSpyPlayers());

		if (Team.getTeamManager().isLogChat()) {
			MessageManager.sendFullMessage(Bukkit.getConsoleSender(), chatMsg.getMessage());
		}
		return chatMsg;
	}

}
