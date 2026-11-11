package teams.display;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import teams.Main;
import teams.Team;
import teams.text.Formatter;

/**
 * The line under the player name WITHOUT the TAB plugin: [Niga] for team members, [NoTeam] for everyone else.
 * It is the vanilla "below name" scoreboard slot, where the number of each player is replaced by our own text.
 * Optional extras after the tag (all off by default): ping, HP and hearts.
 * Turn it off with belowname.builtin to use the TAB plugin instead.
 */
public final class BelowNameDisplay implements Listener {

	private static final String OBJECTIVE = "teams_below";

	private final Map<UUID, String> last = new HashMap<>();
	private Scoreboard board;
	private Objective objective;
	private BukkitTask task;

	public void enable() {
		board = Bukkit.getScoreboardManager().getMainScoreboard();
		Objective old = board.getObjective(OBJECTIVE);
		if (old != null) {
			old.unregister();
		}
		objective = board.registerNewObjective(OBJECTIVE, Criteria.DUMMY, Component.empty(), RenderType.INTEGER);
		objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
		Bukkit.getPluginManager().registerEvents(this, Main.plugin);
		task = Bukkit.getScheduler().runTaskTimer(Main.plugin, this::refreshAll, 10L, 10L);
	}

	public void disable() {
		HandlerList.unregisterAll(this);
		if (task != null) {
			task.cancel();
		}
		try {
			if (objective != null) {
				objective.unregister();
			}
		} catch (Exception ignored) {
			// scoreboard already gone
		}
		last.clear();
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		last.remove(event.getPlayer().getUniqueId());
		board.resetScores(event.getPlayer().getName());
	}

	private void refreshAll() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			refresh(player);
		}
	}

	private void refresh(Player player) {
		String text = build(player);
		if (text == null) {
			if (last.remove(player.getUniqueId()) != null) {
				board.resetScores(player.getName());
			}
			return;
		}
		if (text.equals(last.get(player.getUniqueId()))) {
			return;
		}
		last.put(player.getUniqueId(), text);
		Score score = objective.getScore(player.getName());
		score.setScore(0);
		score.numberFormat(NumberFormat.fixed(Formatter.absolute().process(text)));
	}

	/** The MiniMessage text for one player, null when nothing should be shown */
	private String build(Player player) {
		FileConfiguration c = Main.plugin.getConfig();
		Team team = Team.getTeam(player);
		String line;
		if (team == null) {
			if (!c.getBoolean("belowname.showNoTeam", true)) {
				return null;
			}
			line = c.getString("belowname.noTeam", "<white>[</white><gray>NoTeam</gray><white>]</white>");
		} else {
			boolean owner = team.hasCrown(team.getTeamPlayer(player));
			String tag = team.getColor().toAmpersandHex() + team.getHeadTag(owner);
			line = c.getString("belowname.format", "<white>[</white>{tag}<white>]</white>").replace("{tag}", tag);
		}
		String separator = c.getString("belowname.separator", " <gray>⬝</gray> ");
		if (c.getBoolean("belowname.ping", false)) {
			line += separator + c.getString("belowname.pingFormat", "<white>{ping}</white><#87CEEB>ms</#87CEEB>")
					.replace("{ping}", String.valueOf(player.getPing()));
		}
		if (c.getBoolean("belowname.hp", false)) {
			line += separator + c.getString("belowname.hpFormat", "<white>{hp}</white> <#FF5C5C>HP ♥</#FF5C5C>")
					.replace("{hp}", String.valueOf((int) Math.round(player.getHealth())));
		}
		if (c.getBoolean("belowname.hearts", false)) {
			double hearts = Math.round(player.getHealth()) / 2.0;
			String shown = hearts == Math.floor(hearts) ? String.valueOf((int) hearts) : String.valueOf(hearts);
			line += separator + c.getString("belowname.heartsFormat", "<white>{hearts}</white> <#FF5C5C>♥</#FF5C5C>")
					.replace("{hearts}", shown);
		}
		return line;
	}
}
