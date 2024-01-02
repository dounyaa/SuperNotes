package supernotes.notes;

public interface Note<T> {
    String getTitle();
    void setTitle(String title); 

    T getContent();
    void setContent(T content);

    String getTag();
    void setTag(String content); 

    String getPageId();
    void setPageId(String pageId); 

    
    String getParentPageId();
    void setParentPageId(String pageId);


    String getTime();
    void setTime(String newTime);

    String getType();

    String getPath();
}
