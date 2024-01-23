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
            CommandFunctions.PlayCommand.joinCommand(event);
        }

    }
}
