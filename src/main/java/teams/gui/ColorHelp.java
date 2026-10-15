package teams.gui;

import teams.TeamColor;
import teams.message.MessageManager;
import teams.util.TeamColorUtil;
import org.bukkit.entity.Player;

/**
 * Colours are picked with a command, not a menu. This prints the colours in chat.
 */
public final class ColorHelp {

	private ColorHelp() {
	}

	public static void send(Player player) {
		StringBuilder list = new StringBuilder();
		for (TeamColor color : TeamColor.values()) {
			if (TeamColorUtil.isBanned(color)) {
				continue;
			}
			if (list.length() > 0) {
				list.append(Theme.soft()).append(", ");
			}
			list.append(color.toLegacy()).append(color.getId());
		}
		MessageManager.sendFullMessage(player, Theme.light() + "Change your team colour with " + Theme.main()
				+ "/team color <colour>" + Theme.light() + ". Colours: " + list, false);
	}
}
