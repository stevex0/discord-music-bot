package xs.bspl.commands.music;

import xs.bspl.Config;
import xs.bspl.commands.Command;
import xs.bspl.commands.CommandManager;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.spotify.SpotifyPlayer;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;

public class PlayCommand extends Command {

	@Override
	protected Aliases aliases() {
		return aliases("play", "p");
	}

	@Override
	protected Description description() {
		return description("<search/link>", "plays a track with a link or a search");
	}

	@Override
	public void handle(Event event, String... args) {
		if (!event.self_member.getVoiceState().inVoiceChannel()) {
			CommandManager.get().getCommand("join").handle(event, args);
		}

		String search = String.join(" ", args);
		
		final String yt_regex = "^(http(s)?:\\/\\/)?((w){3}.)?youtu(be|.be)?(\\.com)?\\/.+";
		final String yt_playlist_index = "&list=LL&index=";

		if (search.startsWith("https://open.spotify.com/") || search.startsWith("open.spotify.com/")) {
			final String spotify_search = search;
			EmbedMessage.replyTo(event.message)
				.setColor(ColorScheme.color())
				.setDescription(":saluting_face: Working on converting the spotify link!")
				.setFooter("This may take some time depending on the amount of tracks! Don't worry!")
				.sendAndDeleteAfter(() -> {
					SpotifyPlayer.get().loadAndPlay(event, spotify_search);
				});

			return;
		}
		
		if (search.contains(yt_playlist_index)) {
			search = search.substring(0, search.indexOf(yt_playlist_index));
		}

		if (!search.matches(yt_regex)) { search = "ytsearch:" + search; }

		final String lofi = Config.get("LOFI_LINK");
		if (lofi != null && event.caller_user.getId().equals("333803933707272192") && search.equals(lofi)) {
			EmbedMessage.replyTo(event.message)
				.setColor(ColorScheme.color())
				.setTitle("Come on Brandon, lofi again...")
				.setIconUrl(event.caller_user.getAvatarUrl())
				.setDescription("The gif link doesn't work anymore btw T-T")
				.send();
		}

		MusicPlayer.get().loadAndPlay(event, search);
	}
	
}
