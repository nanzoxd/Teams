package teams.cost;

import teams.Main;
import teams.Team;
import lombok.Getter;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.entity.Player;

@Getter
public class CommandCost {

	private final double cost;
	private final String command;

	/**
	 * Used within Cost manager to track a new commands Cost
	 *
	 * @param command the reference for the command
	 * @param cost    how long of a Cost that command has (in seconds)
	 */
	public CommandCost(String command, double cost) {
		this.command = command;
		this.cost = cost;
	}

	/**
	 * Run when a player runs the command to subtract the cost of the command
	 *
	 * @param player the player to add a Cost for
	 * @return If the player can run the command (if the transaction is a success or
	 * failure)
	 */
	public boolean runCommand(Player player) {
		if (player.hasPermission("teams.cost.bypass")) {
			return true;
		}

		if (Main.econ == null) {
			Main.plugin.getLogger().warning("Could not detect vault, command running with no cost");
			return true;
		}
		EconomyResponse response = Main.econ.withdrawPlayer(player, cost);
		return response.transactionSuccess();
	}

	/**
	 * @param player the player to check for
	 * @return if a player has the money to run that command (to check before
	 * executing the command)
	 */
	public boolean hasBalance(Player player) {
		if (player.hasPermission("teams.cost.bypass")) {
			return true;
		}

		if (Main.econ == null) {
			Main.plugin.getLogger().warning("Could not detect vault, command running with no cost");
			return true;
		}

		return Main.econ.has(player, cost);
	}

}
