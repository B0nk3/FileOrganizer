package com.fileorganizer.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Clasa FileOrganizer organizeaza fisierele dintr-un folder in subfoldere
 * pe baza tipului de fisier (extensie).
 */
public class FileOrganizer {

    /**
     * Organizeaza fisierele din folderul dat in categorii de baza:
     * Documents, Images, Audio, Videos, Archives, Others.
     *
     * @param folderPath calea catre folderul care trebuie organizat
     */
    public void organizeFiles(String folderPath) {
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Folderul specificat nu exista sau nu este valid!");
            return;
        }

        // Mapam extensiile catre categoriile lor
        Map<String, String> categories = new HashMap<>();
        categories.put("pdf", "Documents");
        categories.put("docx", "Documents");
        categories.put("doc", "Documents");
        categories.put("txt", "Documents");
        categories.put("pptx", "Documents");
        categories.put("xls", "Documents");
        categories.put("xlsx", "Documents");

        categories.put("jpg", "Images");
        categories.put("jpeg", "Images");
        categories.put("png", "Images");
        categories.put("gif", "Images");
        categories.put("bmp", "Images");

        categories.put("mp3", "Audio");
        categories.put("wav", "Audio");
        categories.put("flac", "Audio");

        categories.put("mp4", "Videos");
        categories.put("avi", "Videos");
        categories.put("mkv", "Videos");
        categories.put("mov", "Videos");

        categories.put("zip", "Archives");
        categories.put("rar", "Archives");
        categories.put("7z", "Archives");
        categories.put("tar", "Archives");

        File[] files = folder.listFiles();
        if (files == null) {
            System.out.println("Folderul este gol.");
            return;
        }

        // Parcurgem fiecare fisier si il mutam in folderul potrivit
        for (File file : files) {
            if (file.isFile()) {
                String fileName = file.getName();
                String extension = "";

                // Extragem extensia (textul dupa ultimul punct)
                int dotIndex = fileName.lastIndexOf(".");
                if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
                    extension = fileName.substring(dotIndex + 1).toLowerCase();
                }

                // Determinam categoria
                String category = categories.getOrDefault(extension, "Others");

                // Cream folderul de categorie daca nu exista
                File categoryFolder = new File(folderPath + File.separator + category);
                if (!categoryFolder.exists()) {
                    categoryFolder.mkdir();
                }

                // Mutam fisierul in folderul categoriei
                try {
                    Path targetPath = Paths.get(categoryFolder.getAbsolutePath(), file.getName());
                    Files.move(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Mutat: " + fileName + " -> " + category);
                } catch (IOException e) {
                    System.out.println("Eroare la mutarea fisierului: " + fileName);
                    e.printStackTrace();
                }
            }
        }

        System.out.println("Organizarea a fost finalizata!");
    }
}
