package xs.bspl.commands.music.musicplayer;

import java.nio.Buffer;
import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;

import net.dv8tion.jda.api.audio.AudioSendHandler;

/** Provide audio data to be sent to Discord voice connections 
 *	@see {@code AudioSendHandler} */
public class AudioPlayerSendHandler implements AudioSendHandler {
	private final AudioPlayer audio_player;
	private final ByteBuffer byte_buffer;
	private final MutableAudioFrame audio_frame;

	public AudioPlayerSendHandler(AudioPlayer audio_player) {
		this.audio_player = audio_player;
		this.byte_buffer = ByteBuffer.allocate(1024);
		this.audio_frame = new MutableAudioFrame();
		this.audio_frame.setBuffer(this.byte_buffer);
	}

	@Override
	public boolean canProvide() {
		return this.audio_player.provide(this.audio_frame);
	}

	@Override
	public ByteBuffer provide20MsAudio() {
		return (ByteBuffer) ((Buffer) this.byte_buffer).flip(); 
	}

	@Override
	public boolean isOpus() {
		return true;
	}
}
