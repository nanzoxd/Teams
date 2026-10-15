package teams.gui;

import teams.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Every team, biggest first. Clicking a team joins it (works for open teams, or if invited).
 */
public class TeamListMenu extends PagedMenu<Team> {

	public TeamListMenu(Player player) {
		super(player);
	}

	@Override
	protected String title() {
		return Theme.main() + "All teams";
	}

	@Override
	protected List<Team> items() {
		List<Team> teams = new ArrayList<>();
		for (String name : Team.getTeamManager().sortTeamsByMembers()) {
			if (name == null) {
				continue;
			}
			Team team = Team.getTeam(name);
			if (team != null) {
				teams.add(team);
			}
		}
		return teams;
	}

	@Override
	protected ItemStack render(Team team, int index) {
		ItemBuilder builder = new ItemBuilder(team.getColor().getIcon()).name(team.getColor().toLegacy() + team.getName())
				.lore(Theme.soft() + "Members: " + Theme.light() + team.getMembers().size() + "/" + team.getTeamLimit(),
						Theme.soft() + "Score: " + Theme.light() + team.getScore(),
						Theme.soft() + "Open: " + (team.isOpen() ? Theme.good() + "yes" : Theme.bad() + "no"));
		if (team.getDescription() != null && !team.getDescription().isEmpty()) {
			builder.lore(Theme.light() + team.getDescription());
		}
		if (Team.getTeam(player) == null) {
			builder.click("Click", "Join");
		}
		return builder.build();
	}

	@Override
	protected void onItemClick(Team team, InventoryClickEvent event) {
		if (Team.getTeam(player) == null) {
			runAndClose("team join " + team.getName());
		} else {
			runAndClose("team info " + team.getName());
		}
	}

	@Override
	protected Menu previous() {
		return new MainMenu(player);
	}
}
