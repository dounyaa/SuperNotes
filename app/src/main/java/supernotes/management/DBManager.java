package supernotes.management;
import supernotes.notes.Note;

import java.util.ArrayList;

public interface DBManager {
    void createNotesTable();
    void addTextNote(String title, String content, String tag, String parent_page_id, String page_id);
    void addTextNote(String title, String type, String content, String tag, String parent_page_id, String page_id, String time);
    void addImageNote(String title, byte[] imageBytes, String tag, String parent_page_id, String page_id);
    void addImageNote(String title, String type, byte[] imageBytes, String tag, String parent_page_id, String page_id, String time, String path);
    void deleteNoteByTag(String tag);
    ArrayList<Note> getAllNotes();
    ArrayList<Note> getAllNotesByTag(String tag);
    ArrayList<Note> getAllNotesLike(String contentMotif);
    String getParentPageId();
    String getPageId(String Content);
    void updateNoteContentInDB(String pageId, String newContent);
    boolean doesNoteExist(String pageId);
}
