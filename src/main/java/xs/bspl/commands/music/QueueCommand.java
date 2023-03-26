package xs.bspl.commands.music;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import xs.bspl.Bot;
import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.Formatter;

public class QueueCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("queue", "q");
	}

	@Override
	protected Description description() {
		return description("<page>", "shows all the tracks in the queue");
	}

	@Override
	public void handle(Event event, String... args) {
		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();
		final List<AudioTrack> tracks = new ArrayList<>(scheduler.getQueue());

		int page = 1;

		if (args.length == 1) {
			try {
				page = Math.max(Integer.parseInt(args[0]), 1);
				page = Math.min(page, (int) Math.ceil(tracks.size() / 7f));
			} catch (NumberFormatException ignored) {}
		}

		final int start_index = (page - 1) * 7;

		final int padding = 10;
		final int curve = 16;
		final int entry_padding = 8;
		final int entries = 7;
		final int entry_height = 54;
		final int width = 780;
		final int height = (padding * 2) + (entries * (entry_height + entry_padding));
		final int header_width = width - (padding * 2);
		final int entry_width = header_width;

		final int position_font_size = 22;
		final int title_font_size = 18;
		final int author_font_size = 16;
		final int duration_font_size = 16;

		final Font position_font = new Font("Consolas", Font.BOLD, position_font_size);
		final Font duration_font = new Font("Calibri", Font.BOLD, duration_font_size);
		final Font title_font = new Font("Yu Gothic UI", Font.BOLD, title_font_size);
		final Font author_font = new Font("Yu Gothic UI", Font.PLAIN, author_font_size);
		
		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		final Graphics2D g = (Graphics2D) image.getGraphics();
		final FontMetrics position_metrics = g.getFontMetrics(position_font);
		final FontMetrics duration_metrics = g.getFontMetrics(duration_font);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);	
		
		// BACKGROUND
		g.setColor(new Color(25, 26, 31));
		g.fillRect(0, 0, width, height);
		
		// DRAWS EACH ENTRIES
		for (int i = 0; i < entries; ++i) {
			final int x = padding;
			final int y = padding + ((entry_padding + entry_height) * i);

			g.setColor((i % 2 == 0) ? new Color(35, 36, 41) : new Color(38, 39, 44));
			g.fillRoundRect(x, y, entry_width, entry_height, curve, curve);

			final int index = i + start_index;

			// POSITION
			final String position_text = (index >= tracks.size()) ? "---" : "#" + (index + 1);
			
			final int position_text_width = position_metrics.stringWidth(position_text);
			final int position_bg_width = position_text_width + 24;
			final int position_bg_height = position_font_size + 6;
			final int position_bg_y = y + (entry_height / 2) - (position_bg_height / 2);
			final int position_bg_x = x + (position_bg_y - y);
			
			final int position_text_x = position_bg_x + ((position_bg_width - position_text_width) / 2);
			final int position_text_y = position_bg_y + ((position_bg_height - position_font_size) / 2) + position_metrics.getAscent() + 1;
			
			g.setColor(new Color(49, 49, 57));
			g.fillRoundRect(position_bg_x, position_bg_y, position_bg_width, position_bg_height, 20, 20);
			g.setColor(Color.WHITE);
			g.setFont(position_font);
			g.drawString(position_text, position_text_x, position_text_y);

			if (index >= tracks.size()) { continue; }

			final AudioTrack track = tracks.get(index);
			final AudioTrackInfo info = track.getInfo();

			// DRAWS THE THUMBNAIL
			final int thumbnail_x = x + ((position_bg_x - x) * 2) + position_bg_width;
			final int thumbnail_y = y + 4;
			final int thumbnail_height = entry_height  - 8;
			final int thumbnail_width = (4 * thumbnail_height) / 3;

			BufferedImage thumbnail = null;
			try {
				thumbnail = ImageIO.read(new URL(Formatter.getYoutubeThumbnail(info.uri)));
			} catch (Exception ignored) {}
			if (thumbnail != null) {
				g.drawImage(thumbnail, thumbnail_x, thumbnail_y, thumbnail_width, thumbnail_height, null);
			} else {
				g.setColor(Color.WHITE);
				g.fillRect(thumbnail_x, thumbnail_y, thumbnail_width, thumbnail_height);
			}

			// TITLE
			String title_text = Formatter.cleanString(info.title);
			if (title_text.length() > 50) {
				title_text = title_text.substring(0, 47) + "...";
			}

			final int title_x = x + thumbnail_x + thumbnail_width;
			final int title_y = y + 24;
			g.setFont(title_font);
			g.drawString(title_text, title_x, title_y);

			// AUTHOR
			String author_text = Formatter.cleanString(info.author);
			if (author_text.length() > 50) {
				title_text = title_text.substring(0, 47) + "...";
			}
			final int author_y = title_y + title_font_size + 4;
			g.setFont(author_font);
			g.drawString(author_text, title_x, author_y);

			final String duration_string = info.isStream ? 
				"LIVESTREAM" : 
				Formatter.formatTime(track.getDuration());

			final int duration_text_width = duration_metrics.stringWidth(duration_string);
			final int duration_bg_width = duration_text_width + 24;
			final int duration_bg_height = duration_font_size + 12;
			final int duration_bg_x = (x + entry_width) - (position_bg_x - x) - duration_bg_width;
			final int duration_bg_y = (y + (entry_height / 2)) - (duration_bg_height / 2);
			final int duration_text_x = duration_bg_x + ((duration_bg_width - duration_text_width) / 2);
			final int duration_text_y = duration_bg_y + ((duration_bg_height - duration_font_size) / 2) + duration_metrics.getAscent() + 1;

			g.setColor(new Color(49, 49, 57));
			g.fillRoundRect(duration_bg_x, duration_bg_y, duration_bg_width, duration_bg_height, 24, 24);
			g.setColor(Color.WHITE);
			g.setFont(duration_font);
			g.drawString(duration_string, duration_text_x, duration_text_y);
		}

		g.dispose(); // DONE USING GRAPHICS

		final String queue_size = String.format(":notes: **Queue: **`%d` track%s ", tracks.size(), tracks.size() == 1 ? "" : "s");
		final String page_num = tracks.size() == 0 ? "Page `0` / `0`" : "Page `" + page + "` / `" + (int) Math.ceil(tracks.size() / (double) entries)  + "`";
		final String header = String.format("%s - %s", queue_size, page_num);

		try {
			final File file = new File("queue.png");
			ImageIO.write(image, "png", file);
			event.message.reply(header).addFile(file).queue(e -> {
				file.delete();
			});
		} catch (Exception e) { Bot.getControlPanel().logError(e); }
	}

}
