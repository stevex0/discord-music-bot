package xs.bspl.commands.fun;

import net.dv8tion.jda.api.EmbedBuilder;

import xs.bspl.commands.Command;
import xs.bspl.commands.Event;

public class RollCommand extends Command {

	@Override 
	protected Aliases aliases() {
		return aliases("roll");
	}

	@Override 
	protected Description description() {
		return description("<max>", "rolls a random number");
	}

	@Override 
	public void handle(Event event, String... args) {
		int max = 100;
		if (args.length == 1) {
			// Check if args[0] is a valid integer
			try {
				int temp = Integer.parseInt(args[0]);
				if (temp > 0) { max = temp; } // Must be a positive integer
			} catch (NumberFormatException ignored) {}
		}

		int random = (int) (Math.random() * (max + 1));
		
		event.message.replyEmbeds(
			new EmbedBuilder()
				.setColor((int) ((random / (double) max) * 0xffffff))
				.setAuthor(String.format("%s rolled:", event.caller_user.getName()), null, event.caller_user.getAvatarUrl())
				.setDescription("```" + random + "```")
				.build()
		).queue();
	}
}
