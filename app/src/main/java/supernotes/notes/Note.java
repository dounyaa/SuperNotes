package supernotes.notes;

public interface Note<T> {
    int getId();
    void setId(int id);

    T getContent();
    void setContent(T content);

    String getTag();
    void setTag(String content); 

    String getPageId();
    void setPageId(String pageId); 
    
    String getParentPageId();
    void setParentPageId(String pageId);

    String getTime();

    String getType();
}
