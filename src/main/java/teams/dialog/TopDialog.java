package teams.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.entity.Player;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import teams.Main;
import teams.PlayerRank;
import teams.Team;
import teams.TeamPlayer;
import teams.gui.GuiManager;
import teams.gui.MainMenu;
import teams.gui.Theme;
import teams.stats.TeamStats;

/**
 * /team top as a native dialog: #1 #2 #3 buttons with the owner head, Next Page, Search, Back and Close.
 */
public final class TopDialog {

	private static final int PER_PAGE = 10;

	private TopDialog() {
	}

	public static void open(Player player) {
		open(player, 0, null);
	}

	static List<Team> ranking() {
		List<Team> teams = new ArrayList<>();
		for (String name : Team.getTeamManager().sortTeamsByScore()) {
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

	static void open(Player player, int page, String query) {
		List<Team> all = ranking();
		List<Team> shown = new ArrayList<>();
		for (Team t : all) {
			if (query == null || t.getName().toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
				shown.add(t);
			}
		}
		int pages = Math.max(1, (shown.size() + PER_PAGE - 1) / PER_PAGE);
		int current = Math.max(0, Math.min(page, pages - 1));

		List<ActionButton> buttons = new ArrayList<>();
		for (int i = current * PER_PAGE; i < shown.size() && i < (current + 1) * PER_PAGE; i++) {
			Team team = shown.get(i);
			int rank = all.indexOf(team) + 1;
			List<TeamPlayer> owners = team.getRank(PlayerRank.OWNER);
			Component label = Ui.c(Theme.soft() + "#" + rank + " ");
			if (!owners.isEmpty()) {
				label = label.append(Ui.head(owners.get(0).getPlayer())).append(Component.space());
			}
			label = label.append(Ui.c(team.getColor().toLegacy() + team.getName()));
			Component tip = Ui.c(Theme.soft() + "Score: " + Theme.light() + team.getScore() + "\n" + Theme.soft()
					+ "Kills: " + Theme.light() + TeamStats.getKills(team) + "\n" + Theme.good() + "Click for full stats");
			final int teamRank = rank;
			buttons.add(Ui.button(label, tip, Ui.BUTTON, (p, v) -> TeamStatsDialog.open(p, team, teamRank, current, query)));
		}

		if (current < pages - 1) {
			buttons.add(Ui.button(Theme.info() + "Next Page", Theme.soft() + "Page " + (current + 2) + " of " + pages, Ui.BUTTON,
					(p, v) -> open(p, current + 1, query)));
		}
		buttons.add(Ui.button(Theme.warn() + "Search", Theme.soft() + "Find a team by name", Ui.BUTTON,
				(p, v) -> search(p, query)));
		buttons.add(Ui.button(Theme.info() + "Back", null, Ui.BUTTON, (p, v) -> {
			if (current > 0) {
				open(p, current - 1, query);
			} else if (query != null) {
				open(p, 0, null);
			} else if (GuiManager.isEnabled()) {
				new MainMenu(p).open();
			}
		}));

		List<DialogBody> body = new ArrayList<>();
		String sub = query == null ? "Page " + (current + 1) + " of " + pages
				: "Search: " + query + "  -  " + shown.size() + " found";
		body.add(DialogBody.plainMessage(Ui.c(Theme.soft() + sub), 300));
		if (shown.isEmpty()) {
			body.add(DialogBody.plainMessage(Ui.c(Theme.bad() + "No teams found"), 300));
		}

		ActionButton close = Ui.button(Theme.bad() + "Close", null, Ui.BUTTON, (p, v) -> p.closeInventory());
		Dialog dialog = Dialog.create(b -> b.empty()
				.base(DialogBase.builder(Ui.title("Team Top")).body(body).build())
				.type(DialogType.multiAction(buttons, close, 2)));
		player.showDialog(dialog);
	}

	private static void search(Player player, String previous) {
		DialogInput input = DialogInput.text("query", Ui.c(Theme.light() + "Team name"))
				.initial(previous == null ? "" : previous).maxLength(32).width(Ui.WIDE).build();
		ActionButton go = Ui.button(Theme.good() + "Search", null, Ui.BUTTON, (p, v) -> {
			String text = v.getText("query");
			open(p, 0, text == null || text.isBlank() ? null : text.trim());
		});
		ActionButton back = Ui.button(Theme.info() + "Back", null, Ui.BUTTON, (p, v) -> open(p, 0, previous));
		Dialog dialog = Dialog.create(b -> b.empty()
				.base(DialogBase.builder(Ui.title("Search teams")).inputs(List.of(input)).build())
				.type(DialogType.confirmation(go, back)));
		player.showDialog(dialog);
	}
}
