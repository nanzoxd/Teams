package teams.gui;

import teams.Main;

/**
 * The colours used by menus, dialogs and messages. They can be changed in the theme section of config.yml (read live, so /team reload applies them)
 */
public final class Theme {

	private Theme() {
	}

	/** Prefix, usage messages, menu navigation (theme.primary, #F1D58F) */
	public static String primary() {
		return legacy("theme.primary", "#F1D58F");
	}

	/** Important highlights (theme.secondary, #FFF0CC) */
	public static String secondary() {
		return legacy("theme.secondary", "#FFF0CC");
	}

	/** Command names, labels and success (theme.highlight, #FFDC73) */
	public static String highlight() {
		return legacy("theme.highlight", "#FFDC73");
	}

	/** Normal text (theme.text, #FFF1E0) */
	public static String text() {
		return legacy("theme.text", "#FFF1E0");
	}

	/** Hints, secondary information and errors (theme.accent, #FFD7CA) */
	public static String accent() {
		return legacy("theme.accent", "#FFD7CA");
	}

	/** Tick of a setting that is on (theme.on) */
	public static String on() {
		return legacy("theme.on", "#7CFF7C");
	}

	/** Cross of a setting that is off (theme.off) */
	public static String off() {
		return legacy("theme.off", "#FF6B6B");
	}

	// older names, kept so every menu follows the same five colours
	public static String main() {
		return primary();
	}

	public static String light() {
		return text();
	}

	public static String soft() {
		return accent();
	}

	public static String good() {
		return highlight();
	}

	public static String warn() {
		return highlight();
	}

	public static String bad() {
		return accent();
	}

	public static String info() {
		return primary();
	}

	public static String white() {
		return "\u00A7f";
	}

	private static String legacy(String path, String def) {
		String value = Main.plugin.getConfig().getString(path, def);
		return fromHex(value == null ? def : value, def);
	}

	/**
	 * @param hex a hex colour such as #F47F6A (the # is optional)
	 * @param def what to use if hex is not valid
	 * @return the colour in the §x§8§9... format
	 */
	public static String fromHex(String hex, String def) {
		String cleaned = hex.trim();
		if (cleaned.startsWith("#")) {
			cleaned = cleaned.substring(1);
		}
		if (!cleaned.matches("(?i)[0-9A-F]{6}")) {
			if (def == null || def.equals(hex)) {
				return "\u00A7f";
			}
			return fromHex(def, null);
		}
		StringBuilder builder = new StringBuilder("\u00A7x");
		for (char c : cleaned.toCharArray()) {
			builder.append('\u00A7').append(c);
		}
		return builder.toString();
	}
}
