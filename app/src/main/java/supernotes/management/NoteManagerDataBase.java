package supernotes.management;

import supernotes.notes.ImageNote;
import supernotes.notes.Note;
import supernotes.notes.TextNote;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NoteManagerDataBase implements NoteManager {
    DBManager dbManager;

    public NoteManagerDataBase(){
        dbManager = new SQLiteDBManager();
    }

    @Override
    public void addNote(Note note) {
        if (note instanceof TextNote){
            String title = note.getTitle();
            String noteContent = ((TextNote) note).getContent();
            String noteTag = note.getTag();
            String parent_page_id = note.getParentPageId();            
            String page_id = note.getPageId();
            LocalDateTime dateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
            String time = dateTime.format(formatter);

            dbManager.addTextNote(title, "text", noteContent, noteTag, parent_page_id, page_id, time);
        }
        else if (note instanceof ImageNote){
            String title = note.getTitle();
            var imageContent = ((ImageNote) note).getContent();
            String noteTag = note.getTag();
            String parent_page_id = note.getParentPageId();     
            String page_id = note.getPageId();
            LocalDateTime dateTime = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
            String time = dateTime.format(formatter);
            String type = "image";
            String path = note.getPath();

            dbManager.addImageNote(title, "image", imageContent, noteTag, parent_page_id, page_id, time, path);
        }
    }

    @Override
    public void deleteByTag(String tag)
    {
        dbManager.deleteNoteByTag(tag);
    }

    @Override
    public String getParentPageId()
    {
        return dbManager.getParentPageId();
    }

    @Override
    public String getPageId(String Content)
    {
        return dbManager.getPageId(Content);
    }
    
    @Override
    public void updateNoteContentInDB(String pageId, String newContent)
    {
        dbManager.updateNoteContentInDB(pageId, newContent);
    }

    @Override
    public boolean doesNoteExist(String pageId)
    {
        return dbManager.doesNoteExist(pageId);
    }

    @Override
    public List<Note> showAllNotes() {
        return dbManager.getAllNotes();
    }

}
