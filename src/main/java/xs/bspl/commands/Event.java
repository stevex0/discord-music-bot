package xs.bspl.commands;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;

/** A class that encapsulates the necessary information 
 * 	for a how a command can handle an event */
public final class Event {

	public final Guild			guild;
	public final TextChannel	channel;
	public final User			caller_user;
	public final Member			caller_member;
	public final User			self_user;
	public final Member			self_member;
	public final Message		message;
	public final String			message_raw;
	public final JDA			jda;

	public Event(GuildMessageReceivedEvent event) {
		guild			= event.getGuild();
		channel			= event.getChannel();
		caller_user		= event.getAuthor();
		caller_member	= event.getMember();
		self_user		= event.getJDA().getSelfUser();
		self_member		= guild.getSelfMember();
		message 		= event.getMessage();
		message_raw		= message.getContentRaw();
		jda				= event.getJDA();
	}
	
}