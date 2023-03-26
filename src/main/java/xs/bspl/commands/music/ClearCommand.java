package xs.bspl.commands.music;

import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;

public class ClearCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("clear");
	}

	@Override
	protected Description description() {
		return description("clears the queue and stops playing the current track");
	}

	@Override
	public void handle(Event event, String... args) {
		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();

		if (scheduler.getQueue().isEmpty() && scheduler.getAudioPlayer().getPlayingTrack() == null) {
			return;
		}

		scheduler.setRepeat(false);
		scheduler.clearQueue();
		scheduler.getAudioPlayer().stopTrack();

		if (!event.self_member.getVoiceState().inVoiceChannel()) { return; }

		EmbedMessage.replyTo(event.message)
			.setColor(ColorScheme.color())
			.setDescription("The current track has been stopped and the queue has been cleared")
			.send();
	}
	
}
