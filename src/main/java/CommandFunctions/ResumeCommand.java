package CommandFunctions;

import MusicPlayer.PlayerManager;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.util.Objects;

public class ResumeCommand {
    public static boolean resumeCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Member member = event.getMember();
        assert member != null;
        GuildVoiceState memberVoiceState = member.getVoiceState();

        // first check - is user in voice channel? if not stop;
        assert memberVoiceState != null;
        if(!memberVoiceState.inAudioChannel()) {
            MessageWrapper.errorResponse(event, "You need to be in a voice channel to resume a song!");
            return false;
        }

        PlayerManager playerManager = PlayerManager.get();
        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        // added
        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            PlayCommand.syncBotToUserChannel(event, memberVoiceState);
        }

        if (selfVoiceState.inAudioChannel()) {
            if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                event.reply("You need to be in the same channel to resume a song").queue();
                return false;
            }
        }

        // if queue empty reply with 'nothing to resume'

        PlayerManager playerManager = PlayerManager.get();
        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        // second check - is the bot in a voice channel? if not join the user and resume;
        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
            MessageWrapper.genericResponse(event, "Joined & Resumed", "Track by Artist");
            playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).getTrackScheduler().resume();
            return true;
        }

        // third check - if the bot is already in a channel then check if it's the same as the user execute; if not tell them off;
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
