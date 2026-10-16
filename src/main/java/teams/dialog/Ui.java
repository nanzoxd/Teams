package teams.dialog;

import java.time.Duration;
import java.util.function.BiConsumer;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.object.ObjectContents;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import teams.Main;
import teams.gui.Theme;

/** Small helpers shared by the dialogs. */
final class Ui {

	static final int BUTTON = 150;
	static final int WIDE = 310;

	private Ui() {
	}

	/** Theme strings use section colours, this turns them into a component */
	static Component c(String legacy) {
		return LegacyComponentSerializer.legacySection().deserialize(legacy);
	}

	static Component head(OfflinePlayer player) {
		return Component.object(ObjectContents.playerHead(player.getUniqueId()));
	}

	static Component title(String text) {
		return c(Theme.main() + text);
	}

	static Component line(String label, String value) {
		return c(Theme.soft() + label + ": " + Theme.light() + value);
	}

	/** Button that runs the callback on the main thread. Can be clicked as often as the dialog is open. */
	static ActionButton button(Component label, Component tooltip, int width, BiConsumer<Player, DialogResponseView> run) {
		return ActionButton.create(label, tooltip, width, DialogAction.customClick((view, audience) -> {
			if (audience instanceof Player p) {
				Bukkit.getScheduler().runTask(Main.plugin, () -> run.accept(p, view));
			}
		}, ClickCallback.Options.builder().uses(ClickCallback.UNLIMITED_USES).lifetime(Duration.ofMinutes(30)).build()));
	}

	static ActionButton button(String label, String tooltip, int width, BiConsumer<Player, DialogResponseView> run) {
		return button(c(label), tooltip == null ? null : c(tooltip), width, run);
	}
}
