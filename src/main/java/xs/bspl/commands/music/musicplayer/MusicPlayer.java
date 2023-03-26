package xs.bspl.commands.music.musicplayer;

import java.util.HashMap;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.api.entities.Guild;

import xs.bspl.Bot;
import xs.bspl.commands.Event;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.Formatter;
import xs.bspl.util.EmbedMessage;

/** The MusicPlayer class handles loading and playing music tracks  */
public class MusicPlayer {
	private static MusicPlayer instance;

	private final AudioPlayerManager audio_player_manager;
	private final VoiceActivityManager voice_activity_manager;
	private final Map<Long, MusicManager> music_managers; 

	private MusicPlayer() {
		this.audio_player_manager = new DefaultAudioPlayerManager();
		this.music_managers = new HashMap<>();
		this.voice_activity_manager = new VoiceActivityManager();

		AudioSourceManagers.registerRemoteSources(this.audio_player_manager);
        AudioSourceManagers.registerLocalSource(this.audio_player_manager);
	}

	/** Gets the MusicManager for the given guild.
	 *	@param guild The guild for which to get the MusicManager
	 *	@return The music manager for the given guild */
	public MusicManager getMusicManager(Guild guild) {
		long guild_id = guild.getIdLong();
		if (!music_managers.containsKey(guild_id)) {
			final MusicManager manager = new MusicManager(this.audio_player_manager, guild);
			guild.getAudioManager().setSendingHandler(manager.getSendHandler());
			music_managers.put(guild_id, manager);
		}
		return music_managers.get(guild_id);
	}

	/**	Gets the AudioPlayerManager used to manage the audio player.
	 *	@return The audio player manager used to manage the audio player. */
	public AudioPlayerManager getPlayerManager() {
		return audio_player_manager;
	}

	/** Loads and plays a track for the given event and search term.
	 *	@param event The context
	 *	@param search The search term for the track to be loaded */
	public void loadAndPlay(Event event, String search) {
		final MusicManager music_manager = getMusicManager(event.guild);
		final TrackScheduler scheduler = music_manager.getScheduler();

		music_manager.setMusicTextChannel(event.channel);

		this.audio_player_manager.loadItemOrdered(music_manager, search, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				scheduler.setRepeat(false);
				
				EmbedMessage.replyTo(event.message)
					.setColor(ColorScheme.color())
					.setTitle("Adding to queue:")
					.setDescription("*%s*", Formatter.cleanString(track.getInfo().title))
					.setUrl(track.getInfo().uri)
					.setThumbnail(Formatter.getYoutubeThumbnail(track.getInfo().uri))
					.send();
				
				scheduler.queueTrack(track);
			}
			
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				if (playlist.isSearchResult()) {
					trackLoaded(playlist.getTracks().get(0));
				} else {
					scheduler.setRepeat(false);
					
					EmbedMessage.replyTo(event.message)
						.setColor(ColorScheme.color())
						.setTitle("Adding %d tracks to the queue:", playlist.getTracks().size())
						.setDescription("*%s*", Formatter.cleanString(playlist.getName()))
						.setThumbnail(Formatter.getYoutubeThumbnail(playlist.getTracks().get(0).getInfo().uri))
						.send();

					
					playlist.getTracks()
						.stream()
						.forEach(scheduler::queueTrack);
				}
			}

			@Override
			public void noMatches() {
				EmbedMessage.replyTo(event.message)
					.setColor(ColorScheme.error())
					.setTitle("Could not find track:")
					.setDescription(":warning: *%s*", search.replace("ytsearch:", ""))
					.send();
			}
				
			@Override
			public void loadFailed(FriendlyException e) { Bot.getControlPanel().logError(e); }
		});
	}

	/** Returns the VoiceActivityManager instance used by the MusicPlayer.
	 *	The VoiceActivityManager is responsible for tracking voice activity in voice channels.
	 *	@return The voice activity manager */
	public VoiceActivityManager getVoiceActivityManager() {
		return this.voice_activity_manager;
	}

	/** Returns the singleton instance of MusicPlayer.
	 *	@return The singleton instance of MusicPlayer */
	public static MusicPlayer get() {
		if (MusicPlayer.instance == null) {
			MusicPlayer.instance = new MusicPlayer();
		}
		return MusicPlayer.instance;
	}
}
