package CommandFunctions;

import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;

public class PlayCommand {

    public static boolean joinCommand(SlashCommandInteractionEvent event) {
        Logger logger = Logger.getLogger("orion");
        event.deferReply().queue();
        Member member = event.getMember();

        // Check if the bot is already connected in a channel if so end;
        if(Objects.requireNonNull(event.getGuild()).getAudioManager().isConnected()) {
            MessageWrapper.errorResponse(event, "Orion occupies another nebula! Choose a different voice channel for stellar conversation.");
            logger.info("User " + event.getUser().getName() + " tried to use /join, however already connected in a channel.");
            return false;
        }

        // Check if the user is in a voice channel if so continue, if not end;
        assert member != null;
        if(!Objects.requireNonNull(member.getVoiceState()).inAudioChannel()) {
            MessageWrapper.errorResponse(event, "Uh oh, your trail is adrift in the void! Navigate to a voice channel to realign");
            logger.info("User " + event.getUser().getName() + " was not in a channel.");
            return false;
        }

        // Try to join the voice channel
        try {
            VoiceChannel userChannel = Objects.requireNonNull(member.getVoiceState().getChannel()).asVoiceChannel();
            joinVC(userChannel);

            updateActiveChannel(event.getChannel().asTextChannel());

            MessageWrapper.genericResponse(event, "Stardust trail forming... entering your nebula!", userChannel.getName());
            logger.info("Successfully joined " + userChannel.getName() + " at the request of " + event.getUser().getName() + ".");
            return true;

        } catch (Exception e) {
            logger.warning(e.getMessage());
            logger.warning(Arrays.toString(e.getStackTrace()));
        }

        MessageWrapper.errorResponse(event, "Starlight fluctuations! Verify permissions to connect Orion's cosmic network.");
        logger.info("There was an error joining " + event.getUser().getName() + "'s Voice channel.");

        return false;
    }

    public static void joinVC(VoiceChannel voiceChannel) {
        AudioManager audioManager = voiceChannel.getGuild().getAudioManager();
        audioManager.openAudioConnection(voiceChannel);
    }

    private static void updateActiveChannel(TextChannel textChannel) {

    }


}
