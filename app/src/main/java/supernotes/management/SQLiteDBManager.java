package supernotes.management;

import supernotes.notes.ImageNote;
import supernotes.notes.Note;
import supernotes.notes.TextNote;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SQLiteDBManager implements DBManager {
    private Connection connection;

    public SQLiteDBManager() {
        connect();
    }

    @Override
    public void createNotesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS notes (" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "type TEXT NOT NULL," + // Champ pour indiquer le type de note (texte ou image)
                "content TEXT," + // Champs pour stocker le contenu texte
                "image BLOB," + // Champs pour stocker l'image en tant que BLOB
                "parent_page_id TEXT," + "page_id TEXT," + // Champs pour stocker l'ID de la page
                "time TEXT," + "path TEXT" + ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createTagsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Tags (" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "tag TEXT NOT NULL" + ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createNotesTagsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS NotesTags (" + "note_id INTEGER," + "tag_id INTEGER," + "PRIMARY KEY (note_id, tag_id)," + "FOREIGN KEY (note_id) REFERENCES Notes(id)," + "FOREIGN KEY (tag_id) REFERENCES Tags(id)" + ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void createRemindersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS Reminders (" + "id INT AUTO_INCREMENT PRIMARY KEY," + "note_id INT," + "reminder_date_time DATETIME," + "FOREIGN KEY (note_id) REFERENCES Notes(id)" + ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void createLinkNotesTable() {
        String sql = "CREATE TABLE IF NOT EXISTS LinkNotes (" + "id INTEGER PRIMARY KEY AUTOINCREMENT," + "linkname VARCHAR(255)," + "note1_id INTEGER," + "note2_id INTEGER," + "creation_date TEXT," + "FOREIGN KEY (note1_id) REFERENCES Notes(id)," + "FOREIGN KEY (note2_id) REFERENCES Notes(id)" + ")";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addReminder(int noteId, LocalDateTime reminderDateTime) {
        String sql = "INSERT INTO Reminders (note_id, reminder_date_time) VALUES (?, ?)";
        try (var conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, noteId);
            pstmt.setTimestamp(2, Timestamp.valueOf(reminderDateTime));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<LocalDateTime> getReminders(int noteId) {
        List<LocalDateTime> reminders = new ArrayList<>();

        String sql = "SELECT reminder_date_time FROM Reminders WHERE note_id = ?";
        try (Connection conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, noteId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Timestamp reminderTimestamp = rs.getTimestamp("reminder_date_time");
                LocalDateTime reminderDateTime = reminderTimestamp.toLocalDateTime();
                reminders.add(reminderDateTime);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return reminders;
    }

    @Override
    public boolean deleteRemindersByNoteId(int noteId) {
        String sql = "DELETE FROM reminders WHERE note_id = ?";
        boolean success = false;

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, noteId);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Tous les rappels pour la note avec l'ID " + noteId + " ont été supprimés avec succès !");
                success = true;
            } else {
                System.out.println("Aucun rappel trouvé pour la note avec l'ID " + noteId + ".");
            }
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression des rappels pour la note avec l'ID " + noteId + " : " + e.getMessage());
        }

        return success;
    }


    @Override
    public int addTextNote(String content, String tag, String parent_page_id, String page_id, String time) {
        String sql = "INSERT INTO notes (type, content, parent_page_id, page_id, time) VALUES (?, ?, ?, ?, ?)";
        int noteId = -1;

        try (var conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, "text");
            pstmt.setString(2, content);
            pstmt.setString(3, parent_page_id);
            pstmt.setString(4, page_id);
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
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return noteId;
    }

    public int addImageNote(String path, byte[] imageBytes, String tags, String parent_page_id, String page_id, String time) {
        String sql = "INSERT INTO notes (type, path, content, parent_page_id, page_id, time) VALUES (?, ?, ?, ?, ?, ?)";
        int noteId = -1;

        try (var conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, "image");
            pstmt.setString(2, path);
            pstmt.setBytes(3, imageBytes);
            pstmt.setString(5, parent_page_id);
            pstmt.setString(6, page_id);
            pstmt.setString(7, time);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    noteId = generatedKeys.getInt(1); // Récupérer l'ID généré
                    addTagsToNote(tags, noteId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return noteId;
    }

    private List<Integer> addTagsforNotes(String tag) {
        String[] tags = tag.split("\"\\s*\"");

        for (int i = 0; i < tags.length; i++) {
            tags[i] = tags[i].replace("\"", "").trim();
        }

        List<Integer> tagIds = new ArrayList<>();

        for (String tempTag : tags) {
            String sql = "INSERT INTO Tags (tag) VALUES (?)";

            try (var conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setString(1, tempTag);
                pstmt.executeUpdate();

                // Retrieve the generated key (tag_id) after the insertion
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int tagId = generatedKeys.getInt(1);
                        tagIds.add(tagId);
                    } else {
                        throw new SQLException("Failed to retrieve generated key for tag");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                // Handle the exception as needed, e.g., log it or rethrow
            }
        }

        return tagIds;
    }

    private void addTagsToNote(String tag, int noteId) {
        createTagsTable();
        createNotesTagsTable();
        try {
            List<Integer> tempTagIds = addTagsforNotes(tag);

            for (Integer tagId : tempTagIds) {
                // Associe les tags à la note dans la table NotesTags
                String sql = "INSERT INTO NotesTags (note_id, tag_id) VALUES (?, ?)";

                try (var conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setInt(1, noteId);
                    pstmt.setInt(2, tagId);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception as needed, e.g., log it or rethrow
        }
    }

    @Override
    public void deleteNoteByNoteId(int note_id) {
        String sql = "DELETE FROM notes WHERE id = ?";
        try (var conn = this.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, note_id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteNoteByTag(String tag) {
        String deleteNotesSql = "DELETE FROM notes WHERE id IN (SELECT note_id FROM NotesTags WHERE tag_id = (SELECT id FROM Tags WHERE tag = ?))";
        String deleteNotesTagsSql = "DELETE FROM NotesTags WHERE tag_id = (SELECT id FROM Tags WHERE tag = ?)";

        try (var conn = this.getConnection(); PreparedStatement deleteNotesStmt = conn.prepareStatement(deleteNotesSql); PreparedStatement deleteNotesTagsStmt = conn.prepareStatement(deleteNotesTagsSql)) {

            conn.setAutoCommit(false);

            deleteNotesStmt.setString(1, tag);
            deleteNotesStmt.executeUpdate();

            deleteNotesTagsStmt.setString(1, tag);
            deleteNotesTagsStmt.executeUpdate();

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<Note> getAllNotes() {
        String sql = "SELECT notes.*, GROUP_CONCAT(tags.tag, ',') AS tag_list " + "FROM notes " + "LEFT JOIN NotesTags ON notes.id = NotesTags.note_id " + "LEFT JOIN Tags ON NotesTags.tag_id = Tags.id " + "GROUP BY notes.id";

        var result = new ArrayList<Note>();

        try (var conn = this.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {

            // loop through the result set
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
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return result;
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
            System.out.println(e.getMessage());
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
            System.out.println(e.getMessage());
        }

        return result;
    }

    @Override
    public String getParentPageId() {
        String parentPageId = null;
        String sql = "SELECT parent_page_id FROM notes WHERE parent_page_id IS NOT NULL LIMIT 1";

        try (var conn = this.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                parentPageId = rs.getString("parent_page_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return parentPageId;
    }

    @Override
    public String getPageId(String content) {
        String sql = "SELECT page_id FROM notes WHERE content = ?";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, content);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("page_id");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void updateNoteContentInDB(String pageId, String newContent) {
        String sql = "UPDATE notes SET content = ? WHERE page_id = ?";
        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newContent);
            pstmt.setString(2, pageId);
            pstmt.executeUpdate();
            System.out.println("Contenu de la note mis à jour !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean doesNoteExist(String pageId) {
        try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM notes WHERE page_id = ?")) {

            preparedStatement.setString(1, pageId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0; // Si le compte est supérieur à zéro, la note existe
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
            System.out.println("ID de note invalide");
            return linkId;
        }
        List<Integer> tagIds = findTagIdsWithOR(tags);
        if (tagIds.isEmpty()) {
            System.out.println("Aucune donnée n'existe avec le tag");
            return linkId;
        }
        List<Integer> associatedNoteIds = findAssociatedNoteIds(tagIds);
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
        String timestamp = dateTime.format(formatter);

        // Assuming there's a method to insert a record into the LINKNOTES table
        linkId = insertLinkNotes(noteId1, associatedNoteIds, linkName, timestamp);

        return linkId;
    }

    private boolean doesNoteExistWithId(int noteId) {
        try {
            // Assuming you have a database connection named "connection"
            String sql = "SELECT COUNT(*) FROM notes WHERE id = ?";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, noteId);

                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0; // Return true if the note exists, false otherwise
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately (e.g., log or throw)
        }

        // In case of an exception or other issues, assume the note does not exist
        return false;
    }

    private List<Integer> findTagIdsWithOR(String[] tags) {
        List<Integer> tagIds = new ArrayList<>();

        try {
            // Dynamically generate the OR condition for the tag names
            StringBuilder orCondition = new StringBuilder();
            for (int i = 0; i < tags.length; i++) {
                orCondition.append("tag = ?");
                if (i < tags.length - 1) {
                    orCondition.append(" OR ");
                }
            }

            String sql = "SELECT id FROM Tags WHERE " + orCondition;

            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

                // Set parameters for each tag
                for (int i = 0; i < tags.length; i++) {
                    stmt.setString(i + 1, tags[i]);
                }

                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    tagIds.add(resultSet.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately (e.g., log or throw)
        }

        return tagIds;
    }

    private List<Integer> findTagIdsWithAND(String[] tags) {
        List<Integer> tagIds = new ArrayList<>();

        try {
            // Generate the placeholders for the IN clause
            String inClausePlaceholders = String.join(",", Collections.nCopies(tags.length, "?"));

            String sql = "SELECT id FROM Tags WHERE tag IN (" + inClausePlaceholders + ")";

            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                // Set parameters for the IN clause
                for (int i = 0; i < tags.length; i++) {
                    stmt.setString(i + 1, tags[i]);
                }

                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    tagIds.add(resultSet.getInt("id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately (e.g., log or throw)
        }

        return tagIds;
    }


    private List<Integer> findAssociatedNoteIds(List<Integer> tagIds) {
        List<Integer> noteIds = new ArrayList<>();

        try {
            // Dynamically generate the OR condition for the tag IDs
            StringBuilder orCondition = new StringBuilder();
            for (int i = 0; i < tagIds.size(); i++) {
                orCondition.append("tag_id = ?");
                if (i < tagIds.size() - 1) {
                    orCondition.append(" OR ");
                }
            }

            String sql = "SELECT DISTINCT(note_id) FROM NotesTags WHERE " + orCondition;

            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

                // Set parameters for each tag ID
                for (int i = 0; i < tagIds.size(); i++) {
                    stmt.setInt(i + 1, tagIds.get(i));
                }

                ResultSet resultSet = stmt.executeQuery();
                while (resultSet.next()) {
                    noteIds.add(resultSet.getInt("note_id"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Aucun enregistrement trouvé pour les balises");
            // Handle the exception appropriately (e.g., log or throw)
        }

        return noteIds;
    }

    public int insertLinkNotes(int noteId1, List<Integer> noteId2List, String linkName, String creationDate) {
        int linkId = -1;

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);

            try {
                String insertSql = "INSERT INTO LinkNotes (linkname, note1_id, note2_id, creation_date) VALUES (?, ?, ?, ?)";

                try (PreparedStatement insertStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                    for (int noteId2 : noteId2List) {
                        insertStatement.setString(1, linkName);
                        insertStatement.setInt(2, noteId1);
                        insertStatement.setInt(3, noteId2);
                        insertStatement.setString(4, creationDate);
                        insertStatement.addBatch();
                    }

                    // Execute batch and retrieve generated keys
                    int[] updateCounts = insertStatement.executeBatch();
                    try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            linkId = generatedKeys.getInt(1);
                        }
                    }

                    connection.commit();  // Commit the batch
                }
            } catch (SQLException e) {
                System.out.println("SQLException during statement execution: " + e.getMessage());
                connection.rollback();  // Rollback if an exception occurs
                throw e;  // Re-throw the exception after rolling back the transaction
            }
        } catch (SQLException e) {
            System.out.println("Error establishing database connection: " + e.getMessage());
            e.printStackTrace();
        }

        return linkId;
    }


    private boolean doesLinkAlreadyExist(int noteId1, int noteId2) {
        try {
            // Assuming you have a database connection named "connection"
            String sql = "SELECT COUNT(*) FROM LinkNotes WHERE note1_id = ? AND note2_id = ?";
            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, noteId1);
                stmt.setInt(2, noteId2);

                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    int count = resultSet.getInt(1);
                    return count > 0; // Return true if the link already exists, false otherwise
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately (e.g., log or throw)
        }

        // In case of an exception or other issues, assume the link does not exist
        return false;
    }


    @Override
    public int linkNotesWithAND(int noteId, String[] tags, String linkName) throws SQLException {
        createLinkNotesTable();
        int linkId = -1;
        int noteId1 = noteId;
        boolean valid = doesNoteExistWithId(noteId1);
        if (noteId1 == 0 || !valid) {
            System.out.println("ID de note invalide");
            return linkId;
        }
        List<Integer> tagIds = findTagIdsWithAND(tags);
        if (tagIds.isEmpty() || tagIds.size() == 1) {
            System.out.println("Aucune donnée n'existe avec le tag");
            return linkId;
        }
        List<Integer> associatedNoteIds = findAssociatedNoteIds(tagIds);
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
        String timestamp = dateTime.format(formatter);

        // Assuming there's a method to insert a record into the LINKNOTES table
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
            System.out.println("ID de note invalide");
            return linkId;
        }
        List<Integer> tagIds = findTagIdsWithAND(tags);
        if (tagIds.isEmpty() || tagIds.size() == 1) {
            System.out.println("Aucune donnée n'existe avec le tag");
            return linkId;
        }
        List<Integer> associatedNoteIds = findAssociatedNoteIds(tagIds);
        List<Integer> associatednoteIdsAtDate = findAssociatedNoteIdsAtDate(associatedNoteIds, date);
        if (associatednoteIdsAtDate.isEmpty()) {
            System.out.println("Aucune donnée n'existe pour la date: " + date);
            return linkId;
        }
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
        String timestamp = dateTime.format(formatter);

        // Assuming there's a method to insert a record into the LINKNOTES table
        linkId = insertLinkNotes(noteId1, associatednoteIdsAtDate, linkName, timestamp);

        return linkId;
    }

    public int linkNotesWithANDBeforeDate(int noteId, String[] tags, String linkName, String date) {
        createLinkNotesTable();
        int linkId = -1;
        int noteId1 = noteId;
        boolean valid = doesNoteExistWithId(noteId1);
        if (noteId1 == 0 || !valid) {
            System.out.println("ID de note invalide");
            return linkId;
        }
        List<Integer> tagIds = findTagIdsWithAND(tags);
        if (tagIds.isEmpty() || tagIds.size() == 1) {
            System.out.println("Aucune donnée n'existe pour la tags");
            return linkId;
        }
        List<Integer> associatedNoteIds = findAssociatedNoteIds(tagIds);
        List<Integer> associatednoteIdsBeforeDate = findAssociatedNoteIdsBeforeDate(associatedNoteIds, date);
        if (associatednoteIdsBeforeDate.isEmpty()) {
            System.out.println("Aucune donnée n'existe avant la date: " + date);
            return linkId;
        }
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
        String timestamp = dateTime.format(formatter);

        // Assuming there's a method to insert a record into the LINKNOTES table
        linkId = insertLinkNotes(noteId1, associatednoteIdsBeforeDate, linkName, timestamp);

        return linkId;
    }

    public int linkNotesWithANDAfterDate(int noteId, String[] tags, String linkName, String date) {
        createLinkNotesTable();
        int linkId = -1;
        int noteId1 = noteId;
        boolean valid = doesNoteExistWithId(noteId1);
        if (noteId1 == 0 || !valid) {
            System.out.println("ID de note invalide");
            return linkId;
        }
        List<Integer> tagIds = findTagIdsWithAND(tags);
        if (tagIds.isEmpty() || tagIds.size() == 1) {
            System.out.println("Aucune donnée n'existe pour la tags");
            return linkId;
        }
        List<Integer> associatedNoteIds = findAssociatedNoteIds(tagIds);
        List<Integer> associatednoteIdsAfterDate = findAssociatedNoteIdsAfterDate(associatedNoteIds, date);
        if (associatednoteIdsAfterDate.isEmpty()) {
            System.out.println("Aucune donnée n'existe après la date: " + date);
            return linkId;
        }
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
        String timestamp = dateTime.format(formatter);

        // Assuming there's a method to insert a record into the LINKNOTES table
        linkId = insertLinkNotes(noteId1, associatednoteIdsAfterDate, linkName, timestamp);

        return linkId;
    }

    @Override
    public boolean getAllLinksByName(String linkName) {
        createLinkNotesTable();
        String sql = "SELECT * FROM LinkNotes WHERE linkname = ?";
    
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setString(1, linkName.trim()); // Assurez-vous d'utiliser trim() ici
            ResultSet rs = pstmt.executeQuery();
    
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
                        System.out.println(); // Ajouter une ligne vide entre les groupes de dates
                    }
                    System.out.println("Created on : " + creationDate);
                    System.out.print("--------------------------------"); // La longueur de la ligne est fixe
                    System.out.println();
                    tempDate = creationDate;
                }
    
                // Vérifiez si l'id de note a changé
                if (note1Id != tempNoteId) {
                    System.out.println("note id = " + note1Id + "  --->  id =" + note2Id);
                    tempNoteId = note1Id;
                } else {
                    // Imprimer des espaces pour aligner les sorties
                    System.out.println("             --->  id =" + note2Id);
                }
            } while (rs.next());
    
            found = true;
            return found;
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
        return false; // Retourne false si une exception est levée
    }
    


    private List<Integer> findAssociatedNoteIdsAtDate(List<Integer> noteIds, String date) {
        List<Integer> matchingNoteIds = new ArrayList<>();

        try {
            // Assuming the date is stored in the "time" column
            // and the IDs are in the list noteIds
            String sql = "SELECT id FROM notes WHERE " + "substr(time, 8, 4) || '-' || " + "CASE substr(time, 4, 3) " + "   WHEN 'Jan' THEN '01' " + "   WHEN 'Feb' THEN '02' " + "   WHEN 'Mar' THEN '03' " + "   WHEN 'Apr' THEN '04' " + "   WHEN 'May' THEN '05' " + "   WHEN 'Jun' THEN '06' " + "   WHEN 'Jul' THEN '07' " + "   WHEN 'Aug' THEN '08' " + "   WHEN 'Sep' THEN '09' " + "   WHEN 'Oct' THEN '10' " + "   WHEN 'Nov' THEN '11' " + "   WHEN 'Dec' THEN '12' " + "END || '-' || substr(time, 1, 2) = ? AND id IN (" + String.join(",", Collections.nCopies(noteIds.size(), "?")) + ")";


            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately (e.g., log or throw)
        }

        return matchingNoteIds;
    }

    private List<Integer> findAssociatedNoteIdsBeforeDate(List<Integer> noteIds, String date) {
        List<Integer> matchingNoteIds = new ArrayList<>();

        try {
            // Assuming the date is stored in the "time" column
            // and the IDs are in the list noteIds
            String sql = "SELECT id FROM notes WHERE " + "substr(time, 8, 4) || '-' || " + "CASE substr(time, 4, 3) " + "   WHEN 'Jan' THEN '01' " + "   WHEN 'Feb' THEN '02' " + "   WHEN 'Mar' THEN '03' " + "   WHEN 'Apr' THEN '04' " + "   WHEN 'May' THEN '05' " + "   WHEN 'Jun' THEN '06' " + "   WHEN 'Jul' THEN '07' " + "   WHEN 'Aug' THEN '08' " + "   WHEN 'Sep' THEN '09' " + "   WHEN 'Oct' THEN '10' " + "   WHEN 'Nov' THEN '11' " + "   WHEN 'Dec' THEN '12' " + "END || '-' || substr(time, 1, 2) < ? AND id IN (" + String.join(",", Collections.nCopies(noteIds.size(), "?")) + ")";

//            System.out.println("SQL Query: " + sql); // Print the SQL query for debugging

            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately (e.g., log or throw)
        }

        return matchingNoteIds;
    }

    private List<Integer> findAssociatedNoteIdsAfterDate(List<Integer> noteIds, String date) {
        List<Integer> matchingNoteIds = new ArrayList<>();

        try {
            // Assuming the date is stored in the "time" column
            // and the IDs are in the list noteIds
            String sql = "SELECT id FROM notes WHERE " + "substr(time, 8, 4) || '-' || " + "CASE substr(time, 4, 3) " + "   WHEN 'Jan' THEN '01' " + "   WHEN 'Feb' THEN '02' " + "   WHEN 'Mar' THEN '03' " + "   WHEN 'Apr' THEN '04' " + "   WHEN 'May' THEN '05' " + "   WHEN 'Jun' THEN '06' " + "   WHEN 'Jul' THEN '07' " + "   WHEN 'Aug' THEN '08' " + "   WHEN 'Sep' THEN '09' " + "   WHEN 'Oct' THEN '10' " + "   WHEN 'Nov' THEN '11' " + "   WHEN 'Dec' THEN '12' " + "END || '-' || substr(time, 1, 2) > ? AND id IN (" + String.join(",", Collections.nCopies(noteIds.size(), "?")) + ")";

//            System.out.println("SQL Query: " + sql); // Print the SQL query for debugging

            try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle the exception appropriately (e.g., log or throw)
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
            System.out.println("Connexion à SQLite établie.");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:sqlite:notes.db");
            }
        } catch (SQLException e) {
            System.out.println("Get Connexion à SQLite failed.");
        }

        return connection;
    }
}