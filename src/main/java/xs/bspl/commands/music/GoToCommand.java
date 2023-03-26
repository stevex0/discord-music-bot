package xs.bspl.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;
import xs.bspl.util.Formatter;

public class GoToCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("goto");
	}

	@Override
	protected Description description() {
		return description("<seconds>", "go to a specific point of the current track");
	}

	@Override
	public void handle(Event event, String... args) {
		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();
		final AudioTrack np = scheduler.getAudioPlayer().getPlayingTrack();

		if (np == null || np.getInfo().isStream || args.length != 1) { return; }

		try {
			final long point_millis = Integer.parseInt(args[0]) * 1000L;
			if (point_millis >= np.getDuration()) { return; }

			np.setPosition(point_millis);

			EmbedMessage.replyTo(event.message)
				.setColor(ColorScheme.color())
				.setDescription(":watch: Going to ` %s `", Formatter.formatTime(point_millis))
				.send();
		} catch (Exception ignored) {}
	}
	
}
