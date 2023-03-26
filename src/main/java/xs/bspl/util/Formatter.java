package xs.bspl.util;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

/** The Formatter class provides various utility methods for formatting strings and values. */
public final class Formatter {

	private Formatter() {} // Private constructor to prevent instantiation of the class

	/** Removes all extra spaces from a given string and replaces them with a single space.
	 *	@param str The string to be cleaned
	 *	@return A string with remove leading and trailing spaces and all consecutive spaces with one space */
	public static String cleanString(String str) {
		return str.trim().replaceAll("\\s+", " ");
	}

	/** Formats the duration in milliseconds into a string in the format of "hh:mm:ss".
	 *	@param duration The duration to be formatted in milliseconds
	 *	@return The formatted string */
	public static String formatTime(long duration) {
		final long seconds = duration / 1_000 % 60;
		final long minutes = duration / 60_000 % 60;
		final long hours = duration / 3_600_000; 

		final StringBuilder time = new StringBuilder();

		if (hours > 0) {
			time.append((hours < 10) ? "0" + hours : hours).append(":");
		}
		time.append((minutes < 10) ? "0" + minutes : minutes).append(":");
		time.append((seconds < 10) ? "0" + seconds: seconds);
		return time.toString();
	}

	/** Cuts off a given string to fit within a given maximum width.
	 * 	@param str The string to be fitted
	 * 	@param g The Graphics2D object used for rendering the font
	 * 	@param font The font being used to render the string
	 * 	@param max_width The maximum width allowed for the string
	 * 	@return The fitted string */
	public static String fitString(String str, Graphics2D g, Font font, int max_width) {
		final FontMetrics metrics = g.getFontMetrics(font);

		// Remove all unneeded spaces
		str = Formatter.cleanString(str);

		boolean char_removed = false;

		// While the string does not fit we remove a character
		while (metrics.stringWidth(str) > max_width) {
			str = str.substring(0, str.length() - 1);
			char_removed = true;
		}

		// Shows that the string has been cut off
		if (char_removed) {
			str = str.substring(0, str.length() - 3);
			str += "...";
		}

		return str;
	}

	/** Formats the user's name and id into a string.
	 *	@param user The user whose information is being formatted
	 *	@return The formatted string */
	public static String formatUserInfo(User user) {
		return String.format("\"%s\" (id=<@%s>)", user.getName(), user.getId());
	}

	/** Formats the guild's name and id into a string.
	 *	@param guild The guild whose information is being formatted
	 *	@return The formatted string */
	public static String formatGuildInfo(Guild guild) {
		return String.format("\"%s\" (id=%s)", guild.getName(), guild.getId());
	}

	/** Formats the text channel's name and id into a string.
	 *	@param channel The text channel whose information is being formatted
	 *	@return The formatted string */
	public static String formatTextChannelInfo(TextChannel channel) {
		return String.format("\"%s\" (id=%s)", channel.getName(), channel.getId());
	}

	/** Returns the URL of the YouTube video thumbnail.
	 *	@param yt_url The URL of the YouTube video
	 *	@return The URL of the YouTube video thumbnail */
	public static String getYoutubeThumbnail(String yt_url) {
		return String.format("https://img.youtube.com/vi/%s/mqdefault.jpg", yt_url.substring(32));
	}

	/** Returns a string representation of the current timestamp.
	 *	@return A string representation of the current time in the format "MM/dd/yyyy - HH:mm:ss z" */
	public static String getTimestamp() {
		return formatDateTime(System.currentTimeMillis(), "MM/dd/yyyy - HH:mm:ss z");
	}

	/** Formats a timestamp represented as milliseconds since the epoch to a string.
	 *	@param time_millis The timestamp in milliseconds
	 *	@param pattern The pattern to format the timestamp as
	 *	@return The formatted timestamp as a string */
	public static String formatDateTime(long time_millis, String pattern) {
		final Date date = new Date(time_millis);
		final DateFormat formatter = new SimpleDateFormat(pattern);
		formatter.setTimeZone(TimeZone.getTimeZone("EST"));
		return formatter.format(date);
	}
	
}
