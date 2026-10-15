package teams.stats;

import teams.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Tracks the playtime of teams
 */
public class TeamStatsListener implements Listener {

	public TeamStatsListener() {
		// players that are already online (after a reload)
		for (Player player : Bukkit.getOnlinePlayers()) {
			TeamStats.startSession(player);
		}
		// save every 5 minutes, so playtime is not lost if the server crashes
		Bukkit.getScheduler().runTaskTimer(Main.plugin, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				TeamStats.saveSession(player, false);
			}
		}, 20L * 60 * 5, 20L * 60 * 5);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		TeamStats.startSession(event.getPlayer());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		TeamStats.saveSession(event.getPlayer(), true);
	}
}
