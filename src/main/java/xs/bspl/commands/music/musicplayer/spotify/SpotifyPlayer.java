package xs.bspl.commands.music.musicplayer.spotify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import xs.bspl.Bot;
import xs.bspl.Config;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.Formatter;
import xs.bspl.util.EmbedMessage;

/** The SpotifyPlayer class represents a player that can play tracks, playlists and albums from Spotify. */
public class SpotifyPlayer {
	
	private long access_token_time_generated;
	private final SpotifyApi api;

	private static SpotifyPlayer instance;

	/** Constructor that initializes the SpotifyApi object and generates an access token. */
	private SpotifyPlayer() {
		final String id = Config.get("SPOTIFY_CLIENT_ID");
		final String secret = Config.get("SPOTIFY_CLIENT_SECRET");

		api = (id == null || secret == null) ? 
			null : 
			new SpotifyApi.Builder()
				.setClientId(id)
				.setClientSecret(secret)
				.build();

		renewAccessTokenIfNecessary();
	}

	/** Renews the access token if it is expired or null because ~60 minutes the access token expires. */
	private void renewAccessTokenIfNecessary() {
		if (api == null) { return; }
		if (System.currentTimeMillis() < access_token_time_generated + 3_000_000L) { return; }

		try {
			final ClientCredentialsRequest.Builder request = new ClientCredentialsRequest.Builder(api.getClientId(), api.getClientSecret());
			final ClientCredentials credentials = request.grant_type("client_credentials").build().execute();
			final String access_token = credentials.getAccessToken();
			api.setAccessToken(access_token);
			access_token_time_generated = System.currentTimeMillis();
			Bot.getControlPanel().makeLog("SPOTIFY PLAYER: ACCESS TOKEN GENERATED").addField("access_token", access_token).logIt();
		} catch (Exception e) { Bot.getControlPanel().logError(e); }
	}

	/** Searches for a Spotify track, playlist or album by Spotify link.
	 *	@param spotify_link the Spotify link to search for.
	 *	@return A SpotifySearch object representing the search result, or {@code null} if the search failed. */
	private SpotifySearch search(String spotify_link) {
		renewAccessTokenIfNecessary();

		final String[] split = spotify_link.replace("https://open.spotify.com/", "").replace("user/spotify/", "").split("/");

		if (split.length != 2) { return null; }

		final String type = split[0].toLowerCase();
		final String id = split[1].contains("?si=") ? 
			split[1].substring(0, split[1].indexOf("?si=")) : 
			split[1];

		// if (type.length() == 0 || id.length() == 0) { return null; }

		if (type.equals(SpotifySearch.TRACK)) { return searchTrack(id); }
		if (type.equals(SpotifySearch.PLAYLIST)) { return searchPlaylist(id); }
		if (type.equals(SpotifySearch.ALBUM)) { return searchAlbum(id); }

		return null;
	}

	/** Searches for a track in Spotify based on its id.
	 *	@param id The id of the track to search for
	 *	@return A SpotifySearch object containing information about the track, or {@code null} if an error occurred */ 
	private SpotifySearch searchTrack(String id) {
		try {
			final List<String> results = new ArrayList<>();
			final Track track = api.getTrack(id).build().execute();
			final String raw_info = getRawTrackInfo(id);
			final Image[] image = track.getAlbum().getImages();

			if (raw_info != null) { results.add(raw_info); }

			return new SpotifySearch(SpotifySearch.TRACK, track.getName(), results, image.length > 0 ? image[0].getUrl() : null);
		} catch (Exception e) { Bot.getControlPanel().logError(e); return null; }
	}

	/** Searches for a Spotify playlist with the given id.
	 *	@param id The id of the playlist to search for
	 *	@return A SpotifySearch object containing information about the playlist, or {@code null} if an error occurs during the search */
	private SpotifySearch searchPlaylist(String id) {
		try {
			final List<String> results = new ArrayList<>();
			int offset = 0;
			for (;;) {
				final Paging<PlaylistTrack> paging = api.getPlaylistsItems(id).offset(offset).build().execute();

				Arrays.asList(paging.getItems()).parallelStream().forEach(e -> {
					final String raw_info = getRawTrackInfo(e.getTrack().getId());
					if (raw_info != null) { results.add(raw_info); }
				});

				offset += 100;
				if (offset >= paging.getTotal()) { break; }
			}

			final Playlist playlist = api.getPlaylist(id).build().execute();
			final Image[] image = playlist.getImages();

			return new SpotifySearch(SpotifySearch.PLAYLIST, playlist.getName(), results, image.length > 0 ? image[0].getUrl() : null);
		} catch (Exception e) { Bot.getControlPanel().logError(e); return null; }
	}
	/** Searches for a Spotify album with the given id.
	 *	@param id The id of the album to search for
	 *	@return A SpotifySearch object containing information about the album, or {@code null} if an error occurs during the search */
	private SpotifySearch searchAlbum(String id) {
		try {
			final List<String> results = new ArrayList<>();

			int offset = 0;
			for (;;) {
				final Paging<TrackSimplified> paging = api.getAlbumsTracks(id).offset(offset).build().execute();

				Arrays.asList(paging.getItems()).parallelStream().forEach(e -> {
					final String raw_info = getRawTrackInfo(e.getId());
					if (raw_info != null) { results.add(raw_info); }
				});

				offset += 100;
				if (offset >= paging.getTotal()) { break; }
			}

			final Album album = api.getAlbum(id).build().execute();
			final Image[] image = album.getImages();

			return new SpotifySearch(SpotifySearch.ALBUM, album.getName(), results, image.length > 0 ? image[0].getUrl() : null);
		} catch (Exception e) { Bot.getControlPanel().logError(e); return null; }
	}

	/** Retrieves the raw information about a given track based on its id.
	 *	@param id The id of the track to retrieve information for
	 *	@return A string containing the raw information about the track, or {@code null} if an error occurs while retrieving the information */
	private String getRawTrackInfo(String id) {
		try {
			final StringBuilder search = new StringBuilder();
			final Track track = api.getTrack(id).build().execute();
			
			// Append the title
			search.append(track.getName()).append(" - ");

			// Appends all the artist of the given track
			Arrays.asList(track.getArtists()).stream().forEach(e -> {
				search.append(e.getName()).append(" ");
			});

			return search.toString();
		} catch (Exception e) { Bot.getControlPanel().logError(e); return null; }
	}

	/** Attempts to search and queue tracks from a given Spotify link.
	 *	@param event The event that triggered this
	 *	@param spotify_link The Spotify link to search from */
	public void loadAndPlay(Event event, String spotify_link) {
		if (!isSupported()) { // We cannot use the spotify api if it is not set up properly 
			EmbedMessage.replyTo(event.message)
				.setColor(ColorScheme.error())
				.setDescription(":warning: Spotify is not supported because host did not set up it up properly :sob:")
				.send();

			Bot.getControlPanel().makeLog("SPOTIFY PLAYER ERROR: SPOTIFY SUPPORT WAS NOT SET UP PROPERLY").logIt();
			return;
		}

		// Spotify Link -> List of track names
		Bot.getControlPanel().makeLog("SPOTIFY PLAYER: SEARCHING")
			.addField("guild", Formatter.formatGuildInfo(event.guild))
			.addField("link", spotify_link)
			.logIt();

		final long start_search = System.currentTimeMillis();
		final SpotifySearch search = search(spotify_link);

		// An error occured while trying to convert the spotify link into a list of results
		if (search == null) {
			EmbedMessage.replyTo(event.message)
				.setColor(ColorScheme.error())
				.setDescription(":warning: Something went wrong trying to queue that! :sob:")
				.send();

			Bot.getControlPanel().makeLog("SPOTIFY PLAYER ERROR: SPOTIFY SEARCH FAILED")
				.addField("guild", Formatter.formatGuildInfo(event.guild))
				.addField("action_took", (System.currentTimeMillis() - start_search) / 1000.0)
				.logIt();

			return;
		}

		// The search result got nothing so we report an error
		if (search.results.isEmpty()) {
			EmbedMessage.replyTo(event.message)
				.setColor(ColorScheme.error())
				.setDescription(":warning: Unable to convert the spotify link! :sob:")
				.send();
			
			Bot.getControlPanel().makeLog("SPOTIFY PLAYER ERROR: SPOTIFY SEARCH FOUND NOTHING")
				.addField("guild", Formatter.formatGuildInfo(event.guild))
				.addField("action_took", (System.currentTimeMillis() - start_search) / 1000.0)
				.logIt();
			return;
		}

		// Beyond this point means that the search result was successful
		Bot.getControlPanel().makeLog("SPOTIFY PLAYER: SEARCH SUCCESSFUL | NOW CONVERTING")
			.addField("guild", Formatter.formatGuildInfo(event.guild))
			.addField("action_took", (System.currentTimeMillis() - start_search) / 1000.0)
			.logIt();

		// For each search result query, we try to get the AudioTrack through a youtube search.
		final TrackScheduler scheduler = MusicPlayer.get().getMusicManager(event.guild).getScheduler();
		final AudioPlayerManager manager = MusicPlayer.get().getPlayerManager();
		final List<String> no_match = new ArrayList<>();
		final long start_queue = System.currentTimeMillis();
		final List<AudioTrack> buffer = search.results.parallelStream()
			.map(query -> {
				final AtomicReference<AudioTrack> result_track = new AtomicReference<>();
				try {
					manager.loadItem("ytsearch:" + query, new AudioLoadResultHandler() {
						@Override
						public void trackLoaded(AudioTrack track) {
							result_track.set(track);
						}
						@Override
						public void playlistLoaded(AudioPlaylist playlist) {
							result_track.set(playlist.getTracks().get(0));
						}
						@Override
						public void noMatches() {
							no_match.add(query);
							Bot.getControlPanel().makeLog("SPOTIFY PLAYER ERROR: NO MATCH")
								.addField("guild", Formatter.formatGuildInfo(event.guild))
								.addField("query", query)
								.logIt();
						}
						@Override
						public void loadFailed(FriendlyException e) { Bot.getControlPanel().logError(e); }
					}).get(5, TimeUnit.SECONDS);
				} catch (Exception e) { Bot.getControlPanel().logError(e); return null; }
				return result_track.get();
			})
			.filter(Objects::nonNull)
			.collect(Collectors.toList());

		// We got no audiotracks from the youtube search
		if (buffer.isEmpty()) {
			EmbedMessage.replyTo(event.message)
				.setColor(ColorScheme.error())
				.setDescription(":x: Unable to convert tracks")
				.send();

			Bot.getControlPanel().makeLog("SPOTIFY PLAYER ERROR: SPOTIFY SEARCH FOUND NOTHING")
				.addField("guild", Formatter.formatGuildInfo(event.guild))
				.addField("action_took", (System.currentTimeMillis() - start_search) / 1000.0)
				.logIt();

			return;
		}
		
		// We report the tracks that we could not find audiotracks for
		if (!no_match.isEmpty()) {
			final StringBuilder missing_tracks = new StringBuilder();
			final int limit = 12;
			final int missing = no_match.size();
			for (int i = 0; i < Math.min(limit, missing); ++i) {
				missing_tracks.append(String.format("*%s*\n", no_match.get(i)));
			}
			if (limit > missing) {
				missing_tracks.append(String.format("and %d more...", limit - missing));
			}

			EmbedMessage.replyTo(event.message)
				.setColor(ColorScheme.error())
				.setTitle("Unable to find the following track%s:", missing == 1 ? "" : "s")
				.setDescription(missing_tracks.toString())
				.send();
		}

		// Report that we are adding the audiotracks into the queue
		EmbedMessage.replyTo(event.message)
			.setColor(ColorScheme.color())
			.setTitle(search.type.equals(SpotifySearch.TRACK) ? 
				"Adding to queue:" : 
				String.format("Adding %d tracks to the queue:", buffer.size())
			).setDescription(search.type.equals(SpotifySearch.TRACK) ?
				String.format("**%s**", search.results.get(0)) : 
				String.format("*%s*", search.title)
			).setThumbnail(search.thumbnail != null ? search.thumbnail : null)
			.send();

		// Queues all the audiotrack that we got from the youtube search into the track scheduler
		buffer.parallelStream().forEach(scheduler::queueTrack);

		Bot.getControlPanel().makeLog("SPOTIFY PALYER: SUCCESSFULLY QUEUED TRACKS!")
			.addField("guild", Formatter.formatGuildInfo(event.guild))
			.addField("action_took", (System.currentTimeMillis() - start_queue) / 1000.0)
			.logIt();
	}

	/** Returns if spotify is supported.
	 *	@return {@code true} if supported, {@code false} otherwise */
	public boolean isSupported() {
		return api != null;
	}

	/** Returns the instance of SpotifyPlayer 
	 *	@return The instance of SpotifyPlayer */
	public static SpotifyPlayer get() {
		if (SpotifyPlayer.instance == null) {
			SpotifyPlayer.instance = new SpotifyPlayer();
		}
		return SpotifyPlayer.instance;
	}

	/** Inner class encapsulates the necessary information from a search result */
	private static class SpotifySearch {
		public final String type;
		public final String title;
		public final String thumbnail;
		public final List<String> results;

		public static final String TRACK = "track";
		public static final String PLAYLIST = "playlist";
		public static final String ALBUM = "album";

		public SpotifySearch(String type, String title, List<String> results, String thumbnail) {
			this.type		= type;
			this.title		= title;
			this.results	= results;
			this.thumbnail	= thumbnail;
		}
	}
	
}
