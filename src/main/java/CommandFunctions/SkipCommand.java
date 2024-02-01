package CommandFunctions;

import MusicPlayer.PlayerManager;
import Wrapper.DatabaseWrapper;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import okhttp3.internal.ws.RealWebSocket.Message;

import java.util.Objects;

import org.bson.Document;

import Errors.DBConnectionException;
import Errors.DBEmptyQueueException;

public class SkipCommand {

    public static boolean skipCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        short commandCheckResult = VoiceCommandChecks.checkVoiceState(event.getMember().getVoiceState(), event.getGuild().getSelfMember().getVoiceState());

        switch(commandCheckResult) {
            case 0:
                String skipMessage = "Song by Artist";
                try{
                    Document song = new DatabaseWrapper().getNowPlaying(event.getGuild().getId());
                    skipMessage = song.get("songTitle").toString() + " by " + song.get("artist").toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                skipSong(event.getGuild());
                MessageWrapper.genericResponse(event, "Skipped Song", skipMessage);
                return true;
            case 1:
                MessageWrapper.errorResponse(event, "You need to be in a voice channel to skip a song!");
                return false;
            case 2:
                MessageWrapper.errorResponse(event, "I need to be in a voice channel to skip a song!");
                return true;
            case 3:
                MessageWrapper.errorResponse(event, "Orion is currently in another channel");
                return false;
            default:
                MessageWrapper.errorResponse(event, "Unknown error, try again.");
                return false;
        }
    }

    public static boolean skipCommand(ButtonInteractionEvent event) {
        event.deferReply().queue();

        short commandCheckResult = VoiceCommandChecks.checkVoiceState(event.getMember().getVoiceState(), event.getGuild().getSelfMember().getVoiceState());

        switch(commandCheckResult) {
            case 0:
                String skipMessage = "Song by Artist";
                try{
                    Document song = new DatabaseWrapper().getNowPlaying(event.getGuild().getId());
                    skipMessage = song.get("songTitle").toString() + " by " + song.get("artist").toString();
                } catch (DBConnectionException | DBEmptyQueueException e) {
                    MessageWrapper.errorResponse(event, "There is not currently a song playing");
                }
                skipSong(event.getGuild());
                MessageWrapper.genericResponse(event, "Skipped Song", skipMessage);
                return true;
            case 1:
                MessageWrapper.errorResponse(event, "You need to be in a voice channel to skip a song!");
                return false;
            case 2:
                MessageWrapper.errorResponse(event, "I need to be in a voice channel to skip a song!");
                return true;
            case 3:
                MessageWrapper.errorResponse(event, "Orion is currently in another channel");
                return false;
            default:
                MessageWrapper.errorResponse(event, "Unknown error, try again.");
                return false;
        }
    }

    private static void skipSong(Guild guild) {
        try {
            new DatabaseWrapper().unsetNowPlaying(guild.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
        PlayerManager.get().getGuildMusicManager(guild).getTrackScheduler().stop();
    }

    public static boolean skipCommandDepricated(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Member member = event.getMember();
        assert member != null;
        GuildVoiceState memberVoiceState = member.getVoiceState();

        // first check - is user in voice channel? if not stop;
        assert memberVoiceState != null;
        if(!memberVoiceState.inAudioChannel()) {
            MessageWrapper.errorResponse(event, "You need to be in a voice channel to skip a song!");
            return false;
        }

        // if queue empty reply with 'nothing to skip'
        PlayerManager playerManager = PlayerManager.get();
        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        // second check - is the bot in a voice channel? if not join the user and skip;
        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            MessageWrapper.errorResponse(event, "I am not currently in a voice channel, please join a voice channel and try again.");
            return true;
        }

        // third check - if the bot is already in a channel then check if it's the same as the user execute; if not tell them off;
        if (selfVoiceState.getChannel() == memberVoiceState.getChannel()) {
            MessageWrapper.genericResponse(event, "Skipped", "Track by Artist");
            playerManager.getGuildMusicManager(event.getGuild()).getTrackScheduler().stop();
            return true;
        } else {
            MessageWrapper.errorResponse(event, "Orion is currently in another channel");
            return false;

        }

    }
}
