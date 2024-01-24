import Init.CommandBuilder;
import Listeners.CommandListener;
import MusicSearch.Spotify;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

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

        try {
            // Create Log File - Windows/Linux Compatible + Add SimpleFormatting
            FileHandler fileHandler = new FileHandler("logs" + System.getProperty("file.separator") + dtf.format(LocalDateTime.now()) + ".log");
            SimpleFormatter formatter = new SimpleFormatter();
            fileHandler.setFormatter(formatter);
            logger.addHandler(fileHandler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JDA Orion = JDABuilder.createDefault(token)
                .addEventListeners(new CommandListener())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .build().awaitReady();

        CommandBuilder.BuildCommands(Orion);

        // Generate Token on startup & Update Token every hour!
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Spotify.updateAccessToken();
            }
        };
        timer.scheduleAtFixedRate(task, 0, 3600000);
    }

}
