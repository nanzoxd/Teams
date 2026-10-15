package teams.gui;

import teams.Team;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Online players and their team. Left click opens their team, shift click removes them from it.
 */
public class AdminPlayersMenu extends PagedMenu<Player> {

	public AdminPlayersMenu(Player player) {
		super(player);
	}

	@Override
	protected String title() {
		return Theme.main() + "Admin: players";
	}

	@Override
	protected List<Player> items() {
		return new ArrayList<>(Bukkit.getOnlinePlayers());
	}

	@Override
	protected ItemStack render(Player target, int index) {
		Team team = Team.getTeam(target);
		ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD).head(target).name(Theme.main() + target.getName());
		if (team == null) {
			builder.lore(Theme.soft() + "Team: " + Theme.light() + "none");
		} else {
			builder.lore(Theme.soft() + "Team: " + team.getColor().toLegacy() + team.getName(),
					Theme.soft() + "Rank: " + Theme.light() + team.getTeamPlayer(target).getRank().name().toLowerCase())
					.click("Click", "Manage team").click("Shift click", "Remove from team");
		}
		return builder.build();
	}

	@Override
	protected void onItemClick(Player target, InventoryClickEvent event) {
		Team team = Team.getTeam(target);
		if (team == null) {
			return;
		}
		if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
			new ConfirmMenu(player, "Remove " + target.getName() + " from " + team.getName() + "?",
					"teamadmin leave " + target.getName(), () -> new AdminPlayersMenu(player)).open();
		} else {
			new AdminTeamMenu(player, team.getName()).open();
		}
	}

	@Override
	protected Menu previous() {
		return new AdminMenu(player);
	}
}
