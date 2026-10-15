package teams.util;

import teams.Main;

/**
 * Turns letters into small capitals ("Niga" becomes "ɴɪɢᴀ"). Used for the team name / tag shown with the
 * player name (in front of it, or under it with TAB). Turn it off with display.smallCaps in config.yml.
 */
public final class SmallCaps {

	private static final String SMALL = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢ";

	private SmallCaps() {
	}

	public static String apply(String text) {
		if (text == null || !Main.plugin.getConfig().getBoolean("display.smallCaps", true)) {
			return text;
		}
		StringBuilder out = new StringBuilder(text.length());
		for (char c : text.toCharArray()) {
			char lower = Character.toLowerCase(c);
			out.append(lower >= 'a' && lower <= 'z' ? SMALL.charAt(lower - 'a') : c);
		}
		return out.toString();
	}
}
