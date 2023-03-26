package xs.bspl.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;
import xs.bspl.util.Formatter;

public class RewindCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("rewind", "rw");
	}

	@Override
	protected Description description() {
		return description("<seconds>", "rewind by a specific amount of seconds");
	}

	@Override
	public void handle(Event event, String... args) {
		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();
		final AudioTrack np = scheduler.getAudioPlayer().getPlayingTrack();

		if (np == null || np.getInfo().isStream || args.length != 1) { return; }

		try {
			final long rewind_millis = Integer.parseInt(args[0]) * 1000L;
			final long current_position = np.getPosition();
			final long new_position = current_position - rewind_millis;

			if (new_position >= np.getDuration() || new_position < 0) { return; }

			np.setPosition(new_position);

			EmbedMessage.replyTo(event.message)
				.setColor(ColorScheme.color())
				.setDescription(":rewind: Rewinding to ` %s `", Formatter.formatTime(new_position))
				.send();
		} catch (Exception ignored) {}
	}
	
}
