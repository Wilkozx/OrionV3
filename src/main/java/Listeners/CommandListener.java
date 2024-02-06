package Listeners;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.logging.Logger;

public class CommandListener extends ListenerAdapter {
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Logger logger = Logger.getLogger("orion");
        logger.info("@" + Objects.requireNonNull(event.getGuild()).getName() + " #" + event.getChannel().getName() + " - " + event.getUser().getName() + ": " + event.getCommandString());

        if (event.getName().equalsIgnoreCase("join")) {
            CommandFunctions.JoinCommand.joinCommand(event);
        }

        if (event.getName().equalsIgnoreCase("play")) {
            CommandFunctions.PlayCommand.playCommand(event);
        }

        if (event.getName().equalsIgnoreCase("queue")) {
            CommandFunctions.QueueCommand.queueCommand(event);
        }

        if (event.getName().equalsIgnoreCase("pause")) {
            CommandFunctions.PauseCommand.pauseCommand(event);
        }

        if (event.getName().equalsIgnoreCase("resume")) {
            CommandFunctions.ResumeCommand.resumeCommand(event);
        }

        if (event.getName().equalsIgnoreCase("skip")) {
            CommandFunctions.SkipCommand.skipCommand(event);
        }

        if (event.getName().equalsIgnoreCase("stop")) {
            CommandFunctions.StopCommand.stopCommand(event);
        }

        if (event.getName().equalsIgnoreCase("shuffle")) {
            CommandFunctions.ShuffleCommand.shuffleCommand(event);
        }

        if (event.getName().equalsIgnoreCase("loop")) {
            CommandFunctions.LoopCommand.loopCommand(event);
        }

        if (event.getName().equalsIgnoreCase("remove")) {
            CommandFunctions.RemoveCommand.removeCommand(event);
        }

    }
}
