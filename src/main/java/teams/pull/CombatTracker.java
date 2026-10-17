package teams.pull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import teams.Main;

/** Remembers who a player fought last, so /teampull can say who the team is fighting. */
public class CombatTracker implements Listener {

	private record Fight(UUID opponent, long time) {
	}

	private static final Map<UUID, Fight> FIGHTS = new ConcurrentHashMap<>();

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onDamage(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player victim)) {
			return;
		}
		Player damager = null;
		if (e.getDamager() instanceof Player p) {
			damager = p;
		} else if (e.getDamager() instanceof Projectile proj && proj.getShooter() instanceof Player p) {
			damager = p;
		}
		if (damager == null || damager.equals(victim)) {
			return;
		}
		teams.Team mine = teams.Team.getTeam(damager);
		if (mine != null && mine.equals(teams.Team.getTeam(victim))) {
			return;
		}
		long now = System.currentTimeMillis();
		FIGHTS.put(victim.getUniqueId(), new Fight(damager.getUniqueId(), now));
		FIGHTS.put(damager.getUniqueId(), new Fight(victim.getUniqueId(), now));
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		FIGHTS.remove(e.getPlayer().getUniqueId());
	}

	/** @return the player this one fought in the last few seconds (pull.combatSeconds), or null */
	public static Player opponent(Player player) {
		Fight fight = FIGHTS.get(player.getUniqueId());
		if (fight == null) {
			return null;
		}
		long window = Main.plugin.getConfig().getLong("pull.combatSeconds", 15) * 1000L;
		if (System.currentTimeMillis() - fight.time() > window) {
			return null;
		}
		return Bukkit.getPlayer(fight.opponent());
	}
}
