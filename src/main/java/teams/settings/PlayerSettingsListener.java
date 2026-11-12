package teams.settings;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import teams.Main;

/** Applies the personal settings when a player logs in */
public class PlayerSettingsListener implements Listener {

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Bukkit.getScheduler().runTaskLater(Main.plugin, () -> PlayerSettings.applyChatMode(event.getPlayer()), 20L);
	}
}
