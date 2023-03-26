package xs.bspl.commands.music;

import java.util.Queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;
import xs.bspl.util.Formatter;

public class SkipCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("skip", "s");
	}

	@Override
	protected Description description() {
		return description("<tracks>", "skips tracks");
	}

	@Override
	public void handle(Event event, String... args) {
		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();
		final Queue<AudioTrack> queue = scheduler.getQueue();
		final AudioTrack np = scheduler.getAudioPlayer().getPlayingTrack();

		if (np == null && queue.isEmpty()) { return; } 

		int skip_tracks = 1;

		if (args.length == 1) {
			try {
				skip_tracks = Math.max(Integer.parseInt(args[0]), 1);
			} catch (NumberFormatException ignored) {}
		}

		for (int i = 0; i < skip_tracks - 1; ++i) {
			if (queue.poll() == null) { break; }
		}

		EmbedMessage.replyTo(event.message)
			.setColor(ColorScheme.color())
			.setTitle(queue.isEmpty() ? 
				"Skipping the current track" : 
				(skip_tracks == 1 ? "Skipping the current track to play:" : String.format("Skipping %d tracks to play:", skip_tracks))
			).setDescription(Formatter.cleanString(queue.isEmpty() ? np.getInfo().title : queue.peek().getInfo().title))
			.setThumbnail(Formatter.getYoutubeThumbnail(queue.isEmpty() ? np.getInfo().uri : queue.peek().getInfo().uri))
			.send();

		scheduler.nextTrack();
	}
	
}
