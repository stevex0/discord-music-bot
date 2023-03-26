package xs.bspl.commands;

/** An abstract class that represent a command.
 * 	All commands should inhert this class. */
public abstract class Command {

	private final CommandInfo info;
	
	/** Initializes the command with CommandInfo containing its aliases,
	 *	description, required admin permission, and hidden status. */
	protected Command() {
		this.info = new CommandInfo(this);
	}

	/** Returns the aliases for this Command.
	 *	Subclasses must implement this method to provide aliases for their command. 
	 *	@return Aliases object containing aliases; use {@code aliases(String...)}. */
	protected abstract Aliases aliases();

	/** Returns the description for this command. Description may contain an optional argument.
	 * 	Subclasses must implement this method to provide a description for their command.
	 *	@return Description object contain an optional arguement and a description;
	 *		 use {@code description(String, String)}, or {@code description(String)}. */
	protected abstract Description description();
	
	/** Returns if this command requires administrator privileges.
	 * 	Subclassess may override this method to specify if their command require admin permissions of not.
	 *	By default, this returns {@code false}.
	 *	@return {@code true} if this command requires administrator permissions, {@code false} otherwise. */
	protected boolean requireAdmin() {
		return false;
	}

	/** Returns if this command is hidden from the help command.
	 * 	Subclasses may override this method to specify if their command should be hidden or not.
	 * 	By default, this returns the same value as {@code requireAdmin()}
	 * 	@return {@code true} if the command is hidden, {@code false} otherwise. */
	protected boolean isHidden() {
		return requireAdmin();
	}

	/** Handles the execution and functionality of this command.
	 * 	Subclasses must implement this method to provide the logic for this command.
	 * 	@param event The event that triggered this command
	 * 	@param args The arguements passed to this command */
	public abstract void handle(Event event, String... args);
	
	/** Creates an Aliases object containing the provided aliases. 
	 * 	Use this method when implementing the {@code aliases()} method.
	 * 	@param aliases The aliases for this command
	 * 	@return Aliases object containing the provided aliases */
	protected static Aliases aliases(String... aliases) {
		return new Aliases(aliases);
	}

	/** Creates a Description object containing the provided description.
	 * 	Use this method when implementing the {@code description()} method.
	 * 	@param description The description for this command
	 * 	@return Description object containing the provided description */
	protected static Description description(String description) {
		return description(null, description);
	}

	/** Create a Description object containing the provided arguements and description.
	 * 	Use this method when implementing the {@code description()} method.
	 * 	@param optional_arguments The optional arguements for this command
	 * 	@param description The description for this command
	 * 	@return Description object containing the provided description */
	protected static Description description(String optional_arguments, String description) {
		return new Description(optional_arguments, description);
	}

	/** Returns the CommandInfo for this command.
	 * 	@return CommandInfo object containing the aliases, arguements, 
	 * 		description, admin status, and hidden status */
	public final CommandInfo getInfo() {
		return this.info;
	}

	/** An inner class of the Command class representing the aliases for a command. 
	 * 	Use {@code Command.aliases(String...)} to create an instance. */
	protected static class Aliases {
		private final String[] aliases;

		private Aliases(String[] aliases) {
			if (aliases.length == 0) {
				this.aliases = null;
			} else {
				this.aliases = aliases;
			}
		}
	}

	/** An inner class of the Command class representing the description for a command.
	 * 	A description may contain an arguments string along with a description string.
	 * 	Use {@code Command.description(String, String)}, 
	 * 		or {@code Command.description(String)} to create an instance. */
	protected static final class Description {
		private final String arguments;
		private final String description;

		private Description(String arguments, String description) {
			this.arguments		= arguments;
			this.description	= description;
		}
		
	}

	/** An inner class to represent the infomation of a command. 
	 * 	All subclasses of Command should have one. */
	public static final class CommandInfo {
		public final String[] aliases;
		public final String arguments;
		public final String description;
		public final boolean require_admin;
		public final boolean is_hidden;

		private CommandInfo(Command cmd) {
			this.aliases		= cmd.aliases().aliases;
			this.arguments		= cmd.description().arguments;
			this.description	= cmd.description().description;
			this.require_admin	= cmd.requireAdmin();
			this.is_hidden		= cmd.isHidden();
		}
	}

}