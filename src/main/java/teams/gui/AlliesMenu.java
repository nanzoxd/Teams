package teams.gui;

import teams.Team;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * The allies of the team
 */
public class AlliesMenu extends PagedMenu<Team> {

	private final Team team;

	public AlliesMenu(Player player, Team team) {
		super(player);
		this.team = team;
	}

	@Override
	protected String title() {
		return Theme.main() + "Allies";
	}

	@Override
	protected List<Team> items() {
		List<Team> allies = new ArrayList<>();
		for (UUID id : team.getAllies().get()) {
			Team ally = Team.getTeam(id);
			if (ally != null) {
				allies.add(ally);
			}
		}
		return allies;
	}

	@Override
	protected ItemStack render(Team ally, int index) {
		return new ItemBuilder(ally.getColor().getIcon()).name(ally.getColor().toLegacy() + ally.getName())
				.lore(Theme.soft() + "Members: " + Theme.light() + ally.getMembers().size(),
						Theme.soft() + "Score: " + Theme.light() + ally.getScore())
				.click("Click", "Stop being allies").build();
	}

	@Override
	protected void onItemClick(Team ally, InventoryClickEvent event) {
		new ConfirmMenu(player, "End the alliance with " + ally.getName() + "?", "team neutral " + ally.getName(),
				() -> new AlliesMenu(player, team)).open();
	}

	@Override
	protected Menu previous() {
		return new MainMenu(player);
	}

	@Override
	protected void buildExtra() {
		set(47, new ItemBuilder(Material.LIME_CONCRETE).name(Theme.good() + "Ask a team to be allies")
						.lore(Theme.light() + "Type the team name.").build(),
				e -> GuiManager.getInstance().prompt(player, "the name of the team",
						name -> runAndClose("team ally " + name),
						() -> new AlliesMenu(player, team).open()));
	}
}
