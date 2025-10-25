package com.fileorganizer.services;

import java.util.*;

import com.fileorganizer.models.FileInfo;

public class DuplicateFinder {
	public List<List<FileInfo>> findDuplicates(List<FileInfo> files) {
		Map<String, List<FileInfo>> duplicatesMap = new HashMap<>();
		
		for(FileInfo file : files) {
			String name = file.getName();
			duplicatesMap.putIfAbsent(name, new ArrayList<>());
			duplicatesMap.get(name).add(file);
		}
		
		List<List<FileInfo>> duplicates = new ArrayList<>();
		for(List<FileInfo> group : duplicatesMap.values()) {
			if(group.size() > 1) {
				duplicates.add(group);
			}
	
		}
		return duplicates;
	}
}
