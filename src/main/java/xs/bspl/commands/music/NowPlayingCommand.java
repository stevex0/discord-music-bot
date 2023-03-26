package xs.bspl.commands.music;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import javax.imageio.ImageIO;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import xs.bspl.Bot;
import xs.bspl.Config;
import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.Formatter;

public class NowPlayingCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("nowplaying", "np");
	}

	@Override
	protected Description description() {
		return description("displays info on the current playing track");
	}

	@Override
	public void handle(Event event, String... args) {
		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();
		final AudioTrack current_track = scheduler.getAudioPlayer().getPlayingTrack();

		final BufferedImage image = getNowPlayingImage(scheduler, current_track, true);

		// SEND MESSAGE
		try {
			final String msg = current_track != null ? 
				":notes: **Now Playing: **<" + current_track.getInfo().uri + ">" : 
				String.format(
					":x: No track is currently playing! Use ` %splay <search/link> ` to play a track!", 
					Config.get("PREFIX")
				);
			final File file = new File("nowplaying.png");
			ImageIO.write(image, "png", file);
			event.message.reply(msg).addFile(file).queue(e -> {
				file.delete();
			});
		} catch (Exception e) { Bot.getControlPanel().logError(e); }
	}

	public BufferedImage getNowPlayingImage(TrackScheduler scheduler, AudioTrack track, boolean show_timeline) {
		final int width = 780;
		final int height = 160;
		final int padding = 12;

		final int title_font_size = 24;
		final int author_font_size = 20;
		final int time_font_size = 20;

		final boolean is_playing = track != null;

		final Font title_font = new Font("Yu Gothic UI", Font.BOLD, title_font_size);
		final Font author_font = new Font("Yu Gothic UI", Font.PLAIN, author_font_size);
		final Font time_font = new Font("Calibri", Font.BOLD, time_font_size);

		final BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g = (Graphics2D) image.getGraphics();
		final FontMetrics time_metrics = g.getFontMetrics(time_font);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// BACKGROUND
		g.setColor(new Color(25, 26, 31));
		g.fillRect(0, 0, width, height);

		// THUMBNAIL
		final int thumbnail_height = height - (padding * 2);
		final int thumbnail_width = (4 * thumbnail_height) / 3;

		BufferedImage thumbnail = null;

		try { // Loads the thumbnail
			if (is_playing) {
				thumbnail = ImageIO.read(new URL(Formatter.getYoutubeThumbnail(track.getInfo().uri)));
			}
		} catch (Exception e) { thumbnail = null; }

		// Draws the thumbnail
		if (thumbnail != null) {
			g.drawImage(thumbnail, padding, padding, thumbnail_width, thumbnail_height, null);
		} else {
			g.setColor(new Color(35, 36, 41));
			g.fillRect(padding, padding, thumbnail_width, thumbnail_height);
		}

		// TITLE
		final String title = is_playing ? track.getInfo().title : "---";
		final int max_title_width = width - (padding * 3) - thumbnail_width;
		final String title_text = Formatter.fitString(title, g, title_font, max_title_width);
		final int title_text_x = padding * 2 + thumbnail_width;
		final int title_text_y = padding + 24;

		g.setColor(Color.WHITE);
		g.setFont(title_font);
		g.drawString(title_text, title_text_x, title_text_y);

		// AUTHOR
		final String author = is_playing ? track.getInfo().author : "---";
		final String author_text = Formatter.fitString(author, g, author_font, max_title_width);

		g.setFont(author_font);
		g.drawString(author_text, title_text_x, title_text_y + title_font_size + 6);

		// TIME LINE
		final int timeline_width = width - (padding * 3) - thumbnail_width;
		final int timeline_height = 22;
		final int timeline_x = (padding * 2) + thumbnail_width;
		final int timeline_y = height - timeline_height - padding;
		if (show_timeline) {
			// BAR
			final int arc = 18;
			
			g.setColor(new Color(35, 36, 41));
			g.fillRoundRect(timeline_x, timeline_y, timeline_width, timeline_height, arc, arc);

			if (is_playing) {
				final long position = track.getPosition();
				final long duration = track.getDuration();

				final int elapse_width = track.getInfo().isStream ? timeline_width : (int) Math.round(((double) position / duration) * timeline_width);

				// ELASPED COLORED BAR
				g.setColor(new Color(108, 92, 231));
				g.fillRoundRect(timeline_x, timeline_y, elapse_width, timeline_height, arc, arc);
			}
		}

		// DURATION
		final String time_text = is_playing ? 
			(show_timeline ? 
				(track.getInfo().isStream ? 
					Formatter.formatTime(scheduler.elapseTimeMillis()) + " / LIVE" : // position / live 
					Formatter.formatTime(track.getPosition()) + " / " + Formatter.formatTime(track.getDuration()) // postion / duration
				) : (track.getInfo().isStream) ? "LIVESTREAM" : Formatter.formatTime(track.getDuration()) // duration
			) : "---"; // not playing 

		final int time_text_width = time_metrics.stringWidth(time_text);
		final int time_bg_width = time_text_width + 24;
		final int time_bg_height = time_font_size + 12;
		final int time_bg_x = timeline_x + timeline_width - time_bg_width;

		final int time_bg_y = show_timeline ? timeline_y - time_bg_height - (padding / 4) : height - padding  - time_bg_height; 

		final int time_text_x = time_bg_x + ((time_bg_width - time_text_width) / 2);
		final int time_text_y = time_bg_y + ((time_bg_height - time_font_size) / 2) + time_metrics.getAscent() + 1;

		g.setColor(new Color(49, 49, 57));
		g.fillRoundRect(time_bg_x, time_bg_y, time_bg_width, time_bg_height, 12, 12);

		g.setColor(Color.WHITE);
		g.setFont(time_font);
		g.drawString(time_text, time_text_x, time_text_y);

		g.dispose(); // DONE USING GRAPHICS

		return image;
	}
	
}
