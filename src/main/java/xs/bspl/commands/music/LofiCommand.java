package xs.bspl.commands.music;

import xs.bspl.Config;
import xs.bspl.commands.Command;
import xs.bspl.commands.CommandManager;
import xs.bspl.commands.Event;

public class LofiCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("lofi");
	}

	@Override
	protected Description description() {
		return description("plays lofi");
	}

	@Override
	protected boolean isHidden() {
		return true;
	}

	@Override
	public void handle(Event event, String... args) {
		String lofi = Config.get("LOFI_LINK");
		lofi = (lofi == null) ? "lofi 24/7" : lofi;
		CommandManager.get().getCommand("play").handle(event, lofi);
	}
	
}
