package xs.bspl.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import xs.bspl.commands.Command;
import xs.bspl.commands.CommandManager;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;

public class VolumeCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("volume", "v");
	}

	@Override
	protected Description description() {
		return description("[0-200]", "changes the volume of the current track");
	}

	@Override
	public void handle(Event event, String... args) {
		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();
		final AudioPlayer player = scheduler.getAudioPlayer();

		if (player.getPlayingTrack() == null) {
			CommandManager.get().getCommand("nowplaying").handle(event);

			return;
		}

		int volume = player.getVolume();

		if (args.length == 1) {
			try {
				volume = Math.max(0, Math.min(200, Integer.parseInt(args[0])));
			} catch (NumberFormatException ignored) {}
		}

		player.setVolume(volume);

		EmbedMessage.replyTo(event.message)
			.setColor(ColorScheme.color())
			.setDescription(":loud_sound: Volume set to ` %d `%%", volume)
			.send();
	}
	
}
