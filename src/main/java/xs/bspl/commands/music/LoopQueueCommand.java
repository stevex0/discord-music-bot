package xs.bspl.commands.music;

import java.util.Queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;

public class LoopQueueCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("loopqueue", "loopq", "lq");
	}

	@Override
	protected Description description() {
		return description("<on/off/status>", "loops the entire queue *(really scuffed)*");
	}

	@Override
	public void handle(Event event, String... args) {
		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();
		final AudioTrack np = scheduler.getAudioPlayer().getPlayingTrack();
		final Queue<AudioTrack> requeue = scheduler.getRequeue();
		final Queue<AudioTrack> queue = scheduler.getQueue();

		if (np == null || queue.isEmpty()) { return; }

		final boolean is_requeuing = !requeue.isEmpty();

		if (is_requeuing) { // toggle off
			requeue.clear();
			EmbedMessage.replyTo(event.message)
				.setColor(ColorScheme.color())
				.setDescription(":arrow_right: Not looping the current queue")
				.send();
		} else { // toggle on
			requeue.clear();
			requeue.add(np.makeClone());
			queue.stream()
				.filter(track -> !track.getInfo().isStream)
				.map(AudioTrack::makeClone)
				.forEachOrdered(requeue::add);
			
			EmbedMessage.replyTo(event.message)
				.setColor(ColorScheme.color())
				.setDescription(":arrows_counterclockwise: Looping the current the queue (%d tracks)", requeue.size())
				.send();
		}
	}
	
}
