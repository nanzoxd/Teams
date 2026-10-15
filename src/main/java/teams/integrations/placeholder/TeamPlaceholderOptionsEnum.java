package teams.integrations.placeholder;

import teams.Team;
import teams.TeamPlayer;
import teams.integrations.placeholder.provider.*;

/**
 * @author booksaw
 */
public enum TeamPlaceholderOptionsEnum {
	NAME(new NamePlaceholderProvider()), TAG(new TagPlaceholderProvider()),
	DISPLAYNAME(new DisplayNamePlaceholderProvider()), DESCRIPTION(new DescriptionPlaceholderProvider()),
	OPEN(new OpenPlaceholderProvider()), SCORE(new ScorePlaceholderProvider()),
	RANK(new RankPlaceholderProvider()), COLOR(new ColorPlaceholderProvider()), TITLE(new TitlePlaceholderProvider()),
	ONLINELIST(new OnlineListPlaceholderProvider()), OFFLINELIST(new OfflineListPlaceholderProvider()),
	ONLINE(new OnlinePlaceholderProvider()), MEMBERS(new MembersPlaceholderProvider()), DEFAULTMEMBERS(new DefaultMembersPlaceholderProvider()),
	ADMINS(new AdminsPlaceholderProvider()), OWNERS(new OwnersPlaceholderProvider()),
	POSITIONSCORE(new PositionScorePlaceholderProvider()),
	POSITIONMEMBERS(new PositionMembersPlaceholderProvider()), LEVEL(new LevelPlaceholderProvider()),
	MAXMEMBERS(new MaxMembersPlaceholderProvider()), MAXWARPS(new MaxWarpsPlaceholderProvider()),
	PVP(new PvpPlaceholderProvider()),
	HASHOME(new HasHomePlaceholderProvider()),
	TEAMCHAT(new TeamChatPlaceholderProvider()),
	META(new MetaPlaceholderProvider()),
	ANCHOR(new AnchorPlaceholderProvider()),
	COLORNAME(new ColorNamePlaceholderProvider()),
	COLORHEX(new ColorHexPlaceholderProvider()),
	KILLS(new KillsPlaceholderProvider()),
	DEATHS(new DeathsPlaceholderProvider()),
	KDR(new KdrPlaceholderProvider()),
	PLAYTIME(new PlaytimePlaceholderProvider()),
	BELOWNAME(new BelowNamePlaceholderProvider());

	private final IndividualTeamPlaceholderProvider teamProvider;
	private final IndividualTeamPlayerPlaceholderProvider teamPlayerProvider;
	private final IndividualTeamWithDataPlaceholderProvider teamWithDataProvider;

	/**
	 * Constructor to take in an interface per enum value
	 */
	TeamPlaceholderOptionsEnum(IndividualTeamPlaceholderProvider teamProvider) {
		this.teamProvider = teamProvider;
		this.teamPlayerProvider = null;
		this.teamWithDataProvider = null;
	}

	/**
	 * Constructor to take in an interface per enum value
	 */
	TeamPlaceholderOptionsEnum(IndividualTeamPlayerPlaceholderProvider teamPlayerProvider) {
		this.teamPlayerProvider = teamPlayerProvider;
		this.teamProvider = null;
		this.teamWithDataProvider = null;
	}

	TeamPlaceholderOptionsEnum(IndividualTeamWithDataPlaceholderProvider teamWithDataProvider) {
		this.teamWithDataProvider = teamWithDataProvider;
		this.teamProvider = null;
		this.teamPlayerProvider = null;
	}

	public String applyPlaceholderProvider(Team team, TeamPlayer player) {
		if (teamProvider != null) {
			return teamProvider.getPlaceholderForTeam(team);
		}
		if (teamPlayerProvider != null && player != null) {
			return teamPlayerProvider.getPlaceholderForTeamPlayer(team, player);
		}

		return null;
	}

	public String applyPlaceholderProvider(Team team, TeamPlayer player, String data) {
		if (teamWithDataProvider != null) {
			return teamWithDataProvider.getPlaceholderForTeam(team, data);
		}
		// fallback
		return applyPlaceholderProvider(team, player);
	}

	public static TeamPlaceholderOptionsEnum getEnumValue(String str) {
		try {
			return TeamPlaceholderOptionsEnum.valueOf(str.toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public boolean requiresData() {
		return teamWithDataProvider != null;
	}
}
