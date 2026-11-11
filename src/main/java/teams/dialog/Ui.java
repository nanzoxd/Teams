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

	// Dialog colours (config.yml, section dialog). Read live so a reload changes them.
	private static String colour(String key, String def) {
		return Theme.fromHex(Main.plugin.getConfig().getString("dialog." + key, def), def);
	}

	/** Primary text */
	static String text() {
		return colour("text", "#FFF0CC");
	}

	/** Secondary text: labels */
	static String label() {
		return colour("secondary", "#F7E2AD");
	}

	/** Gold highlights */
	static String hi() {
		return colour("gold", "#F1D58F");
	}

	static String pri() {
		return hi();
	}

	static String sec() {
		return text();
	}

	/** Enabled and successful */
	static String on() {
		return colour("on", "#80ED26");
	}

	/** Disabled, negative, back, next and close */
	static String off() {
		return colour("off", "#FF1717");
	}

	/** Information */
	static String info() {
		return colour("info", "#5490FF");
	}

	static String info2() {
		return colour("info2", "#91B8FF");
	}

	static String special() {
		return colour("special", "#FADE55");
	}

	// The only symbols dialogs use
	static final String TICK = "\u2714";
	static final String CROSS = "\u2716";
	static final String SWITCH = "\u21C4";
	static final String GLOBE = "\uD83C\uDF10";
	static final String LEFT = "\u2190";
	static final String RIGHT = "\u2192";
	static final String REFRESH = "\u27F3";

	private static final String BOLD = "\u00A7l";
	private static final String RESET = "\u00A7r";

	static final int BUTTON = 150;
	/** Width of text boxes and text fields */
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

	/** Bold name with an optional head in front, a small line under it, then a divider */
	static Component header(Component icon, String nameColoured, String subtitle) {
		Component name = c(nameColoured).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD);
		return lines(icon == null ? name : icon.append(Component.space()).append(name), c(Ui.label() + subtitle), divider());
	}

	/** One line of a profile: label, then the value */
	static Component row(String label, String value) {
		return c(Ui.label() + label + "  " + Ui.text() + value);
	}

	/** Plain button text */
	static String plain(String label) {
		return Ui.text() + label;
	}

	/** Setting that is on / off: bold tick or cross, then the name */
	static String mark(boolean on, String label) {
		return (on ? Ui.on() + BOLD + TICK : Ui.off() + BOLD + CROSS) + RESET + " " + Ui.text() + label;
	}

	/** Setting with several values: ⇄ Name: Value */
	static String cycle(String label, String value) {
		return Ui.hi() + SWITCH + " " + Ui.text() + label + ": " + Ui.hi() + value;
	}

	static String back(String label) {
		return Ui.off() + BOLD + LEFT + " " + label;
	}

	static String next(String label) {
		return Ui.off() + BOLD + label + " " + RIGHT;
	}

	static String close() {
		return Ui.off() + CROSS + " Close";
	}

	static String refresh(String label) {
		return Ui.hi() + REFRESH + " " + Ui.text() + label;
	}

	static String save() {
		return Ui.on() + BOLD + TICK + RESET + " " + Ui.on() + "Save";
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
