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

public class NextCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("next");
	}

	@Override
	protected Description description() {
		return description("<position>", "moves a specific track to position 1 of the queue");
	}

	@Override
	public void handle(Event event, String... args) {
		if (args.length != 1) return;

		int track_index;

		try {
			track_index = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			return;
		}

		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();
		final Queue<AudioTrack> queue = scheduler.getQueue();

		if (track_index <= 1 || track_index > queue.size()) { return; }

		final List<AudioTrack> track_list = new ArrayList<>(queue);
		final AudioTrack track = track_list.remove(track_index - 1);

		track_list.add(0, track);

		queue.clear();
		queue.addAll(track_list);

		EmbedMessage.replyTo(event.message)
			.setColor(ColorScheme.color())
			.setDescription(
				":arrow_lower_left: `#%d` **%s** is now the next track!", 
				track_index, Formatter.cleanString(track.getInfo().title)
			).send();
	}
	
}
