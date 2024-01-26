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
            StringBuilder stringBuilder2 = new StringBuilder();

            ArrayList<Document> documentArrayList = databaseWrapper.getQueue(event.getGuild().getId());

            for (int i = 0; i < documentArrayList.size(); i++) {
                Document song = documentArrayList.get(i);
                String emoji = "";

                switch(song.get("platform").toString()) {
                    case "YOUTUBE":
                        emoji = "<:YoutubeIcon:1199809575252131950>";
                        break;
                    case "SOUNDCLOUD":
                        emoji = "<:SoundcloudIcon:1199809608659763200>";
                        break;
                    case "SPOTIFY":
                        emoji = "<:SpotifyIcon:1199809594030034984>";
                        break;
                    default:
                        emoji = "❓";
                }

                stringBuilder.append(i).append(song.keySet()).append(" ").append(song.values()).append("\n");
                stringBuilder2.append(emoji)
                              .append(" **" + (i+1) + ".** ")
                              .append("[")
                                  .append(song.get("songTitle").toString())
                              .append("]")
                              .append("(" + song.get("url").toString() + ")")
                              .append(" - " + song.get("artist").toString())
                              .append("\n");
            }

            MessageWrapper.genericResponse(event, "🎵 Current queue 🎵", String.valueOf(stringBuilder2));
            return true;
        } catch (DBEmptyQueueException | DBConnectionException e) {
            MessageWrapper.errorResponse(event, "Error " + e.getMessage());
        }


        return false;
    }

}
