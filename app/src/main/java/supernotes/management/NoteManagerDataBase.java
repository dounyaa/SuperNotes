package supernotes.management;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import supernotes.notes.ImageNote;
import supernotes.notes.Note;
import supernotes.notes.TextNote;
import supernotes.reminders.GoogleCalendarReminder;

public class NoteManagerDataBase implements NoteManager {
    DBManager dbManager;

    public NoteManagerDataBase(){
        dbManager = new SQLiteDBManager();
    }

    @Override
    public int addNote(Note note) {
        int noteId = -1;
        if (note instanceof TextNote) {
            String noteContent = ((TextNote) note).getContent();
            String noteTag = note.getTag();
            String parentPageId = note.getParentPageId();
            String pageId = note.getPageId();
            String time = note.getTime();

            noteId = dbManager.addTextNote(noteContent, noteTag, parentPageId, pageId, time);
            note.setId(noteId);
            
        } else if (note instanceof ImageNote) {
            var imageContent = ((ImageNote) note).getContent();
            String noteTag = note.getTag();
            String parentPageId = note.getParentPageId();
            String pageId = note.getPageId();
            String path = ((ImageNote) note).getPath();
            String time = note.getTime();

            noteId = dbManager.addImageNote(path, imageContent, noteTag, parentPageId, pageId, time);
            note.setId(noteId);

        }
        return noteId;
    }

    
    @Override
    public void addReminder(int noteId, LocalDateTime reminderDateTime) {
        dbManager.addReminder(noteId, reminderDateTime);
    }

    @Override
    public List<LocalDateTime> getReminders(int noteId) {
        return dbManager.getReminders(noteId);
    }

    @Override
    public void deleteByTag(String tag)
    {
        dbManager.deleteNoteByTag(tag);
    }

    @Override
    public void deleteNoteByNoteId(int noteId)
    {
        dbManager.deleteNoteByNoteId(noteId);
    }

    @Override
    public ArrayList<Note> getByTag(String tag)
    {
        return dbManager.getAllNotesByTag(tag);
    }

    @Override
    public String getParentPageId()
    {
        return dbManager.getParentPageId();
    }

    @Override
    public String getPageId(String content)
    {
        return dbManager.getPageId(content);
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

    public boolean deleteRemindersByNoteId(int noteId) {
        return dbManager.deleteRemindersByNoteId(noteId);
    }

    @Override
    public void addNoteWithReminderToCalendar(String noteContent, String reminderDateTime) {
        GoogleCalendarReminder.addEventToCalendar(noteContent, reminderDateTime);
    }

    @Override
    public int linkNotesWithOR(int noteId, String[] tags, String linkName) throws SQLException {
        return dbManager.linkNotesWithOR(noteId,tags,linkName);
    }

    @Override
    public int linkNotesWithAND(int noteId, String[] tags, String linkName) throws SQLException {
        return dbManager.linkNotesWithAND(noteId,tags,linkName);
    }

    @Override
    public int linkNotesWithANDAtDate(int noteId, String[] tags, String linkName, String date) throws SQLException {
        return dbManager.linkNotesWithANDAtDate(noteId,tags,linkName,date);
    }

    @Override
    public int linkNotesWithANDBeforeDate(int noteId, String[] tags, String linkName, String date) throws SQLException {
        return dbManager.linkNotesWithANDBeforeDate(noteId,tags,linkName,date);
    }

    @Override
    public int linkNotesWithANDAfterDate(int noteId, String[] tags, String linkName, String date) {
        return dbManager.linkNotesWithANDAfterDate(noteId,tags,linkName,date);
    }

    @Override
    public boolean getAllLinksByName(String linkName) {
        return dbManager.getAllLinksByName(linkName);
    }


    @Override
    public ArrayList<Note> getAllNotesLike(String contentMotif) {
        return dbManager.getAllNotesLike(contentMotif);
    }
    @Override
    public ArrayList<Note> getNoteByNoteId(int noteId){
        return getNoteByNoteId(noteId);
    }
    @Override
    public ArrayList<Note> getNotesByType(String type){
        return getNotesByType(type);
    }


}
