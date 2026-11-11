package teams.util;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * One time move of an old config.yml to the cream gold palette (theme.version 2).
 * The file is edited as text so comments and layout stay.
 */
public final class ThemeMigration {

	/** Old colour -> new colour. Also used for messages.yml. */
	public static final Map<String, String> COLOURS = Map.ofEntries(
			Map.entry("FFE4D5", "FFF1E0"), Map.entry("FF2E4A", "FFD7CA"), Map.entry("F47F6A", "FFDC73"),
			Map.entry("2EFF2E", "FFDC73"), Map.entry("FAB2A0", "FFD7CA"), Map.entry("FFFFFF", "FFF1E0"),
			Map.entry("FF6A00", "FFF0CC"), Map.entry("FFFF5C", "FFDC73"), Map.entry("FFA500", "F1D58F"),
			Map.entry("B8B8B8", "FFD7CA"), Map.entry("00C3FF", "F1D58F"), Map.entry("FFFBF0", "FFF0CC"),
			Map.entry("FFCFCA", "FFD7CA"), Map.entry("A7C7E7", "F1D58F"), Map.entry("89CFF0", "F1D58F"));

	private static final Pattern HEX = Pattern.compile("&#([0-9A-Fa-f]{6})");

	private static final String THEME = "theme:\n  version: 2\n"
			+ "  primary: '#F1D58F'         # prefix, usage messages, menu navigation\n"
			+ "  secondary: '#FFF0CC'       # important highlights\n"
			+ "  highlight: '#FFDC73'       # command names, labels, success\n"
			+ "  text: '#FFF1E0'            # normal text\n"
			+ "  accent: '#FFD7CA'          # hints, secondary info, errors\n"
			+ "  on: '#7CFF7C'              # tick in settings that are on\n"
			+ "  off: '#FF6B6B'             # cross in settings that are off\n"
			+ "  helpCommand: '#FFDC73'\n  helpDescription: '#FFF1E0'\n"
			+ "prefixFormat: '&#F1D58F[Teams] &#FFF1E0'   # in front of plugin messages (MiniMessage like <#F1D58F> also works)\n";

	private ThemeMigration() {
	}

	/** Replaces every &#RRGGBB that is part of the old palette */
	public static String recolour(String text) {
		Matcher m = HEX.matcher(text);
		StringBuilder out = new StringBuilder();
		while (m.find()) {
			String to = COLOURS.get(m.group(1).toUpperCase());
			m.appendReplacement(out, Matcher.quoteReplacement("&#" + (to == null ? m.group(1) : to)));
		}
		m.appendTail(out);
		return out.toString();
	}

	/** Runs before the config is loaded */
	public static void run(File dataFolder) {
		File file = new File(dataFolder, "config.yml");
		try {
			if (!file.exists()) {
				return;
			}
			if (YamlConfiguration.loadConfiguration(file).getInt("theme.version", 0) >= 2) {
				return;
			}
			String text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
			text = recolour(text);
			Matcher block = Pattern.compile("(?s)theme:\\n.*?\\nprefixFormat:[^\\n]*\\n").matcher(text);
			if (block.find()) {
				text = block.replaceFirst(Matcher.quoteReplacement(THEME));
			}
			Files.writeString(file.toPath(), text, StandardCharsets.UTF_8);
		} catch (Exception e) {
			System.out.println("[Teams] Could not update the theme in config.yml: " + e.getMessage());
		}
	}
}
