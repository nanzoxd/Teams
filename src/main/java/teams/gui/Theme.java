package teams.gui;

import teams.Main;
import teams.TeamColor;

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
	 * @return the colour for normal text (#FFE4D5)
	 */
	public static String light() {
		return legacy("theme.light", "#FFE4D5");
	}

	/**
	 * @return the colour for highlights and borders (#FAB2A0)
	 */
	public static String soft() {
		return legacy("theme.soft", "#FAB2A0");
	}

	/**
	 * @return the colour for errors and dangerous buttons
	 */
	public static String bad() {
		return TeamColor.RED.toLegacy();
	}

	/**
	 * @return the colour for good things (on / yes)
	 */
	public static String good() {
		return TeamColor.GREEN.toLegacy();
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
