package utils;

import java.util.ArrayList;

public class PageList {
    private final ArrayList<Page> pages = new ArrayList<>();
    private int currentPage = 1;

    public PageList() {
    }

    public ArrayList<Page> getPages() {
        return pages;
    }

    public boolean doesPageExist(int pageNumber) {
        return pageNumber < pages.size();
    }

    public void addPage(Page page) {
        pages.add(page);
    }

    public Page getPage(int pageNumber) {
        return pages.get(pageNumber - 1);
    }

    public Page getCurrentPage() {
        return getPage(currentPage);
    }

    public void nextPage() {
        if (currentPage < pages.size() - 1) {
            currentPage++;
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
        }
    }


}
