package supernotes.management;

import supernotes.notes.Note;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public interface DBManager {
    void createNotesTable();

    void createTagsTable();

    void createNotesTagsTable();

    void createRemindersTable();

    void createLinkNotesTable();

    void addReminder(int noteId, LocalDateTime reminderDateTime);

    List<LocalDateTime> getReminders(int noteId);

    boolean deleteRemindersByNoteId(int noteId);
    int addTextNote(String content, String tag, String parentPageId, String pageId, String time);

    int addImageNote(String content, byte[] imageBytes, String tag, String parentPageId, String pageId, String time);

    void deleteNoteByNoteId(int noteId);

    void deleteNoteByTag(String tag);

    ArrayList<Note> getAllNotes();

    ArrayList<Note> getAllNotesByTag(String tag);

    ArrayList<Note> getAllNotesLike(String contentMotif);

    String getParentPageId();

    String getPageId(String content);

    void updateNoteContentInDB(String pageId, String newContent);

    boolean doesNoteExist(String pageId);

    int linkNotesWithOR(int noteId, String[] tags, String linkName) throws SQLException;
    int linkNotesWithAND(int noteId, String[] tags, String linkName) throws SQLException;

    int linkNotesWithANDAtDate(int noteId, String[] tags, String linkName, String date) throws SQLException;

    int linkNotesWithANDBeforeDate(int noteId, String[] tags, String linkName, String date);

    int linkNotesWithANDAfterDate(int noteId, String[] tags, String linkName, String date);

    boolean getAllLinksByName(String linkName);

    ArrayList<Note> getNoteByNoteId(int noteId);

    ArrayList<Note> getNotesByType(String type);


}
