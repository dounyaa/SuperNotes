package supernotes.notes;

import java.awt.*;

public class ImageNote implements Note<byte[]> {
    private String title;
    private byte[] content;
    private String tag;
    private String pageId;
    private String parentPageId;

    public ImageNote() {}

    public ImageNote(String title, byte[] content, String tag,  String parentPageId, String pageId)
    {
        this.title = title;
        this.content = content;
        this.tag = tag;
        this.parentPageId = parentPageId;
        this.pageId = pageId;
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
    
}
