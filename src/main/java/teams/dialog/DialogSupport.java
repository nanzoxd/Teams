package teams.dialog;

import teams.Main;

/** Checks if this server can show native dialogs (Paper 1.21.6+). Does not touch any dialog class itself. */
public final class DialogSupport {

	private static Boolean available;

	private DialogSupport() {
	}

	public static boolean available() {
		if (available == null) {
			try {
				Class.forName("io.papermc.paper.dialog.Dialog");
				available = true;
			} catch (Throwable e) {
				available = false;
			}
		}
		return available;
	}

	/** @return true if /team top and /teamconfig should use dialogs */
	public static boolean enabled() {
		return available() && Main.plugin.getConfig().getBoolean("gui.dialogs", true);
	}
}
