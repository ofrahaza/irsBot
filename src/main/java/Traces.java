import javafx.application.Platform;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Traces {
    public Logger logger  = LogManager.getLogger();
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM HH:mm:ss:SSS ");

    public void writeInfo(String logMessage) {
       // Bot.console.setStyle("-fx-text-fill: red ;");
        if (Bot.logFlag) {
            logger.log(Level.INFO, logMessage);
            String s = "\n" + dateFormat.format(new Date()) + " INFO:    " + logMessage;
            Platform.runLater(() -> Bot.console.appendText(s));
        }
    }

    public void writeError(String logError) {
        logger.log(Level.ERROR,logError);
        String s = "\n" + dateFormat.format(new Date())+  " ERROR:     " + logError;
        Platform.runLater(() -> Bot.console.appendText(s));
    }
}
