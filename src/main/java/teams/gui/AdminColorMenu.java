package teams.gui;

import teams.TeamColor;
import teams.util.TeamColorUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Pick a colour for a team
 */
public class AdminColorMenu extends PagedMenu<TeamColor> {

	private final String teamName;

	public AdminColorMenu(Player player, String teamName) {
		super(player);
		this.teamName = teamName;
	}

	@Override
	protected String title() {
		return Theme.main() + "Admin: colour";
	}

	@Override
	protected List<TeamColor> items() {
		List<TeamColor> list = new ArrayList<>();
		for (TeamColor color : TeamColor.values()) {
			if (!TeamColorUtil.isBanned(color)) {
				list.add(color);
			}
		}
		return list;
	}

	@Override
	protected ItemStack render(TeamColor color, int index) {
		return new ItemBuilder(color.getIcon()).name(color.toLegacy() + color.getDisplayName())
				.click("Click", "Use this colour").build();
	}

	@Override
	protected void onItemClick(TeamColor color, InventoryClickEvent event) {
		runAndReopen("teamadmin color " + teamName + " " + color.getId(), () -> new AdminTeamMenu(player, teamName));
	}

	@Override
	protected Menu previous() {
		return new AdminTeamMenu(player, teamName);
	}
}
