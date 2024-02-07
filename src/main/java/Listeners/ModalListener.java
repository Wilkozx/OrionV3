package Listeners;

import CommandFunctions.PlayCommand;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ModalListener extends ListenerAdapter {
    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        String modalType = event.getModalId();

        System.out.println(modalType);

        switch (modalType) {
            case "playsong":
                PlayCommand.playCommand(event);
                break;
        }
    }
}
