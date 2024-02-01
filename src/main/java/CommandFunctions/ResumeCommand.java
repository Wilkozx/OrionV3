package CommandFunctions;

import MusicPlayer.PlayerManager;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.util.Objects;

public class ResumeCommand {
        public static boolean resumeCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        short commandCheckResult = VoiceCommandChecks.checkVoiceState(event.getMember().getVoiceState(), event.getGuild().getSelfMember().getVoiceState());

        switch (commandCheckResult) {
            case 0:
                PlayerManager.get().getGuildMusicManager(Objects.requireNonNull(event.getGuild())).getTrackScheduler().resume();
                MessageWrapper.genericResponse(event, "Resumed", "The song has been paused");
                return true;
            case 1:
                MessageWrapper.errorResponse(event, "You need to be in a voice channel to resume a song!");
                return false;
            case 2:
                MessageWrapper.errorResponse(event, "I need to be in a voice channel to resume a song!");
                return false;
            case 3:
                MessageWrapper.errorResponse(event, "Orion is currently in another channel");
                return false;
            default:
                MessageWrapper.errorResponse(event, "Unknown error, try again.");
                return false;
        }
    }

    public static boolean resumeCommand(ButtonInteractionEvent event) {
        event.deferReply().queue();

        short commandCheckResult = VoiceCommandChecks.checkVoiceState(event.getMember().getVoiceState(), event.getGuild().getSelfMember().getVoiceState());

        switch (commandCheckResult) {
            case 0:
                PlayerManager.get().getGuildMusicManager(Objects.requireNonNull(event.getGuild())).getTrackScheduler().resume();
                MessageWrapper.genericResponse(event, "Resumed", "The song has been resumed");
                return true;
            case 1:
                MessageWrapper.errorResponse(event, "You need to be in a voice channel to resume a song!");
                return false;
            case 2:
                MessageWrapper.errorResponse(event, "I need to be in a voice channel to resume a song!");
                return false;
            case 3:
                MessageWrapper.errorResponse(event, "Orion is currently in another channel");
                return false;
            default:
                MessageWrapper.errorResponse(event, "Unknown error, try again.");
                return false;
        }
    }
    
    public static boolean resumeCommandDepricated(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Member member = event.getMember();
        assert member != null;
        GuildVoiceState memberVoiceState = member.getVoiceState();

        //  check - is user in voice channel? if not stop;
        assert memberVoiceState != null;
        if(!memberVoiceState.inAudioChannel()) {
            MessageWrapper.errorResponse(event, "You need to be in a voice channel to resume a song!");
            return false;
        }

        PlayerManager playerManager = PlayerManager.get();
        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        //  check - is the bot in a voice channel? if not join the user and resume;
        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
            MessageWrapper.genericResponse(event, "Joined & Resumed", "Track by Artist");
            playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).getTrackScheduler().resume();
            return true;
        }

        //  check - if the bot is already in a channel then check if it's the same as the user execute; if not tell them off;
        if (selfVoiceState.inAudioChannel()) {
            if (selfVoiceState.getChannel() == memberVoiceState.getChannel()) {
                MessageWrapper.genericResponse(event, "Resumed", "Track by Artist");
                playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).getTrackScheduler().resume();
            } else {
                MessageWrapper.errorResponse(event, "Orion is currently in another channel");
            }
            return true;
        }

        return false;
    }

}
