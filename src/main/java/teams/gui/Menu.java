package teams.gui;

import teams.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The base of every menu. Every menu is 6 rows (54 slots) unless it says otherwise.
 */
public abstract class Menu implements InventoryHolder {

	protected final Player player;
	private Inventory inventory;
	private final Map<Integer, Consumer<InventoryClickEvent>> actions = new HashMap<>();

	protected Menu(Player player) {
		this.player = player;
	}

	/**
	 * @return the title of the menu
	 */
	protected abstract String title();

	/**
	 * @return the number of slots (must be a multiple of 9)
	 */
	protected int size() {
		return 54;
	}

	/**
	 * Place the items of the menu, using set(...)
	 */
	protected abstract void build();

	public void open() {
		inventory = Bukkit.createInventory(this, size(), title());
		refresh();
		player.openInventory(inventory);
	}

	/**
	 * Rebuilds the items without closing the menu
	 */
	public void refresh() {
		actions.clear();
		inventory.clear();
		ItemStack filler = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).name(" ").build();
		for (int i = 0; i < inventory.getSize(); i++) {
			inventory.setItem(i, filler);
		}
		build();
	}

	protected void set(int slot, ItemStack item) {
		inventory.setItem(slot, item);
	}

	protected void set(int slot, ItemStack item, Consumer<InventoryClickEvent> action) {
		inventory.setItem(slot, item);
		actions.put(slot, action);
	}

	/**
	 * A back button that opens another menu
	 */
	protected void setBack(int slot, Supplier<Menu> previous) {
		set(slot, new ItemBuilder(Material.ARROW).name(Theme.info() + "Back").build(), e -> previous.get().open());
	}

	protected void setClose(int slot) {
		set(slot, new ItemBuilder(Material.BARRIER).name(Theme.bad() + "Close").build(), e -> player.closeInventory());
	}

	public void handleClick(InventoryClickEvent event) {
		Consumer<InventoryClickEvent> action = actions.get(event.getRawSlot());
		if (action != null) {
			action.accept(event);
		}
	}

	/**
	 * Runs on the next tick (it is not safe to change inventories while a click is being handled)
	 */
	protected void later(Runnable runnable) {
		Bukkit.getScheduler().runTask(Main.plugin, runnable);
	}

	/**
	 * Runs a command as the player and then reopens a menu, so the player sees the change
	 */
	protected void runAndReopen(String command, Supplier<Menu> next) {
		later(() -> {
			player.performCommand(command);
			Bukkit.getScheduler().runTaskLater(Main.plugin, () -> {
				if (player.isOnline()) {
					next.get().open();
				}
			}, 4L);
		});
	}

	/**
	 * Runs a command as the player and closes the menu, so the player can read the answer
	 */
	protected void runAndClose(String command) {
		later(() -> {
			player.closeInventory();
			player.performCommand(command);
		});
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inventory;
	}
}
