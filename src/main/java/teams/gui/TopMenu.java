package teams.gui;

import teams.Main;
import teams.PlayerRank;
import teams.Team;
import teams.TeamPlayer;
import teams.stats.TeamStats;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Team top. Pick what the teams are ranked by with the buttons on the top row:
 * score, kills, deaths, K/D ratio or playtime.
 */
public class TopMenu extends Menu {

	/**
	 * The things teams can be ranked by
	 */
	public enum TopType {
		SCORE("Score", Material.NETHER_STAR),
		KILLS("Kills", Material.DIAMOND_SWORD),
		DEATHS("Deaths", Material.SKELETON_SKULL),
		KDR("K/D ratio", Material.TARGET),
		PLAYTIME("Playtime", Material.CLOCK);

		private final String label;
		private final Material icon;

		TopType(String label, Material icon) {
			this.label = label;
			this.icon = icon;
		}

		/**
		 * @return the number the teams are sorted by
		 */
		double value(Team team) {
			switch (this) {
				case KILLS:
					return TeamStats.getKills(team);
				case DEATHS:
					return TeamStats.getDeaths(team);
				case KDR:
					return TeamStats.getKdr(team);
				case PLAYTIME:
					return TeamStats.getPlaytimeSeconds(team);
				default:
					return team.getScore();
			}
		}

		String display(Team team) {
			switch (this) {
				case KILLS:
					return String.valueOf(TeamStats.getKills(team));
				case DEATHS:
					return String.valueOf(TeamStats.getDeaths(team));
				case KDR:
					return TeamStats.formatKdr(TeamStats.getKdr(team));
				case PLAYTIME:
					return TeamStats.formatDuration(TeamStats.getPlaytimeSeconds(team));
				default:
					return String.valueOf(team.getScore());
			}
		}
	}

	private static final int FIRST_SLOT = 9;
	private static final int MAX_TEAMS = 36;

	private final Menu previous;
	private final TopType type;

	public TopMenu(Player player, Menu previous) {
		this(player, previous, TopType.SCORE);
	}

	public TopMenu(Player player, Menu previous, TopType type) {
		super(player);
		this.previous = previous;
		this.type = type;
	}

	@Override
	protected String title() {
		return Theme.main() + "Team top - " + type.label;
	}

	/**
	 * @return every team, best first
	 */
	private List<Team> ranking() {
		List<Team> teams = new ArrayList<>();
		for (String name : Team.getTeamManager().sortTeamsByScore()) {
			if (name == null) {
				continue;
			}
			Team team = Team.getTeam(name);
			if (team != null) {
				teams.add(team);
			}
		}
		// sorting is stable, so teams with the same value stay in score order
		teams.sort(Comparator.comparingDouble((Team t) -> type.value(t)).reversed());
		return teams;
	}

	@Override
	protected void build() {
		// ---- category buttons
		int slot = 2;
		for (TopType option : TopType.values()) {
			boolean selected = option == type;
			set(slot++, new ItemBuilder(option.icon)
							.name((selected ? Theme.main() : Theme.light()) + option.label)
							.lore(selected ? Theme.good() + "Showing now" : Theme.soft() + "Click to rank by this")
							.glow(selected).build(),
					e -> {
						if (!selected) {
							new TopMenu(player, previous, option).open();
						}
					});
		}

		// ---- teams
		List<Team> teams = ranking();
		int max = Math.min(MAX_TEAMS, Main.plugin.getConfig().getInt("gui.topSize", 10));
		Team own = Team.getTeam(player);

		for (int i = 0; i < teams.size() && i < max; i++) {
			Team team = teams.get(i);
			// the top 3 show the head of the team owner, the rest the colour of the team
			ItemBuilder icon = new ItemBuilder(team.getColor().getIcon());
			if (i < 3) {
				List<TeamPlayer> owners = team.getRank(PlayerRank.OWNER);
				if (!owners.isEmpty()) {
					icon = new ItemBuilder(Material.PLAYER_HEAD).head(owners.get(0).getPlayer());
				}
			}
			set(FIRST_SLOT + i, icon
							.name(Theme.soft() + "#" + (i + 1) + " " + team.getColor().toLegacy() + team.getName())
							.amount(i + 1)
							.lore(statLine(TopType.SCORE, team), statLine(TopType.KILLS, team),
									statLine(TopType.DEATHS, team), statLine(TopType.KDR, team),
									statLine(TopType.PLAYTIME, team),
									Theme.soft() + "Members: " + Theme.light() + team.getMembers().size())
							.click("Click", "Full info")
							.glow(own == team).build(),
					e -> runAndClose("team info " + team.getName()));
		}

		if (teams.isEmpty()) {
			set(22, new ItemBuilder(Material.BARRIER).name(Theme.light() + "No teams yet").build());
		}

		// ---- your team
		if (own != null) {
			int position = teams.indexOf(own) + 1;
			set(49, new ItemBuilder(own.getColor().getIcon()).name(Theme.main() + "Your team")
					.lore(Theme.soft() + "Position by " + type.label.toLowerCase() + ": " + Theme.light() + "#" + position,
							statLine(TopType.SCORE, own), statLine(TopType.KILLS, own),
							statLine(TopType.DEATHS, own), statLine(TopType.KDR, own),
							statLine(TopType.PLAYTIME, own)).build());
		}

		if (previous != null) {
			setBack(45, () -> previous);
		}
		setClose(53);
	}

	/**
	 * A line of the lore, the one the menu is sorted by is highlighted
	 */
	private String statLine(TopType line, Team team) {
		String label = (line == type ? Theme.main() : Theme.soft()) + line.label + ": ";
		return label + Theme.light() + line.display(team);
	}
}
