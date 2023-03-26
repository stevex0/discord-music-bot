package xs.bspl.commands.music;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;

public class ShuffleCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("shuffle");
	}

	@Override
	protected Description description() {
		return description("shuffles the order of the tracks in the queue");
	}

	@Override
	public void handle(Event event, String... args) {
		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();
		final Queue<AudioTrack> queue = scheduler.getQueue();

		if (queue.size() < 2) return;

		final List<AudioTrack> queue_list = new ArrayList<>(queue);

		Collections.shuffle(queue_list);

		queue.clear();
		queue.addAll(queue_list);

		EmbedMessage.replyTo(event.message)
			.setColor(ColorScheme.color())
			.setDescription(":twisted_rightwards_arrows: The queue has been shuffled")
			.send();
	}
	
}
