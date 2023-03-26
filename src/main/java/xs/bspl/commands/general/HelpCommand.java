package xs.bspl.commands.general;

import java.util.List;

import xs.bspl.commands.Command;
import xs.bspl.commands.CommandManager;
import xs.bspl.commands.Event;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;

public class HelpCommand extends Command {

	@Override 
	protected Aliases aliases() {
		return aliases("help");
	}

	@Override 
	protected Description description() {
		return description("shows the functionality of all bot commands");
	}

	@Override 
	public void handle(Event event, String... args) {
		final List<Command> commands = CommandManager.get().getCommandList();
		final StringBuilder command_descriptions = new StringBuilder();

		commands.stream()
			.map(Command::getInfo)
			.filter(cmd -> !cmd.is_hidden)
			.forEach(info -> {
				command_descriptions.append("`")
					.append(info.aliases[0])
					.append(info.arguments != null ? " " + info.arguments : "")
					.append("`: ").append(info.description)
					.append('\n');
			});
		
		EmbedMessage.replyTo(event.message)
			.setColor(ColorScheme.color())
			.setTitle("%s's commands", event.self_user.getName())
			.setIconUrl(event.self_user.getAvatarUrl())
			.setDescription(command_descriptions.toString())
			.send();
	}
}
