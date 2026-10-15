package teams.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * A menu with a list of things, 45 per page. The bottom row has the navigation buttons.
 */
public abstract class PagedMenu<T> extends Menu {

	private static final int PER_PAGE = 45;
	protected int page = 0;

	protected PagedMenu(Player player) {
		super(player);
	}

	protected abstract List<T> items();

	protected abstract ItemStack render(T item, int index);

	protected abstract void onItemClick(T item, InventoryClickEvent event);

	/**
	 * @return the menu to open with the back button, or null for no back button
	 */
	protected abstract Menu previous();

	/**
	 * Add extra buttons to the bottom row (slots 46, 47, 51, 52 are free)
	 */
	protected void buildExtra() {
	}

	@Override
	protected void build() {
		List<T> list = items();
		int pages = Math.max(1, (list.size() + PER_PAGE - 1) / PER_PAGE);
		if (page >= pages) {
			page = pages - 1;
		}

		for (int i = 0; i < PER_PAGE; i++) {
			int index = page * PER_PAGE + i;
			if (index >= list.size()) {
				break;
			}
			T item = list.get(index);
			set(i, render(item, index), e -> onItemClick(item, e));
		}

		Menu previous = previous();
		if (previous != null) {
			setBack(45, () -> previous);
		}
		setClose(53);

		if (page > 0) {
			set(48, new ItemBuilder(Material.ARROW).name(Theme.main() + "Previous page").build(), e -> {
				page--;
				refresh();
			});
		}
		set(49, new ItemBuilder(Material.PAPER).name(Theme.soft() + "Page " + (page + 1) + "/" + pages)
				.lore(Theme.light() + list.size() + " total").build());
		if (page + 1 < pages) {
			set(50, new ItemBuilder(Material.ARROW).name(Theme.main() + "Next page").build(), e -> {
				page++;
				refresh();
			});
		}
		buildExtra();
	}
}
