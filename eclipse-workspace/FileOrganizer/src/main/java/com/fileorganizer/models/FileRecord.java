package com.fileorganizer.models;

public class FileRecord {
    private int id;
    private String name;
    private String source;
    private String destination;
    private String date;

    public FileRecord(int id, String name, String source, String destination, String date) {
        this.id = id;
        this.name = name;
        this.source = source;
        this.destination = destination;
        this.date = date;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getSource() { return source; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
}
