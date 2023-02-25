package com.example.qcm.ui.database;

public class DataItem {
    private int id;
    private String title;
    private String date;
    private String type;
    private int thumbnail;

    public DataItem(int id, String title, String date, String type, int thumbnail) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.type = type;
        this.thumbnail = thumbnail;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public int getThumbnail() {
        return thumbnail;
    }
}
