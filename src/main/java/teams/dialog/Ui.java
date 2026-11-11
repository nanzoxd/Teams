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
import net.kyori.adventure.text.object.PlayerHeadObjectContents;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import teams.Main;
import teams.gui.Theme;

/** Small helpers shared by the dialogs. */
final class Ui {

	// the five theme colours, read live so a reload changes them
	static String text() {
		return Theme.text();
	}

	static String hi() {
		return Theme.highlight();
	}

	static String pri() {
		return Theme.primary();
	}

	static String sec() {
		return Theme.secondary();
	}

	static String label() {
		return Theme.accent();
	}

	static final int BUTTON = 150;
	static final int WIDE = 310;

	private Ui() {
	}

	/** Theme strings use section colours, this turns them into a component */
	static Component c(String legacy) {
		return LegacyComponentSerializer.legacySection().deserialize(legacy);
	}

	/** Inline head for buttons. Online players use their live (SkinsRestorer) skin, offline ones are looked up by the client from the name. */
	static Component head(OfflinePlayer player) {
		try {
			PlayerHeadObjectContents.Builder b = ObjectContents.playerHead();
			if (player.getName() != null) {
				b.name(player.getName());
			}
			Player online = player.getPlayer();
			if (online != null) {
				for (ProfileProperty pp : online.getPlayerProfile().getProperties()) {
					if ("textures".equals(pp.getName())) {
						b.id(player.getUniqueId());
						b.profileProperty(PlayerHeadObjectContents.property("textures", pp.getValue(), pp.getSignature()));
						break;
					}
				}
			}
			return Component.object(b.build());
		} catch (Throwable t) {
			return Component.empty();
		}
	}

	static Component title(String text) {
		return c(Ui.hi() + text);
	}

	static Component lines(Component... parts) {
		return Component.join(net.kyori.adventure.text.JoinConfiguration.newlines(), parts);
	}

	/** A thin divider like the strikethrough line under a header */
	static Component divider() {
		return c(Theme.primary() + " ".repeat(60)).decorate(net.kyori.adventure.text.format.TextDecoration.STRIKETHROUGH);
	}

	/** Bold name with an icon in front and a small line under it */
	static Component header(Component icon, String nameColoured, String subtitle) {
		Component name = c(nameColoured).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD);
		return lines(icon == null ? name : icon.append(Component.space()).append(name), c(Ui.label() + subtitle), divider());
	}

	/** One line of a stats box: coloured symbol, label, value */
	static Component row(String symbol, String label, String value) {
		return c(Ui.hi() + symbol + " " + Ui.label() + label + ": " + Ui.text() + value);
	}

	/** Button text: coloured symbol, then the label */
	static String icon(String symbol, String colour, String label) {
		return colour + symbol + " " + Ui.text() + label;
	}

	/** The team colour as a legacy string */
	static String teamColor(teams.Team team) {
		String hex = team.getColor().toAmpersandHex();
		return Theme.fromHex(hex.replace("&", ""), null);
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
