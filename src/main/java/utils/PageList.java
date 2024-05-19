package utils;

import java.util.ArrayList;

public class PageList {

    private static PageList instance;
    private final ArrayList<Page> pages = new ArrayList<>();
    private int currentPage = 0;

    public PageList() {
    }

    public static PageList getInstance() {
        if (instance == null) {
            instance = new PageList();
        }
        return instance;
    }

    public ArrayList<Page> getPages() {
        return pages;
    }

    public int getTotalSongs() {
        int totalSongs = 0;
        for (Page page : pages) {
            totalSongs += page.getPageSize();
        }
        return totalSongs;
    }

    public void clearPages() {
        pages.clear();
    }

    public int getTotalPages() {
        return pages.size();
    }

    public boolean doesPageExist(int pageNumber) {
        return pageNumber < pages.size();
    }

    public void addPage(Page page) {
        pages.add(page);
    }

    public Page getPage(int pageNumber) {
        return pages.get(pageNumber);
    }

    public Page getCurrentPage() {
        return getPage(currentPage);
    }

    public void nextPage() {
        if (currentPage < pages.size() - 1) {
            currentPage++;
        } else {
            currentPage = 0;
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
        } else {
            currentPage = pages.size() - 1;
        }
    }




}
