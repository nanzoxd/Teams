package teams.util;

import teams.Main;
import teams.TeamColor;

import java.util.List;

/**
 * Helpers for the colours a team is allowed to pick (see colors.banned in config.yml)
 */
public final class TeamColorUtil {

	private TeamColorUtil() {
	}

	public static List<String> getBanned() {
		return Main.plugin.getConfig().getStringList("colors.banned");
	}

	public static boolean isBanned(TeamColor color) {
		return getBanned().stream().anyMatch(b -> b.equalsIgnoreCase(color.getId()));
	}

	/**
	 * @return the ids of all colours a team can pick
	 */
	public static List<String> getAllowedIds() {
		return TeamColor.getIds(getBanned());
	}
}
