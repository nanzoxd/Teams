package teams.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import teams.Main;
import teams.message.MessageManager;
import teams.text.Formatter;

import java.util.Collection;

/**
 * Small helpers for the "sound + click" messages (create / join / leave / disband / invite / ally request).
 * Team chat messages never use these, so they stay silent.
 */
public final class Notify {

	private Notify() {
	}

	/**
	 * Plays the sound configured under {@code sounds.<key>} in config.yml to the player.
	 * Does nothing if sounds are turned off or the sound is empty / "none".
	 */
	public static void sound(@Nullable Player player, @NotNull String key) {
		if (player == null || !player.isOnline()) {
			return;
		}
		if (!Main.plugin.getConfig().getBoolean("sounds.enabled", true)) {
			return;
		}
		ConfigurationSection section = Main.plugin.getConfig().getConfigurationSection("sounds." + key);
		if (section == null) {
			return;
		}
		String name = section.getString("sound", "");
		if (name == null || name.isBlank() || name.equalsIgnoreCase("none")) {
			return;
		}
		float volume = (float) section.getDouble("volume", 1.0);
		float pitch = (float) section.getDouble("pitch", 1.0);
		try {
			player.playSound(player.getLocation(), name.trim().toLowerCase(), volume, pitch);
		} catch (Exception e) {
			Main.plugin.getLogger().warning("Could not play sound '" + name + "' (sounds." + key + ")");
		}
	}

	public static void sound(@NotNull Collection<? extends Player> players, @NotNull String key) {
		for (Player player : players) {
			sound(player, key);
		}
	}

	/**
	 * The (translated) sub command name, e.g. "join".
	 */
	public static String command(@NotNull String name) {
		String translated = MessageManager.getMessage("command." + name);
		return (translated == null || translated.isEmpty()) ? name : translated;
	}

	/**
	 * Sends a message from messages.yml that runs {@code command} when it is clicked.
	 *
	 * @param hoverReference messages.yml reference shown when hovering (gets the same replacements)
	 */
	public static void sendClickable(@NotNull Player player, @NotNull String reference, @NotNull String hoverReference,
									 @NotNull String command, Object... replacements) {
		String text = MessageManager.getMessage(player, reference, replacements);
		if (text.isEmpty()) {
			return;
		}
		Component component = Formatter.absolute().process(text)
				.clickEvent(ClickEvent.runCommand(command));

		String hover = MessageManager.getMessage(player, hoverReference, replacements);
		if (!hover.isEmpty()) {
			component = component.hoverEvent(HoverEvent.showText(Formatter.absolute().process(hover)));
		}
		MessageManager.sendFullMessage(player, component, true);
	}
}
