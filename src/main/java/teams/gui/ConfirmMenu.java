package teams.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

/**
 * "Are you sure?" screen for things that can't be undone
 */
public class ConfirmMenu extends Menu {

	private final String question;
	private final String command;
	private final Supplier<Menu> cancel;

	public ConfirmMenu(Player player, String question, String command, Supplier<Menu> cancel) {
		super(player);
		this.question = question;
		this.command = command;
		this.cancel = cancel;
	}

	@Override
	protected String title() {
		return Theme.main() + "Are you sure?";
	}

	@Override
	protected int size() {
		return 27;
	}

	@Override
	protected void build() {
		set(4, new ItemBuilder(Material.PAPER).name(Theme.main() + question).build());
		set(11, new ItemBuilder(Material.LIME_CONCRETE).name(Theme.good() + "Yes").build(), e -> runAndClose(command));
		set(15, new ItemBuilder(Material.RED_CONCRETE).name(Theme.bad() + "No").build(), e -> cancel.get().open());
	}
}
