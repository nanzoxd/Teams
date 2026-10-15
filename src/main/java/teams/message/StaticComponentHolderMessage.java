package teams.message;

import teams.text.Formatter;

import net.kyori.adventure.text.Component;

public abstract class StaticComponentHolderMessage implements ComponentHolderMessage {

	protected final Component message;

	protected StaticComponentHolderMessage(String message) {
		this.message = Formatter.absolute().process(message);
	}

	protected StaticComponentHolderMessage(Component message) {
		this.message = message;
	}

	@Override
	public Component getMessage() {
		return this.message;
	}
}
