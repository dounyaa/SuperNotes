package supernotes.notes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextNote implements Note<String> {
    private String title;
    private String type;
    private String content;
    private String tag;
    private String ParentPageId;
    private String pageId;
    private String time;

    public TextNote() {
    }

    public TextNote(String title, String content, String tag, String ParentPageId, String pageId) {
        this.title = title;
        this.content = content;
        this.tag = tag;
        this.ParentPageId = ParentPageId;
        this.pageId = pageId;
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
        this.time = dateTime.format(formatter);
    }

    public TextNote(String title, String content, String tag, String parentPageId, String pageId, String time) {
        this.title = title;
        this.content = content;
        this.tag = tag;
        ParentPageId = parentPageId;
        this.pageId = pageId;
        this.time = time;
    }

    public TextNote(String title, String type,String content, String tag, String parentPageId, String pageId, String time) {
        this.title = title;
        this.content = content;
        this.tag = tag;
        ParentPageId = parentPageId;
        this.pageId = pageId;
        this.time = time;
        this.type = type;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public String getPageId() {
        return pageId;
    }

    @Override
    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    @Override
    public String getParentPageId() {
        return ParentPageId;
    }

    @Override
    public void setParentPageId(String ParentPageId) {
        this.ParentPageId = ParentPageId;
    }

    @Override
    public String getTime() {
        return time;
    }

    @Override
    public void setTime(String newTime) {
        this.time = time;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getPath() {
        return null;
    }
}
