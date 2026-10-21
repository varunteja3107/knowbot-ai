package com.knowbot.model;

public class Note {

    private final int id;
    private final String title;
    private final String content;
    private final String createdAt;
    private final String category;
    private final boolean favorite;

    public Note(int id, String title, String content,
                String createdAt, String category, boolean favorite) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.category = category;
        this.favorite = favorite;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getCategory() {
        return category;
    }

    public boolean isFavorite() {
        return favorite;
    }

    @Override
    public String toString() {
        return (favorite ? "★ " : "")
                + title + "  [" + category + "]";
    }
}
