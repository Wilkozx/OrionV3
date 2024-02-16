package CommandFunctions;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildVoiceState;

import java.util.logging.Logger;

public class VoiceCommandChecks {
    public static short checkVoiceState(GuildVoiceState memberVoiceState, GuildVoiceState selfVoiceState){

        if(!memberVoiceState.inAudioChannel()) {
            return 1;
        }
        if (!selfVoiceState.inAudioChannel()) {
            return 2;
        }
        if (!(selfVoiceState.getChannel() == memberVoiceState.getChannel())) {
            return 3;
        }
        return 0;
    }

    public static void isAnyoneListening(JDA orion) {
        Logger logger = Logger.getLogger("orion");

        orion.getGuilds().forEach(guild -> {
            if (guild.getAudioManager().getConnectedChannel() != null) {
                if (guild.getAudioManager().getConnectedChannel().getMembers().size() == 1) {
                    logger.info("Leaving " + guild.getName() + " due to inactivity");
                    guild.getAudioManager().closeAudioConnection();
                }
            }
        });
    }

}


