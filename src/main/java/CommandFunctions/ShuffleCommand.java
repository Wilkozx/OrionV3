package CommandFunctions;

import Wrapper.DatabaseWrapper;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class ShuffleCommand {
    
    public static boolean shuffleCommand(ButtonInteractionEvent event) {
        event.deferReply().queue();
        
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            Boolean shuffle = db.shuffleToggle(event.getGuild().getId());
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
        
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            Boolean shuffle = db.shuffleToggle(event.getGuild().getId());
            if (shuffle) {
                MessageWrapper.genericResponse(event, "Shuffle Enabled", "The queue will now play in a random order.");
            } else {
                MessageWrapper.genericResponse(event, "Shuffle Disabled", "The queue will now play in order.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

}
