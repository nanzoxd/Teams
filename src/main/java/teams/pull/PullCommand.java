package teams.pull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import teams.Main;
import teams.Team;
import teams.TeamPlayer;
import teams.message.MessageManager;
import teams.text.Formatter;
import teams.util.Notify;

/**
 * /teampull: tells the team where you are and who you are fighting.
 * Team pull up [mrnanzo] X[10] Y[10] Z[10] ▪ Combat with [V1nom] (Wolves)
 */
public final class PullCommand extends Command {

	private static boolean registered;
	private static final Map<UUID, Long> LAST = new ConcurrentHashMap<>();

	private PullCommand() {
		super("teampull", "Send your position and fight to your team", "/teampull", List.of("pull"));
		setPermission("teams.standard");
	}

	public static void register() {
		if (registered) {
			return;
		}
		registered = true;
		Bukkit.getCommandMap().register("teams", new PullCommand());
	}

	private static String red(String text) {
		return "&#FF2E4A" + text;
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("Only players can use this.");
			return true;
		}
		Team team = Team.getTeam(player);
		if (team == null) {
			MessageManager.sendMessage(player, "noTeam");
			return true;
		}

		long cooldown = Main.plugin.getConfig().getLong("pull.cooldown", 5) * 1000L;
		long now = System.currentTimeMillis();
		Long last = LAST.get(player.getUniqueId());
		if (last != null && now - last < cooldown) {
			long left = (cooldown - (now - last) + 999) / 1000;
			MessageManager.sendMessage(player, "cooldown.wait", left);
			return true;
		}
		LAST.put(player.getUniqueId(), now);

		Location l = player.getLocation();
		String tag = team.getColor().toAmpersandHex() + (team.getOriginalTag().isEmpty() ? team.getName() : team.getOriginalTag());
		StringBuilder text = new StringBuilder();
		text.append("&#FFFFFF[").append(tag).append("&#FFFFFF] ")
				.append("&#FFE4D5Team pull up &#FFFFFF[").append(red(player.getName())).append("&#FFFFFF] ")
				.append("&#FFFBF0X[").append(l.getBlockX()).append("] Y[").append(l.getBlockY()).append("] Z[")
				.append(l.getBlockZ()).append("]");

		Player enemy = CombatTracker.opponent(player);
		if (enemy != null) {
			Team enemyTeam = Team.getTeam(enemy);
			text.append(" &#FFCFCA▪ &#FFE4D5Combat with &#FFFFFF[").append(red(enemy.getName())).append("&#FFFFFF]");
			if (enemyTeam != null) {
				text.append(" ").append(enemyTeam.getColor().toAmpersandHex()).append(enemyTeam.getName());
			}
		}

		Component message = Formatter.absolute().process(text.toString());
		message = message.hoverEvent(HoverEvent.showText(
				Formatter.absolute().process("&#FFE4D5World: &#FFFFFF" + l.getWorld().getName())));
		for (TeamPlayer tp : team.getMembers().getClone()) {
			Player member = tp.getPlayer().getPlayer();
			if (member != null && member.isOnline()) {
				MessageManager.sendFullMessage(member, message);
				if (!member.equals(player)) {
					Notify.sound(member, "pull");
				}
			}
		}
		return true;
	}
}
