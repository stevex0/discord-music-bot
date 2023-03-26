package xs.bspl.commands.admin;

import xs.bspl.Bot;
import xs.bspl.commands.Command;
import xs.bspl.commands.CommandManager;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.LeaveCommand;

public class ShutdownCommand extends Command {

	@Override 
	protected Aliases aliases() {
		return aliases("shutdown");
	}

	@Override 
	protected Description description() {
		return description("shutdowns the bot (Requires admin permission)");
	}

	@Override 
	protected boolean requireAdmin() {
		return true;
	}

	@Override 
	public void handle(Event event, String... args) {
		Bot.get().getJDA().getGuilds().stream().forEach(guild -> ((LeaveCommand) CommandManager.get().getCommand("leave")).leaveVoiceChannelIn(guild));
		Bot.shutdown();
	}
	
}
