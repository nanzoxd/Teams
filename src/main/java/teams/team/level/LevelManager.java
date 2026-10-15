package teams.team.level;

import teams.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Level;

public class LevelManager {

	private static final Map<Integer, TeamLevel> levels = new HashMap<>();

	/**
	 * Reloads all levels from the config.yml file.
	 */
	public static void reload() {
		levels.clear();
		FileConfiguration config = Main.plugin.getConfig();
		ConfigurationSection section = config.getConfigurationSection("levels");

		// level 1 is made from the teams: section (members, warps, chests, admins, owners)
		levels.put(1, new TeamLevel(1,
				config.getInt("teams.maxMembers", 10),
				config.getInt("teams.maxChests", 2),
				config.getInt("teams.maxWarps", 2),
				config.getInt("teams.maxAdmins", 5),
				config.getInt("teams.maxOwners", 2),
				"0", new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>(config.getStringList("teams.rankLore"))));

		if (section == null) {
			return;
		}

		for (String key : section.getKeys(false)) {
			try	{
				int levelNum = Integer.parseInt(key.replace("l", ""));

				int teamLimit = section.getInt(key + ".maxMembers", section.getInt(key + ".teamLimit", 10));
				int maxChests = section.getInt(key + ".maxChests", 0);
				int maxWarps = section.getInt(key + ".maxWarps", 0);
				int maxAdmins = section.getInt(key + ".maxAdmins", -1);
				int maxOwners = section.getInt(key + ".maxOwners", 1);

				String price = section.getString(key + ".price", "0");

				List<String> startCmds = section.getStringList(key + ".startCommands");
				List<String> endCmds = section.getStringList(key + ".endCommands");
				List<String> lore = section.getStringList(key + ".rankLore");

				TeamLevel teamLevel = new TeamLevel(
						levelNum,
						teamLimit,
						maxChests,
						maxWarps,
						maxAdmins,
						maxOwners,
						price,
						startCmds,
						endCmds,
						lore
				);

				levels.put(levelNum, teamLevel);

			} catch (NumberFormatException e) {
				Main.plugin.getLogger().warning("Invalid level key format in config (expected l1, l2...): " + key);
			}
		}
	}


	/**
	 * Retrieves a specific level object.
	 * @param level The level number.
	 * @return The TeamLevel object
	 */
	public static TeamLevel getLevel(int level) {
		return levels.get(level);
	}

	public static TeamLevel getNextLevel(int currentLevel) {
		return levels.get(currentLevel + 1);
	}

	public static boolean hasNextLevel(int currentLevel) {
		return levels.containsKey(currentLevel + 1);
	}

	public static TeamLevel getFinalLevel() {
		return levels.get(getMaxLevel());
	}

	public static int getMaxLevel() {
		return levels.keySet().stream().mapToInt(v -> v).max().orElse(1);
	}

	public static boolean exists(int level) {
		return levels.containsKey(level);
	}

	/**
	 * Returns an unmodifiable view of all loaded levels.
	 * @return Map of Level ID -> TeamLevel
	 */
	public static Map<Integer, TeamLevel> getLevels() {
		return Collections.unmodifiableMap(levels);
	}

}
