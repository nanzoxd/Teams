package teams.gui;

import teams.Team;
import teams.TeamPlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Members of any team, for admins. No rank restrictions, the admin commands decide.
 */
public class AdminMembersMenu extends PagedMenu<TeamPlayer> {

	private final String teamName;

	public AdminMembersMenu(Player player, String teamName) {
		super(player);
		this.teamName = teamName;
	}

	private Team team() {
		return Team.getTeam(teamName);
	}

	@Override
	protected String title() {
		return Theme.main() + "Admin: members";
	}

	@Override
	protected List<TeamPlayer> items() {
		Team team = team();
		if (team == null) {
			return new ArrayList<>();
		}
		List<TeamPlayer> members = new ArrayList<>(team.getMembers().get());
		members.sort(Comparator.comparingInt((TeamPlayer p) -> -p.getRank().value));
		return members;
	}

	@Override
	protected ItemStack render(TeamPlayer member, int index) {
		String name = member.getPlayer().getName() == null ? "unknown" : member.getPlayer().getName();
		return new ItemBuilder(Material.PLAYER_HEAD).head(member.getPlayer()).name(Theme.main() + name)
				.lore(Theme.soft() + "Rank: " + Theme.light() + member.getRank().name().toLowerCase(),
						Theme.soft() + "Status: " + (member.isOnline() ? Theme.good() + "online" : Theme.light() + "offline"))
				.click("Left click", "Promote").click("Right click", "Demote")
				.click("Shift left", "Make owner").click("Shift right", "Remove").build();
	}

	@Override
	protected void onItemClick(TeamPlayer member, InventoryClickEvent event) {
		String name = member.getPlayer().getName();
		if (name == null) {
			return;
		}
		if (event.getClick() == ClickType.SHIFT_LEFT) {
			runAndReopen("teamadmin setowner " + name, () -> new AdminMembersMenu(player, teamName));
		} else if (event.getClick() == ClickType.SHIFT_RIGHT) {
			new ConfirmMenu(player, "Remove " + name + " from the team?", "teamadmin leave " + name,
					() -> new AdminMembersMenu(player, teamName)).open();
		} else if (event.getClick() == ClickType.RIGHT) {
			runAndReopen("teamadmin demote " + name, () -> new AdminMembersMenu(player, teamName));
		} else {
			runAndReopen("teamadmin promote " + name, () -> new AdminMembersMenu(player, teamName));
		}
	}

	@Override
	protected Menu previous() {
		return new AdminTeamMenu(player, teamName);
	}

	@Override
	protected void buildExtra() {
		set(47, new ItemBuilder(Material.PAPER).name(Theme.good() + "Add a player")
						.lore(Theme.light() + "Type the player's name.").build(),
				e -> GuiManager.getInstance().prompt(player, "the name of the player to add",
						name -> runAndClose("teamadmin join " + teamName + " " + name),
						() -> new AdminMembersMenu(player, teamName).open()));
	}
}
