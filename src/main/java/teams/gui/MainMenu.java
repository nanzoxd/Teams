package teams.gui;

import teams.PlayerRank;
import teams.Team;
import teams.TeamPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

/**
 * What /team opens. Shows every feature of the plugin.
 */
public class MainMenu extends Menu {

	public MainMenu(Player player) {
		super(player);
	}

	@Override
	protected String title() {
		return Theme.main() + "Teams";
	}

	@Override
	protected void build() {
		Team team = Team.getTeam(player);
		if (team == null) {
			buildNoTeam();
		} else {
			buildTeam(team);
		}
		setClose(49);
	}

	private void buildNoTeam() {
		set(4, new ItemBuilder(Material.WHITE_BANNER).name(Theme.main() + "You are not in a team")
				.lore(Theme.light() + "Create a team, or join one that is open.").build());

		set(20, new ItemBuilder(Material.EMERALD).name(Theme.good() + "Create a team")
				.lore(Theme.light() + "Pick a name, you get a random colour.")
				.click("Click", "Create").build(),
				e -> GuiManager.getInstance().prompt(player, "the name of your new team",
						name -> runAndClose("team create " + name),
						() -> new MainMenu(player).open()));

		set(22, new ItemBuilder(Material.NETHER_STAR).name(Theme.info() + "Team top")
				.lore(Theme.light() + "The best teams by score.")
				.click("Click", "Open").build(),
				e -> new TopMenu(player, new MainMenu(player)).open());

		set(24, new ItemBuilder(Material.COMPASS).name(Theme.info() + "Browse teams")
				.lore(Theme.light() + "See every team and join an open one.")
				.click("Click", "Open").build(),
				e -> new TeamListMenu(player).open());

		set(31, new ItemBuilder(Material.BOOK).name(Theme.info() + "Commands")
				.lore(Theme.light() + "Show the list of commands.")
				.click("Click", "Show").build(),
				e -> runAndClose("team help"));
	}

	private void buildTeam(Team team) {
		TeamPlayer teamPlayer = team.getTeamPlayer(player);
		PlayerRank rank = teamPlayer == null ? PlayerRank.DEFAULT : teamPlayer.getRank();
		boolean staff = rank == PlayerRank.OWNER || rank == PlayerRank.ADMIN;

		String color = team.getColor().toLegacy();

		// ---- team info
		set(4, new ItemBuilder(team.getColor().getIcon()).name(color + team.getName())
						.lore(Theme.soft() + "Tag: " + color + (team.getOriginalTag().isEmpty() ? "-" : "[" + team.getOriginalTag() + "]"),
								Theme.soft() + "Colour: " + color + team.getColor().getDisplayName(),
								Theme.soft() + "Your rank: " + Theme.light() + rank.name().toLowerCase(),
								Theme.soft() + "Members: " + Theme.light() + team.getMembers().size() + "/" + team.getTeamLimit(),
								Theme.soft() + "Score: " + Theme.light() + team.getScore(),
								Theme.soft() + "Level: " + Theme.light() + team.getLevel(),
								Theme.soft() + "Open: " + yesNo(team.isOpen()) + Theme.soft() + "  PvP: " + yesNo(team.isPvp()))
						.click("Click", "Full info").build(),
				e -> runAndClose("team info"));

		// ---- row 1
		set(19, new ItemBuilder(Material.PLAYER_HEAD).head(player).name(Theme.info() + "Members")
						.lore(Theme.light() + "See and manage your members.").click("Click", "Open").build(),
				e -> new MembersMenu(player, team).open());

		set(20, new ItemBuilder(Material.NETHER_STAR).name(Theme.info() + "Team top")
						.lore(Theme.light() + "The best teams by score.").click("Click", "Open").build(),
				e -> new TopMenu(player, new MainMenu(player)).open());

		set(21, new ItemBuilder(team.getColor().getIcon()).name(Theme.warn() + "Team colour")
						.lore(Theme.light() + "Now: " + color + team.getColor().getDisplayName(),
								Theme.light() + "Change it with /team color <colour>",
								staff ? "" : Theme.bad() + "Only the owner can change it.")
						.click("Click", "Show the colours").build(),
				e -> {
					player.closeInventory();
					ColorHelp.send(player);
				});

		set(22, new ItemBuilder(Material.COMPARATOR).name(Theme.info() + "Settings")
						.lore(Theme.light() + "Name, tag, description, open, PvP, home.",
								staff ? "" : Theme.bad() + "Only admins and the owner can change these.")
						.click("Click", "Open").build(),
				e -> new SettingsMenu(player, team).open());

		set(23, new ItemBuilder(Material.ENDER_PEARL).name(Theme.info() + "Warps")
						.lore(Theme.light() + team.getWarps().size() + "/" + team.getMaxWarps() + " warps")
						.click("Click", "Open").build(),
				e -> new WarpsMenu(player, team).open());

		set(24, new ItemBuilder(Material.SHIELD).name(Theme.info() + "Allies")
						.lore(Theme.light() + team.getAllies().size() + " allies").click("Click", "Open").build(),
				e -> new AlliesMenu(player, team).open());

		set(25, new ItemBuilder(Material.COMPASS).name(Theme.info() + "Browse teams")
						.lore(Theme.light() + "See every team.").click("Click", "Open").build(),
				e -> new TeamListMenu(player).open());

		// ---- row 2
		set(28, new ItemBuilder(Material.RED_BED).name(Theme.info() + "Team home")
						.click("Left click", "Teleport home")
						.click("Right click", "Set the home here (admins)").build(),
				e -> {
					if (e.getClick() == ClickType.RIGHT) {
						runAndClose("team sethome");
					} else {
						runAndClose("team home");
					}
				});

		set(29, new ItemBuilder(Material.WRITABLE_BOOK).name(Theme.warn() + "Team chat")
						.lore(Theme.light() + "Talk only to your team.")
						.click("Click", "Turn on / off").build(),
				e -> runAndClose("team chat"));

		set(30, new ItemBuilder(Material.BOOK).name(Theme.warn() + "Ally chat")
						.lore(Theme.light() + "Talk to your team and allies.")
						.click("Click", "Turn on / off").build(),
				e -> runAndClose("team allychat"));

		set(31, new ItemBuilder(Material.ENDER_CHEST).name(Theme.info() + "Team ender chest")
						.click("Click", "Open").build(),
				e -> runAndClose("team echest"));

		set(32, new ItemBuilder(Material.EXPERIENCE_BOTTLE).name(Theme.warn() + "Level")
						.lore(Theme.light() + "Level " + team.getLevel() + ", score " + team.getScore())
						.click("Left click", "See the perks")
						.click("Right click", "Level up (costs score)").build(),
				e -> {
					if (e.getClick() == ClickType.RIGHT) {
						runAndClose("team rankup");
					} else {
						runAndClose("team rank");
					}
				});

		set(33, new ItemBuilder(Material.PAPER).name(Theme.good() + "Invite a player")
						.lore(Theme.light() + "Type the player's name.").click("Click", "Invite").build(),
				e -> GuiManager.getInstance().prompt(player, "the name of the player to invite",
						name -> runAndClose("team invite " + name),
						() -> new MainMenu(player).open()));

		set(34, new ItemBuilder(Material.OAK_SIGN).name(Theme.warn() + "Title")
						.lore(Theme.light() + "Set the title shown next to your name.").click("Click", "Set").build(),
				e -> GuiManager.getInstance().prompt(player, "your new title",
						title -> runAndClose("team title " + title),
						() -> new MainMenu(player).open()));

		// ---- leave
		set(40, new ItemBuilder(Material.RED_CONCRETE).name(Theme.bad() + "Leave team")
						.lore(Theme.light() + "You will have to be invited or join again.").click("Click", "Leave").build(),
				e -> new ConfirmMenu(player, "Leave " + team.getName() + "?", "team leave",
						() -> new MainMenu(player)).open());
	}

	private static String yesNo(boolean value) {
		return value ? Theme.good() + "yes" : Theme.bad() + "no";
	}
}
