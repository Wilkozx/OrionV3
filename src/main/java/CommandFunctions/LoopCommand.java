package CommandFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import Wrapper.DatabaseWrapper;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class LoopCommand {
        public static boolean loopCommand(ButtonInteractionEvent event) {
        event.deferReply().queue();
        
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            Boolean loop = db.loopToggle(event.getGuild().getId());
            if (loop) {
                MessageWrapper.genericResponse(event, "Loop Enabled", "The queue will now add played songs to the back of the queue.");
            } else {
                MessageWrapper.genericResponse(event, "Loop Disabled", "The queue will no longer add played songs back into the queue.");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
    }

    public static void loopCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Logger logger = Logger.getLogger("orion");
        Guild guild = event.getGuild();
        
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            Boolean loop = db.loopToggle(event.getGuild().getId());

            if (loop) {
                MessageWrapper.genericResponse(event, "Loop Enabled", "The queue will now add played songs to the back of the queue.");
                try {
                    Message message = guild.getTextChannelById(db.getActiveChannel(guild)).retrieveMessageById(db.getActiveMessage(guild.getId())).complete();

                    List<ActionRow> actionRows = message.getActionRows();
                    List<Button> actionBar = new ArrayList<>(actionRows.get(0).getButtons());

                    Button button = Button.secondary("loopoff", Emoji.fromFormatted("<:loop1:1201904065878179860>"));
                    actionBar.set(4, button);

                    List<ActionRow> newActionRows = new ArrayList<ActionRow>();
                    newActionRows.add(ActionRow.of(actionBar));
                    newActionRows.add(actionRows.get(1));

                    guild.getTextChannelById(db.getActiveChannel(guild)).editMessageEmbedsById(db.getActiveMessage(guild.getId()), message.getEmbeds().get(0)).setComponents(newActionRows).queue();
                    logger.info("Loop button updated for guild " + event.getGuild().getId());
                } catch (Exception e) {
                    logger.warning("Failed to update loop button for guild " + event.getGuild().getId());;
                }
            } else {
                MessageWrapper.genericResponse(event, "Loop Disabled", "The queue will no longer add played songs back into the queue.");
                try {
                    Message message = guild.getTextChannelById(db.getActiveChannel(guild)).retrieveMessageById(db.getActiveMessage(guild.getId())).complete();

                    List<ActionRow> actionRows = message.getActionRows();
                    List<Button> actionBar = new ArrayList<>(actionRows.get(0).getButtons());

                    Button button = Button.secondary("loopon", Emoji.fromFormatted("<:loop2:1201904067404894300>"));
                    actionBar.set(4, button);

                    List<ActionRow> newActionRows = new ArrayList<ActionRow>();
                    newActionRows.add(ActionRow.of(actionBar));
                    newActionRows.add(actionRows.get(1));

                    guild.getTextChannelById(db.getActiveChannel(guild)).editMessageEmbedsById(db.getActiveMessage(guild.getId()), message.getEmbeds().get(0)).setComponents(newActionRows).queue();
                    logger.info("Loop button updated for guild " + event.getGuild().getId());
                } catch (Exception e) {
                    logger.warning("Failed to update loop button for guild " + event.getGuild().getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
