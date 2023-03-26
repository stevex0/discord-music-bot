package xs.bspl.commands.music;

import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicPlayer;

public class JoinCommand extends Command {

	@Override 
	protected Aliases aliases() {
		return aliases("join", "j");
	}

	@Override 
	protected Description description() {
		return description("join your voice channel");
	}

	@Override 
	public void handle(Event event, String... args) {
		// Is caller in a voice channel
		if (!event.caller_member.getVoiceState().inVoiceChannel()) { return; }

		// Is bot already in a voice channel
		if (event.self_member.getVoiceState().inVoiceChannel()) { return; }

		final VoiceChannel vc = event.caller_member.getVoiceState().getChannel();
		final AudioManager audio_manager = event.guild.getAudioManager();

		audio_manager.openAudioConnection(vc);
		audio_manager.setSelfDeafened(true);

		MusicPlayer.get().getMusicManager(event.guild).setMusicTextChannel(event.channel);
		MusicPlayer.get().getVoiceActivityManager().setActive(event.guild.getIdLong());
	}
	
}
