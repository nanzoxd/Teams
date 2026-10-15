package teams.gui;

import teams.Main;
import teams.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Handles clicks in the menus, and the "type it in chat" questions some buttons ask
 */
public class GuiManager implements Listener {

	private static final long PROMPT_TIMEOUT_TICKS = 20L * 60;
	private static GuiManager instance;

	private final Map<UUID, Prompt> prompts = new ConcurrentHashMap<>();

	public GuiManager() {
		instance = this;
	}

	public static GuiManager getInstance() {
		return instance;
	}

	/**
	 * @return true if /team should open the menu
	 */
	public static boolean isEnabled() {
		return instance != null && Main.plugin.getConfig().getBoolean("gui.enabled", true);
	}

	/**
	 * @return true if /teamadmin should open the admin menu
	 */
	public static boolean isAdminEnabled() {
		return isEnabled() && Main.plugin.getConfig().getBoolean("gui.admin", true);
	}

	/**
	 * Opens the main menu for the player
	 */
	public static void open(Player player) {
		new MainMenu(player).open();
	}

	/**
	 * Closes the menu, asks the player to type something in chat and gives it to the consumer.
	 *
	 * @param player   the player
	 * @param question what the player has to type, i.e. "the new team name"
	 * @param onInput  gets what the player typed (runs on the main thread)
	 * @param onCancel what to do if they type cancel
	 */
	public void prompt(Player player, String question, Consumer<String> onInput, Runnable onCancel) {
		Prompt prompt = new Prompt(onInput, onCancel);
		prompts.put(player.getUniqueId(), prompt);
		player.closeInventory();
		MessageManager.sendFullMessage(player, Theme.soft() + "\u2192 " + Theme.main() + "Type " + question
				+ Theme.light() + " in chat. Type " + Theme.main() + "cancel" + Theme.light() + " to go back.", false);

		Bukkit.getScheduler().runTaskLater(Main.plugin, () -> {
			if (prompts.remove(player.getUniqueId(), prompt) && player.isOnline()) {
				MessageManager.sendFullMessage(player, Theme.light() + "The question timed out.", false);
			}
		}, PROMPT_TIMEOUT_TICKS);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent event) {
		Prompt prompt = prompts.remove(event.getPlayer().getUniqueId());
		if (prompt == null) {
			return;
		}
		event.setCancelled(true);
		String message = event.getMessage().trim();

		Bukkit.getScheduler().runTask(Main.plugin, () -> {
			if (message.equalsIgnoreCase("cancel") || message.isEmpty()) {
				prompt.onCancel.run();
			} else {
				prompt.onInput.accept(message);
			}
		});
	}

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (!(event.getView().getTopInventory().getHolder() instanceof Menu)) {
			return;
		}
		// nothing can be taken out of or put into a menu
		event.setCancelled(true);

		if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
			return;
		}
		((Menu) event.getView().getTopInventory().getHolder()).handleClick(event);
	}

	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		if (event.getView().getTopInventory().getHolder() instanceof Menu) {
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent event) {
		prompts.remove(event.getPlayer().getUniqueId());
	}

	private static class Prompt {
		private final Consumer<String> onInput;
		private final Runnable onCancel;

		private Prompt(Consumer<String> onInput, Runnable onCancel) {
			this.onInput = onInput;
			this.onCancel = onCancel;
		}
	}
}
