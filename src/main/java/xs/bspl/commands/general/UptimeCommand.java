package xs.bspl.commands.general;

import xs.bspl.Bot;
import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.Formatter;
import xs.bspl.util.EmbedMessage;

public class UptimeCommand extends Command {
	@Override
	protected Aliases aliases() {
		return aliases("uptime");
	}

	@Override
	protected Description description() {
		return description("display the uptime");
	}

	@Override
	public void handle(Event event, String... args) {
		final long current_time = System.currentTimeMillis();
		EmbedMessage.replyTo(event.message)
			.setColor(ColorScheme.color())
			.setDescription(
				"**Start Time**: `%s %s`\n**Uptime**: `%s`",
				Formatter.formatDateTime(Bot.getStartTime(), "MM/dd/yyyy"),
				Formatter.formatDateTime(Bot.getStartTime(), "HH:mm:ss z"),
				Formatter.formatTime(current_time - Bot.getStartTime())
			).send();
	}
}
