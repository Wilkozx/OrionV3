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

public class ShuffleCommand {
    
    public static boolean shuffleCommand(ButtonInteractionEvent event) {
        event.deferReply().queue();
        Guild guild = event.getGuild();
        
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            Boolean shuffle = db.shuffleToggle(guild.getId());
            if (shuffle) {
                MessageWrapper.genericResponse(event, "Shuffle Enabled", "The queue will now play in a random order.");
            } else {
                MessageWrapper.genericResponse(event, "Shuffle Disabled", "The queue will now play in order.");
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
    }

    public static void shuffleCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Logger logger = Logger.getLogger("orion");
        Guild guild = event.getGuild();
        
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            Boolean shuffle = db.shuffleToggle(event.getGuild().getId());
            if (shuffle) {
                MessageWrapper.genericResponse(event, "Shuffle Enabled", "The queue will now play in a random order.");
                try {
                    Message message = guild.getTextChannelById(db.getActiveChannel(guild)).retrieveMessageById(db.getActiveMessage(guild.getId())).complete();

                    List<ActionRow> actionRows = message.getActionRows();
                    List<Button> actionBar = new ArrayList<>(actionRows.get(0).getButtons());

                    Button button = Button.secondary("shuffleoff", Emoji.fromFormatted("<:shuffle1:1201904069736665158>"));
                    actionBar.set(0, button);

                    List<ActionRow> newActionRows = new ArrayList<ActionRow>();
                    newActionRows.add(ActionRow.of(actionBar));
                    newActionRows.add(actionRows.get(1));

                    guild.getTextChannelById(db.getActiveChannel(guild)).editMessageEmbedsById(db.getActiveMessage(guild.getId()), message.getEmbeds().get(0)).setComponents(newActionRows).queue();
                } catch (Exception e) {
                    logger.warning("Failed to update shuffle button for guild " + event.getGuild().getId());;
                }
            } else {
                MessageWrapper.genericResponse(event, "Shuffle Disabled", "The queue will now play in order.");
                try {
                    Message message = guild.getTextChannelById(db.getActiveChannel(guild)).retrieveMessageById(db.getActiveMessage(guild.getId())).complete();

                    List<ActionRow> actionRows = message.getActionRows();
                    List<Button> actionBar = new ArrayList<>(actionRows.get(0).getButtons());

                    Button button = Button.secondary("shuffleon", Emoji.fromFormatted("<:shuffle2:1201904071087489034>"));
                    actionBar.set(0, button);

                    List<ActionRow> newActionRows = new ArrayList<ActionRow>();
                    newActionRows.add(ActionRow.of(actionBar));
                    newActionRows.add(actionRows.get(1));

                    guild.getTextChannelById(db.getActiveChannel(guild)).editMessageEmbedsById(db.getActiveMessage(guild.getId()), message.getEmbeds().get(0)).setComponents(newActionRows).queue();
                    logger.info("Updated shuffle button for guild " + event.getGuild().getId());
                } catch (Exception e) {
                    logger.warning("Failed to update shuffle button for guild " + event.getGuild().getId());;
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

}
