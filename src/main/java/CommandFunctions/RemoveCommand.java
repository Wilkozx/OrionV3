package CommandFunctions;

import org.bson.Document;

import Wrapper.DatabaseWrapper;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class RemoveCommand {
    public static boolean removeCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        try {
            DatabaseWrapper db = new DatabaseWrapper();
            Document song = db.removeSong(event.getGuild().getId(), event.getOption("index").getAsInt());
            MessageWrapper.genericResponse(event, "Removed", "The song " + song.get("songTitle") + " has been removed from the queue.");
            return true;
        } catch (Exception e) {
            MessageWrapper.errorResponse(event, "Error " + "Failed to remove song from queue, please enter the number that appears next to the song when you look at the /queue.");
            e.printStackTrace();
            return false;
        }
    }
}
