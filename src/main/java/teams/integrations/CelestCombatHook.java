package teams.integrations;

import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.plugin.Plugin;

import teams.Main;
import teams.Team;

/**
 * CelestCombat-Pro: players in the same team can not put each other in combat.
 * CelestCombat fires a cancellable PreCombatEvent before it tags someone, we cancel it for teammates.
 * Done with reflection, so Teams still loads when CelestCombat is not installed.
 */
public final class CelestCombatHook {

	private static Listener current;

	private CelestCombatHook() {
	}

	@SuppressWarnings("unchecked")
	public static void register() {
		if (current != null) {
			HandlerList.unregisterAll(current);
			current = null;
		}
		if (!Main.plugin.getConfig().getBoolean("combat.noTagTeammates", true)) {
			return;
		}
		Plugin celest = Bukkit.getPluginManager().getPlugin("CelestCombat-Pro");
		if (celest == null || !celest.isEnabled()) {
			return;
		}
		try {
			Class<? extends Event> eventClass = (Class<? extends Event>) Class.forName(
					"com.shyamstudio.celestCombatPro.api.events.PreCombatEvent", true, celest.getClass().getClassLoader());
			Method getAttacker = eventClass.getMethod("getAttacker");
			Method setCancelled = eventClass.getMethod("setCancelled", boolean.class);
			Listener listener = new Listener() {
			};
			Bukkit.getPluginManager().registerEvent(eventClass, listener, EventPriority.HIGH, (l, event) -> {
				if (!eventClass.isInstance(event)) {
					return;
				}
				try {
					Player attacker = (Player) getAttacker.invoke(event);
					Player victim = ((PlayerEvent) event).getPlayer();
					if (attacker == null || victim == null) {
						return;
					}
					Team team = Team.getTeam(attacker);
					if (team != null && team.equals(Team.getTeam(victim))) {
						setCancelled.invoke(event, true);
					}
				} catch (ReflectiveOperationException ignored) {
					// different CelestCombat version, leave combat as it is
				}
			}, Main.plugin);
			current = listener;
			Main.plugin.getLogger().info("CelestCombat-Pro found: teammates can not combat tag each other.");
		} catch (ReflectiveOperationException | LinkageError e) {
			Main.plugin.getLogger().warning("Could not hook into CelestCombat-Pro: " + e.getMessage());
		}
	}
}
