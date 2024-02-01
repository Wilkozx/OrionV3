package CommandFunctions;

import net.dv8tion.jda.api.entities.GuildVoiceState;

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
}
