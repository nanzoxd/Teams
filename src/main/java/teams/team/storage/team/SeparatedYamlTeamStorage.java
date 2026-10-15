package teams.team.storage.team;

import teams.ConfigManager;
import teams.Main;
import teams.Team;
import teams.team.storage.storageManager.SeparatedYamlStorageManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class SeparatedYamlTeamStorage extends YamlTeamStorage {

	/**
	 * The path to the folder which contains all team information
	 */
	public static final String TEAMSAVEPATH = "teamInfo" + File.separator;

	public static File getTeamSaveFile() {
		return new File(Main.plugin.getDataFolder() + File.separator + TEAMSAVEPATH);
	}

	protected final FileConfiguration config;
	protected final ConfigManager configManager;

	public SeparatedYamlTeamStorage(Team team, SeparatedYamlStorageManager teamStorage) {
		super(team, teamStorage);

		configManager = new ConfigManager("teamInfo", TEAMSAVEPATH + team.getID().toString());
		config = configManager.config;
	}

	@Override
	public ConfigurationSection getConfig() {
		return config;
	}

	@Override
	protected void saveFile() {
		if (Main.placeholderAPI) {
			Main.plugin.getTeamPlaceholders().invalidateCache();
		}
		configManager.save(false);
	}

}
