import CommandFunctions.VoiceCommandChecks;
import Init.CommandBuilder;
import Listeners.ButtonListener;
import Listeners.CommandListener;
import Listeners.DisconnectListener;
import Listeners.ModalListener;
import MusicSearch.SpotifyWrapper;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        // Get Token - Set Token
        Dotenv dotenv = Dotenv.load();
        String token = dotenv.get("TOKEN");

        // Create Logger - Create Date.Time Formatting
        final Logger logger = Logger.getLogger("orion");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd-HH.mm.ss");

        FileHandler fileHandler = null;

        try {
            fileHandler = new FileHandler("logs/latest.log", false);
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FileHandler finalFileHandler = fileHandler;
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            finalFileHandler.close();
            logger.removeHandler(finalFileHandler);
            File oldFile = new File("logs/latest.log");
            File newFile = new File("logs/" + dtf.format(LocalDateTime.now()) + ".log");
            newFile.getParentFile().mkdirs();
            if (oldFile.exists()) {
                oldFile.renameTo(newFile);
            }
        }));

        JDA Orion = JDABuilder.createDefault(token)
                .addEventListeners(new CommandListener(), new DisconnectListener(), new ButtonListener(), new ModalListener())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build().awaitReady();

        CommandBuilder.BuildCommands(Orion);
//        Orion.addEventListener(new DisconnectListener());
//        Orion.addEventListener(new GuildJoin());

        // Generate Token on startup & Update Token every hour!
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                SpotifyWrapper.updateAccessToken();
            }
        };
        timer.scheduleAtFixedRate(task, 0, 3600000);

        // Check if anyone is listening every 5 minutes
        Timer timer2 = new Timer();
        TimerTask task2 = new TimerTask() {
            @Override
            public void run() {
                VoiceCommandChecks.isAnyoneListening(Orion);
            }
        };
        timer2.scheduleAtFixedRate(task2, 0, 300000);

    }
}
