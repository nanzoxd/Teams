package teams.settings;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import teams.Main;
import teams.Team;
import teams.TeamPlayer;

/**
 * Settings every player changes for themselves (playersettings.yml): team notices, chat on login,
 * the owner crown, the locator bar and muted teammates.
 */
public final class PlayerSettings {

	private static YamlConfiguration config;
	private static File file;

	private PlayerSettings() {
	}

	private static synchronized YamlConfiguration cfg() {
		if (config == null) {
			file = new File(Main.plugin.getDataFolder(), "playersettings.yml");
			config = YamlConfiguration.loadConfiguration(file);
		}
		return config;
	}

	private static void save() {
		try {
			cfg().save(file);
		} catch (IOException e) {
			Main.plugin.getLogger().warning("Could not save playersettings.yml: " + e.getMessage());
		}
	}

	public static boolean get(UUID id, String key, boolean def) {
		return cfg().getBoolean(id + "." + key, def);
	}

	public static void set(UUID id, String key, boolean value) {
		cfg().set(id + "." + key, value);
		save();
	}

	public static String getString(UUID id, String key, String def) {
		return cfg().getString(id + "." + key, def);
	}

	public static void setString(UUID id, String key, String value) {
		cfg().set(id + "." + key, value);
		save();
	}

	/** @return true if the viewer muted the target in team chat */
	public static boolean isMuted(UUID viewer, UUID target) {
		return cfg().getStringList(viewer + ".muted").contains(target.toString());
	}

	/** @return true if the target is muted after the change */
	public static boolean toggleMute(UUID viewer, UUID target) {
		List<String> list = new ArrayList<>(cfg().getStringList(viewer + ".muted"));
		boolean muted = !list.remove(target.toString());
		if (muted) {
			list.add(target.toString());
		}
		cfg().set(viewer + ".muted", list);
		save();
		return muted;
	}

	/** Puts the player in the chat they chose to start in (public, team or ally) */
	public static void applyChatMode(Player player) {
		String mode = getString(player.getUniqueId(), "chatMode", "public");
		Team team = Team.getTeam(player);
		if (team == null || mode.equals("public")) {
			return;
		}
		TeamPlayer teamPlayer = team.getTeamPlayer(player);
		if (teamPlayer == null) {
			return;
		}
		if (mode.equals("team")) {
			teamPlayer.setTeamChat(true);
			teamPlayer.setAllyChat(false);
		} else if (mode.equals("ally")) {
			teamPlayer.setAllyChat(true);
			teamPlayer.setTeamChat(false);
		}
	}
}
