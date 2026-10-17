package teams;

import teams.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * A class to handle a teleport with a delay
 *
 * @author booksaw
 */
public class PlayerTeleport {

	final String reference;
	private final Player player;
	private final Location location;
	private final Location playerLoc;

	/**
	 * This will start the cooldown (there is no delay)
	 *
	 * @param player    The player to teleport
	 * @param location  the location to teleport them to
	 * @param reference the reference for the message that should be sent when the
	 *                  player is teleported
	 * @throws Exception Well, isn't this a generic one?
	 */
	public PlayerTeleport(Player player, Location location, String reference) throws Exception {
		if (location == null || location.getWorld() == null) {
			throw new Exception("Location or world is null");
		}

		this.player = player;
		this.location = location;
		this.reference = reference;

		this.playerLoc = player.getLocation();


		if (Main.plugin.getConfig().getBoolean("noTeleport")) {
			MessageManager.sendMessage(player, "teleport.noTeleport", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
			return;
		}

		if (player.hasPermission("teams.warmup.bypass")) {
			MessageManager.sendMessage(player, "teleport.bypass");
			Bukkit.getScheduler().runTask(Main.plugin, this::runTp);
			return;
		}

		int wait = Main.plugin.getConfig().getInt("tpDelay");
		if (wait <= 0) {
			Bukkit.getScheduler().runTask(Main.plugin, this::runTp);
			return;
		}

		// sending the wait message
		MessageManager.sendMessage(player, "teleport.wait", wait);

		// checks every few ticks, so moving cancels the teleport straight away instead of after the whole delay
		final long total = 20L * wait;
		final long[] passed = {0};
		Bukkit.getScheduler().runTaskTimer(Main.plugin, task -> {
			if (!player.isOnline()) {
				task.cancel();
				return;
			}
			if (!canTp()) {
				task.cancel();
				cancel();
				return;
			}
			passed[0] += 4;
			if (passed[0] >= total) {
				task.cancel();
				try {
					runTp();
				} catch (Exception e) {
					throw new NullPointerException();
				}
			}
		}, 4L, 4L);

	}

	public void runTp() {
		if (location == null || location.getWorld() == null) {
			Main.plugin.getLogger().warning("[Teams] Attempted to teleport to a null location or world.");
			return;
		}

		player.teleport(location);
		teams.util.Notify.sound(player, "teleport");
	}

	public boolean canTp() {

		if (!Main.plugin.getConfig().getBoolean("noMove")) {
			return true;
		}

		return playerLoc.getWorld() == player.getLocation().getWorld() && playerLoc.distance(player.getLocation()) <= Math.abs(Main.plugin.getConfig().getInt("maxMove"));
	}

	public void cancel() {
		MessageManager.sendMessage(player, "teleport.fail");
	}

}
