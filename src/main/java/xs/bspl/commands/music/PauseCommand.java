package xs.bspl.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import xs.bspl.commands.Command;
import xs.bspl.commands.CommandManager;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;

public class PauseCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("pause");
	}

	@Override
	protected Description description() {
		return description("pauses the current track");
	}

	@Override
	public void handle(Event event, String... args) {
		final AudioPlayer player = MusicPlayer.get().getMusicManager(event.guild).getPlayer();
		if (player.getPlayingTrack() == null) {
			CommandManager.get().getCommand("nowplaying").handle(event);
			return;
		}

		final boolean paused = !player.isPaused();
		
		player.setPaused(paused);

		EmbedMessage.replyTo(event.message)
			.setColor(ColorScheme.color())
			.setDescription(paused ? ":pause_button: Player is paused" : ":arrow_forward: Player is unpaused")
			.send();
	}
	
}
