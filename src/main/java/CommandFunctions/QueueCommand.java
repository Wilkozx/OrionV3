package CommandFunctions;

import Errors.DBConnectionException;
import Errors.DBEmptyQueueException;
import Wrapper.DatabaseWrapper;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.bson.Document;

import java.util.ArrayList;
import java.util.logging.Logger;

public class QueueCommand {
    public static boolean queueCommand(SlashCommandInteractionEvent event) {
        Logger logger = Logger.getLogger("orion");

        event.deferReply().queue();
        try {
            DatabaseWrapper databaseWrapper = new DatabaseWrapper();
            StringBuilder stringBuilder = new StringBuilder();

            ArrayList<Document> documentArrayList = databaseWrapper.getQueue(event.getGuild().getId());

            for (int i = 0; i < documentArrayList.size(); i++) {
                Document song = documentArrayList.get(i);
                stringBuilder.append(i).append(song.keySet()).append(" ").append(song.values()).append("\n");
            }

            MessageWrapper.genericResponse(event, "Current queue", String.valueOf(stringBuilder));
            return true;
        } catch (DBEmptyQueueException | DBConnectionException e) {
            MessageWrapper.errorResponse(event, "Error " + e.getMessage());
        }


        return false;
    }

}
