package supernotes.notes;

public class TextNoteFactory implements NoteFactory {

    @Override
    public Note createNote(String title, String noteContent, String tag, String parentPageId, String pageId) {
        return new TextNote(title, noteContent, tag, parentPageId, pageId);
    }
}