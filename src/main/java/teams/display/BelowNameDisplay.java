package teams.display;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

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
 * Optional extras after the tag (all off by default): ping and hearts.
 * Turn it off with belowname.builtin to use the TAB plugin instead.
 */
public final class BelowNameDisplay implements Listener {

	private static final String OBJECTIVE = "teams_below";

	// defaults, used when config.yml has no value (or no valid hex colour)
	private static final String DEFAULT_HEARTS_SHADOW = "#FF2954";
	private static final String DEFAULT_PING_SHADOW = "#555555";
	private static final String DEFAULT_PING = "<shadow:{pingShadowColor}><white>{ping}</white><#87CEEB>ms</#87CEEB></shadow>";
	private static final String DEFAULT_HEARTS = "<shadow:{shadowColor}><white>{hearts}</white> <#FF2954>\u2665</#FF2954></shadow>";
	private static final Pattern HEX = Pattern.compile("#[0-9A-Fa-f]{6}");

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

	/** A #RRGGBB colour from the config, or the default when it is missing or not a valid hex colour */
	private static String hex(String value, String def) {
		return value != null && HEX.matcher(value.trim()).matches() ? value.trim() : def;
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
			line += separator + c.getString("belowname.pingFormat", DEFAULT_PING)
					.replace("{pingShadowColor}", hex(c.getString("belowname.pingShadowColor"), DEFAULT_PING_SHADOW))
					.replace("{ping}", String.valueOf(player.getPing()));
		}
		if (c.getBoolean("belowname.hearts", false)) {
			double hearts = Math.round(player.getHealth()) / 2.0;
			String shown = hearts == Math.floor(hearts) ? String.valueOf((int) hearts) : String.valueOf(hearts);
			line += separator + c.getString("belowname.heartsFormat", DEFAULT_HEARTS)
					.replace("{shadowColor}", hex(c.getString("belowname.shadowColor"), DEFAULT_HEARTS_SHADOW))
					.replace("{hearts}", shown);
		}
		return line;
	}
}
