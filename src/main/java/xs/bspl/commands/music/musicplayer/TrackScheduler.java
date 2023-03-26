package xs.bspl.commands.music.musicplayer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import xs.bspl.Bot;
import xs.bspl.commands.CommandManager;
import xs.bspl.commands.music.NowPlayingCommand;

/** The TrackScheduler class is responsible for managing the audio tracks that are played by the AudioPlayer. */
public class TrackScheduler extends AudioEventAdapter {
	private final AudioPlayer audio_player;
	private final Guild guild;
	private final BlockingQueue<AudioTrack> queue;
	private final BlockingQueue<AudioTrack> requeue;

	private boolean repeating = false;
	private long track_start_time = 0;

	public TrackScheduler(AudioPlayer audio_player, Guild guild) {
		this.audio_player = audio_player;
		this.guild = guild;
		this.queue = new LinkedBlockingQueue<>();
		this.requeue = new LinkedBlockingQueue<>();
	}

	@Override
	public void onTrackStart(AudioPlayer player, AudioTrack track) {
		player.setPaused(false);
		
		final TextChannel channel = MusicPlayer.get().getMusicManager(guild).getMusicTextChannel();

		if (this.repeating || channel == null) { return; }

		try {
			final BufferedImage image = ((NowPlayingCommand) CommandManager.get().getCommand("nowplaying")).getNowPlayingImage(this, track, false);
			final File file = new File("nowplaying.png");
			ImageIO.write(image, "png", file);
			channel.sendMessage(":notes: **Now Playing: **<" + track.getInfo().uri + ">").addFile(file).queue(e -> {
				file.delete();
			});
		} catch (Exception e) { Bot.getControlPanel().logError(e); }

		track_start_time = System.currentTimeMillis();

		if (track.getInfo().isStream) { this.repeating = true; }
	}

	@Override
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext) {
			if (this.repeating) {
				this.audio_player.startTrack(track.makeClone(), false);
				return;
			}

			this.audio_player.setVolume(100);
			nextTrack();

			if (player.getPlayingTrack() == null && this.queue.isEmpty() &&!this.requeue.isEmpty()) {
				this.requeue.stream().forEach(this::queueTrack);
				this.requeue.clear();
			}
		}
	}
	/** Starts playing the next track in the queue */
	public void nextTrack() {
		this.audio_player.startTrack(this.queue.poll(), false);
		this.repeating = false;
	}

	/** Adds an AudioTrack to the queue.
	 *	@param track The AudioTrack to add */
	public void queueTrack(AudioTrack track) {
		if (!this.audio_player.startTrack(track, true)) {
			this.queue.offer(track);
		}
	}

	/** Returns whether the TrackScheduler is currently repeating the same track.
	 *	@return {@code true} if repeating, {@code false} otherwise */
	public boolean isRepeating() {
		return this.repeating;
	}

	/** Sets whether the TrackScheduler should repeat the same track.
	 *	@param b {@code true} to repeat, {@code false} otherwise */
	public void setRepeat(boolean b) {
		this.repeating = b;
	}

	/** Clears the audio track queue. */
	public void clearQueue() {
		this.queue.clear();
	}

	/** Returns the audio track queue.
	 *	@return The audio track queue */
	public Queue<AudioTrack> getQueue() {
		return this.queue;
	}

	/** Returns the audio track re-queue for repeating tracks.
	 *	This queue is used by the loop queue command. 
	 *	Any tracks left in this queue will be put back into the main queue once the main queue finishes.
	 *	@return The audio track re-queue */
	public Queue<AudioTrack> getRequeue() {
		return this.requeue;
	}

	/** Returns the audio player used by this track scheduler.
	 *	@return The audio player used by this track scheduler  */
	public AudioPlayer getAudioPlayer() {
		return this.audio_player;
	}

	/** Returns the elapsed time since the start of the current playing track, in milliseconds.
	 *	Use to get the elapse time of a livestream.
	 *	DO NOT USE THIS unless you are trying to get the elapse time of a livestream
	 *	@return The elapsed time since the start of the current playing track, in milliseconds  */
	public long elapseTimeMillis() {
		return System.currentTimeMillis() - this.track_start_time;
	}

	/** Returns the guild associated with this track scheduler.
	 *	@return The guild associated with this track scheduler */
	public Guild getGuild() {
		return this.guild;
	}
}
