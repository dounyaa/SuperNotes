package supernotes.management;

import com.google.api.client.util.DateTime;
import supernotes.fileHandling.FileHandler;
import supernotes.notes.ImageNote;
import supernotes.notes.Note;
import supernotes.notes.NoteFactory;
import supernotes.notionAPI.NotionApiManager;
import supernotes.notionAPI.NotionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLineInterface {
    private final NoteFactory textNoteFactory;
    private final NoteFactory imageNoteFactory;
    private final FileHandler fileHandler;
    private final NoteManager noteManager;
    private final NotionApiManager notionApiManager;
    private final NotionManager notionManager;

    public CommandLineInterface(NoteFactory textNoteFactory, NoteFactory imageNoteFactory, FileHandler fileHandler, NotionManager notionManager, NotionApiManager notionApiManager) {
        this.textNoteFactory = textNoteFactory;
        this.imageNoteFactory = imageNoteFactory;
        this.fileHandler = fileHandler;
        this.noteManager = new NoteManagerDataBase();
        this.notionApiManager = notionApiManager;
        this.notionManager = notionManager;
    }

    public void parseCommand(String command) throws SQLException {
        Pattern addNotePattern = Pattern.compile("sn add \"([^\"]*)\"(?: --tag ([^\"]*(?:\"[^\"]*\"[^\"]*)*)?)?");
        Matcher addNoteMatcher = addNotePattern.matcher(command);

        Pattern exportPDFPattern = Pattern.compile("^sn export --all \"(.*)\"$");
        Matcher exportPDFMatcher = exportPDFPattern.matcher(command);

        Pattern deleteNotesPattern = Pattern.compile("^sn delete --tag \"(.*)\"$");
        Matcher deleteNotesMatcher = deleteNotesPattern.matcher(command);

        Pattern exportPDFUsingTagPattern = Pattern.compile("^sn export --tag \"(.*)\" \"(.*)\"$");
        Matcher exportPDFUsingTagMatcher = exportPDFUsingTagPattern.matcher(command);

        Pattern exportPDFFilterTagPattern = Pattern.compile("sn export --word \"([^\"]*)\" \"([^\"]*)\"?");
        Matcher exportPDFFilterTagMatcher = exportPDFFilterTagPattern.matcher(command);

        Pattern getNotionPageContentPattern = Pattern.compile("sn notion get --page \"(.*)\"$");
        Matcher getNotionPageContentMatcher = getNotionPageContentPattern.matcher(command);

        Pattern updateNotionPageContentPattern = Pattern.compile("sn notion update \"(.*)\" --note \"(.*)\"$");
        Matcher updateNotionPageContentnMatcher = updateNotionPageContentPattern.matcher(command);

        Pattern createNotionPagePattern = Pattern.compile("sn notion create \"(.*)\"$");
        Matcher createNotionPageMatcher = createNotionPagePattern.matcher(command);

        Pattern helpPattern = Pattern.compile("sn --help");
        Matcher helpMatcher = helpPattern.matcher(command);

        Pattern showAllPattern = Pattern.compile("sn show notes");
        Matcher showAllMatcher = showAllPattern.matcher(command);

        Pattern addNoteWithReminderPattern = Pattern.compile("sn add \"([^\"]*)\"(?: --tag \"([^\"]*)\")? --reminder \"([^\"]*)\"");
        Matcher addNoteWithReminderMatcher = addNoteWithReminderPattern.matcher(command);

        Pattern getNoteWithReminderPattern = Pattern.compile("sn get --reminder --tag \"(.*)\"$");
        Matcher getNoteWithReminderMatcher = getNoteWithReminderPattern.matcher(command);

        Pattern deleteReminderForNotePattern = Pattern.compile("sn delete --reminder --tag \"(.*)\"$");
        Matcher deleteReminderForNoteMatcher = deleteReminderForNotePattern.matcher(command);

        Pattern exportTXTPattern = Pattern.compile("sn export --text \"(.*)\"$");
        Matcher exportTXTMatcher = exportTXTPattern.matcher(command);

        Pattern linkNotesPatternWithOR = Pattern.compile("sn link --id \"([^\"]+)\" --tag \"([^\"]+)\"(?: +or +\"([^\"]+)\")?(?:\\s+--name \"([^\"]+)\")?");
        Matcher linkNotesMatcherWithOR = linkNotesPatternWithOR.matcher(command);

        Pattern linkNotesPatternWithAND = Pattern.compile("sn link --id \"([^\"]+)\" --tag \"([^\"]+)\" and \"([^\"]+)\" --name \"([^\"]+)\"");
        Matcher linkNotesMatcherWithAND = linkNotesPatternWithAND.matcher(command);

        Pattern linkNotesPatternWithANDAndAt = Pattern.compile("sn link --id \"([^\"]+)\" --tag \"([^\"]+)\" and \"([^\"]+)\" --name \"([^\"]+)\" --at \"([^\"]+)\"");
        Matcher linkNotesMatcherWithANDAndAt = linkNotesPatternWithANDAndAt.matcher(command);

        Pattern linkNotesPatternWithANDAndBefore = Pattern.compile("sn link --id \"([^\"]+)\" --tag \"([^\"]+)\" and \"([^\"]+)\" --name \"([^\"]+)\" --before \"([^\"]+)\"");
        Matcher linkNotesMatcherWithANDAndBefore = linkNotesPatternWithANDAndBefore.matcher(command);

        Pattern linkNotesPatternWithANDAndAfter = Pattern.compile("sn link --id \"([^\"]+)\" --tag \"([^\"]+)\" and \"([^\"]+)\" --name \"([^\"]+)\" --after \"([^\"]+)\"");
        Matcher linkNotesMatcherWithANDAndAfter = linkNotesPatternWithANDAndAfter.matcher(command);

        Pattern showAllLinksByNamePattern = Pattern.compile("sn show --link\\s+\"([^\"]+)\"");
        Matcher showAllLinksByNameMatcher = showAllLinksByNamePattern.matcher(command);

        if (addNoteMatcher.matches()) {
            String noteContent = addNoteMatcher.group(1);
            String noteTag = addNoteMatcher.group(2);

            NoteFactory noteFactory = isImage(noteContent) ? imageNoteFactory : textNoteFactory;
            Note note = noteFactory.createNote(noteContent, noteTag, null, null);

            int id = noteManager.addNote(note);
            System.out.println("Note ajoutée avec succès avec l'identifiant : " + id);
        } else if (deleteNotesMatcher.matches()) {
            String noteTag = deleteNotesMatcher.group(1);

            noteManager.deleteByTag(noteTag);
            System.out.println("notes exporter avec succès !");

        } else if (exportPDFUsingTagMatcher.matches()) {
            String filePath = exportPDFUsingTagMatcher.group(2);
            String noteTag = exportPDFUsingTagMatcher.group(1);

            fileHandler.exportPdfFile(filePath, noteTag);
            System.out.println("notes exporter avec succès !");

        } else if (exportPDFFilterTagMatcher.matches()) {
            String filePath = exportPDFFilterTagMatcher.group(2);
            String filter = exportPDFFilterTagMatcher.group(1);

            fileHandler.exportPdfFileUsingFilter(filePath, filter);
            System.out.println("notes exporter avec succès !");

        } else if (exportPDFMatcher.matches()) {
            String filePath = exportPDFMatcher.group(1);

            fileHandler.exportPdfFile(filePath, null);
            System.out.println("notes exporter avec succès !");

        } else if (getNotionPageContentMatcher.matches()) {
            String pageId = getNotionPageContentMatcher.group(1);

            String notionPage = notionApiManager.retrievePageContent(pageId);
            String parentId = notionManager.extractParentPageId(notionPage);
            String content = notionManager.extractPageTitle(notionPage);

            // Vérifiez si la page existe déjà dans la base de données
            if (!noteManager.doesNoteExist(pageId)) {
                NoteFactory noteFactory = isImage(content) ? imageNoteFactory : textNoteFactory;
                Note note = noteFactory.createNote(content, "notion", parentId, pageId);
                noteManager.addNote(note);
                System.out.println("Page ajoutée à la base de données : ");
            }
        } else if (updateNotionPageContentnMatcher.matches()) {
            String newContent = updateNotionPageContentnMatcher.group(1);
            String oldContent = updateNotionPageContentnMatcher.group(2);

            String pageId = noteManager.getPageId(oldContent);
            if (pageId != null) {
                String propertiesJson = "{ \"properties\": { \"title\": [{\"text\": {\"content\": \"" + newContent + "\"}}] } }";

                String updatedPage = notionApiManager.updatePageProperties(pageId, propertiesJson);
                noteManager.updateNoteContentInDB(pageId, newContent);
            } else {
                System.out.println("Page introuvable");
            }
        } else if (createNotionPageMatcher.matches()) {
            String content = createNotionPageMatcher.group(1);

            // Vérifier si l'ID de page parent est déjà enregistré dans la base de données
            String parentPageId = noteManager.getParentPageId(); // Récupérer l'ID de page parent enregistré

            if (parentPageId == null) {
                // Demander à l'utilisateur d'entrer l'ID de la page parent pour la première utilisation
                System.out.println("Veuillez entrer l'ID de la page : ");
                Scanner scanner = new Scanner(System.in);
                parentPageId = scanner.nextLine();
            }

            String propertiesJson = "{ " + "\"parent\": { \"page_id\": \"" + parentPageId + "\"}, " + "\"properties\": { " + "\"title\": [{ \"text\": { \"content\": \"" + content + "\"} }], " + "\"Content\": [{ \"text\": { \"content\": \"Contenu de la note\" } }], " + "\"Tag\": [{ \"text\": { \"content\": \"Tag de la note\" } }] " + "} " + "}";

            NoteFactory noteFactory = isImage(content) ? imageNoteFactory : textNoteFactory;
            String newPage = notionApiManager.createNotionPage(parentPageId, propertiesJson);

            if (newPage != null && !newPage.isEmpty()) {
                String newPageId = notionManager.extractNewPageId(newPage);
                Note note = noteFactory.createNote(content, "notion", parentPageId, newPageId);
                System.out.println("Note ajoutée avec succès !");
                noteManager.addNote(note);

            }
        } else if (showAllMatcher.matches()) {
            List<Note> showAllNotes = new ArrayList<>();
            showAllNotes = noteManager.showAllNotes();
            System.out.println("Notes :- \n\n\n");
            showAllNotesDesigner(showAllNotes);
        } else if (getNoteWithReminderMatcher.matches()) {
            String tag = getNoteWithReminderMatcher.group(1);

            List<Note> allNotesByTag = noteManager.getByTag(tag);

            if (!allNotesByTag.isEmpty()) {
                for (Note note : allNotesByTag) {
                    int noteId = note.getId(); // Récupération de l'ID de la note

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                    List<LocalDateTime> reminders = noteManager.getReminders(noteId);

                    if (!reminders.isEmpty()) {
                        // Affiche les rappels associés à la note avec le tag spécifique
                        System.out.println("Rappels pour la note avec l'ID " + noteId + " : " + note.getContent());
                        for (LocalDateTime reminder : reminders) {
                            String formattedReminder = reminder.format(formatter);
                            System.out.println("- " + formattedReminder);
                        }
                    } else {
                        // Aucun rappel trouvé pour cette note
                        System.out.println("Aucun rappel trouvé pour la note avec l'ID " + noteId + " : " + note.getContent());
                    }
                }
            } else {
                System.out.println("Aucune note trouvée avec ce tag.");
            }
        } else if (helpMatcher.matches()) {
            displayHelp();
        } else if (addNoteWithReminderMatcher.matches()) {
            try {
                String noteContent = addNoteWithReminderMatcher.group(1);
                String noteTag = addNoteWithReminderMatcher.group(2);
                String reminder = addNoteWithReminderMatcher.group(3);

                // Création d'une nouvelle note
                NoteFactory noteFactory = isImage(noteContent) ? imageNoteFactory : textNoteFactory;
                Note note = noteFactory.createNote(noteContent, noteTag, null, null);
                int noteId = noteManager.addNote(note);

                DateTimeFormatter userDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
                LocalDateTime reminderDateTime = LocalDateTime.parse(reminder, userDateTimeFormatter);

                noteManager.addReminder(noteId, reminderDateTime);

                // Formatter pour le format attendu par Google Calendar
                DateTimeFormatter googleCalendarDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

                // Conversion de la date saisie par l'utilisateur au format LocalDateTime
                LocalDateTime localDateTime = LocalDateTime.parse(reminder, userDateTimeFormatter);
                String formattedDate = localDateTime.atOffset(ZoneOffset.UTC).format(googleCalendarDateTimeFormatter);

                DateTime startDateTime = new DateTime(formattedDate);

                noteManager.addNoteWithReminderToCalendar(noteContent, startDateTime.toString());

                System.out.println("Note avec rappel ajoutée avec succès !");
            } catch (DateTimeParseException e) {
                System.out.println("Format de date/heure invalide. Utilisez le format 'yyyy-MM-dd HH:mm'.");
            }
        } else if (deleteReminderForNoteMatcher.matches()) {
            String tag = deleteReminderForNoteMatcher.group(1);

            List<Note> allNotesByTag = noteManager.getByTag(tag);

            if (!allNotesByTag.isEmpty()) {
                boolean anyReminderDeleted = false;
                for (Note note : allNotesByTag) {
                    int noteId = note.getId(); // Récupération de l'ID de la note
                    boolean reminderDeleted = noteManager.deleteRemindersByNoteId(noteId);
                    if (!reminderDeleted) {
                        anyReminderDeleted = true;
                    } else {
                        // Aucun rappel trouvé pour cette note
                        System.out.println("Aucun rappel trouvé pour la note avec le tag '" + tag + "' : " + note.getContent());
                    }
                }
                if (anyReminderDeleted) {
                    System.out.println("Rappels pour les notes avec le tag '" + tag + "' ont été supprimés avec succès !");
                }
            } else {
                System.out.println("Aucune note trouvée avec ce tag.");
            }
        } else if (exportTXTMatcher.matches()) {
            String filePath = exportTXTMatcher.group(1);

            fileHandler.exportToText(filePath);
            System.out.println("notes exporter avec succès !");

        } else if (linkNotesMatcherWithANDAndAt.find()) {
            int noteId = Integer.parseInt(linkNotesMatcherWithANDAndAt.group(1));
            String[] tags;

            if (linkNotesMatcherWithANDAndAt.group(3) != null) {
                tags = new String[]{linkNotesMatcherWithANDAndAt.group(2), linkNotesMatcherWithANDAndAt.group(3)};
            } else {
                tags = new String[]{linkNotesMatcherWithANDAndAt.group(2)};
            }

            String linkName = linkNotesMatcherWithANDAndAt.group(4);
            String date = linkNotesMatcherWithANDAndAt.group(5);
            // Assuming noteManager.linkNotes returns an int
            int linkId = noteManager.linkNotesWithANDAtDate(noteId, tags, linkName, date);
            if (linkId == -1) {
                System.out.println("Erreur!");
            } else {
                System.out.println("Notes liées avec succès.");
            }
        } else if (linkNotesMatcherWithANDAndBefore.find()) {
            int noteId = Integer.parseInt(linkNotesMatcherWithANDAndBefore.group(1));
            String[] tags;

            if (linkNotesMatcherWithANDAndBefore.group(3) != null) {
                tags = new String[]{linkNotesMatcherWithANDAndBefore.group(2), linkNotesMatcherWithANDAndBefore.group(3)};
            } else {
                tags = new String[]{linkNotesMatcherWithANDAndBefore.group(2)};
            }

            String linkName = linkNotesMatcherWithANDAndBefore.group(4);
            String date = linkNotesMatcherWithANDAndBefore.group(5);
            // Assuming noteManager.linkNotes returns an int
            int linkId = noteManager.linkNotesWithANDBeforeDate(noteId, tags, linkName, date);
            if (linkId == -1) {
                System.out.println("Erreur!");
            } else {
                System.out.println("Notes liées avec succès.");
            }
        } else if (linkNotesMatcherWithANDAndAfter.find()) {
            int noteId = Integer.parseInt(linkNotesMatcherWithANDAndAfter.group(1));
            String[] tags;

            if (linkNotesMatcherWithANDAndAfter.group(3) != null) {
                tags = new String[]{linkNotesMatcherWithANDAndAfter.group(2), linkNotesMatcherWithANDAndAfter.group(3)};
            } else {
                tags = new String[]{linkNotesMatcherWithANDAndAfter.group(2)};
            }

            String linkName = linkNotesMatcherWithANDAndAfter.group(4);
            String date = linkNotesMatcherWithANDAndAfter.group(5);
            // Assuming noteManager.linkNotes returns an int
            int linkId = noteManager.linkNotesWithANDAfterDate(noteId, tags, linkName, date);
            if (linkId == -1) {
                System.out.println("Erreur!");
            } else {
                System.out.println("Notes liées avec succès.");
            }
        } else if (linkNotesMatcherWithAND.matches()) {
            int noteId = Integer.parseInt(linkNotesMatcherWithAND.group(1));
            String[] tags;

            if (linkNotesMatcherWithAND.group(3) != null) {
                tags = new String[]{linkNotesMatcherWithAND.group(2), linkNotesMatcherWithAND.group(3)};
            } else {
                tags = new String[]{linkNotesMatcherWithAND.group(2)};
            }

            String linkName = linkNotesMatcherWithAND.group(4);

            // Assuming noteManager.linkNotes returns an int
            int linkId = noteManager.linkNotesWithAND(noteId, tags, linkName);
            if (linkId == -1) {
                System.out.println("Erreur!");
            } else {
                System.out.println("Notes liées avec succès. Link ID: " + linkId);
            }
        } else if (linkNotesMatcherWithOR.matches()) {
            int noteId = Integer.parseInt(linkNotesMatcherWithOR.group(1));
            String[] tags;

            if (linkNotesMatcherWithOR.group(3) != null) {
                tags = new String[]{linkNotesMatcherWithOR.group(2), linkNotesMatcherWithOR.group(3)};
            } else {
                tags = new String[]{linkNotesMatcherWithOR.group(2)};
            }

            String linkName = linkNotesMatcherWithOR.group(4);

            // Assuming noteManager.linkNotes returns an int
            int linkId = noteManager.linkNotesWithOR(noteId, tags, linkName);
            if (linkId == -1) {
                System.out.println("Erreur!");
            } else {
                System.out.println("Notes liées avec succès. Link ID: " + linkId);
            }
        } else if (showAllLinksByNameMatcher.matches()) {
            String linkName = showAllLinksByNameMatcher.group(1);
            boolean linkExistenceCheck = noteManager.getAllLinksByName(linkName);
            if (!linkExistenceCheck) {
                System.out.println("No Links found with Link Name : " + linkName);
            }
        } else if (!command.equals("exit")) {
            System.out.println("Commande invalide. Tapez 'sn --help' pour afficher l'aide.");
        }
    }

    private boolean isImage(String path) {
        return path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".gif");
    }

    public void displayHelp() {
        System.out.println("Bienvenue dans SuperNotes !");
        System.out.println("Voici les commandes disponibles :");
        System.out.println("- Pour ajouter une note texte: sn add \"Contenu de la note\" --tag \"Tag de la note\" (le tag est facultatif)");
        System.out.println("- Pour ajouter une note image : sn add \"Chemin de l'image\" --tag \"Tag de la note\" (le tag est facultatif)");
        System.out.println("- Pour exporter toutes les notes en PDF : sn export --all \"Chemin du fichier PDF\"");
        System.out.println("- Pour exporter toutes les notes en TXT : sn export --text \"Chemin du fichier TXT\"");
        System.out.println("- Pour supprimer des notes par tag : sn delete --tag \"Tag de la note\"");
        System.out.println("- Pour exporter des notes par tag en PDF : sn export --tag \"Tag de la note\" \"Chemin du fichier PDF\"");
        System.out.println("- Pour exporter des notes filtrées par mot clé en PDF : sn export --word \"Mot clé\" \"Chemin du fichier PDF\" (le mot clé est facultatif)");
        System.out.println("- Pour obtenir le contenu d'une page Notion : sn notion get --page \"ID de la page\"");
        System.out.println("- Pour mettre à jour le contenu d'une page Notion : sn notion update \"Nouveau contenu\" --note \"Ancien contenu\"");
        System.out.println("- Pour créer une nouvelle page Notion : sn notion create \"Contenu de la nouvelle page\"");
        System.out.println("- Pour ajouter une note avec rappel : sn add \"Contenu de la note\" --tag \"Tag de la note\" --reminder \"Date et heure du rappel\"");
        System.out.println("- Pour afficher les rappels pour une note par tag : sn get --reminder --tag \"Tag de la note\"");
        System.out.println("- Pour supprimer les rappels pour une note par tag : sn delete --reminder --tag \"Tag de la note\"");
        System.out.println("- Pour afficher toutes les notes : sn show notes");
        System.out.println("Pour quitter l'application : exit");

        System.out.println("\nExemples :");
        System.out.println("- sn add \"Acheter du lait\" --tag \"Courses\"");
        System.out.println("- sn export --all \"C:\\Users\\Utilisateur\\Desktop\\notes.pdf\"");
        System.out.println("- sn delete --tag \"Courses\"");
        System.out.println("- sn notion get --page \"123456\"");
        System.out.println("- sn notion update \"Nouveau contenu\" --note \"Ancien contenu\"");
        System.out.println("- sn notion create \"Contenu de la nouvelle page\"");
        System.out.println("- sn add \"Rendez-vous chez le dentiste\" --tag \"Rendez-vous\" --reminder \"2023-12-31 09:00\"");
        System.out.println("- sn delete --reminder --tag \"Rendez-vous\"");
    }


    public void showAllNotesDesigner(List<Note> notes) {
        String boldText = "\u001B[1m";
        String resetText = "\u001B[0m";
        int noteLength = 33;
        int tagLength = 18;
        int timeLength = 22;
        for (Note n : notes) {
            String content = n.getContent().toString();
            if (content != null && ((content.length()) > noteLength)) {
                noteLength = content.length();
            }
        }
        for (Note n : notes) {
            String tag = n.getTag();
            if (tag != null && (tagLength < (tag.length()))) {
                tagLength = tag.length();
            }
        }
        for (int i = 0; i < noteLength; i++) {
            System.out.print("-");
        }
        for (int i = 0; i < tagLength; i++) {
            System.out.print("-");
        }
        for (int i = 0; i < (timeLength + 7); i++) {
            System.out.print("-");
        }
        String fh1 = "| Note Content";
        System.out.print("\n|" + boldText + " Note Content" + resetText);
        for (int i = 0; i < (noteLength - fh1.length() + 2); i++) {
            System.out.print(" ");
        }
        String fh2 = "| Tag";
        System.out.print("|" + boldText + " Tag" + resetText);
        for (int i = 0; i < (tagLength - (fh2.length()) + 2); i++) {
            System.out.print(" ");
        }
        String fh3 = "| Time";
        System.out.print("|" + boldText + " Time" + resetText);
        for (int i = 0; i < (timeLength - (fh3.length()) + 2); i++) {
            System.out.print(" ");
        }
        System.out.print("|\n");

        for (int i = 0; i < noteLength; i++) {
            System.out.print("-");
        }
        for (int i = 0; i < tagLength; i++) {
            System.out.print("-");
        }
        for (int i = 0; i < (timeLength + 7); i++) {
            System.out.print("-");
        }
        System.out.print("\n");

        for (Note n : notes) {
            String content = " ";

// Check if getContent() returns null before invoking toString()
            if (n.getContent() != null) {
                content = n.getContent().toString();
            } else {
                content = " ";
            }

// Check if getType() returns null before invoking equals()
            if (n.getType() != null && n.getType().equals("image")) {
                String path = ((ImageNote) n).getPath();
                content = path;
            }
            String tag = n.getTag();
            if (tag == null) {
                tag = " ";
            }
            String time = n.getTime();
            if (time == null) {
                time = " ";
            }

            int contentlength = content.length();

            int startPoint = 0;
            int endPoint = noteLength;
            int furtherCall = 0;

            if (contentlength > noteLength) {
                System.out.print("| " + content.substring(startPoint, endPoint));
                furtherCall = 1;
            } else System.out.print("| " + content);
            for (int i = 0; i < (noteLength - content.length()); i++) {
                System.out.print(" ");
            }
            System.out.print("| " + tag);
            for (int i = 0; i < (tagLength - tag.length()); i++) {
                System.out.print(" ");
            }
            System.out.print("| " + time);
            for (int i = 0; i < (timeLength - time.length()); i++) {
                System.out.print(" ");
            }
            System.out.print("|");
            while (furtherCall == 1) {
                contentlength = contentlength - noteLength - 1;
                startPoint = endPoint + 1;

                System.out.print("\n");
                if (contentlength > noteLength) {
                    endPoint = endPoint + noteLength + 1;
                    System.out.print("| " + content.substring(startPoint, endPoint));
                    for (int i = 0; i < (noteLength - (endPoint - startPoint)); i++) {
                        System.out.print(" ");
                    }
                    System.out.print("|");
                    for (int i = 0; i < tagLength + 1; i++) {
                        System.out.print(" ");
                    }
                    System.out.print("|");
                    for (int i = 0; i < timeLength + 1; i++) {
                        System.out.print(" ");
                    }
                    System.out.print("|");
                    furtherCall = 1;
                } else {
                    endPoint = endPoint + contentlength;
                    System.out.print("| " + content.substring(startPoint, endPoint));
                    for (int i = 0; i < (noteLength - (endPoint - startPoint)); i++) {
                        System.out.print(" ");
                    }
                    System.out.print("|");
                    for (int i = 0; i < tagLength + 1; i++) {
                        System.out.print(" ");
                    }
                    System.out.print("|");
                    for (int i = 0; i < timeLength + 1; i++) {
                        System.out.print(" ");
                    }
                    System.out.print("|");
                    furtherCall = 0;
                }
            }
            System.out.print("\n");
            System.out.print("|");
            for (int i = 0; i <= noteLength; i++) {
                System.out.print("-");
            }
            System.out.print("|");
            for (int i = 0; i <= tagLength; i++) {
                System.out.print("-");
            }
            System.out.print("|");
            for (int i = 3; i < (timeLength + 4); i++) {
                System.out.print("-");
            }
            System.out.print("|");
            System.out.print("\n");

        }
    }

}

