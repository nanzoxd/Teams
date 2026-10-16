package teams.gui;

import teams.Team;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * What /teamadmin opens. Everything an admin needs, one click away.
 * Every button runs the matching /teamadmin command, so permissions are still checked by the command.
 */
public class AdminMenu extends Menu {

	public AdminMenu(Player player) {
		super(player);
	}

	@Override
	protected String title() {
		return Theme.main() + "Team admin";
	}

	@Override
	protected void build() {
		int teams = Team.getTeamManager().sortTeamsByMembers().length;

		set(4, new ItemBuilder(Material.NETHER_STAR).name(Theme.main() + "Team management")
				.lore(Theme.soft() + "Teams: " + Theme.light() + teams,
						Theme.soft() + "Players online: " + Theme.light() + Bukkit.getOnlinePlayers().size()).build());

		set(19, new ItemBuilder(Material.CHEST).name(Theme.info() + "All teams")
						.lore(Theme.light() + "Browse and manage every team.")
						.click("Click", "Open").build(),
				e -> new AdminTeamsMenu(player).open());

		set(21, new ItemBuilder(Material.PLAYER_HEAD).name(Theme.info() + "Online players")
						.lore(Theme.light() + "See who is in which team.")
						.click("Click", "Open").build(),
				e -> new AdminPlayersMenu(player).open());

		set(23, new ItemBuilder(Material.EMERALD).name(Theme.good() + "Create a team")
						.lore(Theme.light() + "Makes a team without an owner.")
						.click("Click", "Create").build(),
				e -> GuiManager.getInstance().prompt(player, "the name of the new team",
						name -> runAndClose("teamadmin create " + name),
						() -> new AdminMenu(player).open()));

		set(25, new ItemBuilder(Material.ENDER_EYE).name(Theme.warn() + "Chat spy")
						.lore(Theme.light() + "Toggle seeing every team chat.")
						.click("Click", "Toggle").build(),
				e -> runAndReopen("teamadmin chatspy", () -> new AdminMenu(player)));

		set(30, new ItemBuilder(Material.REPEATER).name(Theme.accent() + "Reload")
						.lore(Theme.light() + "Reload config and messages.")
						.click("Click", "Reload").build(),
				e -> runAndReopen("teamadmin reload", () -> new AdminMenu(player)));

		set(32, new ItemBuilder(Material.TNT).name(Theme.bad() + "Purge scores")
						.lore(Theme.light() + "Reset the score of every team.", Theme.bad() + "This cannot be undone.")
						.click("Click", "Purge").build(),
				e -> new ConfirmMenu(player, "Reset the score of ALL teams?", "teamadmin purge",
						() -> new AdminMenu(player)).open());

		set(40, new ItemBuilder(Material.BOOK).name(Theme.info() + "Admin commands")
						.lore(Theme.light() + "Show the list of admin commands.")
						.click("Click", "Show").build(),
				e -> runAndClose("teamadmin help"));

		setClose(49);
	}
}
