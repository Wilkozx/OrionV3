package Listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import CommandFunctions.LoopCommand;
import CommandFunctions.PauseCommand;
import CommandFunctions.QueueCommand;
import CommandFunctions.ResumeCommand;
import CommandFunctions.ShuffleCommand;
import CommandFunctions.SkipCommand;
import CommandFunctions.StopCommand;
import Wrapper.MessageWrapper;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
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
                QueueCommand.queueCommand(event);
                break;
            case "pause":
                if (PauseCommand.pauseCommand(event)) {
                    List<ActionRow> actionRows = event.getMessage().getActionRows();
                    List<Button> actionBar = new ArrayList<>(actionRows.get(0).getButtons());

                    Button resumeButton = Button.secondary("resume", Emoji.fromFormatted("<:play1:1201898936705486868>"));
                    actionBar.set(2, resumeButton);

                    List<ActionRow> newActionRows = new ArrayList<ActionRow>();
                    newActionRows.add(ActionRow.of(actionBar));
                    newActionRows.add(actionRows.get(1));
                    event.getMessage().editMessage(event.getMessage().getContentRaw()).setComponents(newActionRows).queue();
                }
                break;
            case "resume":
                if (ResumeCommand.resumeCommand(event)) {
                    List<ActionRow> actionRows = event.getMessage().getActionRows();
                    List<Button> actionBar = new ArrayList<>(actionRows.get(0).getButtons());

                    Button pauseButton = Button.secondary("pause", Emoji.fromFormatted("<:pause1:1201898934054690926>"));
                    actionBar.set(2, pauseButton);

                    List<ActionRow> newActionRows = new ArrayList<ActionRow>();
                    newActionRows.add(ActionRow.of(actionBar));
                    newActionRows.add(actionRows.get(1));

                    event.getMessage().editMessage(event.getMessage().getContentRaw()).setComponents(newActionRows).queue();
                }
                break;
            case "shuffleon":
                if (ShuffleCommand.shuffleCommand(event)) {
                    List<ActionRow> actionRows = event.getMessage().getActionRows();
                    List<Button> actionBar = new ArrayList<>(actionRows.get(0).getButtons());

                    Button button = Button.secondary("shuffleoff", Emoji.fromFormatted("<:shuffle1:1201904069736665158>"));
                    actionBar.set(0, button);

                    List<ActionRow> newActionRows = new ArrayList<ActionRow>();
                    newActionRows.add(ActionRow.of(actionBar));
                    newActionRows.add(actionRows.get(1));

                    event.getMessage().editMessage(event.getMessage().getContentRaw()).setComponents(newActionRows).queue();
                }
                break;
            case "shuffleoff":
                if (ShuffleCommand.shuffleCommand(event)) {
                    List<ActionRow> actionRows = event.getMessage().getActionRows();
                    List<Button> actionBar = new ArrayList<>(actionRows.get(0).getButtons());

                    Button button = Button.secondary("shuffleon", Emoji.fromFormatted("<:shuffle2:1201904071087489034>"));
                    actionBar.set(0, button);

                    List<ActionRow> newActionRows = new ArrayList<ActionRow>();
                    newActionRows.add(ActionRow.of(actionBar));
                    newActionRows.add(actionRows.get(1));

                    event.getMessage().editMessage(event.getMessage().getContentRaw()).setComponents(newActionRows).queue();
                }
                break;
            case "loopon":
                if (LoopCommand.loopCommand(event)) {
                    List<ActionRow> actionRows = event.getMessage().getActionRows();
                    List<Button> actionBar = new ArrayList<>(actionRows.get(0).getButtons());

                    Button button = Button.secondary("loopoff", Emoji.fromFormatted("<:loop1:1201904065878179860>"));
                    actionBar.set(4, button);

                    List<ActionRow> newActionRows = new ArrayList<ActionRow>();
                    newActionRows.add(ActionRow.of(actionBar));
                    newActionRows.add(actionRows.get(1));

                    event.getMessage().editMessage(event.getMessage().getContentRaw()).setComponents(newActionRows).queue();
                }
                break;
            case "loopoff":
                if (LoopCommand.loopCommand(event)) {
                    List<ActionRow> actionRows = event.getMessage().getActionRows();
                    List<Button> actionBar = new ArrayList<>(actionRows.get(0).getButtons());

                    Button button = Button.secondary("loopon", Emoji.fromFormatted("<:loop2:1201904067404894300>"));
                    actionBar.set(4, button);

                    List<ActionRow> newActionRows = new ArrayList<ActionRow>();
                    newActionRows.add(ActionRow.of(actionBar));
                    newActionRows.add(actionRows.get(1));

                    event.getMessage().editMessage(event.getMessage().getContentRaw()).setComponents(newActionRows).queue();
                }
                break;
            default:
                MessageWrapper.errorResponse(event, "Invalid button, unknown error. Please try again.");
        }

    }
}
