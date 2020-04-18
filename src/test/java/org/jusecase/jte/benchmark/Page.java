package org.jusecase.jte.benchmark;

import java.util.List;

public abstract class Page {
    private final int visits;

    private static final List<MenuItem> MENU = List.of(
            new MenuItem("Home", "home.html"),
            new MenuItem("News", "news.html"),
            new MenuItem("About", "about.html")
    );

    protected Page(int visits) {
        this.visits = visits;
    }

    public abstract String getTitle();

    public abstract String getDescription();

    public abstract String getTemplate();

    public List<MenuItem> getMenu() {
        return MENU;
    }

    public int getVisits() {
        return visits;
    }
}
