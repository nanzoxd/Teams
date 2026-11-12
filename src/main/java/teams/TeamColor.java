package teams;

import lombok.Getter;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The colours a team can have. Every colour is a full hex colour (not one of the 16 vanilla colours).
 * <p>
 * Each colour is stored using a single letter (<code>code</code>) so it fits in the old one character
 * database column. Old teams that still have a vanilla colour code (0-9, a-f) are converted automatically.
 */
@Getter
public enum TeamColor {

	GREEN('A', "Green", "8EDC9A", Material.GREEN_DYE),
	BLUE('B', "Blue", "7FB0F5", Material.LIGHT_BLUE_DYE),
	RED('C', "Red", "F28B82", Material.RED_DYE),
	PINK('D', "Pink", "F8B4D9", Material.PINK_DYE),
	PURPLE('E', "Purple", "B48CF0", Material.PURPLE_DYE),
	YELLOW('F', "Yellow", "FFF1A8", Material.YELLOW_DYE),
	ORANGE('G', "Orange", "FFC48A", Material.ORANGE_DYE),
	CYAN('H', "Cyan", "8EEBEB", Material.CYAN_DYE),
	ROSE('I', "Rose", "E78FA0", Material.PINK_TULIP),
	BROWN('J', "Brown", "C9A27E", Material.BROWN_DYE),
	WHITE('K', "White", "F8F8F8", Material.WHITE_DYE),
	BLACK('L', "Black", "7A7A80", Material.BLACK_DYE),
	GRAY('M', "Gray", "C4C7CC", Material.LIGHT_GRAY_DYE),
	NAVY('N', "Navy", "7D8BDB", Material.BLUE_DYE),
	MINT('O', "Mint", "B5F2D8", Material.PRISMARINE_SHARD),
	LAVENDER('P', "Lavender", "DCCDF5", Material.ALLIUM),
	GOLD('Q', "Gold", "E6C25C", Material.GOLD_INGOT),
	PEACH('R', "Peach", "FFCBB0", Material.HONEYCOMB),
	SKY('S', "Sky", "B6E6FF", Material.HEART_OF_THE_SEA),
	LIME('T', "Lime", "CDEB8B", Material.LIME_DYE);

	/**
	 * The single letter used to store this colour
	 */
	private final char code;
	/**
	 * The pretty name of the colour (i.e. Sky)
	 */
	private final String displayName;
	/**
	 * The hex of the colour, without the #
	 */
	private final String hex;
	/**
	 * The item that represents this colour in the gui
	 */
	private final Material icon;

	TeamColor(char code, String displayName, String hex, Material icon) {
		this.code = code;
		this.displayName = displayName;
		this.hex = hex;
		this.icon = icon;
	}

	/**
	 * @return the lower case name used in commands (i.e. sky)
	 */
	public String getId() {
		return name().toLowerCase(Locale.ROOT);
	}

	/**
	 * @return the hex with a hash (i.e. #87CEEB)
	 */
	public String getHexWithHash() {
		return "#" + hex;
	}

	/**
	 * @return the colour as a java colour
	 */
	public Color getAwtColor() {
		return Color.decode(getHexWithHash());
	}

	/**
	 * @return the colour as a bungee chat colour
	 */
	public net.md_5.bungee.api.ChatColor asBungee() {
		return net.md_5.bungee.api.ChatColor.of(getHexWithHash());
	}

	/**
	 * @return the colour as the legacy section sign hex format (§x§8§7§C§E§E§B), usable in any minecraft text
	 */
	public String toLegacy() {
		StringBuilder builder = new StringBuilder("\u00A7x");
		for (char c : hex.toCharArray()) {
			builder.append('\u00A7').append(c);
		}
		return builder.toString();
	}

	/**
	 * @return the colour in the &amp;#RRGGBB format, which is understood by the message system and the TAB plugin
	 */
	public String toAmpersandHex() {
		return "&#" + hex;
	}

	/**
	 * @return the colour as an adventure (mini message) tag
	 */
	public String toAdventureOpen() {
		return "<color:#" + hex + ">";
	}

	/**
	 * @return the closing adventure (mini message) tag
	 */
	public String toAdventureClose() {
		return "</color>";
	}

	@Override
	public String toString() {
		return toLegacy();
	}

	/**
	 * @return a random colour
	 */
	public static @NotNull TeamColor random() {
		TeamColor[] all = values();
		return all[ThreadLocalRandom.current().nextInt(all.length)];
	}

	/**
	 * Get a colour from what a player typed (name, or hex with or without a hash)
	 *
	 * @param input what the player typed
	 * @return the colour, or null if there is none
	 */
	public static @Nullable TeamColor fromInput(@Nullable String input) {
		if (input == null || input.isEmpty()) {
			return null;
		}
		String cleaned = input.trim().toLowerCase(Locale.ROOT);
		for (TeamColor color : values()) {
			if (color.getId().equals(cleaned)) {
				return color;
			}
		}
		String hexInput = cleaned.startsWith("#") ? cleaned.substring(1) : cleaned;
		for (TeamColor color : values()) {
			if (color.hex.equalsIgnoreCase(hexInput)) {
				return color;
			}
		}
		return null;
	}

	/**
	 * Loads a colour from storage. Old (vanilla) colour codes are converted to the closest new colour.
	 *
	 * @param stored the stored value
	 * @return the colour, or a random colour if the stored value is not valid
	 */
	public static @NotNull TeamColor fromStorage(@Nullable String stored) {
		if (stored == null || stored.isEmpty()) {
			return random();
		}
		char first = stored.charAt(0);

		for (TeamColor color : values()) {
			if (color.code == first) {
				return color;
			}
		}

		// old data: the 16 vanilla colour codes
		switch (Character.toLowerCase(first)) {
			case '0':
				return BLACK;
			case '1':
				return NAVY;
			case '2':
				return GREEN;
			case '3':
				return CYAN;
			case '4':
				return RED;
			case '5':
				return PURPLE;
			case '6':
				return GOLD;
			case '7':
			case '8':
				return GRAY;
			case '9':
				return BLUE;
			case 'a':
				return LIME;
			case 'b':
				return SKY;
			case 'c':
				return ROSE;
			case 'd':
				return PINK;
			case 'e':
				return YELLOW;
			case 'f':
				return WHITE;
			default:
				return random();
		}
	}

	/**
	 * @return the ids of every colour that is not in the banned list
	 */
	public static List<String> getIds(List<String> banned) {
		List<String> ids = new ArrayList<>();
		for (TeamColor color : values()) {
			if (banned == null || banned.stream().noneMatch(b -> b.equalsIgnoreCase(color.getId()))) {
				ids.add(color.getId());
			}
		}
		return ids;
	}
}
