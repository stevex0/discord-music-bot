package xs.bspl.commands.music;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;
import xs.bspl.util.Formatter;

public class RemoveCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("remove", "r");
	}

	@Override
	protected Description description() {
		return description("<position>", "removes a specific track from the queue");
	}

	@Override
	public void handle(Event event, String... args) {
		if (args.length != 1) return;

		int track_index;

		try {
			track_index = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) { return; }

		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();
		final Queue<AudioTrack> queue = scheduler.getQueue();

		if (track_index < 1 || track_index > queue.size()) { return; }

		final List<AudioTrack> track_list = new ArrayList<>(queue);
		final AudioTrack removed_track = track_list.remove(track_index - 1);

		queue.clear();
		queue.addAll(track_list);

		EmbedMessage.replyTo(event.message)
			.setColor(ColorScheme.color())
			.setDescription(
				":wastebasket: Removed `#%d` **%s** from the queue", 
				track_index, Formatter.cleanString(removed_track.getInfo().title)
			).send();
	}
	
}
