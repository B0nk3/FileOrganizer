package com.fileorganizer.models;

/**
 * Clasa FileInfo reprezinta informatii despre un fisier.
 * Include numele fisierului, dimensiunea si data ultimei modificari.
 */
public class FileInfo {

    private String name;          // numele fisierului
    private String size;          // dimensiunea fisierului (ex. "12.34 KB")
    private String lastModified;  // data ultimei modificari

    /**
     * Constructor pentru FileInfo.
     *
     * @param name numele fisierului
     * @param size dimensiunea fisierului
     * @param lastModified data ultimei modificari
     */
    public FileInfo(String name, String size, String lastModified) {
        this.name = name;
        this.lastModified = lastModified;
        this.size = size;
    }

    /**
     * Returneaza numele fisierului.
     * @return numele fisierului
     */
    public String getName() {
        return name;
    }

    /**
     * Returneaza dimensiunea fisierului.
     * @return dimensiunea fisierului
     */
    public String getSize() {
        return size;
    }

    /**
     * Returneaza data ultimei modificari a fisierului.
     * @return data ultimei modificari
     */
    public String getLastModified() {
        return lastModified;
    }
}
