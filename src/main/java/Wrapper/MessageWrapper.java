package Wrapper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import org.bson.Document;

import java.awt.*;
import java.util.logging.Logger;

public class MessageWrapper {
    public static void genericResponse(SlashCommandInteractionEvent event, String title, String body, Color colour) { //TRY NOT TO USE SEE BELOW
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setDescription(body);
        embedBuilder.setColor(colour);

        event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public static void genericResponse(SlashCommandInteractionEvent event, String title, String body) { //USE THIS IF YOU DONT NEED A SPECIAL COLOR
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setDescription(body);
        embedBuilder.setColor(new Color(255,69,0));

        event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public static void errorResponse(SlashCommandInteractionEvent event, String error) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(error);
        embedBuilder.setAuthor("Error", null, "https://www.lifepng.com/wp-content/uploads/2020/12/Letter-X-Roundlet-png-hd.png");
        embedBuilder.setColor(Color.red);

        event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public static void startedPlaying(TextChannel textChannel, Document song) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Now Playing");
        embedBuilder.setDescription("[" + song.get("songTitle").toString() + "](" + song.get("url").toString() + ") - " + song.get("artist").toString());
        embedBuilder.setFooter(song.get("platform").toString());
        embedBuilder.setColor(new Color(255,69,0));

        Boolean[] buttons = new Boolean[3];
        Logger logger = Logger.getLogger("orion");

        buttons[0] = false; //paused
        buttons[1] = false; //loop
        buttons[2] = false; //shuffle

        try {
            String guildID =  textChannel.getGuild().getId();
            DatabaseWrapper db = new DatabaseWrapper();
            Document nowPlaying = db.getNowPlaying(guildID);
            Document settings = db.getSettings(guildID);

            buttons[0] = nowPlaying.get("paused").toString().equals("true");
            buttons[1] = settings.get("loop").toString().equals("true");
            buttons[2] = settings.get("shuffle").toString().equals("true");
        } catch (Exception e) {
            logger.info(e.toString());
        }

        Button playpause;
        Button loop;
        Button shuffle;

        if (buttons[0]) {
            playpause =  Button.secondary("resume", Emoji.fromFormatted("<:play1:1201898936705486868>"));
        } else {
            playpause =  Button.secondary("pause", Emoji.fromFormatted("<:pause1:1201898934054690926>"));
        }

        if (buttons[1]) {
            loop =  Button.secondary("loopoff", Emoji.fromFormatted("<:loop1:1201904065878179860>"));
        } else {
            loop =  Button.secondary("loopon", Emoji.fromFormatted("<:loop2:1201904067404894300>"));
        }

        if (buttons[2]) {
            shuffle =  Button.secondary("shuffleoff", Emoji.fromFormatted("<:shuffle1:1201904069736665158>"));
        } else {
            shuffle =  Button.secondary("shuffleon", Emoji.fromFormatted("<:shuffle2:1201904071087489034>"));
        }
        
        Button stop = Button.secondary("stop", Emoji.fromFormatted("<:stop1:1201904074690400286>"));
        Button skip = Button.secondary("skip", Emoji.fromFormatted("<:skip1:1201904072001585174>"));
        Button list = Button.secondary("list", Emoji.fromFormatted("<:list1:1201904063143223306>"));

        textChannel.sendMessageEmbeds(embedBuilder.build()).setActionRow(shuffle, stop, playpause, skip, loop).addActionRow(list).queue();
    }

    public static void Disconnected(GuildVoiceUpdateEvent event) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setColor(new Color(255, 85, 0));
        eb.setTitle("Goodbye.");
        eb.setDescription("I have been destroyed and have left  🔊 **" + event.getOldValue().getName() + "**");

        TextChannel channel = event.getGuild().getDefaultChannel().asTextChannel();
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            channel = event.getGuild().getTextChannelById(db.getActiveChannel(event.getGuild().getId()));
        }  catch  (Exception ignore) {}

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    public static void Moved(GuildVoiceUpdateEvent event) {
        
        EmbedBuilder eb = new EmbedBuilder();

        eb.setColor(new Color(255, 85, 0));
        eb.setTitle("Active channel moved.");
        eb.setDescription("I have been moved from  🔊 **" + event.getChannelLeft().getName() + "** to 🔊 **" + event.getChannelJoined().getName() + "**");

        TextChannel channel = event.getGuild().getDefaultChannel().asTextChannel();
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            channel = event.getGuild().getTextChannelById(db.getActiveChannel(event.getGuild().getId()));
        }  catch  (Exception ignore) {}

        channel.sendMessageEmbeds(eb.build()).queue();
    }
}
