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
        embedBuilder.setAuthor("Error", null, "https://www.lifepng.com/wp-content/uploads/2020/12/Letter-X-Roundlet-png-hd.png");
        embedBuilder.setColor(Color.red);

        event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
    }
}
