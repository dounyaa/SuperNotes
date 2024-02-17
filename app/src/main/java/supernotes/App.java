package supernotes;

import supernotes.file_handling.*;
import supernotes.helpers.InputScanner;
import supernotes.management.*;
import supernotes.notes.*;
import supernotes.notionAPI.NotionAPI;
import supernotes.notionAPI.NotionPageManager;

import java.sql.SQLException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) throws SQLException {
        FileManager fileManager = new FileManager();
        CommandLineInterface commandLineInterface = new CommandLineInterface(new TextNoteFactory(), new ImageNoteFactory(), fileManager, new NotionPageManager(), new NotionAPI());
        String command = "";
        Scanner scanner = InputScanner.getInstance();
        do {
            LOGGER.info("Veuillez entrer votre commande : ");
            command = scanner.nextLine();

            commandLineInterface.parseCommand(command);

        } while (!command.equals("exit"));

        scanner.reset();
    }
}
