package teams.gui;

import teams.Team;
import teams.Warp;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The warps of the team
 */
public class WarpsMenu extends PagedMenu<Warp> {

	private final Team team;

	public WarpsMenu(Player player, Team team) {
		super(player);
		this.team = team;
	}

	@Override
	protected String title() {
		return Theme.main() + "Team warps";
	}

	@Override
	protected List<Warp> items() {
		List<Warp> warps = new ArrayList<>(team.getWarps().get());
		warps.sort(Comparator.comparing(Warp::getName, String.CASE_INSENSITIVE_ORDER));
		return warps;
	}

	@Override
	protected ItemStack render(Warp warp, int index) {
		return new ItemBuilder(warp.hasPassword() ? Material.ENDER_EYE : Material.ENDER_PEARL)
				.name(Theme.main() + warp.getName())
				.lore(warp.hasPassword() ? Theme.light() + "Password protected" : Theme.light() + "No password")
				.click("Left click", "Teleport")
				.click("Right click", "Delete").build();
	}

	@Override
	protected void onItemClick(Warp warp, InventoryClickEvent event) {
		if (event.getClick() == ClickType.RIGHT) {
			new ConfirmMenu(player, "Delete warp " + warp.getName() + "?", "team delwarp " + warp.getName(),
					() -> new WarpsMenu(player, team)).open();
		} else if (warp.hasPassword()) {
			GuiManager.getInstance().prompt(player, "the password of the warp",
					password -> runAndClose("team warp " + warp.getName() + " " + password),
					() -> new WarpsMenu(player, team).open());
		} else {
			runAndClose("team warp " + warp.getName());
		}
	}

	@Override
	protected Menu previous() {
		return new MainMenu(player);
	}

	@Override
	protected void buildExtra() {
		set(47, new ItemBuilder(Material.LIME_CONCRETE).name(Theme.main() + "New warp here")
						.lore(Theme.light() + team.getWarps().size() + "/" + team.getMaxWarps() + " used").build(),
				e -> GuiManager.getInstance().prompt(player, "the name of the new warp",
						name -> runAndClose("team setwarp " + name),
						() -> new WarpsMenu(player, team).open()));
	}
}
