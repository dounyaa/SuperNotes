package supernotes.management;

import supernotes.notes.ImageNote;
import supernotes.notes.Note;
import supernotes.notes.TextNote;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class SQLiteDBManager implements DBManager {
    private Connection connection;

    public SQLiteDBManager()
    {
        connect();
    }

    @Override
    public void createNotesTable() {

//        // Check if the "time" column exists
//        boolean timeColumnExists = checkTimeColumn();
//
//        if (!timeColumnExists) {
//            // If the "time" column is missing, add it to the table
//            addTimeColumn();
//        }
        String sql = "CREATE TABLE IF NOT EXISTS notes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "title TEXT," +
                "type TEXT NOT NULL," + // Champ pour indiquer le type de note (texte ou image)
                "content TEXT," + // Champs pour stocker le contenu texte
                "image BLOB," + // Champs pour stocker l'image en tant que BLOB
                "tag TEXT," + // Champs pour stocker le tag texte (ajout de la virgule ici)
                "parent_page_id TEXT," +
                "page_id TEXT," + // Champs pour stocker l'ID de la page
                "time TEXT" +
                ")";
    
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    private boolean checkTimeColumn() {
//        // Check if the "time" column exists in the table
//        String checkSql = "PRAGMA table_info(notes)";
//        try (Statement stmt = connection.createStatement();
//             ResultSet rs = stmt.executeQuery(checkSql)) {
//
//            while (rs.next()) {
//                String columnName = rs.getString("name");
//                if ("time".equals(columnName)) {
//                    return true; // "time" column exists
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//
//        return false; // "time" column does not exist
//    }
//
//    private void addTimeColumn() {
//        // Add the "time" column to the table
//        String alterSql = "ALTER TABLE notes ADD COLUMN time TEXT";
//        try (Statement stmt = connection.createStatement()) {
//            stmt.execute(alterSql);
//            System.out.println("Ajout de la colonne « heure » au tableau des notes.");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void addTextNote(String title, String content, String tag, String parent_page_id, String page_id) {

    }


    @Override
    public void addTextNote(String title, String type, String content, String tag, String parent_page_id, String page_id, String time)
    {
        String sql = "INSERT INTO notes (title, type, content, tag, parent_page_id, page_id, time) VALUES (?, ?, ?, ?, ?, ?,?)";
        try (var conn = this.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, title);
            pstmt.setString(2, "text");
            pstmt.setString(3, content);            
            pstmt.setString(4, tag);
            pstmt.setString(5, parent_page_id);
            pstmt.setString(6, page_id);
            pstmt.setString(7,time);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void addImageNote(String title, byte[] imageBytes, String tag, String parent_page_id, String page_id) {

    }

    @Override
    public void addImageNote(String title, String type, byte[] imageBytes, String tag, String parent_page_id, String page_id, String time, String path)
    {
        String sql = "INSERT INTO notes (title, type, content, image, tag, parent_page_id, page_id, time) VALUES (?, ?, ?, ?, ?, ?,?,?)";
        try (var conn = this.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);) {
            pstmt.setString(1, title);
            pstmt.setString(2, "image");
            pstmt.setString(3,"Chemin de l'image : "+path);
            pstmt.setBytes(4, imageBytes);
            pstmt.setString(5, tag);
            pstmt.setString(6, parent_page_id);
            pstmt.setString(7, page_id);
            pstmt.setString(8,time);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteNoteByTag(String tag)
    {
        String sql = "DELETE FROM notes WHERE tag = ?";
        try (var conn = this.getConnection();
                PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, tag);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ArrayList<Note> getAllNotes()
    {
        String sql = "SELECT * FROM notes";

        var result = new ArrayList<Note>();

        try (var conn = this.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // loop through the result set
            while (rs.next()) {
                var type = rs.getString("type");
                if (type.equals("text")){
                    result.add(new TextNote(
                            rs.getString("title"),
                            rs.getString("type"),
                            rs.getString("content"),
                            rs.getString("tag"),
                            rs.getString("parent_page_id"),
                            rs.getString("page_id"),
                            rs.getString("time")));
                }
                else if (type.equals("image")){
                    result.add(new ImageNote(
                            rs.getString("title"),
                            rs.getString("type"),
                            rs.getBytes("image"),
                            rs.getString("tag"),
                            rs.getString("parent_page_id"),
                            rs.getString("page_id"),
                            rs.getString("time"),
                            rs.getString("content")));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }

    @Override
    public ArrayList<Note> getAllNotesByTag(String tag)
    {
        String sql = "SELECT * FROM notes WHERE tag = ?";

        var result = new ArrayList<Note>();

        try (var conn = this.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);) {
            stmt.setString(1, tag);

            try(ResultSet rs = stmt.executeQuery())
            {
                // loop through the result set
                while (rs.next()) {
                    var type = rs.getString("type");
                    if (type.equals("text")){
                        result.add(new TextNote(
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("tag"),
                            rs.getString("parent_page_id"),
                            rs.getString("page_id"),
                                rs.getString("time")));
                    }
                    else if (type.equals("image")){
                        result.add(new ImageNote(
                                rs.getString("title"),
                                rs.getBytes("image"),
                                rs.getString("tag"),
                                rs.getString("parent_page_id"),
                                rs.getString("page_id"),
                                rs.getString("time")));
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return result;
    }

    @Override
    public ArrayList<Note> getAllNotesLike(String contentMotif)
    {
        String sql = "SELECT * FROM notes WHERE content LIKE ?";

        var result = new ArrayList<Note>();

        try (var conn = this.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql);) {
            stmt.setString(1, "%"+contentMotif+"%");

            try(ResultSet rs = stmt.executeQuery())
            {
                // loop through the result set
                while (rs.next()) {
                    var type = rs.getString("type");
                    if (type.equals("text")){
                        result.add(new TextNote(
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getString("tag"),
                            rs.getString("parent_page_id"),
                            rs.getString("page_id"),
                                rs.getString("time")));
                    }
                    else if (type.equals("image")){
                        result.add(new ImageNote(
                                rs.getString("title"),
                                rs.getBytes("image"),
                                rs.getString("tag"),
                                rs.getString("parent_page_id"),
                                rs.getString("page_id"),
                                rs.getString("time")));
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
    
        try (var conn = this.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
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
        
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
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
        try(Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setString(1, newContent);
            pstmt.setString(2, pageId);
            pstmt.executeUpdate();
            System.out.println("Contenu de la note mis à jour !");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean doesNoteExist(String pageId) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement("SELECT COUNT(*) FROM notes WHERE page_id = ?")) {
            
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
    


    private void connect()
    {
        try {
            // Charger le pilote JDBC SQLite
            Class.forName("org.sqlite.JDBC");
            // Créer une connexion à la base de données
            connection = DriverManager.getConnection("jdbc:sqlite:notes.db");
            createNotesTable();
            System.out.println("Connexion à SQLite établie.");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection()
    {
        try
        {
            if(connection == null || connection.isClosed())
            {
                connection  = DriverManager.getConnection("jdbc:sqlite:notes.db");
            }
        }
        catch (SQLException e) {
            System.out.println("Get Connexion à SQLite failed.");
        }

        return connection;
    }
}
