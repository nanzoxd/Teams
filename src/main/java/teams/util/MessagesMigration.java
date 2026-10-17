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

	private static final int VERSION = 2;
	private static final String[] KEYS = {"chat.syntax", "allychat.syntax", "invite.invite"};

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
			current.set("messagesVersion", String.valueOf(VERSION));
			current.save(file);
			Main.plugin.getLogger().info("messages.yml updated: new team chat, ally chat and invite text. Announcements removed.");
		} catch (Exception e) {
			Main.plugin.getLogger().warning("Could not update messages.yml: " + e.getMessage());
		}
	}
}
