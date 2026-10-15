package teams.stats;

import teams.Team;
import teams.TeamPlayer;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The statistics of a team that are shown on team top: kills, deaths, K/D ratio and playtime.
 * <p>
 * They are stored in the team meta (stat_kills, stat_deaths, stat_playtime), so they are saved by every storage type.
 */
public final class TeamStats {

	public static final String KILLS = "stat_kills";
	public static final String DEATHS = "stat_deaths";
	public static final String PLAYTIME = "stat_playtime";

	/**
	 * When each online player's current (not yet saved) playtime started, in milliseconds
	 */
	private static final Map<UUID, Long> sessionStart = new ConcurrentHashMap<>();

	private TeamStats() {
	}

	// ---------------------------------------------------------------- kills and deaths

	public static long getKills(Team team) {
		return getLong(team, KILLS);
	}

	public static long getDeaths(Team team) {
		return getLong(team, DEATHS);
	}

	public static void addKill(Team team) {
		team.setAndSaveMeta(KILLS, String.valueOf(getKills(team) + 1));
	}

	public static void addDeath(Team team) {
		team.setAndSaveMeta(DEATHS, String.valueOf(getDeaths(team) + 1));
	}

	/**
	 * @return kills divided by deaths. With no deaths it is the number of kills.
	 */
	public static double getKdr(Team team) {
		long kills = getKills(team);
		long deaths = getDeaths(team);
		return deaths == 0 ? kills : (double) kills / deaths;
	}

	public static String formatKdr(double kdr) {
		return String.format(Locale.ROOT, "%.2f", kdr);
	}

	// ---------------------------------------------------------------- playtime

	/**
	 * @return the playtime of the team in seconds (saved time plus the sessions that are running now)
	 */
	public static long getPlaytimeSeconds(Team team) {
		long total = getLong(team, PLAYTIME);
		long now = System.currentTimeMillis();
		for (TeamPlayer member : team.getMembers().get()) {
			Long start = sessionStart.get(member.getPlayerUUID());
			if (start != null) {
				total += Math.max(0, (now - start) / 1000);
			}
		}
		return total;
	}

	/**
	 * Called when a player joins the server
	 */
	public static void startSession(Player player) {
		sessionStart.put(player.getUniqueId(), System.currentTimeMillis());
	}

	/**
	 * Adds the time since the last save to the player's team. Called on quit and every few minutes.
	 *
	 * @param remove true if the player is leaving the server
	 */
	public static void saveSession(Player player, boolean remove) {
		Long start = remove ? sessionStart.remove(player.getUniqueId()) : sessionStart.get(player.getUniqueId());
		if (start == null) {
			return;
		}
		long now = System.currentTimeMillis();
		if (!remove) {
			sessionStart.put(player.getUniqueId(), now);
		}
		long seconds = (now - start) / 1000;
		if (seconds <= 0) {
			return;
		}
		Team team = Team.getTeam(player);
		if (team != null) {
			team.setAndSaveMeta(PLAYTIME, String.valueOf(getLong(team, PLAYTIME) + seconds));
		}
	}

	/**
	 * @return i.e. "3d 4h", "2h 15m" or "45m"
	 */
	public static String formatDuration(long seconds) {
		long days = seconds / 86400;
		long hours = (seconds % 86400) / 3600;
		long minutes = (seconds % 3600) / 60;
		if (days > 0) {
			return days + "d " + hours + "h";
		}
		if (hours > 0) {
			return hours + "h " + minutes + "m";
		}
		return minutes + "m";
	}

	private static long getLong(Team team, String key) {
		try {
			return Long.parseLong(team.getMeta().get().get(key).orElse("0"));
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
