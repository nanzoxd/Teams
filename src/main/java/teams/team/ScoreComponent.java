package teams.team;

import teams.Main;
import teams.team.storage.team.StoredTeamValue;

public class ScoreComponent extends IntTeamComponent {

	final int minScore;

	public ScoreComponent() {
		super();
		minScore = Main.plugin.getConfig().getInt("minScore");
	}

	@Override
	public StoredTeamValue getSectionHeading() {
		return StoredTeamValue.SCORE;
	}

	@Override
	public void set(Integer value) {
		if (value < minScore) {
			super.set(minScore);
		} else {
			super.set(value);
		}
	}

}
