package xs.bspl.util;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

/** A class that represents an embedded message that can be sent to a Discord text channel or replied to a message. */
public class EmbedMessage {
	private final TextChannel channel;
	private final Message message;

	private String title_text = null;
	private String title_url = null;
	private String icon_url = null;

	private final EmbedBuilder builder;

	/** If you use this constructor, then a message will be sent to this channel 
	 *	@param channel A text channel in which an embed message will be sent */
	private EmbedMessage(TextChannel channel) {
		this.channel = channel;
		this.message = null;
		this.builder = new EmbedBuilder();
	}

	/** If you use this constructor, then a message will be replied to the given message
	 *	@param message A message to be repiled to */
	private EmbedMessage(Message message) {
		this.channel = null;
		this.message = message;
		this.builder = new EmbedBuilder();
	}

	/** Sets the title of the embed message.
	 *	@param title A format string for the title text.
	 *	@param args Arguments to format the title string.
	 *	@return This EmbedMessage instance */
	public EmbedMessage setTitle(String title, Object... args) {
		this.title_text = String.format(title, args);
		this.builder.setAuthor(this.title_text, this.title_url, this.icon_url);
		return this;
	}

	/**  Sets the URL for the title of the embed message.
	 *	@param url The URL to set
	 *	@return This EmbedMessage instance */
	public EmbedMessage setUrl(String url) {
		this.title_url = url;
		this.builder.setAuthor(this.title_text, this.title_url, this.icon_url);
		return this;
	}

	/** Sets the icon URL for the title of the embed message.
	 *	@param url The URL to set
	 *	@return This EmbedMessage instance */
	public EmbedMessage setIconUrl(String url) {
		this.icon_url = url;
		this.builder.setAuthor(this.title_text, this.title_url, this.icon_url);
		return this;
	}

	/** Sets the description of the embed message.
	 *	@param description A format string for the description text
	 *	@param args Arguments to format the description string
	 *	@return This EmbedMessage instance */
	public EmbedMessage setDescription(String description, Object... args) {
		this.builder.setDescription(String.format(description, args));
		return this;
	}

	/**  Sets the thumbnail image for the embed message.
	 *	@param url The URL of the thumbnail image
	 *	@return This EmbedMessage instance */
	public EmbedMessage setThumbnail(String url) {
		if (url != null) {
			this.builder.setThumbnail(url);
		}
		return this;
	}

	/** Sets the color of the embed message.
	 * @param color The color to set
	 * @return This EmbedMessage instance */
	public EmbedMessage setColor(int color) {
		this.builder.setColor(color);
		return this;
	}

	/** Sets the footer of the embed message.
	 *	@param footer A format string for the footer text 
	 *	@param args Arguments to format the footer string
	 *	@return This EmbedMessage instance */
	public EmbedMessage setFooter(String footer, Object... args) {
		this.builder.setFooter(String.format(footer, args));
		return this;
	}

	/** Add a field of the embed message.
	 *	@param title The title of the field
	 *	@param value The value of the field
	 *	@param inline {@code true} if inline, {@code false} otherwise
	 *	@return The EmbedMessage instance */
	public EmbedMessage addField(String title, String value, boolean inline) {
		this.builder.addField(title, value, inline);
		return this;
	}

	/** Sends the embed message to its intended destination. */
	public void send() {
		if (channel != null) {
			channel.sendMessageEmbeds(builder.build()).queue();
		}
		if (message != null) {
			message.replyEmbeds(builder.build()).queue();
		}
	}

	
	/** Sends the embed message to its intended destination and deletes the message after the given action has been executed.
	 *	@param action The action to execute before deleting the message */
	public void sendAndDeleteAfter(Action action) {
		if (channel != null) {
			channel.sendMessageEmbeds(builder.build()).queue(e -> {
				action.execute();
				e.delete().queue();
			});
		}

		if (message != null) {
			message.replyEmbeds(builder.build()).queue(e -> {
				action.execute();
				e.delete().queue();
			});
		}
	}

	/** Constructs a new EmbedMessage instance that will send an embed to the given TextChannel.
	 *	@param channel The TextChannel to send the embed to
	 *	@return A new EmbedMessage instance */
	public static EmbedMessage sendTo(TextChannel channel) {
		return new EmbedMessage(channel);
	}

	/** Constructs a new EmbedMessage instance that will send an embed as a reply to the given Message.
	 *	@param message The Message to reply to
	 *	@return A new EmbedMessage instance */
	public static EmbedMessage replyTo(Message message) {
		return new EmbedMessage(message);
	}

	/** An interface for actions to be executed before a message is deleted. */
	public static interface Action { void execute(); }
	
}
