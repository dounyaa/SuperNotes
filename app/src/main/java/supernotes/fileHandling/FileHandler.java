package supernotes.fileHandling;

public interface FileHandler {
    void exportPdfFile(String filePath, String tag);
    void exportPdfFileUsingFilter(String filePath, String filter);
}
