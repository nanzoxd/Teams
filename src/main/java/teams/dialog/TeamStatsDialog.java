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

/** Everything the leaderboard measures for one team, then every player with their head. */
final class TeamStatsDialog {

	private TeamStatsDialog() {
	}

	private static Component row(String symbol, String label, String value) {
		return Ui.c(Ui.P3 + symbol + " " + Ui.P5 + label + ": " + Ui.P1 + value);
	}

	static void open(Player player, Team team, int rank, int page, String query) {
		String color = Ui.teamColor(team);

		// text box 1: all the numbers, no gaps
		Component stats = Ui.lines(
				Ui.c(color + team.getName() + Ui.P5 + "  •  " + Ui.P3 + "#" + rank),
				Ui.divider(),
				row("⚡", "Score", String.valueOf(team.getScore())),
				row("⚔", "Kills", String.valueOf(TeamStats.getKills(team))),
				row("☠", "Deaths", String.valueOf(TeamStats.getDeaths(team))),
				row("⚔", "K/D ratio", TeamStats.formatKdr(TeamStats.getKdr(team))),
				row("⌚", "Playtime", TeamStats.formatDuration(TeamStats.getPlaytimeSeconds(team))),
				row("♥", "Members", team.getMembers().size() + " / " + team.getTeamLimit()));

		// text box 2: every player with their head, owners first
		List<Component> people = new ArrayList<>();
		people.add(Ui.c(Ui.P3 + "Players"));
		people.add(Ui.divider());
		for (PlayerRank r : new PlayerRank[]{PlayerRank.OWNER, PlayerRank.ADMIN, PlayerRank.DEFAULT}) {
			String tag = r == PlayerRank.OWNER ? "Owner" : r == PlayerRank.ADMIN ? "Admin" : "Member";
			for (TeamPlayer tp : team.getRank(r)) {
				String name = tp.getPlayer().getName() == null ? "?" : tp.getPlayer().getName();
				people.add(Ui.head(tp.getPlayer()).append(Component.space())
						.append(Ui.c(Ui.P1 + name + Ui.P5 + "  " + tag)));
			}
		}

		List<DialogBody> body = new ArrayList<>();
		body.add(DialogBody.plainMessage(stats, 300));
		body.add(DialogBody.plainMessage(Ui.lines(people.toArray(new Component[0])), 300));

		ActionButton back = Ui.button(Theme.info() + "← Back", null, Ui.BUTTON, (p, v) -> TopDialog.open(p, page, query));
		ActionButton close = Ui.button(Theme.bad() + "Close", null, Ui.BUTTON, (p, v) -> p.closeInventory());
		Dialog dialog = Dialog.create(b -> b.empty()
				.base(DialogBase.builder(Ui.title("Team Top")).body(body).build())
				.type(DialogType.multiAction(List.of(back), close, 1)));
		player.showDialog(dialog);
	}
}
