package supernotes.management;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import supernotes.notes.ImageNote;
import supernotes.notes.Note;
import supernotes.notes.TextNote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SQLiteDBManager implements DBManager {
    public static final String ID_COLUMN_DEFINITION = "id INTEGER PRIMARY KEY AUTOINCREMENT";
    private static final Logger LOGGER = LoggerFactory.getLogger(SQLiteDBManager.class);
    private Connection connection;


    public SQLiteDBManager() {
        connect();
    }

    private void executeStatement(String sql) {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }
    }

    @Override
    public void createNotesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS notes (" + SQLiteDBManager.ID_COLUMN_DEFINITION + "," + "type TEXT NOT NULL," + // Champ pour indiquer le type de note (texte ou image)
                "content TEXT," + // Champs pour stocker le contenu texte
                "image BLOB," + // Champs pour stocker l'image en tant que BLOB
                "parent_page_id TEXT," + "page_id TEXT," + // Champs pour stocker l'ID de la page
                "time TEXT," + "path TEXT" + ")";

        executeStatement(sql);
    }

    @Override
    public void createTagsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Tags (" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "tag TEXT NOT NULL" + ")";

        executeStatement(sql);
    }

    @Override
    public void createNotesTagsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS NotesTags (" + "note_id INTEGER," + "tag_id INTEGER," + "PRIMARY KEY (note_id, tag_id)," + "FOREIGN KEY (note_id) REFERENCES Notes(id)," + "FOREIGN KEY (tag_id) REFERENCES Tags(id)" + ")";

        executeStatement(sql);
    }


    @Override
    public void createRemindersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Reminders (" + "id INT AUTO_INCREMENT PRIMARY KEY," + "note_id INT," + "reminder_date_time DATETIME," + "FOREIGN KEY (note_id) REFERENCES Notes(id)" + ")";

        executeStatement(sql);
    }

    @Override
    public void createLinkNotesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS LinkNotes (" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "linkname VARCHAR(255)," + "note1_id INTEGER," + "note2_id INTEGER," + "creation_date TEXT," + "FOREIGN KEY (note1_id) REFERENCES Notes(id)," + "FOREIGN KEY (note2_id) REFERENCES Notes(id)" + ")";
        try (Connection conn = getConnection()) {
            if (conn != null) {
                executeStatement(sql);
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }
    }

    @Override
    public void addReminder(int noteId, LocalDateTime reminderDateTime) {
        String sql = "INSERT INTO Reminders (note_id, reminder_date_time) VALUES (?, ?)";
        try {
            var conn = this.getConnection();
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, noteId);
                    pstmt.setTimestamp(2, Timestamp.valueOf(reminderDateTime));
                    pstmt.executeUpdate();
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }
    }


    @Override
    public List<LocalDateTime> getReminders(int noteId) {
        List<LocalDateTime> reminders = new ArrayList<>();

        String sql = "SELECT reminder_date_time FROM Reminders WHERE note_id = ?";
        try {
            var conn = this.getConnection();
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, noteId);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            Timestamp reminderTimestamp = rs.getTimestamp("reminder_date_time");
                            LocalDateTime reminderDateTime = reminderTimestamp.toLocalDateTime();
                            reminders.add(reminderDateTime);
                        }
                    }
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }

        return reminders;
    }


    @Override
    public boolean deleteRemindersByNoteId(int noteId) {
        String sql = "DELETE FROM reminders WHERE note_id = ?";
        boolean success = false;

        try {
            var conn = getConnection();
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, noteId);

                    int rowsAffected = pstmt.executeUpdate();
                    if (rowsAffected > 0) {
                        LOGGER.info("Tous les rappels pour la note avec l'ID {} ont été supprimés avec succès !", noteId);
                        success = true;
                    } else {
                        LOGGER.info("Aucun rappel trouvé pour la note avec l'ID {} ", noteId);
                    }
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.info("Erreur lors de la suppression des rappels pour la note avec l'ID {} : {}", noteId, e.getMessage());
        }

        return success;
    }


    @Override
    public int addTextNote(String content, String tag, String parentPageId, String pageId, String time) {
        String sql = "INSERT INTO notes (type, content, parent_page_id, page_id, time) VALUES (?, ?, ?, ?, ?)";
        int noteId = -1;

        try {
            var conn = getConnection();
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, "text");
                    pstmt.setString(2, content);
                    pstmt.setString(3, parentPageId);
                    pstmt.setString(4, pageId);
                    pstmt.setString(5, time);

                    int rowsInserted = pstmt.executeUpdate();
                    if (rowsInserted > 0) {
                        ResultSet generatedKeys = pstmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            noteId = generatedKeys.getInt(1); // Récupérer l'ID généré
                            if (tag != null) {
                                addTagsToNote(tag, noteId);
                            }
                        }
                    }
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }

        return noteId;
    }


    public int addImageNote(String path, byte[] imageBytes, String tags, String parentPageId, String pageId, String time) {
        String sql = "INSERT INTO notes (type, path, content, parent_page_id, page_id, time) VALUES (?, ?, ?, ?, ?, ?)";
        int noteId = -1;

        try {
            var conn = getConnection();
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, "image");
                    pstmt.setString(2, path);
                    pstmt.setBytes(3, imageBytes);
                    pstmt.setString(4, parentPageId);
                    pstmt.setString(5, pageId);
                    pstmt.setString(6, time);

                    int rowsInserted = pstmt.executeUpdate();
                    if (rowsInserted > 0) {
                        ResultSet generatedKeys = pstmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            noteId = generatedKeys.getInt(1); // Récupérer l'ID généré
                            addTagsToNote(tags, noteId);
                        }
                    }
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }

        return noteId;
    }

    private List<Integer> addTagsforNotes(String tag) {
        String[] tags = tag.split("\"\\s*\"");

        for (int i = 0; i < tags.length; i++) {
            tags[i] = tags[i].replace("\"", "").trim();
        }

        List<Integer> tagIds = new ArrayList<>();

        var conn = this.getConnection();
        if (conn != null) {
            for (String tempTag : tags) {
                String sql = "INSERT INTO Tags (tag) VALUES (?)";

                try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, tempTag);
                    pstmt.executeUpdate();

                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int tagId = generatedKeys.getInt(1);
                            tagIds.add(tagId);
                        } else {
                            throw new SQLException("Échec de la récupération de la clé générée pour le tag");
                        }
                    }
                } catch (SQLException e) {
                    LOGGER.error("Une erreur s'est produite.", e);
                }
            }
        } else {
            LOGGER.info("La connexion à la base de données est nulle.");
        }

        return tagIds;
    }


    private void addTagsToNote(String tag, int noteId) {
        createTagsTable();
        createNotesTagsTable();
        List<Integer> tempTagIds = addTagsforNotes(tag);

        for (Integer tagId : tempTagIds) {
            // Associe les tags à la note dans la table NotesTags
            String sql = "INSERT INTO NotesTags (note_id, tag_id) VALUES (?, ?)";

            try {
                var conn = this.getConnection();
                if (conn != null) {
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setInt(1, noteId);
                        pstmt.setInt(2, tagId);
                        pstmt.executeUpdate();
                    }
                } else {
                    LOGGER.info("La connexion à la base de données est nulle.");
                }
            } catch (SQLException e) {
                LOGGER.error("Une erreur s'est produite.", e);
            }
        }
    }


    @Override
    public void deleteNoteByNoteId(int noteId) {
        String sql = "DELETE FROM notes WHERE id = ?";
        try {
            var conn = this.getConnection();
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, noteId);
                    pstmt.executeUpdate();
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }
    }

    @Override
    public ArrayList<Note> getNotesByType(String type) {
        String sql = "SELECT * FROM notes WHERE type = ?";

        ArrayList<Note> result = new ArrayList<>();

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int retrievedNoteId = rs.getInt("id");
                    String tags = rs.getString("tag_list");

                    if ("text".equals(type)) {
                        TextNote textNote = new TextNote(rs.getString("content"), tags, rs.getString("parent_page_id"), rs.getString("page_id"));
                        textNote.setId(retrievedNoteId);
                        textNote.setTime(rs.getString("time"));
                        result.add(textNote);
                    } else if ("image".equals(type)) {
                        ImageNote imageNote = new ImageNote(rs.getString("path"), rs.getBytes("content"), tags, rs.getString("parent_page_id"), rs.getString("page_id"));
                        imageNote.setId(retrievedNoteId);
                        imageNote.setTime(rs.getString("time"));
                        result.add(imageNote);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite lors de la récupération des notes de type " + type, e);
        }

        return result;
    }

    @Override
    public String searchResultFromOllama(String searchParameter) {
        try {
            createOllamaSearchTable();
            String searchResult = ollamaSearchProcessing(searchParameter);
            if (!searchResult.isEmpty()) {
                LocalDateTime dateTime = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
                String time = dateTime.format(formatter);
                int searchId = addSearchResultToTable(searchResult, time);
                String finalResult = "Search ID : " + searchId + "\nSearch Time : " + time + "\nSearch Result:\n" + searchResult;
                return finalResult;
            }
        } catch (Exception e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }
        return null;
    }

    private int addSearchResultToTable(String searchResult, String time) {
        String sql = "INSERT INTO OllamaSearchResults(search_result, time) VALUES (?,?)";
        int searchId = -1;
        Connection conn1 = getConnection();
        try {
            if (conn1 != null) {
                try (PreparedStatement pstmt = conn1.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    pstmt.setString(1, searchResult);
                    pstmt.setString(2, time);

                    int rowsInserted = pstmt.executeUpdate();
                    if (rowsInserted > 0) {
                        ResultSet generatedKeys = pstmt.getGeneratedKeys();
                        if (generatedKeys.next()) {
                            searchId = generatedKeys.getInt(1); // Récupérer l'ID généré

                        }
                    }
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }

        return searchId;
    }

    @Override
    public void createOllamaSearchTable() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS OllamaSearchResults (" + "id INTEGER PRIMARY KEY AUTOINCREMENT, " + "search_result TEXT NOT NULL, " + "time TEXT" + ")";
            executeStatement(sql);
        } catch (Exception e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }
    }

    @Override
    public ArrayList<Note> getNoteByNoteId(int noteId) {
        String sql = "SELECT * FROM notes WHERE id = ?";

        ArrayList<Note> result = new ArrayList<>();

        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, noteId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String type = rs.getString("type");
                    int retrievedNoteId = rs.getInt("id");
                    String tags = rs.getString("tag_list");

                    if ("text".equals(type)) {
                        TextNote textNote = new TextNote(rs.getString("content"), tags, rs.getString("parent_page_id"), rs.getString("page_id"));
                        textNote.setId(retrievedNoteId);
                        textNote.setTime(rs.getString("time"));
                        result.add(textNote);
                    } else if ("image".equals(type)) {
                        ImageNote imageNote = new ImageNote(rs.getString("path"), rs.getBytes("content"), tags, rs.getString("parent_page_id"), rs.getString("page_id"));
                        imageNote.setId(retrievedNoteId);
                        imageNote.setTime(rs.getString("time"));
                        result.add(imageNote);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite lors de la récupération de la note avec l'ID " + noteId, e);
        }

        return result;
    }


    @Override
    public void deleteNoteByTag(String tag) {
        String deleteNotesSql = "DELETE FROM notes WHERE id IN (SELECT note_id FROM NotesTags WHERE tag_id = (SELECT id FROM Tags WHERE tag = ?))";
        String deleteNotesTagsSql = "DELETE FROM NotesTags WHERE tag_id = (SELECT id FROM Tags WHERE tag = ?)";

        try {
            var conn = this.getConnection();
            if (conn != null) {
                try (PreparedStatement deleteNotesStmt = conn.prepareStatement(deleteNotesSql); PreparedStatement deleteNotesTagsStmt = conn.prepareStatement(deleteNotesTagsSql)) {

                    conn.setAutoCommit(false);

                    deleteNotesStmt.setString(1, tag);
                    deleteNotesStmt.executeUpdate();

                    deleteNotesTagsStmt.setString(1, tag);
                    deleteNotesTagsStmt.executeUpdate();

                    conn.commit();
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }
    }


    @Override
    public ArrayList<Note> getAllNotes() {
        String sql = "SELECT notes.*, GROUP_CONCAT(tags.tag, ',') AS tag_list " + "FROM notes " + "LEFT JOIN NotesTags ON notes.id = NotesTags.note_id " + "LEFT JOIN Tags ON NotesTags.tag_id = Tags.id " + "GROUP BY notes.id";

        var result = new ArrayList<Note>();

        try {
            var conn = this.getConnection();
            if (conn != null) {
                try (Statement stmt = conn.createStatement()) {
                    try (ResultSet rs = stmt.executeQuery(sql)) {

                        while (rs.next()) {
                            var type = rs.getString("type");
                            int noteId = rs.getInt("id");

                            String tags = rs.getString("tag_list");

                            if (type.equals("text")) {
                                TextNote textNote = new TextNote(rs.getString("content"), tags, rs.getString("parent_page_id"), rs.getString("page_id"));
                                textNote.setId(noteId);
                                textNote.setTime(rs.getString("time"));
                                result.add(textNote);
                            } else if (type.equals("image")) {
                                ImageNote imageNote = new ImageNote(rs.getString("path"), rs.getBytes("content"), tags, rs.getString("parent_page_id"), rs.getString("page_id"));
                                imageNote.setId(noteId);
                                imageNote.setTime(rs.getString("time"));
                                result.add(imageNote);
                            }
                        }
                    }
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }

        return result;
    }

    @Override
    public boolean getAllLinksByName(String linkName) {
        createLinkNotesTable();
        String sql = "SELECT * FROM LinkNotes WHERE linkname = ?";

        try (Connection conn = getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, linkName.trim());
                    try (ResultSet rs = pstmt.executeQuery()) {

                        boolean found = false;
                        if (!rs.next()) {
                            return false; // Aucun lien trouvé
                        }

                        // Variables pour garder la trace des informations précédentes
                        int tempNoteId = -1;
                        String tempDate = null;

                        do {
                            int note1Id = rs.getInt("note1_id");
                            int note2Id = rs.getInt("note2_id");
                            String creationDate = rs.getString("creation_date"); // Retrieve creation date

                            // Vérifiez si la date de création a changé
                            if (!creationDate.equals(tempDate)) {
                                if (tempDate != null) {
                                    LOGGER.info(" "); // Ajouter une ligne vide entre les groupes de dates
                                }
                                LOGGER.info("Created on : {} ", creationDate);
                                System.out.print("--------------------------------"); // La longueur de la ligne est fixe
                                LOGGER.info(" ");
                                tempDate = creationDate;
                            }

                            // Vérifiez si l'id de note a changé
                            if (note1Id != tempNoteId) {
                                LOGGER.info("note id = {}   --->  id = {}", note1Id, note2Id);
                                tempNoteId = note1Id;
                            } else {
                                // Imprimer des espaces pour aligner les sorties
                                LOGGER.info("             --->  id = {}", note2Id);
                            }
                        } while (rs.next());

                        found = true;

                        return found;
                    }
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.info("An error occurred: {} ", e.getMessage());
            LOGGER.error("Une erreur s'est produite.", e);
        }
        return false;
    }


    @Override
    public ArrayList<Note> getAllNotesByTag(String tag) {
        String sql = "SELECT notes.*, GROUP_CONCAT(tags.tag, ',') AS tag_list " + "FROM notes " + "LEFT JOIN NotesTags ON notes.id = NotesTags.note_id " + "LEFT JOIN Tags ON NotesTags.tag_id = Tags.id " + "WHERE tags.tag = ? " + "GROUP BY notes.id";

        var result = new ArrayList<Note>();

        try (var conn = this.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tag);

            try (ResultSet rs = stmt.executeQuery()) {
                // loop through the result set
                while (rs.next()) {
                    var type = rs.getString("type");
                    int noteId = rs.getInt("id");
                    String tags = rs.getString("tag_list");


                    if (type.equals("text")) {
                        TextNote textNote = new TextNote(rs.getString("content"), tags, rs.getString("parent_page_id"), rs.getString("page_id"));
                        textNote.setId(noteId);
                        result.add(textNote);
                    } else if (type.equals("image")) {
                        ImageNote imageNote = new ImageNote(rs.getString("path"), rs.getBytes("content"), tags, rs.getString("parent_page_id"), rs.getString("page_id"));
                        imageNote.setId(noteId);
                        result.add(imageNote);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.info(e.getMessage());
        }

        return result;
    }

    @Override
    public ArrayList<Note> getAllNotesLike(String contentMotif) {
        String sql = "SELECT notes.*, GROUP_CONCAT(tags.tag, ',') AS tag_list " + "FROM notes " + "LEFT JOIN NotesTags ON notes.id = NotesTags.note_id " + "LEFT JOIN Tags ON NotesTags.tag_id = Tags.id " + "WHERE notes.content LIKE ? " + "GROUP BY notes.id";

        var result = new ArrayList<Note>();

        try (var conn = this.getConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, "%" + contentMotif + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    var type = rs.getString("type");
                    int noteId = rs.getInt("id");

                    String tags = rs.getString("tag_list");

                    if (type.equals("text")) {
                        TextNote textNote = new TextNote(rs.getString("content"), tags, rs.getString("parent_page_id"), rs.getString("page_id"));
                        textNote.setId(noteId);
                        result.add(textNote);

                    } else if (type.equals("image")) {
                        ImageNote imageNote = new ImageNote(rs.getString("path"), rs.getBytes("content"), tags, rs.getString("parent_page_id"), rs.getString("page_id"));
                        imageNote.setId(noteId);
                        result.add(imageNote);
                    }
                }
            }

        } catch (SQLException e) {
            LOGGER.info(e.getMessage());
        }

        return result;
    }

    @Override
    public String getParentPageId() {
        String parentPageId = null;
        String sql = "SELECT parent_page_id FROM notes WHERE parent_page_id IS NOT NULL LIMIT 1";

        try {
            var conn = this.getConnection();
            if (conn != null) {
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    if (rs.next()) {
                        parentPageId = rs.getString("parent_page_id");
                    }
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }

        return parentPageId;
    }


    @Override
    public String getPageId(String content) {
        String sql = "SELECT page_id FROM notes WHERE content = ?";
        try (Connection conn = getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, content);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return rs.getString("page_id");
                        }
                    }
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }
        return null;
    }


    public void updateNoteContentInDB(String pageId, String newContent) {
        String sql = "UPDATE notes SET content = ? WHERE page_id = ?";
        try (Connection conn = getConnection()) {
            if (conn != null) {
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, newContent);
                    pstmt.setString(2, pageId);
                    pstmt.executeUpdate();
                    LOGGER.info("Contenu de la note mis à jour !");
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }
    }

    public boolean doesNoteExist(String pageId) {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM notes WHERE page_id = ?")) {
                    preparedStatement.setString(1, pageId);

                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            int count = resultSet.getInt(1);
                            return count > 0; // Si le compte est supérieur à zéro, la note existe
                        }
                    }
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }
        return false;
    }

    @Override
    public int linkNotesWithOR(int noteId, String[] tags, String linkName) throws SQLException {
        createLinkNotesTable();
        int linkId = -1;
        int noteId1 = noteId;
        boolean valid = doesNoteExistWithId(noteId1);
        if (noteId1 == 0 || !valid) {
            LOGGER.info("ID de note invalide");
            return linkId;
        }
        List<Integer> tagIds = findTagIdsWithOR(tags);
        if (tagIds.isEmpty()) {
            LOGGER.info("Aucune donnée n'existe avec le tag");
            return linkId;
        }
        List<Integer> associatedNoteIds = findAssociatedNoteIds(tagIds);
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
        String timestamp = dateTime.format(formatter);

        linkId = insertLinkNotes(noteId1, associatedNoteIds, linkName, timestamp);

        return linkId;
    }

    private boolean doesNoteExistWithId(int noteId) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT COUNT(*) FROM notes WHERE id = ?";
            if (conn != null) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, noteId);


                    try (ResultSet resultSet = stmt.executeQuery()) {
                        if (resultSet.next()) {
                            int count = resultSet.getInt(1);
                            return count > 0; // Return true if the note exists, false otherwise
                        }
                    }
                }
            } else {
                LOGGER.info("La connexion à la base de données est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }

        return false;
    }

    private List<Integer> findTagIdsWithOR(String[] tags) {
        List<Integer> tagIds = new ArrayList<>();

        try {
            StringBuilder orCondition = new StringBuilder();
            for (int i = 0; i < tags.length; i++) {
                orCondition.append("tag = ?");
                if (i < tags.length - 1) {
                    orCondition.append(" OR ");
                }
            }

            String sql = "SELECT id FROM Tags WHERE " + orCondition;

            try (Connection conn = getConnection()) {
                if (conn != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {

                        for (int i = 0; i < tags.length; i++) {
                            stmt.setString(i + 1, tags[i]);
                        }

                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                tagIds.add(resultSet.getInt("id"));
                            }
                        }
                    }
                } else {
                    LOGGER.info("La connexion à la base de données est nulle.");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }

        return tagIds;
    }

    private List<Integer> findTagIdsWithAND(String[] tags) {
        List<Integer> tagIds = new ArrayList<>();

        try {
            String inClausePlaceholders = String.join(",", Collections.nCopies(tags.length, "?"));

            String sql = "SELECT id FROM Tags WHERE tag IN (" + inClausePlaceholders + ")";

            try (Connection conn = getConnection()) {
                if (conn != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        for (int i = 0; i < tags.length; i++) {
                            stmt.setString(i + 1, tags[i]);
                        }

                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                tagIds.add(resultSet.getInt("id"));
                            }
                        }
                    }
                } else {
                    LOGGER.info("La connexion à la base de données est nulle.");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }

        return tagIds;
    }


    private List<Integer> findAssociatedNoteIds(List<Integer> tagIds) {
        List<Integer> noteIds = new ArrayList<>();

        try {
            StringBuilder orCondition = new StringBuilder();
            for (int i = 0; i < tagIds.size(); i++) {
                orCondition.append("tag_id = ?");
                if (i < tagIds.size() - 1) {
                    orCondition.append(" OR ");
                }
            }

            String sql = "SELECT DISTINCT(note_id) FROM NotesTags WHERE " + orCondition;

            try (Connection conn = getConnection()) {
                if (conn != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        for (int i = 0; i < tagIds.size(); i++) {
                            stmt.setInt(i + 1, tagIds.get(i));
                        }

                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                noteIds.add(resultSet.getInt("note_id"));
                            }
                        }
                    }
                } else {
                    LOGGER.info("La connexion à la base de données est nulle.");
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Aucun enregistrement trouvé pour les balises");
        }

        return noteIds;
    }


    public int insertLinkNotes(int noteId1, List<Integer> noteId2List, String linkName, String creationDate) {
        int linkId = -1;

        try (Connection conn = getConnection()) {
            if (conn != null) {
                conn.setAutoCommit(false);

                try {
                    String insertSql = "INSERT INTO LinkNotes (linkname, note1_id, note2_id, creation_date) VALUES (?, ?, ?, ?)";

                    try (PreparedStatement insertStatement = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        for (int noteId2 : noteId2List) {
                            insertStatement.setString(1, linkName);
                            insertStatement.setInt(2, noteId1);
                            insertStatement.setInt(3, noteId2);
                            insertStatement.setString(4, creationDate);
                            insertStatement.addBatch();
                        }

                        insertStatement.executeBatch();

                        try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                            if (generatedKeys.next()) {
                                linkId = generatedKeys.getInt(1);
                            }
                        }

                        conn.commit();
                    }
                } catch (SQLException e) {
                    LOGGER.info("SQLException during statement execution: {} ", e.getMessage());
                    conn.rollback();
                    throw e;
                }
            } else {
                LOGGER.info("Erreur lors de l'établissement de la connexion à la base de données : la connexion est nulle.");
            }
        } catch (SQLException e) {
            LOGGER.info("Erreur lors de l'établissement de la connexion à la base de données : {} ", e.getMessage());
            LOGGER.error("Une erreur s'est produite. ", e);
        }

        return linkId;
    }


    @Override
    public int linkNotesWithAND(int noteId, String[] tags, String linkName) throws SQLException {
        createLinkNotesTable();
        int linkId = -1;
        int noteId1 = noteId;
        boolean valid = doesNoteExistWithId(noteId1);
        if (noteId1 == 0 || !valid) {
            LOGGER.info("ID de note invalide");
            return linkId;
        }
        List<Integer> tagIds = findTagIdsWithAND(tags);
        if (tagIds.isEmpty() || tagIds.size() == 1) {
            LOGGER.info("Aucune donnée n'existe avec le tag");
            return linkId;
        }
        List<Integer> associatedNoteIds = findAssociatedNoteIds(tagIds);
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
        String timestamp = dateTime.format(formatter);

        linkId = insertLinkNotes(noteId1, associatedNoteIds, linkName, timestamp);

        return linkId;
    }

    @Override
    public int linkNotesWithANDAtDate(int noteId, String[] tags, String linkName, String date) throws SQLException {
        createLinkNotesTable();
        int linkId = -1;
        int noteId1 = noteId;
        boolean valid = doesNoteExistWithId(noteId1);
        if (noteId1 == 0 || !valid) {
            LOGGER.info("ID de note invalide");
            return linkId;
        }
        List<Integer> tagIds = findTagIdsWithAND(tags);
        if (tagIds.isEmpty() || tagIds.size() == 1) {
            LOGGER.info("Aucune donnée n'existe avec le tag");
            return linkId;
        }
        List<Integer> associatedNoteIds = findAssociatedNoteIds(tagIds);
        List<Integer> associatednoteIdsAtDate = findAssociatedNoteIdsAtDate(associatedNoteIds, date);
        if (associatednoteIdsAtDate.isEmpty()) {
            LOGGER.info("Aucune donnée n'existe pour la date: {} ", date);
            return linkId;
        }
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
        String timestamp = dateTime.format(formatter);

        linkId = insertLinkNotes(noteId1, associatednoteIdsAtDate, linkName, timestamp);

        return linkId;
    }

    public int linkNotesWithANDBeforeDate(int noteId, String[] tags, String linkName, String date) {
        createLinkNotesTable();
        int linkId = -1;
        int noteId1 = noteId;
        boolean valid = doesNoteExistWithId(noteId1);
        if (noteId1 == 0 || !valid) {
            LOGGER.info("ID de note invalide");
            return linkId;
        }
        List<Integer> tagIds = findTagIdsWithAND(tags);
        if (tagIds.isEmpty() || tagIds.size() == 1) {
            LOGGER.info("Aucune donnée n'existe pour la tags");
            return linkId;
        }
        List<Integer> associatedNoteIds = findAssociatedNoteIds(tagIds);
        List<Integer> associatednoteIdsBeforeDate = findAssociatedNoteIdsBeforeDate(associatedNoteIds, date);
        if (associatednoteIdsBeforeDate.isEmpty()) {
            LOGGER.info("Aucune donnée n'existe avant la date: {} ", date);
            return linkId;
        }
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
        String timestamp = dateTime.format(formatter);

        linkId = insertLinkNotes(noteId1, associatednoteIdsBeforeDate, linkName, timestamp);

        return linkId;
    }

    public int linkNotesWithANDAfterDate(int noteId, String[] tags, String linkName, String date) {
        createLinkNotesTable();
        int linkId = -1;
        int noteId1 = noteId;
        boolean valid = doesNoteExistWithId(noteId1);
        if (noteId1 == 0 || !valid) {
            LOGGER.info("ID de note invalide");
            return linkId;
        }
        List<Integer> tagIds = findTagIdsWithAND(tags);
        if (tagIds.isEmpty() || tagIds.size() == 1) {
            LOGGER.info("Aucune donnée n'existe pour la tags");
            return linkId;
        }
        List<Integer> associatedNoteIds = findAssociatedNoteIds(tagIds);
        List<Integer> associatednoteIdsAfterDate = findAssociatedNoteIdsAfterDate(associatedNoteIds, date);
        if (associatednoteIdsAfterDate.isEmpty()) {
            LOGGER.info("Aucune donnée n'existe après la date: {} ", date);
            return linkId;
        }
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
        String timestamp = dateTime.format(formatter);

        linkId = insertLinkNotes(noteId1, associatednoteIdsAfterDate, linkName, timestamp);

        return linkId;
    }


    private List<Integer> findAssociatedNoteIdsAtDate(List<Integer> noteIds, String date) {
        List<Integer> matchingNoteIds = new ArrayList<>();

        try {
            String sql = "SELECT id FROM notes WHERE " + "substr(time, 8, 4) || '-' || " + "CASE substr(time, 4, 3) " + "   WHEN 'Jan' THEN '01' " + "   WHEN 'Feb' THEN '02' " + "   WHEN 'Mar' THEN '03' " + "   WHEN 'Apr' THEN '04' " + "   WHEN 'May' THEN '05' " + "   WHEN 'Jun' THEN '06' " + "   WHEN 'Jul' THEN '07' " + "   WHEN 'Aug' THEN '08' " + "   WHEN 'Sep' THEN '09' " + "   WHEN 'Oct' THEN '10' " + "   WHEN 'Nov' THEN '11' " + "   WHEN 'Dec' THEN '12' " + "END || '-' || substr(time, 1, 2) = ? AND id IN (" + String.join(",", Collections.nCopies(noteIds.size(), "?")) + ")";

            try (Connection conn = getConnection()) {
                if (conn != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, date);

                        // Set parameters for note IDs
                        for (int i = 0; i < noteIds.size(); i++) {
                            stmt.setInt(i + 2, noteIds.get(i));
                        }

                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                matchingNoteIds.add(resultSet.getInt("id"));
                            }
                        }
                    }
                } else {
                    LOGGER.info("La connexion à la base de données est nulle.");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }

        return matchingNoteIds;
    }


    private List<Integer> findAssociatedNoteIdsBeforeDate(List<Integer> noteIds, String date) {
        List<Integer> matchingNoteIds = new ArrayList<>();

        try {
            String sql = "SELECT id FROM notes WHERE " + "substr(time, 8, 4) || '-' || " + "CASE substr(time, 4, 3) " + "   WHEN 'Jan' THEN '01' " + "   WHEN 'Feb' THEN '02' " + "   WHEN 'Mar' THEN '03' " + "   WHEN 'Apr' THEN '04' " + "   WHEN 'May' THEN '05' " + "   WHEN 'Jun' THEN '06' " + "   WHEN 'Jul' THEN '07' " + "   WHEN 'Aug' THEN '08' " + "   WHEN 'Sep' THEN '09' " + "   WHEN 'Oct' THEN '10' " + "   WHEN 'Nov' THEN '11' " + "   WHEN 'Dec' THEN '12' " + "END || '-' || substr(time, 1, 2) < ? AND id IN (" + String.join(",", Collections.nCopies(noteIds.size(), "?")) + ")";

            try (Connection conn = getConnection()) {
                if (conn != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, date);

                        // Set parameters for note IDs
                        for (int i = 0; i < noteIds.size(); i++) {
                            stmt.setInt(i + 2, noteIds.get(i));
                        }

                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                matchingNoteIds.add(resultSet.getInt("id"));
                            }
                        }
                    }
                } else {
                    LOGGER.info("La connexion à la base de données est nulle.");
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }

        return matchingNoteIds;
    }

    private List<Integer> findAssociatedNoteIdsAfterDate(List<Integer> noteIds, String date) {
        List<Integer> matchingNoteIds = new ArrayList<>();

        try {
            String sql = "SELECT id FROM notes WHERE " + "substr(time, 8, 4) || '-' || " + "CASE substr(time, 4, 3) " + "   WHEN 'Jan' THEN '01' " + "   WHEN 'Feb' THEN '02' " + "   WHEN 'Mar' THEN '03' " + "   WHEN 'Apr' THEN '04' " + "   WHEN 'May' THEN '05' " + "   WHEN 'Jun' THEN '06' " + "   WHEN 'Jul' THEN '07' " + "   WHEN 'Aug' THEN '08' " + "   WHEN 'Sep' THEN '09' " + "   WHEN 'Oct' THEN '10' " + "   WHEN 'Nov' THEN '11' " + "   WHEN 'Dec' THEN '12' " + "END || '-' || substr(time, 1, 2) > ? AND id IN (" + String.join(",", Collections.nCopies(noteIds.size(), "?")) + ")";

            try (Connection conn = getConnection()) {
                if (conn != null) {
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, date);

                        // Set parameters for note IDs
                        for (int i = 0; i < noteIds.size(); i++) {
                            stmt.setInt(i + 2, noteIds.get(i));
                        }

                        try (ResultSet resultSet = stmt.executeQuery()) {
                            while (resultSet.next()) {
                                matchingNoteIds.add(resultSet.getInt("id"));
                            }
                        }
                    }
                } else {
                    LOGGER.info("La connexion à la base de données est nulle.");
                }
            }
        } catch (SQLException e) {
            LOGGER.info("Une erreur SQL s'est produite : {}", e.getMessage());
        }

        return matchingNoteIds;
    }


    private void connect() {
        try {
            // Charger le pilote JDBC SQLite
            Class.forName("org.sqlite.JDBC");
            // Créer une connexion à la base de données
            connection = DriverManager.getConnection("jdbc:sqlite:notes.db");
            createNotesTable();
            createRemindersTable();
            LOGGER.info("Connexion à SQLite établie.");
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.error("Une erreur s'est produite.", e);
        }
    }

    private Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:sqlite:notes.db");
            }
        } catch (SQLException e) {
            LOGGER.info("Une erreur s'est produite lors de la connexion à SQLite {}: ", e.getMessage());
        }

        return connection;
    }

    @Override
    public String ollamaSearchProcessing(String searchParameter) {
        HttpURLConnection urlConnection = null; // Declare the variable outside the try block
        try {
            URL url = new URL("http://localhost:11434/api/generate");

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);

            String postData = "{\"model\": \"llama2\", \"prompt\": \"" + searchParameter + "\", \"stream\": false}";

            try (OutputStream os = urlConnection.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            String response = extractResponseFromJson(responseBuilder.toString());
            return response;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    @Override
    public String extractResponseFromJson(String jsonResponse) {
        JSONObject jsonObject = new JSONObject(jsonResponse);
        String response = jsonObject.getString("response");

        return response;
    }

    @Override
    public String findContentBySearchId(String searchId) {
        String searchContent = null;
        try {
            createOllamaSearchTable();
            String sql = "SELECT search_result FROM OllamaSearchResults WHERE id = ?";

            try (Connection newConn = getConnection();
                 PreparedStatement statement = newConn.prepareStatement(sql)) {

                statement.setString(1, searchId);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        searchContent = resultSet.getString("search_result");
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("An error occurred while finding content by search ID", e);
        }

        return searchContent;
    }
}