package supernotes.notes;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class ImageNote implements Note<byte[]> {
    private int id;
    private String type = "image";
    private byte[] content;
    private String tag;
    private String pageId;
    private String parentPageId;
    private String time;
    private String path;

    public ImageNote(String path, byte[] content, String tag, String parentPageId, String pageId) {
        this.path = path;
        this.content = content;
        this.tag = tag;
        this.parentPageId = parentPageId;
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
    public byte[] getContent() {
        return content;
    }

    @Override
    public void setContent(byte[] content) {
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
        return parentPageId;
    }

    @Override
    public void setParentPageId(String parentPageId) {
        this.parentPageId = parentPageId;
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

    public String getPath() {
        return path;
    }

    public void setPath() {
       this.path = path;
    }


}
