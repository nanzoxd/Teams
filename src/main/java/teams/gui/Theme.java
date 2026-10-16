package teams.gui;

import teams.Main;

/**
 * The salmon colours used by the menus. They can be changed in the theme section of config.yml
 */
public final class Theme {

	private Theme() {
	}

	/**
	 * @return the colour for important text (#F47F6A)
	 */
	public static String main() {
		return legacy("theme.main", "#F47F6A");
	}

	/**
	 * @return the colour for normal text (#FFFFFF)
	 */
	public static String light() {
		return legacy("theme.light", "#FFFFFF");
	}

	/**
	 * @return the colour for highlights and borders (#FAB2A0)
	 */
	public static String soft() {
		return legacy("theme.soft", "#FAB2A0");
	}

	/**
	 * @return red (#FF2E4A): errors, delete, no, off
	 */
	public static String bad() {
		return legacy("theme.bad", "#FF2E4A");
	}

	/**
	 * @return green (#2EFF2E): create, add, yes, on
	 */
	public static String good() {
		return legacy("theme.good", "#2EFF2E");
	}

	/**
	 * @return yellow (#FFFF5C): edit and settings buttons
	 */
	public static String warn() {
		return legacy("theme.warn", "#FFFF5C");
	}

	/**
	 * @return blue (#00C3FF): buttons that open another menu
	 */
	public static String info() {
		return legacy("theme.info", "#00C3FF");
	}

	/**
	 * @return orange (#FF6A00): tools like reload and teleport
	 */
	public static String accent() {
		return legacy("theme.accent", "#FF6A00");
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
