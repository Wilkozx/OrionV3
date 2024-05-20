package Wrapper;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

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

    public static void genericResponse(ButtonInteractionEvent event, String title, String body) { 
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setDescription(body);
        embedBuilder.setColor(new Color(255,69,0));

        event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
    }

    public static void genericResponse(ModalInteractionEvent event, String title, String body) { 
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

        event.getHook().sendMessageEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }

    public static void errorResponse(ButtonInteractionEvent event, String error) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(error);
        embedBuilder.setAuthor("Error", null, "https://www.lifepng.com/wp-content/uploads/2020/12/Letter-X-Roundlet-png-hd.png");
        embedBuilder.setColor(Color.red);

        event.getHook().sendMessageEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }

    public static void errorResponse(ModalInteractionEvent event, String error) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setDescription(error);
        embedBuilder.setAuthor("Error", null, "https://www.lifepng.com/wp-content/uploads/2020/12/Letter-X-Roundlet-png-hd.png");
        embedBuilder.setColor(Color.red);

        event.getHook().sendMessageEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }

    public static void errorResponse(TextChannel textChannel, String title, String body) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setDescription(body);
        embedBuilder.setAuthor("Error", null, "https://www.lifepng.com/wp-content/uploads/2020/12/Letter-X-Roundlet-png-hd.png");
        embedBuilder.setColor(Color.red);


        textChannel.sendMessageEmbeds(embedBuilder.build()).queue();
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
            Document settings = db.getSettings(guildID);

            buttons[1] = settings.getBoolean("loop");
            buttons[2] = settings.getBoolean("shuffle");
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
        Button play = Button.secondary("play", Emoji.fromFormatted("<:playsong:1204826666828959816>"));
        Button playlistAdd = Button.secondary("playlistAdd", Emoji.fromFormatted("<:slyskull:1012105662081290421>"));
        Button playlistLoad = Button.secondary("playlistLoad", Emoji.fromFormatted("<:slyskull:1012105662081290421>"));
        Button website = Button.secondary("website", Emoji.fromFormatted("<:website:1204827491634511882>"));

        textChannel.sendMessageEmbeds(embedBuilder.build()).setActionRow(shuffle, stop, playpause, skip, loop).addActionRow(list, play, website).queue(
            (message) -> {
                String messageID = message.getId();
                try {
                    new DatabaseWrapper().setActiveMessage(textChannel.getGuild().getId(), messageID);
                } catch (Exception e) {
                    logger.warning(e.getStackTrace().toString());
                }
                logger.info("Active message: " + messageID);
            }
        );
    }

    public static void Disconnected(GuildVoiceUpdateEvent event) {
        EmbedBuilder eb = new EmbedBuilder();

        eb.setColor(new Color(255, 85, 0));
        eb.setTitle("Shutting Down...");
        eb.setDescription("I have left  🔊 **" + event.getOldValue().getName() + "**");

        TextChannel channel = event.getGuild().getDefaultChannel().asTextChannel();
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            channel = event.getGuild().getTextChannelById(db.getActiveChannel(event.getGuild()));
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
            channel = event.getGuild().getTextChannelById(db.getActiveChannel(event.getGuild()));
        }  catch  (Exception ignore) {}

        channel.sendMessageEmbeds(eb.build()).queue();
    }

    public static void playModal(ButtonInteractionEvent event) {
        Modal.Builder mb = Modal.create("playsong", "Play Song");

        TextInput song = TextInput.create("song", "Enter a song to add to the queue", TextInputStyle.SHORT)
            .setPlaceholder("Song title or URL")
            .setMinLength(1)
            .setMaxLength(150)
            .setRequired(true)
            .build();

        mb.addComponents(ActionRow.of(song));

        event.replyModal(mb.build()).queue();
    }

    public static void queueResponse(SlashCommandInteractionEvent event, String title, String body) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setDescription(body);
        embedBuilder.setColor(new Color(255,69,0));

        // TODO: CHANGE EMOJIS FOR LASTPAGE / NEXTPAGE BELOW
        Button lastpage = Button.secondary("lastpage", Emoji.fromFormatted("<:stop1:1201904074690400286>"));
        Button nextpage = Button.secondary("nextpage", Emoji.fromFormatted("<:skip1:1201904072001585174>"));

        event.getHook().sendMessageEmbeds(embedBuilder.build()).setActionRow(lastpage, nextpage).queue();
    }

    public static void queueResponse(ButtonInteractionEvent event, String title, String body) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title);
        embedBuilder.setDescription(body);
        embedBuilder.setColor(new Color(255,69,0));

        // TODO: CHANGE EMOJIS FOR LASTPAGE / NEXTPAGE BELOW
        Button lastpage = Button.secondary("lastpage", Emoji.fromFormatted("<:stop1:1201904074690400286>"));
        Button nextpage = Button.secondary("nextpage", Emoji.fromFormatted("<:skip1:1201904072001585174>"));

        event.getHook().sendMessageEmbeds(embedBuilder.build()).setActionRow(lastpage, nextpage).queue();
    }
}
