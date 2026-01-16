package com.fileorganizer.controllers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.fileorganizer.models.FileRecord;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class DataBaseManager {
	private static final String URL = "jdbc:sqlite:file_history.db";
	
	public static void createNewDataBase() {
		String sql = "CREATE TABLE IF NOT EXISTS file_log ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " file_name TEXT NOT NULL,"
                + " source_path TEXT NOT NULL,"
                + " destination_path TEXT NOT NULL,"
                + " move_date DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";
		
		try(Connection conn = DriverManager.getConnection(URL);
				Statement stmt =  conn.createStatement()) {
				stmt.execute(sql);
				
				} catch (SQLException e) {
					System.out.println(e.getMessage());
					
				}
	}
	public static void logFileMove(String name, String source, String destination) {
		String sql = "INSERT INTO file_log(file_name, source_path, destination_path) VALUES(?,?,?)";
		try(Connection conn = DriverManager.getConnection(URL);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {
			pstmt.setString(1, name);
			pstmt.setString(2, source);
			pstmt.setString(3, destination);
			pstmt.executeUpdate();
		} catch(SQLException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static ObservableList<FileRecord> getHistory() {
	    ObservableList<FileRecord> list = FXCollections.observableArrayList();
	    String sql = "SELECT * FROM file_log";

	    try (Connection conn = DriverManager.getConnection(URL);
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {

	        while (rs.next()) {
	            list.add(new FileRecord(
	                rs.getInt("id"),
	                rs.getString("file_name"),
	                rs.getString("source_path"),
	                rs.getString("destination_path"),
	                rs.getString("move_date")
	            ));
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return list;
	}
}
