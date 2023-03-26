package xs.bspl.commands.admin;

import xs.bspl.commands.Command;
import xs.bspl.commands.Event;

public class NicknameCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("nickname", "nick");
	}

	@Override
	protected Description description() {
		return description("<name>", "changes the nickname of this bot");
	}

	@Override
	protected boolean requireAdmin() {
		return true;
	}

	@Override
	public void handle(Event event, String... args) {
		if (args.length == 0) return;

		if (args.length == 1 && args[0].equalsIgnoreCase("reset")) {
			event.guild.modifyNickname(event.self_member, null).queue();
		}

		final String nickname = (args.length == 1 && args[0].equalsIgnoreCase("reset")) ? 
			null : 
			String.join(" ", args);

		event.guild.modifyNickname(event.self_member, nickname).queue();
		event.message.delete().queue();
	}
	
}
