package teams.dialog;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import teams.Main;
import teams.PlayerRank;
import teams.Team;
import teams.TeamPlayer;
import teams.stats.TeamStats;

/**
 * Team profile: the name as a header, rows of label and value, then the members. No icons in front of the rows,
 * status is shown with colour.
 */
final class TeamStatsDialog {

	private TeamStatsDialog() {
	}

	static void open(Player player, Team team, int rank, int page, String query) {
		String color = Ui.teamColor(team);
		List<TeamPlayer> owners = team.getRank(PlayerRank.OWNER);
		Component head = owners.isEmpty() ? null : Ui.head(owners.get(0).getPlayer());

		StringBuilder ownerNames = new StringBuilder();
		for (TeamPlayer o : owners) {
			ownerNames.append(ownerNames.length() == 0 ? "" : ", ").append(o.getPlayer().getName());
		}
		int online = 0;
		for (TeamPlayer tp : team.getMembers().getClone()) {
			if (tp.isOnline()) {
				online++;
			}
		}

		long kills = TeamStats.getKills(team);
		long deaths = TeamStats.getDeaths(team);
		List<Component> rows = new ArrayList<>();
		rows.add(Ui.header(head, color + team.getName(), "Team Profile  -  Rank #" + rank));
		if (ownerNames.length() > 0) {
			rows.add(Ui.row("Owner", ownerNames.toString()));
		}
		rows.add(Ui.row("Score", Ui.hi() + team.getScore()));
		rows.add(Ui.row("Kills / Deaths", Ui.on() + kills + Ui.text() + " / " + Ui.off() + deaths + Ui.label() + "  ("
				+ TeamStats.formatKdr(TeamStats.getKdr(team)) + " K/D)"));
		rows.add(Ui.row("Playtime", TeamStats.formatDuration(TeamStats.getPlaytimeSeconds(team))));
		rows.add(Ui.row("Members", team.getMembers().size() + " / " + team.getTeamLimit() + Ui.label() + "  (" + online + " online)"));
		rows.add(Ui.row("Level", String.valueOf(team.getLevel())));
		rows.add(Ui.row("Joining", team.isOpen() ? Ui.on() + "Open" : Ui.label() + "Invite only"));
		rows.add(Ui.row("Home", team.getTeamHome() != null ? Ui.on() + "Set" : Ui.off() + "Not set"));
		if (Main.plugin.getConfig().getBoolean("allies.enabled", true)) {
			rows.add(Ui.row("Allies", String.valueOf(team.getAllies().getClone().size())));
		}
		String description = team.getDescription();
		if (description != null && !description.isBlank()) {
			rows.add(Ui.row("About", description));
		}

		// members, owners first, each with their head. Online players are green.
		List<Component> people = new ArrayList<>();
		people.add(Ui.c(Ui.text() + "Members"));
		people.add(Ui.divider());
		for (PlayerRank r : new PlayerRank[]{PlayerRank.OWNER, PlayerRank.ADMIN, PlayerRank.DEFAULT}) {
			String tag = r == PlayerRank.OWNER ? Ui.special() + "Owner" : r == PlayerRank.ADMIN ? Ui.info2() + "Admin" : Ui.label() + "Member";
			for (TeamPlayer tp : team.getRank(r)) {
				String name = tp.getPlayer().getName() == null ? "?" : tp.getPlayer().getName();
				people.add(Ui.head(tp.getPlayer()).append(Component.space())
						.append(Ui.c((tp.isOnline() ? Ui.on() : Ui.text()) + name + "  " + tag)));
			}
		}

		List<DialogBody> body = new ArrayList<>();
		body.add(DialogBody.plainMessage(Ui.lines(rows.toArray(new Component[0])), 300));
		body.add(DialogBody.plainMessage(Ui.lines(people.toArray(new Component[0])), 300));

		ActionButton back = Ui.button(Ui.back("Back"), null, Ui.BUTTON, (p, v) -> TopDialog.open(p, page, query));
		ActionButton close = Ui.button(Ui.close(), null, Ui.BUTTON, (p, v) -> p.closeInventory());
		Dialog dialog = Dialog.create(b -> b.empty()
				.base(DialogBase.builder(Ui.title("Team Profile")).body(body).build())
				.type(DialogType.multiAction(List.of(back, close), null, 2)));
		player.showDialog(dialog);
	}
}
