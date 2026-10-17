package teams.commands;

import teams.ConfigManager;
import teams.Main;
import teams.PlayerRank;
import teams.commands.presets.TeamSubCommand;
import teams.cooldown.CooldownManager;
import teams.cost.CostManager;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Objects;

public class PermissionParentCommand extends ParentCommand {

	ConfigManager yamlManager;
	YamlConfiguration config;

	public PermissionParentCommand(String command) {
		super(command);

		loadConfig(command);
	}

	public PermissionParentCommand(CostManager prices, CooldownManager cooldowns, String command) {
		super(prices, cooldowns, command, false);
		loadConfig(command);
	}

	private void loadConfig(String command) {
		yamlManager = new ConfigManager(command + "permissions", false);
		config = yamlManager.config;
	}

	/** Switching one of these off in config.yml (features.*) turns off every command of the group */
	private static final java.util.Map<String, String> GROUPS = java.util.Map.ofEntries(
			java.util.Map.entry("ally", "allies"), java.util.Map.entry("neutral", "allies"),
			java.util.Map.entry("allychat", "allies"),
			java.util.Map.entry("warp", "warps"), java.util.Map.entry("setwarp", "warps"),
			java.util.Map.entry("delwarp", "warps"), java.util.Map.entry("warps", "warps"),
			java.util.Map.entry("home", "homes"), java.util.Map.entry("sethome", "homes"),
			java.util.Map.entry("delhome", "homes"),
			java.util.Map.entry("chest", "chests"), java.util.Map.entry("echest", "chests"));

	@Override
	public void addSubCommand(SubCommand command) {
		String group = GROUPS.get(command.getCommand());
		if (group != null && !Main.plugin.getConfig().getBoolean("features." + group, true)) {
			return;
		}

		if (config.isConfigurationSection(command.getCommand())) {
			// already exists
			// checking if it is enabled
			if (config.getBoolean(command.getCommand() + ".enabled")) {
				super.addSubCommand(command);

				if (command instanceof TeamSubCommand) {
					PlayerRank rank = PlayerRank
							.getRank(Objects.requireNonNull(config.getString(command.getCommand() + ".rank")));
					if (rank == null) {
						Main.plugin.getLogger().warning("The command " + command.getCommand()
								+ " has the rank set to something invalid, using the default rank");
					} else {
						((TeamSubCommand) command).setRequiredRank(rank);
					}
				}

			}
		} else {
			// needs generating
			super.addSubCommand(command);
			config.set(command.getCommand() + ".enabled", true);

			if (command instanceof TeamSubCommand) {
				config.set(command.getCommand() + ".rank", ((TeamSubCommand) command).getDefaultRank().toString());
			}

			yamlManager.save(false);

		}
	}

	/** The loaded teampermissions.yml */
	public YamlConfiguration getPermissionConfig() {
		return config;
	}

	public void savePermissions() {
		yamlManager.save(false);
	}

	/** Changes the rank a command needs right now (the file is saved separately) */
	public void applyRank(String command, PlayerRank rank) {
		for (SubCommand sub : getSubCommands().values()) {
			if (sub instanceof TeamSubCommand && sub.getCommand().equals(command)) {
				((TeamSubCommand) sub).setRequiredRank(rank);
			}
		}
	}

	public boolean isEnabled(String command) {
		return config.getBoolean(command + ".enabled");
	}

}
