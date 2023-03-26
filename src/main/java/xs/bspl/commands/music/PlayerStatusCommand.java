package xs.bspl.commands.music;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicManager;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;
import xs.bspl.util.Formatter;

public class PlayerStatusCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("status");
	}

	@Override
	protected Description description() {
		return description("display infomation of the player");
	}

	@Override
	protected boolean isHidden() {
		return true;
	}

	@Override
	public void handle(Event event, String... args) {
		final MusicManager manager = MusicPlayer.get().getMusicManager(event.guild);
		final TrackScheduler scheduler = manager.getScheduler();
		final AudioPlayer player = manager.getPlayer();
		final AudioTrack np = player.getPlayingTrack();

		EmbedMessage.replyTo(event.message)
			.setTitle("%s's Player Status:", event.self_user.getName())
			.setIconUrl(event.self_user.getAvatarUrl())
			.setColor(ColorScheme.color())
			.setThumbnail(np != null ? Formatter.getYoutubeThumbnail(np.getInfo().uri) : null)
			.addField("Currently Playing:", np != null ? np.getInfo().title : "---", false)
			.addField("Tracks in Queued:", " `" + scheduler.getQueue().size() + "` ", false)
			.addField("Queue Repeat:", " `" + (!scheduler.getRequeue().isEmpty() ? "true (" + scheduler.getRequeue().size() + ")" : "false") + "` ", false)
			.addField("Volume:", " `" + player.getVolume() + "%` ", true)
			.addField("Paused:", " `" + player.isPaused() + "` ", true)
			.addField("Repeat:", " `" + (np != null && np.getInfo().isStream ? "false" : scheduler.isRepeating()) + "` ", true)
			.send();
	}
	
}
