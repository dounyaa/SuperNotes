package supernotes.management;

import supernotes.notes.Note;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public interface NoteManager{
    int addNote(Note note);
    ArrayList<Note> getByTag(String tag);
    void deleteByTag(String tag);
    String getParentPageId();
    String getPageId(String Content);
    void updateNoteContentInDB(String pageId, String newContent);
    boolean doesNoteExist(String pageId);
    List<Note> showAllNotes();
    void addReminder(int noteId, LocalDateTime reminderDateTime);
    List<LocalDateTime> getReminders(int noteId);
    boolean deleteRemindersByNoteId(int noteId);
    void addNoteWithReminderToCalendar(String noteContent, String reminderDateTime);

    int linkNotesWithOR(int noteId, String[] tags, String linkName) throws SQLException;
    int linkNotesWithAND(int noteId, String[] tags, String linkName) throws SQLException;
    int linkNotesWithANDAtDate(int noteId, String[] tags, String linkName, String date) throws SQLException;

    int linkNotesWithANDBeforeDate(int noteId, String[] tags, String linkName, String date) throws SQLException;

    int linkNotesWithANDAfterDate(int noteId, String[] tags, String linkName, String date);

    boolean getAllLinksByName(String linkName);
}
