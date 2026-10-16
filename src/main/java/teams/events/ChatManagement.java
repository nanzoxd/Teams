package teams.events;

import teams.Main;
import teams.PlayerRank;
import teams.Team;
import teams.TeamPlayer;
import teams.message.MessageManager;
import teams.text.LegacyTextUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

public class ChatManagement implements Listener {

	private static PrefixType doPrefix;
	public final List<CommandSender> spy = new ArrayList<>();

	public static void enable() {
		if (!Main.plugin.getConfig().getBoolean("display.chat.enabled", true)) {
			doPrefix = PrefixType.NONE;
		} else {
			doPrefix = PrefixType.getType(Main.plugin.getConfig().getString("display.chat.show", "tag"));
		}
	}

	/**
	 * This detects when the player speaks and either adds a prefix or puts the
	 * message in the team chat
	 *
	 * @param event the chat event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onChat(AsyncPlayerChatEvent event) {

		if (event.isCancelled()) {
			return;
		}

		Player p = event.getPlayer();
		Team team = Team.getTeam(p);

		if (team == null) {
			return;
		}

		TeamPlayer teamPlayer = team.getTeamPlayer(p);

		if (teamPlayer == null) {
			throw new IllegalStateException("Player " + p.getName() + " is registered to be in a team, yet has no playerdata associated with that team");
		}

		String anyChatToGlobalPrefix = Main.plugin.getConfig().getString("chat.prefixes.teamOrAllyToGlobal", "!");
		String globalToTeamPrefix = Main.plugin.getConfig().getString("chat.prefixes.globalToTeam", "!");
		String globalToAllyPrefix = Main.plugin.getConfig().getString("chat.prefixes.globalToAlly", "?");

		if (teamPlayer.isInTeamChat() || teamPlayer.isInAllyChat()) {
			if (!anyChatToGlobalPrefix.isEmpty() && event.getMessage().startsWith(anyChatToGlobalPrefix) && event.getMessage().length() > anyChatToGlobalPrefix.length()) {
				event.setMessage(event.getMessage().substring(anyChatToGlobalPrefix.length()));
			} else {
				// Player is not sending to global chat
				event.setCancelled(true);

				if (teamPlayer.isInTeamChat()) {
					team.getTeamMessageController().sendTeamChatMessage(teamPlayer, event.getMessage());
				} else {
					team.getTeamMessageController().sendAllyChatMessage(teamPlayer, event.getMessage());
				}
				// Used as some chat plugins do not accept when a message is cancelled
				event.setMessage("");
				event.setFormat("");
				return;
			}
		} else if (
				(!globalToTeamPrefix.isEmpty() && event.getMessage().startsWith(globalToTeamPrefix) && event.getMessage().length() > globalToTeamPrefix.length())
						|| (!globalToAllyPrefix.isEmpty() && event.getMessage().startsWith(globalToAllyPrefix) && event.getMessage().length() > globalToAllyPrefix.length())
		) {
			// Player is not sending to global chat
			event.setCancelled(true);

			if (event.getMessage().startsWith(globalToTeamPrefix)) {
				team.getTeamMessageController().sendTeamChatMessage(teamPlayer, event.getMessage().substring(globalToTeamPrefix.length()));
			} else {
				team.getTeamMessageController().sendAllyChatMessage(teamPlayer, event.getMessage().substring(globalToAllyPrefix.length()));
			}
			// Used as some chat plugins do not accept when a message is cancelled
			event.setMessage("");
			event.setFormat("");
			return;
		}

		if (doPrefix != PrefixType.NONE) {
			event.setFormat(LegacyTextUtils.parseAllAdventure(doPrefix.getUpdatedFormat(p, event.getFormat(), team)));
			// event.setFormat(ChatColor.AQUA + "[" + team.getName() + "] " + ChatColor.WHITE + event.getFormat());
		}

	}

	@EventHandler
	public void spyQuit(PlayerQuitEvent e) {
		spy.remove(e.getPlayer());
	}

	enum PrefixType {
		NONE, NAME, TAG;

		public static PrefixType getType(String str) {
			str = str.toLowerCase().trim();
			switch (str) {
				case "name":
				case "true":
					return NAME;
				case "tag":
					return TAG;
				default:
					return NONE;
			}
		}

		public String getUpdatedFormat(Player p, String format, Team team) {
			TeamPlayer teamPlayer = team.getTeamPlayer(p);
			String crown = teamPlayer != null && teamPlayer.getRank() == PlayerRank.OWNER ? Team.getOwnerCrown() : "";
			// The colour is written as &#RRGGBB like every other message (mixing section signs with MiniMessage tags
			// made the tag come out in the wrong colour), the brackets come from prefixSyntax (white)
			String color = team.getColor().toAmpersandHex();
			switch (this) {
				case NAME:
					return MessageManager.getMessage(p, "prefixSyntax", color + crown + team.getName(), format);
				case TAG:
					String tag = team.getOriginalTag().isEmpty() ? team.getName() : team.getOriginalTag();
					return MessageManager.getMessage(p, "prefixSyntax", color + crown + tag, format);
				default:
					return format;
			}
		}

	}

}
