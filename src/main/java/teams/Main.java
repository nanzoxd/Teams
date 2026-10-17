package teams;

import teams.message.BukkitAudiences;
import teams.dialog.TeamConfigCommand;
import teams.integrations.CelestCombatHook;
import teams.pull.CombatTracker;
import teams.pull.PullCommand;
import teams.commands.HelpCommand;
import teams.commands.ParentCommand;
import teams.commands.PermissionParentCommand;
import teams.commands.team.*;
import teams.commands.team.chest.ChestCheckCommand;
import teams.commands.team.chest.ChestClaimCommand;
import teams.commands.team.chest.ChestRemoveCommand;
import teams.commands.team.chest.ChestRemoveallCommand;
import teams.commands.teama.*;
import teams.commands.teama.chest.*;
import teams.commands.teama.meta.MetaGetTeama;
import teams.commands.teama.meta.MetaRemoveTeama;
import teams.commands.teama.meta.MetaSetTeama;
import teams.commands.teama.score.AddScore;
import teams.commands.teama.score.RemoveScore;
import teams.commands.teama.score.SetScore;
import teams.cooldown.CooldownManager;
import teams.cost.CostManager;
import teams.customEvents.post.PostTeamsReloadEvent;
import teams.gui.GuiManager;
import teams.stats.TeamStatsListener;
import teams.events.*;
import teams.events.MCTeamManagement.BelowNameType;
import teams.extension.ExtensionManager;
import teams.integrations.TabHook;
import teams.integrations.UltimateClaimsManager;
import teams.integrations.WorldGuardManagerV7;
import teams.integrations.apollo.ApolloManager;
import teams.integrations.hologram.DHHologramManager;
import teams.integrations.hologram.HDHologramManager;
import teams.integrations.hologram.HologramManager;
import teams.integrations.placeholder.TeamPlaceholders;
import teams.message.MessageManager;
import teams.score.ScoreManagement;
import teams.team.level.LevelManager;
import teams.team.storage.StorageType;
import teams.team.storage.convert.Converter;
import teams.team.storage.storageManager.SeparatedYamlStorageManager;
import teams.team.storage.storageManager.YamlStorageManager;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Main class of the plugin, extends JavaPlugin
 *
 * @author booksaw
 */
public class Main extends JavaPlugin {

	public static Main plugin;
	public static Economy econ = null;
	public static Permission perms = null;
	public static boolean placeholderAPI = false;
	public boolean useHolograms = false;
	public MCTeamManagement teamManagement;
	public StaffTab staffTab;
	public TabHook tabHook;
	public ChatManagement chatManagement;
	public WorldGuardManagerV7 wgManagement;
	@Getter
	private PermissionParentCommand teamCommand;

	@Getter
	private ParentCommand teamaCommand;

	@Getter
	private BooksawCommand teamBooksawCommand;

	@Getter
	private TeamPlaceholders teamPlaceholders;

	@Getter
	ExtensionManager extensionManager;

	/**
	 * If the ultimateClaims expansion has been enabled
	 */
	@Getter
	private boolean ultimateClaimsEnabled = false;


	/**
	 * This is used to store the config file in which the the teams data is stored
	 */
	FileConfiguration teams;
	private DamageManagement damageManagement;

	private HomeAnchorManagement homeAnchorManagement;

	private ConfigManager configManager;

	@Getter
	private BukkitAudiences adventure;

	public boolean isAdventure() {
		return adventure != null;
	}

	@Override
	public void onLoad() {
		plugin = this;
		configManager = new ConfigManager("config", true);

		if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null
				&& configManager.config.getBoolean("worldGuard.enabled")) {
			char ver = Bukkit.getPluginManager().getPlugin("WorldGuard").getDescription().getVersion().charAt(0);
			if (ver == '7') {
				wgManagement = new WorldGuardManagerV7();
			} else {
				Main.plugin.getLogger().warning("Your version of worldgaurd ("
						+ Bukkit.getPluginManager().getPlugin("WorldGuard").getDescription().getVersion()
						+ ") is not yet supported (Currently supported: version 7.x.x), the teams flags will not be usable");
			}
		}
	}

	@Override
	public void onEnable() {

		if (adventure == null) try {
			adventure = BukkitAudiences.create(this);
		} catch (Exception e) {
			getLogger().severe("Failed to create BukkitAudiences: " + e.getMessage());
			adventure = null;
		}

		MessageManager.setupMessageSender(adventure);

		loadCustomConfigs();

		LevelManager.reload();

		setupStorage();

		ChatManagement.enable();

		if (!new File(getDataFolder(), "tab-example.yml").exists()) {
			saveResource("tab-example.yml", false);
		}

		setupExtension();

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
				&& Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("PlaceholderAPI")).isEnabled()) {
			placeholderAPI = true;
			teamPlaceholders = new TeamPlaceholders(this);
			teamPlaceholders.register();
		}

		if (Bukkit.getPluginManager().getPlugin("UltimateClaims") != null
				&& Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("UltimateClaims")).isEnabled()) {
			if (getConfig().getBoolean("ultimateClaims.enabled")) {
				ultimateClaimsEnabled = true;
				new UltimateClaimsManager();
			}
		}

		useHolograms = setupHolograms();

		if (!setupEconomy() || !getConfig().getBoolean("useVault")) {
			econ = null;
		}

		if (!setupPermissions()) {
			perms = null;
		}

		setupCommands();
		setupListeners();
	}

	@Override
	public void onDisable() {

		if (extensionManager != null) {
			extensionManager.unloadExtensions();
		}

		for (Entry<Player, Team> temp : InventoryManagement.adminViewers.entrySet()) {
			temp.getKey().closeInventory();
			temp.getValue().saveEchest();
		}

		if (useHolograms) {
			HologramManager.holoManager.disable();
		}


		if (teamManagement != null) {
			teamManagement.removeAll(false);
			teamManagement = null;
		}

		if (tabHook != null) {
			tabHook.disable();
			tabHook = null;
		}

		if (staffTab != null) {
			staffTab.removeAll();
			staffTab = null;
		}

		if (homeAnchorManagement != null) {
			homeAnchorManagement.unregisterEvent();
			homeAnchorManagement = null;
		}

		HandlerList.unregisterAll(this); // unregister all Listeners
		Bukkit.getServer().getMessenger().unregisterIncomingPluginChannel(this);

		damageManagement = null;
		chatManagement = null;

		Team.disable();

		if (adventure != null) {
			adventure.close();
			adventure = null;
		}

		configManager = null;
	}

	public void loadCustomConfigs() {

		String language = getConfig().getString("language");
		if (language == null || language.isEmpty() || language.equals("en")) {
			language = "messages";
		}

		File f = new File(getDataFolder(), language + ".yml");

		try {
			if (!f.exists()) {
				saveResource(language + ".yml", false);
			}
		} catch (Exception e) {
			Main.plugin.getLogger().warning("Could not load selected language: " + language
					+ ", using the default language");
			Main.plugin.getLogger().warning("Reverting to english so the plugin can still function");
			language = "messages";
			if (!new File(getDataFolder(), "messages.yml").exists()) {
				saveResource("messages.yml", false);
			}
		}

		MessageManager.addMessages(language);

		if (!language.equals("messages")) {
			ConfigManager messagesConfigManager = new ConfigManager("messages", true);
			MessageManager.addBackupMessages(messagesConfigManager.config);
		}

		if (getConfig().getBoolean("combat.disableCombat")) {
			if (damageManagement == null) {
				damageManagement = new DamageManagement();
				getServer().getPluginManager().registerEvents(damageManagement, this);
			}

		} else {
			if (damageManagement != null) {
				Main.plugin.getLogger().log(Level.WARNING, "Restart server for damage changes to apply");
			}
		}

		// loading the fully custom help message option
		HelpCommand.setupHelp();

	}

	/*
	 * Determines which holograms plugin the server is running, then creates a new
	 * HologramManager instance for the respective plugin.
	 */
	private boolean setupHolograms() {
		boolean hdHolos = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
		if (hdHolos) {
			new HDHologramManager();
		}
		boolean dhHolos = Bukkit.getPluginManager().isPluginEnabled("DecentHolograms");
		// Check to make sure the server isn't running both hologram plugins.
		// We don't need two HologramManager instances.
		if (!hdHolos && dhHolos) {
			new DHHologramManager();
		}
		return hdHolos || dhHolos;
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return true;
	}

	private boolean setupPermissions() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}

		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
		if (rsp == null) {
			return false;
		}
		perms = rsp.getProvider();
		return perms != null;
	}

	public void reload() {
		getLogger().info("Starting Teams reload...");

		onDisable();

		reloadConfig();
		configManager = new ConfigManager("config", true);

		onEnable();
		Bukkit.getPluginManager().callEvent(new PostTeamsReloadEvent());

		getLogger().info("Teams reload complete.");
	}

	public void setupCommands() {
		TeamConfigCommand.register();
		PullCommand.register();
		teamCommand = new PermissionParentCommand(new CostManager("team"), new CooldownManager("team"), "team");
		// add all sub commands here
		teamCommand.addSubCommands(new CreateCommand(teamCommand), new LeaveCommand(), new DisbandCommand(),
				new DescriptionCommand(), new InviteCommand(), new JoinCommand(), new NameCommand(), new OpenCommand(),
				new InfoCommand(teamCommand), new KickCommand(), new PromoteCommand(), new DemoteCommand(),
				new HomeCommand(), new SethomeCommand(), new BanCommand(), new UnbanCommand(),
				new ChatCommand(teamCommand), new ColorCommand(), new TitleCommand(), new TopCommand(),
				new RankCommand(), new DelhomeCommand(), new AllyCommand(), new NeutralCommand(),
				new AllyChatCommand(teamCommand), new ListCommand(), new WarpCommand(), new SetWarpCommand(),
				new DelwarpCommand(), new WarpsCommand(), new EchestCommand(), new RankupCommand(), new TagCommand());

		if (getConfig().getBoolean("anchor.enable")) {
			teamCommand.addSubCommands(new AnchorCommand(), new SetAnchorCommand());
		}

		if (getConfig().getBoolean("combat.disableCombat")) {
			teamCommand.addSubCommand(new PvpCommand());
		}
		// only used if a team is only allowed a single owner
		if (getConfig().getBoolean("teams.singleOwner")) {
			teamCommand.addSubCommand(new SetOwnerCommand());
		}

		ParentCommand chest = new PermissionParentCommand("chest");
		chest.addSubCommands(new ChestClaimCommand(), new ChestRemoveCommand(), new ChestRemoveallCommand(), new ChestCheckCommand());
		teamCommand.addSubCommand(chest);

		teamBooksawCommand = new BooksawCommand("team", teamCommand, "teams.standard", "All commands for teams",
				getConfig().getStringList("command.team"));

		teamaCommand = new ParentCommand("teamadmin");

		teamaCommand.addSubCommands(new ReloadTeama(), new ChatSpyTeama(), new TitleTeama(),
				new VersionTeama("version"), new VersionTeama("debug"), new HomeTeama(), new NameTeama(),
				new DescriptionTeama(), new OpenTeama(), new InviteTeama(), new CreateTeama(), new JoinTeama(),
				new LeaveTeama(), new PromoteTeama(), new DemoteTeama(), new WarpTeama(), new SetwarpTeama(),
				new DelwarpTeama(), new PurgeTeama(), new DisbandTeama(), new ColorTeama(), new EchestTeama(),
				new SetrankTeama(teamaCommand), new TagTeama(), new TeleportTeama(teamaCommand), new AllyTeama(),
				new NeutralTeama(), new ImportmessagesTeama());


		if (getConfig().getBoolean("anchor.enable")) {
			teamaCommand.addSubCommands(new AnchorTeama(), new SetAnchorTeama());
		}

		if (getConfig().getBoolean("teams.singleOwner")) {
			teamaCommand.addSubCommand(new SetOwnerTeama());
		}

		ParentCommand teamaScoreCommand = new ParentCommand("score");
		teamaScoreCommand.addSubCommands(new AddScore(), new SetScore(), new RemoveScore());
		teamaCommand.addSubCommand(teamaScoreCommand);

		ParentCommand teamaChestCommand = new ParentCommand("chest");
		teamaChestCommand.addSubCommands(new ChestClaimTeama(), new ChestRemoveTeama(), new ChestRemoveallTeama(),
				new ChestEnableClaims(), new ChestDisableClaims());
		teamaCommand.addSubCommand(teamaChestCommand);

		ParentCommand teamaMetaCommand = new ParentCommand("meta");
		teamaMetaCommand.addSubCommands(new MetaSetTeama(), new MetaGetTeama(), new MetaRemoveTeama());
		teamaCommand.addSubCommand(teamaMetaCommand);

		if (useHolograms) {
			ParentCommand teamaHoloCommand = new ParentCommand("holo");
			teamaHoloCommand.addSubCommands(new CreateHoloTeama(), new RemoveHoloTeama());
			teamaCommand.addSubCommand(teamaHoloCommand);
		}

		new BooksawCommand("teamadmin", teamaCommand, "teams.admin", "All admin commands for teams",
				getConfig().getStringList("command.teama"));

	}

	public void setupListeners() {
		// display.tab off = the tab list and name tags are left completely normal
		// with the TAB plugin the tag is shown UNDER the name (TabHook), without it IN FRONT of the name
		if (tabHook == null && getConfig().getBoolean("tab.hook", true) && TabHook.isInstalled()) {
			tabHook = new TabHook();
			getServer().getPluginManager().registerEvents(tabHook, this);
			tabHook.enable();
		}
		BelowNameType type = tabHook == null && getConfig().getBoolean("display.tab.enabled", true)
				? BelowNameType.getType(getConfig().getString("display.tab.position", "prefix"))
				: BelowNameType.FALSE;
		// minecraft (scoreboard) teams are only created when they are actually needed, so Teams does not
		// get in the way of plugins like TAB
		boolean needsScoreboardTeams = type != BelowNameType.FALSE
				|| !getConfig().getBoolean("teamOptions.collide", true)
				|| getConfig().getBoolean("teamOptions.privateDeath", false)
				|| getConfig().getBoolean("teamOptions.privateName", false)
				|| getConfig().getBoolean("teamOptions.canSeeFriendlyInvisibles", false);
		if (needsScoreboardTeams) {
			if (teamManagement == null) {
				teamManagement = new MCTeamManagement(type);

				teamManagement.displayBelowNameForAll();
				getServer().getPluginManager().registerEvents(teamManagement, this);
				Main.plugin.getLogger().info("teamManagement declared: " + teamManagement);
			} else {
				Main.plugin.getLogger().info("Not loading management");
				if (teamManagement != null) {
					Main.plugin.getLogger().log(Level.WARNING, "Restart server for minecraft team changes to apply");
				}
			}
		}


		// staff (OPs / teams.admin) at the top of the tab list, in the staff colour
		if (staffTab == null && tabHook == null && getConfig().getBoolean("staff.enabled", true)) {
			staffTab = new StaffTab();
			getServer().getPluginManager().registerEvents(staffTab, this);
			staffTab.refreshAll();
		}

		getServer().getPluginManager().registerEvents((chatManagement = new ChatManagement()), this);
		getServer().getPluginManager().registerEvents(new GuiManager(), this);
		getServer().getPluginManager().registerEvents(new ScoreManagement(), this);
		getServer().getPluginManager().registerEvents(new TeamStatsListener(), this);
		getServer().getPluginManager().registerEvents(new AllyManagement(), this);
		getServer().getPluginManager().registerEvents(new MessagesManagement(), this);
		getServer().getPluginManager().registerEvents(new CombatTracker(), this);
		CelestCombatHook.register();
		getServer().getPluginManager().registerEvents(new PresenceManagement(), this);

		// disabling the chest checks (hoppers most importantly) to reduce needless
		// performance cost
		if (teamCommand.isEnabled("chest")) {
			getServer().getPluginManager().registerEvents(new ChestManagement(), this);
		}

		getServer().getPluginManager().registerEvents(new InventoryManagement(), this);
		getServer().getPluginManager().registerEvents(new RankupEvents(), this);
		if (getConfig().getBoolean("anchor.enable")) {
			homeAnchorManagement = new HomeAnchorManagement(this);
			homeAnchorManagement.registerEvent();
		}

		if (getConfig().getBoolean("apollo.teamview.enabled", true)) {
			new ApolloManager();
		}
	}

	@Override
	public @NotNull FileConfiguration getConfig() {
		return configManager.config;
	}

	public void setupStorage() {
		File f = new File("plugins/Teams/" + YamlStorageManager.TEAMLISTSTORAGELOC + ".yml");

		if (!f.exists()) {
			Main.plugin.saveResource("teams.yml", false);
		}

		YamlConfiguration teamStorage = YamlConfiguration.loadConfiguration(f);
		StorageType from = StorageType.getStorageType(teamStorage.getString("storageType", "FLATFILE"));
		StorageType to = StorageType.getStorageType(getConfig().getString("storageType", ""));

		if (from != to) {
			Converter converter = Converter.getConverter(from, to);

			if (converter == null) {
				Main.plugin.getLogger().info("Cannot convert to the selected storage type (" + to.toString()
						+ "), continuing with preexisting one (" + from.toString() + ")");
				to = from;
			} else {
				converter.convertStorage();
			}
		}

		Team.setupTeamManager(to);
		Team.getTeamManager().loadTeams();
	}

	public void setupExtension() {
		extensionManager = new ExtensionManager(this, new File(getDataFolder(), "extensions"));
		extensionManager.initializeExtensions();

		int enableTick = getConfig().getInt("extension.enableTick", 1);
		if (enableTick <= 0) {
			extensionManager.enableExtensions();
		} else {
			// Run later
			Bukkit.getScheduler().runTaskLater(this, () ->
					extensionManager.enableExtensions(), enableTick);
		}
	}
}
