package teams.team.level;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a specific level of a team.
 * This object holds all configuration data for a level.
 */
@Getter
@RequiredArgsConstructor
public class TeamLevel {

	private final int level;

	// --- Limits & Features ---
	private final int teamLimit;
	private final int maxChests;
	private final int maxWarps;
	private final int maxAdmins;
	private final int maxOwners;

	// Upgrades
	/**
	 * The raw price string from config (the amount of score needed, e.g. "100" or "100s").
	 * @see #getCostValue()
	 */
	private final String price;

	// Commands
	private final List<String> startCommands;
	private final List<String> endCommands;

	private final List<String> rankLore;


	public double getCostValue() {
		if (price == null || price.isEmpty()) return 0;
		try {
			String number = price.trim();
			char last = number.charAt(number.length() - 1);
			if (Character.isLetter(last)) {
				number = number.substring(0, number.length() - 1);
			}
			return Double.parseDouble(number);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Levels are always bought with score points.
	 *
	 * @return true
	 */
	public boolean isScoreCost() {
		return true;
	}

	/**
	 * Returns the lore with translated color codes.
	 * @return List of colored strings.
	 */
	public List<String> getColoredLore() {
		List<String> colored = new ArrayList<>();
		if (rankLore != null) {
			for (String line : rankLore) {
				colored.add(ChatColor.translateAlternateColorCodes('&', line));
			}
		}
		return colored;
	}
}
