package xs.bspl.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import xs.bspl.commands.Command;
import xs.bspl.commands.CommandManager;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;

public class ReplayCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("replay");
	}

	@Override
	protected Description description() {
		return description("restarts the current track");
	}

	@Override
	public void handle(Event event, String... args) {
		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();
		final AudioTrack current_track = scheduler.getAudioPlayer().getPlayingTrack();

		if (current_track == null) {
			CommandManager.get().getCommand("nowplaying").handle(event, args);
			return;
		}

		if (current_track.getInfo().isStream) { return; } // Livestreams cannot be restarted

		scheduler.getAudioPlayer().startTrack(current_track.makeClone(), false);

		EmbedMessage.replyTo(event.message)
			.setColor(ColorScheme.color())
			.setDescription(":leftwards_arrow_with_hook: Restarting current track")
			.send();
	}
	
}
