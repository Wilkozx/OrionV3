package Listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import CommandFunctions.PauseCommand;
import CommandFunctions.ResumeCommand;
import CommandFunctions.SkipCommand;
import CommandFunctions.StopCommand;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class ButtonListener extends ListenerAdapter{
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Logger logger = Logger.getLogger("orion");
        String eventType = event.getButton().getId().toLowerCase();
        logger.info(event.toString());
        switch (eventType) {
            case "skip":
                SkipCommand.skipCommand(event);
                break;
            case "stop":
                StopCommand.stopCommand(event);
                break;
            case "list":
                //QueueCommand.queueCommand(event);
                break;
            case "pause":
                if (PauseCommand.pauseCommand(event)) {
                    List<Button> actionBar = new ArrayList<>(event.getMessage().getActionRows().get(0).getButtons());
                    Button resumeButton = Button.secondary("resume", Emoji.fromFormatted("<:play1:1201898936705486868>"));
                    actionBar.set(2, resumeButton);
                    event.getMessage().editMessage(event.getMessage().getContentRaw()).setActionRow(actionBar).queue();
                }
                break;
            case "resume":
                if (ResumeCommand.resumeCommand(event)) {
                    List<Button> actionBar = new ArrayList<>(event.getMessage().getActionRows().get(0).getButtons());
                    Button pauseButton = Button.secondary("pause", Emoji.fromFormatted("<:pause1:1201898934054690926>"));
                    actionBar.set(2, pauseButton);
                    event.getMessage().editMessage(event.getMessage().getContentRaw()).setActionRow(actionBar).queue();
                }
                break;
            case "shuffleon":
                //ShuffleCommand.shuffleOnCommand(event);
                break;
            case "shuffleoff":
                //ShuffleCommand.shuffleOffCommand(event);
                break;
            case "loopon":
                //LoopCommand.loopOnCommand(event);
                break;
            case "loopoff":
                //LoopCommand.loopOffCommand(event);
                break;
        }

    }
}
