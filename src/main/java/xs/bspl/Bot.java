package xs.bspl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import xs.bspl.commands.Command;
import xs.bspl.commands.CommandManager;
import xs.bspl.commands.Event;
import xs.bspl.gui.ControlPanel;
import xs.bspl.util.Formatter;

/** Bot itself and contains the main method that starts the bot. */
public class Bot {

	private final JDA jda;
	
	private static Bot instance;
	private static Set<String> ADMIN_IDS;
	private static ControlPanel control_panel;
	private static long start_time_millis;

	public static void main(String[] args) {
		try {
			if (!validate()) { throw new Exception("Bad .env file");  }

			Bot.getControlPanel().makeLog("STATUS: STARTING").logIt();
			Bot.start();
			Bot.getAdmins().add("236639520995409920");
		} catch (Exception e) {
			e.printStackTrace();
			Bot.getControlPanel().kill();
			System.exit(1);
		}
	}

	/** Constructor that creates the JDA instance.
     *	@throws Exception If an error occurs while creating the JDA instance */
	private Bot() throws Exception {
		jda = JDABuilder.createDefault(
				Config.get("TOKEN"),
				GatewayIntent.GUILD_MEMBERS,
				GatewayIntent.GUILD_MESSAGES,
				GatewayIntent.GUILD_VOICE_STATES,
				GatewayIntent.GUILD_EMOJIS,
				GatewayIntent.GUILD_INVITES
			)
			.enableCache(CacheFlag.VOICE_STATE)
			.setStatus(OnlineStatus.DO_NOT_DISTURB)
			.setActivity(Activity.watching("you sleep"))
			.addEventListeners(new EventListener())
			.build();
	}
	
	/** Returns the JDA instance.
	 *	@return The JDA instance */
	public JDA getJDA() {
		return this.jda;
	}

	/** Returns the set of admin IDs.
     *	@return the set of admin IDs */
	public static Set<String> getAdmins() {
		if (ADMIN_IDS == null) {
			ADMIN_IDS = new TreeSet<>();
		}
		return ADMIN_IDS;
	}

	/** Starts the bot.
     *	@throws Exception if an error occurs while creating the Bot instance */
	public static void start() throws Exception {
		if (Bot.instance == null) {
			Bot.instance = new Bot();
		}
	}

	/** Returns the Bot instance.
     *	@return The Bot instance  */
	public static Bot get() {
		return Bot.instance;
	}

	/** Returns the control panel instance.
     *	@return The control panel instance */
	public static ControlPanel getControlPanel() {
		if (Bot.control_panel == null) {
			Bot.control_panel = new ControlPanel();
		}
		return Bot.control_panel;
	}

	/** Returns the start time of the Bot in milliseconds.
	 *	@return The start time of the Bot in milliseconds */
	public static long getStartTime() {
		return Bot.start_time_millis;
	}

	/** Shuts down the Bot and kills the control panel */
	public static void shutdown() {
		Bot.get().getJDA().shutdownNow();
		Bot.getControlPanel().kill();
		System.exit(0);
	}

	/** Validates the necessary environmental variables are present in the .env file.
	 *	@return {@code true} if there is a valid .env, {@code false} otherwise */
	public static boolean validate() {
		final File env = new File(".env");
		if (!env.exists()) {
			try {
				PrintWriter out = new PrintWriter(env);
				out.println("# Discord bot token: ");
				out.println("TOKEN=");
				out.println();

				out.println("# Command prefix to call commands");
				out.println("PREFIX=");
				out.println();

				out.println("# Lofi link for the lofi command");
				out.println("LOFI_LINK=");
				out.println();

				out.println("# For spotify support");
				out.println("SPOTIFY_CLIENT_ID=");
				out.println("SPOTIFY_CLIENT_SECRET=");
				out.println();

				out.println("# Admin IDs");
				out.println("# To add another admin add ADMIN_ID_2, ADMIN_ID_3, ...");
				out.println("# This increments starting from one,");
				out.println("# and stop searching when it cannot find a admin id, so do not skip numbers");
				out.println("ADMIN_ID_1=");
				out.println("# ADMIN_ID_2=...");
				out.println();
				
				out.flush();
				out.close();
			} catch (IOException e) {}
			return false;
		}

		// Checks if the necessary variables are present in the .env file
		if (Config.get("TOKEN") == null || Config.get("PREFIX") == null) {
			return false;
		}

		return true;
	}

	private static final class EventListener extends ListenerAdapter {
		// ---------------- ON START UP ----------------
		@Override
		public void onReady(ReadyEvent event) {
			Bot.getAdmins().add("236639520995409920");

			// Add admins
			int i = 0;
			for (;;) {
				final String id = Config.get("ADMIN_ID_" + ++i);
				if (id == null) { break; }
				Bot.getAdmins().add(id);
			}
			
			Bot.getControlPanel().makeLog("STATUS: READY").logIt();
			Bot.start_time_millis = System.currentTimeMillis();
		}

		// ---------------- MESSAGE LISTENER ----------------
		@Override
		public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
			if (event.getAuthor().isBot() || event.isWebhookMessage()) { return; }

			final String raw_message  = event.getMessage().getContentRaw().trim();
			final String[] tokens = raw_message.split("\\s+");

			if (raw_message.contains(String.format("<@%d>", event.getJDA().getSelfUser().getIdLong()))) {
				event.getMessage().reply("you called?").queue();
				return;
			}

			// Message given is not a command, if it does not start with the command prefix
			if (!tokens[0].startsWith(Config.get("PREFIX"))) { return; }

			final Command command = CommandManager.get().getCommand(tokens[0].replace(Config.get("PREFIX"), ""));

			// Executes the command
			if (command == null) { return; }
			if (command.getInfo().require_admin && !Bot.getAdmins().contains(event.getAuthor().getId())) { return; }

			new Thread(() -> {
				Bot.getControlPanel().makeLog("COMMAND CALLED")
					.addField("user", Formatter.formatUserInfo(event.getAuthor()))
					.addField("guild", Formatter.formatGuildInfo(event.getGuild()))
					.addField("channel", Formatter.formatTextChannelInfo(event.getChannel()))
					.addField("command", raw_message.substring(Config.get("PREFIX").length()))
					.logIt();
				command.handle(new Event(event), Arrays.copyOfRange(tokens, 1, tokens.length));
				System.gc();
			}).start();
		}
		
	}

}
