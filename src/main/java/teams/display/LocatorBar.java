package teams.display;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;

import teams.Main;
import teams.Team;
import teams.settings.PlayerSettings;

/**
 * Team locator bar (Minecraft 1.21.6+): players without a team do not show on the locator bar, and players can
 * hide themselves in their own settings. The vanilla bar works by distance, so it cannot be limited to teammates only.
 * <p>
 * A transient attribute modifier on waypoint_transmit_range is used (the base value is never touched), so other
 * plugins that change the locator bar keep working and nothing stays behind when the plugin stops.
 */
public final class LocatorBar implements Listener {

	private final NamespacedKey key = new NamespacedKey(Main.plugin, "locator_hidden");
	private BukkitTask task;

	public void enable() {
		if (attribute() == null) {
			Main.plugin.getLogger().warning("locator.enabled is on, but this server has no locator bar (needs Minecraft 1.21.6 or newer).");
			return;
		}
		Bukkit.getPluginManager().registerEvents(this, Main.plugin);
		task = Bukkit.getScheduler().runTaskTimer(Main.plugin, this::refreshAll, 20L, 20L);
	}

	public void disable() {
		HandlerList.unregisterAll(this);
		if (task != null) {
			task.cancel();
			task = null;
		}
		for (Player player : Bukkit.getOnlinePlayers()) {
			apply(player, false);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Bukkit.getScheduler().runTaskLater(Main.plugin, () -> refresh(event.getPlayer()), 5L);
	}

	private static Attribute attribute() {
		try {
			return Attribute.WAYPOINT_TRANSMIT_RANGE;
		} catch (Throwable e) {
			return null;
		}
	}

	private void refreshAll() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			refresh(player);
		}
	}

	private void refresh(Player player) {
		boolean hidden = Team.getTeam(player) == null || !PlayerSettings.get(player.getUniqueId(), "locator", true);
		apply(player, hidden);
	}

	private void apply(Player player, boolean hidden) {
		try {
			AttributeInstance instance = player.getAttribute(attribute());
			if (instance == null) {
				return;
			}
			AttributeModifier existing = null;
			for (AttributeModifier modifier : instance.getModifiers()) {
				if (key.equals(modifier.getKey())) {
					existing = modifier;
				}
			}
			if (hidden && existing == null) {
				// x (1 - 1) = 0 blocks: nobody sees this player on the bar
				instance.addTransientModifier(new AttributeModifier(key, -1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1));
			} else if (!hidden && existing != null) {
				instance.removeModifier(existing);
			}
		} catch (Throwable ignored) {
			// not supported on this server
		}
	}
}
