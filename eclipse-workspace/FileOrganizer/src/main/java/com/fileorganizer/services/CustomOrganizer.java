package com.fileorganizer.services;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;



public class CustomOrganizer {
	public void customOrganizer(String folderPath, String regula, String destinatie) {
		File folder = new File(folderPath);
		
		if (!folder.exists() || !folder.isDirectory()) {
		    System.out.println("Folderul introdus nu exista sau nu este director!");
		    return;
		}
		
		if (destinatie == null || destinatie.isBlank()) {
		    System.out.println("Destinația nu este validă.");
		    return;
		}
		
		File[] files = folder.listFiles();
		if(files == null)
		{
			System.out.println("Folderul este gol");
			return;
		}
		for(File f : files) {
			String numeFisier = null;		
			if(!f.isFile()) {
				continue;
			}
			numeFisier = f.getName();
				if(numeFisier.toLowerCase().contains(regula.toLowerCase())) {
					File categoryFolder = new File(folderPath + File.separator + destinatie);

					if(!categoryFolder.exists())
						categoryFolder.mkdir();
			
					
					try {
						Path targetPath = Paths.get(categoryFolder.getAbsolutePath(), f.getName());
						Files.move(f.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
						System.out.println("Mutat: " + numeFisier + " -> " + destinatie);
					} catch (IOException e) {
	                    System.out.println("Eroare la mutarea fisierului: " + numeFisier);
	                    e.printStackTrace();
	                }
				}
		}	
	}
	
	
}
