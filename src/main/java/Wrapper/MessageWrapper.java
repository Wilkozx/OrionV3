package Wrapper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class MessageWrapper {
    public static void genericResponse(SlashCommandInteractionEvent event, String title, String body, Color colour) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setDescription(body);
        embedBuilder.setColor(colour);

        event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public static void errorResponse(SlashCommandInteractionEvent event, String error) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(error);
        embedBuilder.setAuthor("Error", null, "https://banner2.cleanpng.com/20240115/egx/transparent-sad-face-unclear-expression-sad-yellow-face-with-closed-ey65a60b5e5e64d4.5105028617053807023866.jpg");
        embedBuilder.setColor(Color.red);

        event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
