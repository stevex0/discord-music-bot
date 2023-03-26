package xs.bspl.commands;

import java.util.ArrayList;
import java.util.List;

import xs.bspl.commands.admin.NicknameCommand;
import xs.bspl.commands.admin.ShutdownCommand;
import xs.bspl.commands.fun.RollCommand;
import xs.bspl.commands.general.HelpCommand;
import xs.bspl.commands.general.UptimeCommand;
import xs.bspl.commands.music.JoinCommand;
import xs.bspl.commands.music.LeaveCommand;
import xs.bspl.commands.music.LofiCommand;
import xs.bspl.commands.music.LoopCommand;
import xs.bspl.commands.music.LoopQueueCommand;
import xs.bspl.commands.music.NextCommand;
import xs.bspl.commands.music.NowPlayingCommand;
import xs.bspl.commands.music.PauseCommand;
import xs.bspl.commands.music.PlayCommand;
import xs.bspl.commands.music.PlayerStatusCommand;
import xs.bspl.commands.music.QueueCommand;
import xs.bspl.commands.music.RemoveCommand;
import xs.bspl.commands.music.ReplayCommand;
import xs.bspl.commands.music.RewindCommand;
import xs.bspl.commands.music.ShuffleAllCommand;
import xs.bspl.commands.music.ShuffleCommand;
import xs.bspl.commands.music.SkipCommand;
import xs.bspl.commands.music.VolumeCommand;
import xs.bspl.commands.music.ClearCommand;
import xs.bspl.commands.music.FastForwardCommand;
import xs.bspl.commands.music.GoToCommand;

/** CommandManager class is responsible for managing all the commands available to the bot.
 *	It stores a list of registered commands and provides methods for getting a specific command
 *	and accessing the list of all commands.*/
public class CommandManager {

	private List<Command> command_list;

	private static CommandManager instance;

	/** Constructor for CommandManager.
	 *	Registers all the available commands to the command_list. */
	private CommandManager() {
		// General commands:
		registerCommand(new HelpCommand());
		registerCommand(new UptimeCommand());

		// Music commands:
		registerCommand(new JoinCommand());
		registerCommand(new LeaveCommand());
		registerCommand(new PlayCommand());
		registerCommand(new QueueCommand());
		registerCommand(new NowPlayingCommand());
		registerCommand(new SkipCommand());
		registerCommand(new LoopCommand());
		registerCommand(new ClearCommand());
		registerCommand(new RemoveCommand());
		registerCommand(new ShuffleCommand());
		registerCommand(new ShuffleAllCommand());
		registerCommand(new NextCommand());
		registerCommand(new LofiCommand());
		registerCommand(new FastForwardCommand());
		registerCommand(new RewindCommand());
		registerCommand(new GoToCommand());
		registerCommand(new VolumeCommand());
		registerCommand(new ReplayCommand());
		registerCommand(new LoopQueueCommand());
		registerCommand(new PauseCommand());
		registerCommand(new PlayerStatusCommand());

		// Fun commands:
		registerCommand(new RollCommand());

		// Admin commands:
		registerCommand(new ShutdownCommand());
		registerCommand(new NicknameCommand());
	}

	/** Returns the Command object that matches the given command alias.
	 *	@param search_alias The command alias to search for
	 *	@return The Command object that matches the given alias, or {@code null} if not found*/
	public Command getCommand(String search_alias) {
		for (final Command command : getCommandList()) {
			for (final String command_alias : command.getInfo().aliases) {
				if (command_alias.equalsIgnoreCase(search_alias)) return command;		
			}
		}
		return null;
	}

	/** Returns the list of all registered commands.
	 *	@return The list of all registered commands */
	public List<Command> getCommandList() {
		if (this.command_list == null) {
			this.command_list = new ArrayList<>();
		}
		return command_list;
	}

	/** Adds the given command to the list of registered commands.
	 *	If the command does not have an alias, it logs an error message and does not register the command.
	 *	@param command The command to register */
	private void registerCommand(Command command) {
		if (command == null) { return; }
		if (command.getInfo().aliases == null) {
			System.err.printf("Command: %s does not have an alias to be called.\n", command.getClass().getSimpleName());
			return;
		}

		getCommandList().add(command);
	}

	/** Returns the singleton instance of CommandManager.
	 *	If the instance has not been initialized, it initializes it and returns it.
	 *	@return The singleton instance of CommandManager */
	public static CommandManager get() {
		if (CommandManager.instance == null) {
			CommandManager.instance = new CommandManager();
		}
		return CommandManager.instance;
	}

}
