package teams.message;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import net.kyori.adventure.audience.Audience;

/** Paper already ships Adventure, so every CommandSender is an Audience. Drop in replacement for the platform lib. */
public final class BukkitAudiences {

	private BukkitAudiences() {
	}

	public static BukkitAudiences create(Plugin plugin) {
		return new BukkitAudiences();
	}

	public Audience sender(CommandSender sender) {
		return sender;
	}

	public Audience player(Player player) {
		return player;
	}

	public Audience filter(Predicate<CommandSender> filter) {
		List<Audience> list = new ArrayList<>();
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (filter.test(p)) list.add(p);
		}
		if (filter.test(Bukkit.getConsoleSender())) list.add(Bukkit.getConsoleSender());
		return Audience.audience(list);
	}

	public void close() {
	}
}
