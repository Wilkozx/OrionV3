package utils;

import org.bson.Document;

import java.util.ArrayList;

public class Page {
    private final int pageNumber;
    private final int pageSize;
    private final ArrayList<Document> pageContent = new ArrayList<>(15);

    public Page(int pageNumber, int pageSize) {
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
    }

    public int getPageNumber() {
        return pageNumber + 1;
    }

    public int getPageSize() {
        return pageContent.size();
    }

    public ArrayList<Document> getPageContent() {
        return pageContent;
    }

    public void addContent(Document content) {
        pageContent.add(content);
    }

}
