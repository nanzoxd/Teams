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

	/** Every command that was added, also the switched off ones, so they can be turned on again without a restart */
	private final java.util.Map<String, SubCommand> registry = new java.util.LinkedHashMap<>();

	private boolean groupOff(String command) {
		String group = GROUPS.get(command);
		return group != null && !Main.plugin.getConfig().getBoolean("features." + group, true);
	}

	private void applyConfiguredRank(SubCommand command) {
		if (command instanceof TeamSubCommand && config.contains(command.getCommand() + ".rank")) {
			PlayerRank rank = PlayerRank.getRank(String.valueOf(config.getString(command.getCommand() + ".rank")));
			if (rank == null) {
				Main.plugin.getLogger().warning("The command " + command.getCommand()
						+ " has the rank set to something invalid, using the default rank");
			} else {
				((TeamSubCommand) command).setRequiredRank(rank);
			}
		}
	}

	@Override
	public void addSubCommand(SubCommand command) {
		registry.put(command.getCommand(), command);
		boolean off = groupOff(command.getCommand());

		if (config.isConfigurationSection(command.getCommand())) {
			if (config.getBoolean(command.getCommand() + ".enabled") && !off) {
				super.addSubCommand(command);
			}
			applyConfiguredRank(command);
		} else {
			// needs generating
			if (!off) {
				super.addSubCommand(command);
			}
			config.set(command.getCommand() + ".enabled", true);

			if (command instanceof TeamSubCommand) {
				config.set(command.getCommand() + ".rank", ((TeamSubCommand) command).getDefaultRank().toString());
			}

			yamlManager.save(false);
		}
	}

	/**
	 * Applies teampermissions.yml and the features.* groups right now (no restart or reload needed):
	 * turned off commands disappear (also from tab completion), turned on ones come back, ranks change.
	 */
	public void refreshLive() {
		for (SubCommand command : registry.values()) {
			boolean on = config.getBoolean(command.getCommand() + ".enabled", true) && !groupOff(command.getCommand());
			String key = getReference(command);
			boolean present = getSubCommands().get(key) == command;
			if (on && !present) {
				super.addSubCommand(command);
			} else if (!on && present) {
				getSubCommands().remove(key);
			}
			applyConfiguredRank(command);
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
