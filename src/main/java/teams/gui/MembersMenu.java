package teams.gui;

import teams.PlayerRank;
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
 * All the members of the team. Admins and the owner can promote, demote and kick from here.
 */
public class MembersMenu extends PagedMenu<TeamPlayer> {

	private final Team team;

	public MembersMenu(Player player, Team team) {
		super(player);
		this.team = team;
	}

	@Override
	protected String title() {
		return Theme.main() + "Members";
	}

	@Override
	protected List<TeamPlayer> items() {
		List<TeamPlayer> members = new ArrayList<>(team.getMembers().get());
		// owner first, then admins, then the rest
		members.sort(Comparator.comparingInt((TeamPlayer p) -> -p.getRank().value));
		return members;
	}

	@Override
	protected ItemStack render(TeamPlayer member, int index) {
		String name = member.getPlayer().getName() == null ? "unknown" : member.getPlayer().getName();
		ItemBuilder builder = new ItemBuilder(Material.PLAYER_HEAD).head(member.getPlayer())
				.name(Theme.main() + name)
				.lore(Theme.soft() + "Rank: " + Theme.light() + member.getRank().name().toLowerCase(),
						Theme.soft() + "Status: " + (member.isOnline() ? Theme.good() + "online" : Theme.light() + "offline"));
		if (member.getTitle() != null && !member.getTitle().isEmpty()) {
			builder.lore(Theme.soft() + "Title: " + Theme.light() + member.getTitle());
		}
		if (canManage(member)) {
			builder.click("Left click", "Promote").click("Right click", "Demote").click("Shift click", "Kick");
		}
		return builder.build();
	}

	private boolean canManage(TeamPlayer target) {
		TeamPlayer self = team.getTeamPlayer(player);
		if (self == null || target.getPlayerUUID().equals(player.getUniqueId())) {
			return false;
		}
		return self.getRank().value > target.getRank().value;
	}

	@Override
	protected void onItemClick(TeamPlayer member, InventoryClickEvent event) {
		if (!canManage(member)) {
			return;
		}
		String name = member.getPlayer().getName();
		if (name == null) {
			return;
		}
		if (event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT) {
			new ConfirmMenu(player, "Kick " + name + "?", "team kick " + name,
					() -> new MembersMenu(player, team)).open();
		} else if (event.getClick() == ClickType.RIGHT) {
			runAndReopen("team demote " + name, () -> refreshed());
		} else {
			runAndReopen("team promote " + name, () -> refreshed());
		}
	}

	private Menu refreshed() {
		Team current = Team.getTeam(player);
		return current == null ? new MainMenu(player) : new MembersMenu(player, current);
	}

	@Override
	protected Menu previous() {
		return new MainMenu(player);
	}

	@Override
	protected void buildExtra() {
		PlayerRank rank = team.getTeamPlayer(player) == null ? PlayerRank.DEFAULT : team.getTeamPlayer(player).getRank();
		if (rank != PlayerRank.DEFAULT) {
			set(47, new ItemBuilder(Material.PAPER).name(Theme.good() + "Invite a player")
							.lore(Theme.light() + "Type the player's name.").build(),
					e -> GuiManager.getInstance().prompt(player, "the name of the player to invite",
							name -> runAndClose("team invite " + name),
							() -> new MembersMenu(player, team).open()));
		}
	}
}
