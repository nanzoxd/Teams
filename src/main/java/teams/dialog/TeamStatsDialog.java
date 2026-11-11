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
import teams.PlayerRank;
import teams.Team;
import teams.TeamPlayer;
import teams.gui.Theme;
import teams.stats.TeamStats;

/** Team info in the same layout as a player stats page: header, line, numbers, players, buttons. */
final class TeamStatsDialog {

	private TeamStatsDialog() {
	}

	static void open(Player player, Team team, int rank, int page, String query) {
		String color = Ui.teamColor(team);
		List<TeamPlayer> owners = team.getRank(PlayerRank.OWNER);
		Component icon = owners.isEmpty() ? null : Ui.head(owners.get(0).getPlayer());

		int kills = TeamStats.getKills(team);
		int deaths = TeamStats.getDeaths(team);
		Component stats = Ui.lines(
				Ui.header(icon, color + team.getName(), "Team Stats  \u2022  #" + rank),
				Ui.row("\u26A1", "Score", String.valueOf(team.getScore())),
				Ui.row("\u2694", "Kills", Theme.on() + kills + Ui.text() + " / " + Theme.off() + deaths + Ui.label() + " ("
						+ TeamStats.formatKdr(TeamStats.getKdr(team)) + " K/D)"),
				Ui.row("\uD83D\uDD53", "Playtime", TeamStats.formatDuration(TeamStats.getPlaytimeSeconds(team))),
				Ui.row("\u2756", "Members", team.getMembers().size() + " / " + team.getTeamLimit()));

		// players, owners first, each with their head
		List<Component> people = new ArrayList<>();
		people.add(Ui.c(Ui.sec() + "Players"));
		people.add(Ui.divider());
		for (PlayerRank r : new PlayerRank[]{PlayerRank.OWNER, PlayerRank.ADMIN, PlayerRank.DEFAULT}) {
			String tag = r == PlayerRank.OWNER ? "Owner" : r == PlayerRank.ADMIN ? "Admin" : "Member";
			for (TeamPlayer tp : team.getRank(r)) {
				String name = tp.getPlayer().getName() == null ? "?" : tp.getPlayer().getName();
				people.add(Ui.head(tp.getPlayer()).append(Component.space())
						.append(Ui.c(Ui.text() + name + Ui.label() + "  " + tag)));
			}
		}

		List<DialogBody> body = new ArrayList<>();
		body.add(DialogBody.plainMessage(stats, 300));
		body.add(DialogBody.plainMessage(Ui.lines(people.toArray(new Component[0])), 300));

		ActionButton back = Ui.button(Ui.icon("\u2190", Ui.pri(), "Back"), null, Ui.BUTTON, (p, v) -> TopDialog.open(p, page, query));
		ActionButton close = Ui.button(Ui.icon("\u2716", Theme.off(), "Close"), null, Ui.BUTTON, (p, v) -> p.closeInventory());
		Dialog dialog = Dialog.create(b -> b.empty()
				.base(DialogBase.builder(Ui.title("Team Info")).body(body).build())
				.type(DialogType.multiAction(List.of(back), close, 2)));
		player.showDialog(dialog);
	}
}
