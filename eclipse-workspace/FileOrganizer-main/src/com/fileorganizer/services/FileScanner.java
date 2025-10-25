package com.fileorganizer.services;

import java.io.File;

/**
 * Clasa FileScanner se ocupa cu scanarea unui folder
 * si afisarea fisierelor existente in consola.
 */
public class FileScanner {

    /**
     * Scaneaza un folder si afiseaza numele fisierelor in consola.
     *
     * @param folderPath calea catre folderul de scanat
     */
    public void scanFolder(String folderPath) {
        File folder = new File(folderPath);

        // Verificam daca folderul exista si este valid
        if(!folder.exists() || !folder.isDirectory()) {
            System.out.println("Folderul nu exista sau nu este valid!");
            return;
        }

        File[] files = folder.listFiles();

        // Daca folderul este gol
        if(files == null || files.length == 0) {
            System.out.println("Folderul este gol");
            return;
        }

        // Afisam fisierele din folder
        System.out.println("Fisiere in folderul " + folderPath + ":");
        for(File f : files) {
            System.out.println(f.getName());
        }
    }
}
