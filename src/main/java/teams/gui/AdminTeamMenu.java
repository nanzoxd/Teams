package teams.gui;

import teams.Team;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Manage one team. The team is looked up by name each time, so the menu always shows fresh values
 * (and closes itself back to the list if the team was disbanded or renamed).
 */
public class AdminTeamMenu extends Menu {

	private final String teamName;

	public AdminTeamMenu(Player player, String teamName) {
		super(player);
		this.teamName = teamName;
	}

	@Override
	protected String title() {
		return Theme.main() + "Admin: " + teamName;
	}

	@Override
	protected void build() {
		Team team = Team.getTeam(teamName);
		if (team == null) {
			set(22, new ItemBuilder(Material.BARRIER).name(Theme.bad() + "This team no longer exists").build());
			setBack(45, () -> new AdminTeamsMenu(player));
			setClose(53);
			return;
		}
		String color = team.getColor().toLegacy();

		set(4, new ItemBuilder(team.getColor().getIcon()).name(color + team.getName())
						.lore(Theme.soft() + "Tag: " + color + (team.getOriginalTag().isEmpty() ? "-" : "[" + team.getOriginalTag() + "]"),
								Theme.soft() + "Colour: " + color + team.getColor().getDisplayName(),
								Theme.soft() + "Members: " + Theme.light() + team.getMembers().size() + "/" + team.getTeamLimit(),
								Theme.soft() + "Score: " + Theme.light() + team.getScore(),
								Theme.soft() + "Level: " + Theme.light() + team.getLevel(),
								Theme.soft() + "Open: " + (team.isOpen() ? Theme.good() + "yes" : Theme.bad() + "no"))
						.click("Click", "Full info").build(),
				e -> runAndClose("team info " + teamName));

		// ---- row 1: members
		set(19, new ItemBuilder(Material.PLAYER_HEAD).name(Theme.info() + "Members")
						.lore(Theme.light() + "Promote, demote, remove, add.")
						.click("Click", "Open").build(),
				e -> new AdminMembersMenu(player, teamName).open());

		set(20, new ItemBuilder(Material.OAK_SIGN).name(Theme.warn() + "Rename")
						.lore(Theme.light() + "Now: " + color + team.getName()).click("Click", "Change").build(),
				e -> GuiManager.getInstance().prompt(player, "the new team name",
						name -> later(() -> {
							player.performCommand("teamadmin name " + teamName + " " + name);
							later(() -> new AdminTeamMenu(player, Team.getTeam(name) != null ? name : teamName).open());
						}),
						() -> new AdminTeamMenu(player, teamName).open()));

		set(21, new ItemBuilder(Material.NAME_TAG).name(Theme.warn() + "Tag")
						.lore(Theme.light() + "Now: " + color + (team.getOriginalTag().isEmpty() ? "-" : team.getOriginalTag()))
						.click("Click", "Change").build(),
				e -> GuiManager.getInstance().prompt(player, "the new tag",
						tag -> runAndReopen("teamadmin tag " + teamName + " " + tag, () -> new AdminTeamMenu(player, teamName)),
						() -> new AdminTeamMenu(player, teamName).open()));

		set(22, new ItemBuilder(Material.WRITABLE_BOOK).name(Theme.warn() + "Description")
						.lore(Theme.light() + (team.getDescription() == null || team.getDescription().isEmpty() ? "none" : team.getDescription()))
						.click("Click", "Change").build(),
				e -> GuiManager.getInstance().prompt(player, "the new description",
						text -> runAndReopen("teamadmin description " + teamName + " " + text, () -> new AdminTeamMenu(player, teamName)),
						() -> new AdminTeamMenu(player, teamName).open()));

		set(23, new ItemBuilder(team.getColor().getIcon()).name(Theme.warn() + "Colour")
						.lore(Theme.light() + "Now: " + color + team.getColor().getDisplayName()).click("Click", "Pick").build(),
				e -> new AdminColorMenu(player, teamName).open());

		set(24, new ItemBuilder(team.isOpen() ? Material.LIME_DYE : Material.GRAY_DYE).name(Theme.warn() + "Open to everyone")
						.lore(Theme.soft() + "Now: " + (team.isOpen() ? Theme.good() + "open" : Theme.bad() + "closed"))
						.click("Click", "Toggle").build(),
				e -> runAndReopen("teamadmin open " + teamName, () -> new AdminTeamMenu(player, teamName)));

		// ---- row 2: score
		set(28, new ItemBuilder(Material.LIME_CONCRETE).name(Theme.good() + "+10 score").click("Click", "Add").build(),
				e -> runAndReopen("teamadmin score add " + teamName + " 10", () -> new AdminTeamMenu(player, teamName)));
		set(29, new ItemBuilder(Material.RED_CONCRETE).name(Theme.bad() + "-10 score").click("Click", "Remove").build(),
				e -> runAndReopen("teamadmin score remove " + teamName + " 10", () -> new AdminTeamMenu(player, teamName)));
		set(30, new ItemBuilder(Material.GOLD_INGOT).name(Theme.warn() + "Set score")
						.lore(Theme.light() + "Now: " + team.getScore()).click("Click", "Type a number").build(),
				e -> GuiManager.getInstance().prompt(player, "the new score",
						n -> runAndReopen("teamadmin score set " + teamName + " " + n, () -> new AdminTeamMenu(player, teamName)),
						() -> new AdminTeamMenu(player, teamName).open()));

		// ---- row 2: tools
		set(32, new ItemBuilder(Material.ENDER_CHEST).name(Theme.info() + "Ender chest")
						.lore(Theme.light() + "View and edit it.").click("Click", "Open").build(),
				e -> runAndClose("teamadmin echest " + teamName));
		set(33, new ItemBuilder(Material.ENDER_PEARL).name(Theme.accent() + "Teleport to home")
						.click("Click", "Teleport").build(),
				e -> runAndClose("teamadmin home " + teamName));
		set(34, new ItemBuilder(Material.WHITE_BANNER).name(Theme.warn() + "Make allies / neutral")
						.lore(Theme.light() + "Type the other team's name.").click("Click", "Ally").click("Right click", "Neutral").build(),
				e -> GuiManager.getInstance().prompt(player, "the other team's name",
						other -> runAndClose((e.isRightClick() ? "teamadmin neutral " : "teamadmin ally ") + teamName + " " + other),
						() -> new AdminTeamMenu(player, teamName).open()));

		// ---- danger
		set(40, new ItemBuilder(Material.TNT).name(Theme.bad() + "Disband team")
						.lore(Theme.bad() + "This cannot be undone.").click("Click", "Disband").build(),
				e -> new ConfirmMenu(player, "Disband " + teamName + "?", "teamadmin disband " + teamName,
						() -> new AdminTeamMenu(player, teamName)).open());

		setBack(45, () -> new AdminTeamsMenu(player));
		setClose(53);
	}
}
