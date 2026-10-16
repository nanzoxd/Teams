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

/** Everything the leaderboard measures for one team. */
final class TeamStatsDialog {

	private TeamStatsDialog() {
	}

	static void open(Player player, Team team, int rank, int page, String query) {
		List<DialogBody> body = new ArrayList<>();

		List<TeamPlayer> owners = team.getRank(PlayerRank.OWNER);
		Component owner = Ui.c(Theme.soft() + "Owner: ");
		if (owners.isEmpty()) {
			owner = owner.append(Ui.c(Theme.light() + "-"));
		} else {
			owner = owner.append(Ui.head(owners.get(0).getPlayer())).append(Component.space())
					.append(Ui.c(Theme.light() + String.valueOf(owners.get(0).getPlayer().getName())));
		}
		body.add(DialogBody.plainMessage(Ui.c(Theme.soft() + "Position: " + Theme.main() + "#" + rank), 300));
		body.add(DialogBody.plainMessage(owner, 300));
		body.add(DialogBody.plainMessage(Ui.line("Score", String.valueOf(team.getScore())), 300));
		body.add(DialogBody.plainMessage(Ui.line("Kills", String.valueOf(TeamStats.getKills(team))), 300));
		body.add(DialogBody.plainMessage(Ui.line("Deaths", String.valueOf(TeamStats.getDeaths(team))), 300));
		body.add(DialogBody.plainMessage(Ui.line("K/D ratio", TeamStats.formatKdr(TeamStats.getKdr(team))), 300));
		body.add(DialogBody.plainMessage(Ui.line("Playtime", TeamStats.formatDuration(TeamStats.getPlaytimeSeconds(team))), 300));
		body.add(DialogBody.plainMessage(Ui.line("Members", team.getMembers().size() + " / " + team.getTeamLimit()), 300));

		Component name = Ui.c(team.getColor().toLegacy() + team.getName());
		ActionButton back = Ui.button(Theme.info() + "Back", null, Ui.BUTTON, (p, v) -> TopDialog.open(p, page, query));
		ActionButton close = Ui.button(Theme.bad() + "Close", null, Ui.BUTTON, (p, v) -> p.closeInventory());
		Dialog dialog = Dialog.create(b -> b.empty()
				.base(DialogBase.builder(name).body(body).build())
				.type(DialogType.multiAction(List.of(back), close, 1)));
		player.showDialog(dialog);
	}
}
