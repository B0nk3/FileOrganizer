package com.fileorganizer.controllers;

import com.fileorganizer.models.FileInfo;
import com.fileorganizer.services.DuplicateFinder;
import com.fileorganizer.services.FileScanner;
import java.text.SimpleDateFormat;

import java.util.List;
import java.io.File;
import java.util.ArrayList;

/**
 * Clasa MainController gestioneaza logica aplicatiei FileOrganizer.
 * Se ocupa de scanarea fisierelor dintr-un folder si gasirea duplicatelor.
 */
public class MainController {

    private FileScanner scanner = new FileScanner(); // obiect pentru scanare fisiere

    /**
     * Scaneaza un folder si returneaza o lista de obiecte FileInfo.
     * Fiecare obiect contine numele fisierului, dimensiunea si data ultimei modificari.
     *
     * @param folderPath calea catre folderul de scanat
     * @return lista de obiecte FileInfo
     */
    public List<FileInfo> scanFiles(String folderPath) {
        List<FileInfo> filesList = new ArrayList<>();
        File folder = new File(folderPath);
        File[] files = folder.listFiles();

        if(files != null) {
            for(File f : files) {    
                // Formatarea datei ultimei modificari
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                // Calcularea dimensiunii fisierului
                double size = f.length() / 1024;
                String sizeStr;
                if(size < 1024.0) {
                    sizeStr = String.format("%.2f KB", size);
                } else {
                    sizeStr = String.format("%.2f MB", size / 1024);
                }

                String lastModified = sdf.format(f.lastModified());

                // Adaugarea fisierului in lista
                filesList.add(new FileInfo(f.getName(), sizeStr, lastModified));
            }
        }

        return filesList;
    }

    /**
     * Gaseste fisierele duplicate dintr-o lista de FileInfo.
     *
     * @param filesList lista de fisiere pentru verificare duplicat
     * @return lista de liste, fiecare lista contine fisiere duplicate
     */
    public List<List<FileInfo>> findDuplicates(List<FileInfo> filesList) {
        DuplicateFinder finder = new DuplicateFinder();
        return finder.findDuplicates(filesList);
    }
}
