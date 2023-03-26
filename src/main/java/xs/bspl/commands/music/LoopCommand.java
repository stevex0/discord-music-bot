package xs.bspl.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;

public class LoopCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("loop");
	}

	@Override
	protected Description description() {
		return description("<on/off>", "loops the current track");
	}

	@Override
	public void handle(Event event, String... args) {
		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();
		final AudioTrack np = scheduler.getAudioPlayer().getPlayingTrack();

		if (np == null || np.getInfo().isStream) {
			return;
		}

		boolean status = !scheduler.isRepeating();

		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("on")) {
				status = true;
			}
			if (args[0].equalsIgnoreCase("off")) {
				status = false;
			}
		}

		scheduler.setRepeat(status);

		EmbedMessage.replyTo(event.message)
			.setColor(ColorScheme.color())
			.setDescription(scheduler.isRepeating() ? 
				":repeat: Looping the current track" : 
				":arrow_right_hook: No longer looping the current track"
			).send();
	}
	
}
