package teams.team;

import teams.Team;
import teams.team.storage.team.TeamStorage;
import teams.util.Notify;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AllyRequestComponent extends UuidSetComponent {

	@Override
	public void load(TeamStorage section) {
		load(section.getAllyRequestList());
	}

	@Override
	public void save(TeamStorage storage) {
		storage.setAllyRequestList(getConvertedList());
	}

	@Override
	public String getSectionHeading() {
		return "allyrequests";
	}

	@Override
	public void add(Team team, UUID component) {
		super.add(team, component);

		Team t = Team.getTeam(component);
		// notifying all online owners of the team
		for (Player online : team.getMembers().getOnlinePlayers()) {
			Notify.sendClickable(online, "ally.request", "ally.hover",
					"/team " + Notify.command("ally") + " " + t.getName(), t.getDisplayName());
			Notify.sound(online, "ally");
		}

		team.getStorage().addAllyRequest(component);

	}

	@Override
	public void remove(Team team, UUID component) {
		super.remove(team, component);

		team.getStorage().removeAllyRequest(component);
	}

}
