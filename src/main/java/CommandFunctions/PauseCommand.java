package CommandFunctions;

import MusicPlayer.PlayerManager;
import Wrapper.DatabaseWrapper;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class PauseCommand {

    public static boolean pauseCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Logger logger = Logger.getLogger("orion");
        Guild guild = event.getGuild();

        short commandCheckResult = VoiceCommandChecks.checkVoiceState(event.getMember().getVoiceState(), event.getGuild().getSelfMember().getVoiceState());

        switch (commandCheckResult) {
            case 0:
                PlayerManager.get().getGuildMusicManager(Objects.requireNonNull(event.getGuild())).getTrackScheduler().pause();
                MessageWrapper.genericResponse(event, "Paused", "The song has been paused");
                try {
                    DatabaseWrapper db = new DatabaseWrapper();
                    Message message = guild.getTextChannelById(db.getActiveChannel(guild)).retrieveMessageById(db.getActiveMessage(guild.getId())).complete();
                    
                    List<ActionRow> actionRows = message.getActionRows();
                    List<Button> actionBar = new ArrayList<>(actionRows.get(0).getButtons());

                    Button button = Button.secondary("resume", Emoji.fromFormatted("<:play1:1201898936705486868>"));
                    actionBar.set(2, button);

                    List<ActionRow> newActionRows = new ArrayList<ActionRow>();
                    newActionRows.add(ActionRow.of(actionBar));
                    newActionRows.add(actionRows.get(1));

                    guild.getTextChannelById(db.getActiveChannel(guild)).editMessageEmbedsById(db.getActiveMessage(guild.getId()), message.getEmbeds().get(0)).setComponents(newActionRows).queue();
                } catch (Exception e) {
                    logger.warning("Failed to update pause button for guild " + event.getGuild().getId());
                    e.printStackTrace();
                }
                return true;
            case 1:
                MessageWrapper.errorResponse(event, "You need to be in a voice channel to pause a song!");
                return false;
            case 2:
                MessageWrapper.errorResponse(event, "I need to be in a voice channel to pause a song!");
                return false;
            case 3:
                MessageWrapper.errorResponse(event, "Orion is currently in another channel");
                return false;
            default:
                MessageWrapper.errorResponse(event, "Unknown error, try again.");
                return false;
        }
    }

    public static boolean pauseCommand(ButtonInteractionEvent event) {
        event.deferReply().queue();

        short commandCheckResult = VoiceCommandChecks.checkVoiceState(event.getMember().getVoiceState(), event.getGuild().getSelfMember().getVoiceState());

        switch (commandCheckResult) {
            case 0:
                PlayerManager.get().getGuildMusicManager(Objects.requireNonNull(event.getGuild())).getTrackScheduler().pause();
                MessageWrapper.genericResponse(event, "Paused", "The song has been paused");
                return true;
            case 1:
                MessageWrapper.errorResponse(event, "You need to be in a voice channel to pause a song!");
                return false;
            case 2:
                MessageWrapper.errorResponse(event, "I need to be in a voice channel to pause a song!");
                return false;
            case 3:
                MessageWrapper.errorResponse(event, "Orion is currently in another channel");
                return false;
            default:
                MessageWrapper.errorResponse(event, "Unknown error, try again.");
                return false;
        }
    }

    public static boolean pauseCommandDepricated(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Member member = event.getMember();
        assert member != null;
        GuildVoiceState memberVoiceState = member.getVoiceState();

        // first check - is user in voice channel? if not stop;
        assert memberVoiceState != null;
        if(!memberVoiceState.inAudioChannel()) {
            MessageWrapper.errorResponse(event, "You need to be in a voice channel to pause a song!");
            return false;
        }

        PlayerManager playerManager = PlayerManager.get();
        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        // second check - is the bot in a voice channel? if not join the user and pause;
        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
            MessageWrapper.genericResponse(event, "Joined & Paused", "Track by Artist");
            playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).getTrackScheduler().pause();
            return true;
        }

        // third check - if the bot is already in a channel then check if it's the same as the user execute; if not tell them off;
        if (selfVoiceState.inAudioChannel()) {
            if (selfVoiceState.getChannel() == memberVoiceState.getChannel()) {
                MessageWrapper.genericResponse(event, "Paused", "Track by Artist");
                playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).getTrackScheduler().pause();
            } else {
                MessageWrapper.errorResponse(event, "Orion is currently in another channel");
            }
            return true;
        }

        return false;
    }
}
