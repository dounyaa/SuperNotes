package supernotes.notes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class TextNote implements Note<String> {
    private int id;
    private String type = "text";
    private String content;
    private String tag;
    private String ParentPageId;
    private String pageId;
    private String time;


    public TextNote(String content, String tag, String ParentPageId, String pageId) {
        this.content = content;
        this.tag = tag;
        this.ParentPageId = ParentPageId;
        this.pageId = pageId;
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy h:mm a");
        this.time = dateTime.format(formatter);
    }

    @Override 
    public int getId() {
        return this.id;
    }
    @Override
    public void setId(int id) {
        this.id = id;
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
    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String getType() {
        return type;
    }

}
