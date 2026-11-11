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
			Component label = Ui.c((rank <= 3 ? Ui.hi() : Ui.pri()) + "#" + rank + " ");
			if (!owners.isEmpty()) {
				label = label.append(Ui.head(owners.get(0).getPlayer())).append(Component.space());
			}
			label = label.append(Ui.c(Ui.teamColor(team) + team.getName()));
			Component tip = Ui.lines(
					Ui.row("\u26A1", "Score", String.valueOf(team.getScore())),
					Ui.row("\u2694", "Kills", String.valueOf(TeamStats.getKills(team))),
					Ui.c(Ui.label() + "Click for full stats"));
			final int teamRank = rank;
			buttons.add(Ui.button(label, tip, Ui.BUTTON, (p, v) -> TeamStatsDialog.open(p, team, teamRank, current, query)));
		}

		// bottom row like the stats page: Back, Search, Next
		buttons.add(Ui.button(Ui.icon("\u2190", Ui.pri(), "Back"), Ui.label() + (current > 0 ? "Previous page" : query != null ? "Clear the search" : "Back to the menu"),
				Ui.BUTTON, (p, v) -> {
					if (current > 0) {
						open(p, current - 1, query);
					} else if (query != null) {
						open(p, 0, null);
					} else if (GuiManager.isEnabled()) {
						new MainMenu(p).open();
					}
				}));
		buttons.add(Ui.button(Ui.icon("\u26A1", Ui.hi(), "Search"), Ui.label() + "Type a name in the box, then click", Ui.BUTTON,
				(p, v) -> {
					String text = v.getText("query");
					open(p, 0, text == null || text.isBlank() ? null : text.trim());
				}));
		if (current < pages - 1) {
			buttons.add(Ui.button(Ui.icon("\u2192", Ui.pri(), "Next Page"), Ui.label() + "Page " + (current + 2) + " of " + pages, Ui.WIDE,
					(p, v) -> open(p, current + 1, query)));
		}

		String sub = (query == null ? shown.size() + " teams" : "\"" + query + "\"  \u2022  " + shown.size() + " found")
				+ "  \u2022  Page " + (current + 1) + " / " + pages;
		List<DialogBody> body = new ArrayList<>();
		body.add(DialogBody.plainMessage(Ui.header(null, Ui.sec() + "Team Top", sub), 300));
		if (shown.isEmpty()) {
			body.add(DialogBody.plainMessage(Ui.c(Ui.label() + "No teams found"), 300));
		}
		DialogInput input = DialogInput.text("query", Ui.c(Ui.hi() + "Find a team"))
				.initial(query == null ? "" : query).maxLength(32).width(Ui.WIDE).build();

		ActionButton close = Ui.button(Ui.icon("\u2716", Theme.off(), "Close"), null, Ui.WIDE, (p, v) -> p.closeInventory());
		Dialog dialog = Dialog.create(b -> b.empty()
				.base(DialogBase.builder(Ui.title("Team Top")).body(body).inputs(List.of(input)).build())
				.type(DialogType.multiAction(buttons, close, 2)));
		player.showDialog(dialog);
	}
}
