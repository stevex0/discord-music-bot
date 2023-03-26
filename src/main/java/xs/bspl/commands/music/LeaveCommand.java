package xs.bspl.commands.music;

import net.dv8tion.jda.api.entities.Guild;

import xs.bspl.commands.Command;
import xs.bspl.commands.Event;
import xs.bspl.commands.music.musicplayer.MusicManager;
import xs.bspl.commands.music.musicplayer.MusicPlayer;
import xs.bspl.commands.music.musicplayer.TrackScheduler;

public class LeaveCommand extends Command {

	@Override 
	protected Aliases aliases() {
		return aliases("leave", "l", "dc", "die");
	}

	@Override 
	protected Description description() {
		return description("leave the voice channel");
	}

	@Override 
	public void handle(Event event, String... args) {
		if (!event.self_member.getVoiceState().inVoiceChannel()) { return; }

		leaveVoiceChannelIn(event.guild);
	}
	
	public void leaveVoiceChannelIn(Guild guild) {
		final MusicManager manager = MusicPlayer.get().getMusicManager(guild);
		final TrackScheduler scheduler = manager.getScheduler();

		MusicPlayer.get().getVoiceActivityManager().setUnactive(guild.getIdLong());
		
		guild.getAudioManager().closeAudioConnection();
		
		scheduler.setRepeat(false);
		scheduler.clearQueue();
		
		manager.getPlayer().stopTrack();
		manager.setMusicTextChannel(null);
	}
	
}
