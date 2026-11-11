package teams.util;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.bukkit.configuration.file.YamlConfiguration;

import teams.Main;

/**
 * Servers keep their own messages.yml, so text changed in a new version never reaches them.
 * Once (messagesVersion), the messages that changed are replaced with the new ones and the old announcements are removed.
 */
public final class MessagesMigration {

	private static final int VERSION = 4;
	private static final String[] KEYS = {"chat.syntax", "allychat.syntax", "invite.invite", "join.notify", "leave.notify",
			"disband.success", "disband.notify"};

	private MessagesMigration() {
	}

	public static void run(File file) {
		try {
			if (!file.exists()) {
				return;
			}
			YamlConfiguration current = YamlConfiguration.loadConfiguration(file);
			if (current.getInt("messagesVersion", 0) >= VERSION) {
				return;
			}
			var stream = Main.plugin.getResource("messages.yml");
			if (stream == null) {
				return;
			}
			YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
			for (String key : KEYS) {
				if (defaults.contains(key)) {
					current.set(key, defaults.get(key));
				}
			}
			for (String key : new ArrayList<>(current.getKeys(false))) {
				if (key.startsWith("announce.")) {
					current.set(key, null);
				}
			}
			// palette: every old colour becomes its new one, except the red name in chat and the join / leave arrows
			for (String key : current.getKeys(false)) {
				Object value = current.get(key);
				if (value instanceof String text && !key.equals("messagesVersion") && !java.util.Set.of(KEYS).contains(key)) {
					current.set(key, ThemeMigration.recolour(text));
				}
			}
			current.set("messagesVersion", String.valueOf(VERSION));
			current.save(file);
			Main.plugin.getLogger().info("messages.yml updated: new team chat, ally chat and invite text. Announcements removed. Colours moved to the new palette.");
		} catch (Exception e) {
			Main.plugin.getLogger().warning("Could not update messages.yml: " + e.getMessage());
		}
	}
}
