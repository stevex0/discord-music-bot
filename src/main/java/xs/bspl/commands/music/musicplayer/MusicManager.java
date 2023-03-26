package xs.bspl.commands.music.musicplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

/** This class represents a music manager that manages an audio player,
 *	a track scheduler, and an audio send handler for specific guild. */
public class MusicManager {
	private final AudioPlayer audio_player;
	private final TrackScheduler track_scheduler;
	private final AudioSendHandler audio_send_handler;
	
	private TextChannel music_text_channel;

	public MusicManager(AudioPlayerManager audio_player_manager, Guild guild) {
		this.audio_player = audio_player_manager.createPlayer();
		this.track_scheduler = new TrackScheduler(this.audio_player, guild);
		this.audio_player.addListener(this.track_scheduler);
		this.audio_send_handler = new AudioPlayerSendHandler(this.audio_player);
	}

	/** Returns the audio player used by this MusicManager.
	 *	@return The audio player */
	public AudioPlayer getPlayer() {
		return this.audio_player;
	}

	/** Returns the track scheduler used by this MusicManager.
	 *	@return The track scheduler */
	public TrackScheduler getScheduler() {
		return this.track_scheduler;
	}

	/** Returns the audio send handler used by this MusicManager.
	 *	@return The audio send handler */
	public AudioSendHandler getSendHandler() {
		return this.audio_send_handler;
	}

	/** Returns the text channel where music messages are sent.
	 *	@return The text channel */
	public TextChannel getMusicTextChannel() {
		return this.music_text_channel;
	}
	
	/** Sets the text channel where music messages are sent.
	 *	@param channel The text channel */
	public void setMusicTextChannel(TextChannel channel) {
		this.music_text_channel = channel;
	}
	
}
