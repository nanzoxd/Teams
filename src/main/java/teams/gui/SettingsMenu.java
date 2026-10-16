package teams.gui;

import teams.Team;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Team settings. The commands behind the buttons check the rank, so members can look but not change.
 */
public class SettingsMenu extends Menu {

	private final Team team;

	public SettingsMenu(Player player, Team team) {
		super(player);
		this.team = team;
	}

	@Override
	protected String title() {
		return Theme.main() + "Team settings";
	}

	private Menu again() {
		Team current = Team.getTeam(player);
		return current == null ? new MainMenu(player) : new SettingsMenu(player, current);
	}

	@Override
	protected void build() {
		String color = team.getColor().toLegacy();

		set(4, new ItemBuilder(team.getColor().getIcon()).name(color + team.getName())
				.lore(Theme.light() + (team.getDescription() == null || team.getDescription().isEmpty()
						? "No description" : team.getDescription())).build());

		set(10, new ItemBuilder(team.isOpen() ? Material.LIME_DYE : Material.GRAY_DYE)
						.name(Theme.warn() + "Open team: " + (team.isOpen() ? Theme.good() + "yes" : Theme.bad() + "no"))
						.lore(Theme.light() + "Open teams can be joined by anyone.").click("Click", "Switch").build(),
				e -> runAndReopen("team open", this::again));

		set(11, new ItemBuilder(team.isPvp() ? Material.IRON_SWORD : Material.WOODEN_SWORD)
						.name(Theme.warn() + "Team PvP: " + (team.isPvp() ? Theme.good() + "on" : Theme.bad() + "off"))
						.lore(Theme.light() + "If members can hurt each other.").click("Click", "Switch").build(),
				e -> runAndReopen("team pvp", this::again));

		set(12, new ItemBuilder(Material.NAME_TAG).name(Theme.warn() + "Rename team")
						.lore(Theme.light() + "Now: " + color + team.getName()).click("Click", "Change").build(),
				e -> GuiManager.getInstance().prompt(player, "the new team name",
						name -> runAndClose("team name " + name), () -> again().open()));

		set(13, new ItemBuilder(Material.OAK_SIGN).name(Theme.warn() + "Team tag")
						.lore(Theme.light() + "Now: " + color + (team.getOriginalTag().isEmpty() ? "-" : team.getOriginalTag()),
								Theme.light() + "The tag shows in chat and below names.").click("Click", "Change").build(),
				e -> GuiManager.getInstance().prompt(player, "the new tag",
						tag -> runAndClose("team tag " + tag), () -> again().open()));

		set(14, new ItemBuilder(Material.WRITABLE_BOOK).name(Theme.warn() + "Description")
						.click("Click", "Change").build(),
				e -> GuiManager.getInstance().prompt(player, "the new description",
						text -> runAndClose("team description " + text), () -> again().open()));

		set(15, new ItemBuilder(team.getColor().getIcon()).name(Theme.warn() + "Colour")
						.lore(Theme.light() + "Now: " + color + team.getColor().getDisplayName(),
								Theme.light() + "Change it with /team color <colour>").click("Click", "Show the colours").build(),
				e -> {
					player.closeInventory();
					ColorHelp.send(player);
				});

		set(19, new ItemBuilder(Material.RED_BED).name(Theme.warn() + "Set home here")
						.lore(Theme.light() + (team.getTeamHome() == null ? "No home set" : "A home is set")).click("Click", "Set").build(),
				e -> runAndClose("team sethome"));

		set(20, new ItemBuilder(Material.TNT).name(Theme.bad() + "Delete home").click("Click", "Delete").build(),
				e -> runAndReopen("team delhome", this::again));

		set(25, new ItemBuilder(Material.RED_CONCRETE).name(Theme.bad() + "Disband team")
						.lore(Theme.light() + "This deletes the team for everyone.").click("Click", "Disband").build(),
				e -> new ConfirmMenu(player, "Disband " + team.getName() + "?", "team disband", this::again).open());

		setBack(45, () -> new MainMenu(player));
		setClose(53);
	}
}
