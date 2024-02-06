package CommandFunctions;

import Wrapper.DatabaseWrapper;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

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
        
        try {
            DatabaseWrapper db = new DatabaseWrapper();
            Boolean loop = db.loopToggle(event.getGuild().getId());
            if (loop) {
                MessageWrapper.genericResponse(event, "Loop Enabled", "The queue will now add played songs to the back of the queue.");
            } else {
                MessageWrapper.genericResponse(event, "Loop Disabled", "The queue will no longer add played songs back into the queue.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
