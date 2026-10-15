package teams.gui;

import teams.Team;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Every team. Click one to manage it. The sort button switches between members and score.
 */
public class AdminTeamsMenu extends PagedMenu<Team> {

	private boolean byScore = false;

	public AdminTeamsMenu(Player player) {
		super(player);
	}

	@Override
	protected String title() {
		return Theme.main() + "Admin: all teams";
	}

	@Override
	protected List<Team> items() {
		List<Team> list = new ArrayList<>();
		String[] names = byScore ? Team.getTeamManager().sortTeamsByScore() : Team.getTeamManager().sortTeamsByMembers();
		for (String name : names) {
			if (name == null) {
				continue;
			}
			Team team = Team.getTeam(name);
			if (team != null) {
				list.add(team);
			}
		}
		return list;
	}

	@Override
	protected ItemStack render(Team team, int index) {
		return new ItemBuilder(team.getColor().getIcon()).name(team.getColor().toLegacy() + team.getName())
				.lore(Theme.soft() + "Members: " + Theme.light() + team.getMembers().size() + "/" + team.getTeamLimit(),
						Theme.soft() + "Score: " + Theme.light() + team.getScore(),
						Theme.soft() + "Open: " + (team.isOpen() ? Theme.good() + "yes" : Theme.bad() + "no"))
				.click("Click", "Manage").build();
	}

	@Override
	protected void onItemClick(Team team, InventoryClickEvent event) {
		new AdminTeamMenu(player, team.getName()).open();
	}

	@Override
	protected Menu previous() {
		return new AdminMenu(player);
	}

	@Override
	protected void buildExtra() {
		set(47, new ItemBuilder(Material.HOPPER).name(Theme.main() + "Sort by: " + Theme.light() + (byScore ? "score" : "members"))
						.click("Click", "Change").build(),
				e -> {
					byScore = !byScore;
					page = 0;
					refresh();
				});
	}
}
