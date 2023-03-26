package xs.bspl.commands.music.musicplayer;

import java.util.Map;
import java.util.TreeMap;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import xs.bspl.Bot;
import xs.bspl.commands.CommandManager;
import xs.bspl.commands.music.LeaveCommand;
import xs.bspl.util.ColorScheme;
import xs.bspl.util.EmbedMessage;
import xs.bspl.util.Formatter;

/** A voice activity manager for Discord guilds. It manages the bot's activity in a voice channel and
 *	removes itself from a voice channel if they have been alone for over a certain period of time. */
public class VoiceActivityManager {
	private boolean managing;
	private final Map<Long, Long> active_guilds;

	private static final long TEN_SECONDS = 10_000L;
	private static final long ONE_HOUR = 60_000L * 60;

	public VoiceActivityManager() {
		this.active_guilds = new TreeMap<>();
		this.managing = false;
	}

	/** Sets the specified guild as active in the voice activity manager. 
	 *	This means that the guild's voice channel activity will be managed by the manager.
	 *	@param guild_id The id of the guild to set as active. */
	public void setActive(Long guild_id) {
		this.active_guilds.put(guild_id, ONE_HOUR);

		check();
	}

	/** Sets the specified guild as unactive in the voice activity manager. 
	 *	This means that the guild's voice channel activity will no longer be managed by the manager.
	 *	@param guild_id The id of the guild to set as unactive. */
	public void setUnactive(Long guild_id) {
		this.active_guilds.remove(guild_id);
	}

	/** Checks the activity of the active guilds and manages their voice channels accordingly.
	 *	This method is called automatically when a guild is set as active. */
	private void check() {
		if (this.managing) { return; } // Already managing
		
		managing = true;
		new Thread(() -> {
			final JDA jda = Bot.get().getJDA();
			Bot.getControlPanel().makeLog("VOICE ACTIVITY MANAGER: STARTED MANAGING").logIt();
			while (managing) {
				try {
					Thread.sleep(TEN_SECONDS); // Wait 10 seconds before continuing 
				} catch (InterruptedException e) { Bot.getControlPanel().logError(e); }

				// There no active guilds to check
				if (this.active_guilds.isEmpty()) { this.managing = false; break; }

				// For each guild, check and modify their activity
				for (final Long guild_id : this.active_guilds.keySet()) {
					final Guild guild = jda.getGuildById(guild_id);
					final GuildVoiceState voice_state = guild.getSelfMember().getVoiceState();
					
					// If they are not in a voice channel we can remove them from being active
					if (!voice_state.inVoiceChannel()) { this.setUnactive(guild_id); continue; }
					
					final boolean is_alone = voice_state.getChannel().getMembers().size() == 1;
					final long time_remaining = this.active_guilds.get(guild_id);

					// If they have been alone for a while, then we remove the remaining time before they leave
					// else we say they are active and we reset the remaining time
					if (is_alone) {
						if (time_remaining == ONE_HOUR) {
							Bot.getControlPanel().makeLog("VOICE ACTIVITY MANAGER: INACTIVITY DETECTED")
								.addField("guild", Formatter.formatGuildInfo(guild))
								.logIt();
						}
						this.active_guilds.put(guild_id, time_remaining - TEN_SECONDS);
					} else {
						if (time_remaining != ONE_HOUR) {
							Bot.getControlPanel().makeLog("VOICE ACTIVITY MANAGER: ACTIVITY AFTER INACTIVITY DETECTED")
								.addField("guild", Formatter.formatGuildInfo(guild))
								.logIt();

							this.setActive(guild_id);
						}
					}

					// If they ran out of time, then they will leave the voice channel
					if (time_remaining <= 0) {
						((LeaveCommand) CommandManager.get().getCommand("leave")).leaveVoiceChannelIn(guild);
						EmbedMessage.sendTo(MusicPlayer.get().getMusicManager(guild).getMusicTextChannel())
							.setColor(ColorScheme.color())
							.setDescription(":alarm_clock: **Left due to inactivity!**")
							.send();
						
						this.setUnactive(guild_id); 

						Bot.getControlPanel().makeLog("VOICE ACTIVITY MANAGER: DISCONNECTION DUE TO ACTIVITY")
							.addField("guild", Formatter.formatGuildInfo(guild))
							.logIt();
					}
				}
			}
			Bot.getControlPanel().makeLog("VOICE ACTIVITY MANAGER: FINSIHED MANAGING").logIt();
		}).start();
	}
}
