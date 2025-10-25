package com.fileorganizer;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import com.fileorganizer.controllers.MainController;
import com.fileorganizer.models.FileInfo;

import com.fileorganizer.services.FileOrganizer;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.collections.ObservableList;

/**
 * Clasa principala FileOrganizer.
 * Creeaza fereastra, tabel pentru fisiere si tabel pentru duplicate.
 */
public class Main extends Application {

    /**
     * Metoda start este punctul de intrare al aplicatiei.
     * Creeaza elemente UI, butoane si logica de procesare.
     *
     * @param stage Fereastra principala
     */
    @Override
    public void start(Stage stage) {
        // Layout principal
        VBox layout = new VBox();
        layout.setSpacing(15);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #f5f5f5;");

        // Titlu
        Label title = new Label("📁 File Organizer");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Butoane
        Button scanButton = new Button("🔍 Scan Files");
        Button findDupButton = new Button("🧩 Find Duplicates");
        scanButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8;");
        findDupButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8;");
        scanButton.setOnMouseEntered(e -> scanButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8;"));
        scanButton.setOnMouseExited(e -> scanButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8;"));
        findDupButton.setOnMouseEntered(e -> findDupButton.setStyle("-fx-background-color: #1e8449; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8;"));
        findDupButton.setOnMouseExited(e -> findDupButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8;"));
        Button organizeFiles = new Button("Organize file");
        organizeFiles.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8;");
        organizeFiles.setOnMouseEntered(e -> organizeFiles.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8;"));
        organizeFiles.setOnMouseExited(e -> organizeFiles.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 8;"));

        // Bara cu butoane
        HBox buttonsBar = new HBox(10, scanButton, findDupButton, organizeFiles);
        buttonsBar.setAlignment(Pos.CENTER);

        // Tabel principal
        TableView<FileInfo> table = new TableView<>();
        table.setPlaceholder(new Label("No files scanned."));
        table.setStyle("-fx-background-radius: 8; -fx-border-radius: 8;");
        TableColumn<FileInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        TableColumn<FileInfo, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSize()));
        TableColumn<FileInfo, String> dateCol = new TableColumn<>("Last Modified");
        dateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLastModified()));
        table.getColumns().addAll(nameCol, sizeCol, dateCol);

        // Tabel pentru duplicate
        Label dupLabel = new Label("Duplicates:");
        dupLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        TableView<FileInfo> duplicatesTable = new TableView<>();
        duplicatesTable.setPlaceholder(new Label("No duplicates found."));
        TableColumn<FileInfo, String> dupNameCol = new TableColumn<>("Duplicate Name");
        dupNameCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));
        TableColumn<FileInfo, String> dupSizeCol = new TableColumn<>("Size");
        dupSizeCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSize()));
        TableColumn<FileInfo, String> dupDateCol = new TableColumn<>("Last Modified");
        dupDateCol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getLastModified()));
        duplicatesTable.getColumns().addAll(dupNameCol, dupSizeCol, dupDateCol);

        // Controller logic
        MainController controller = new MainController();

        // Actiuni butoane
        scanButton.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select folder to scan");
            File selectedDir = chooser.showDialog(stage);

            if (selectedDir != null) {
                List<FileInfo> files = controller.scanFiles(selectedDir.getAbsolutePath());
                table.setItems(FXCollections.observableArrayList(files));
            }
        });

        findDupButton.setOnAction(e -> {
            ObservableList<FileInfo> currentFiles = table.getItems();
            if (currentFiles.isEmpty()) {
                duplicatesTable.getItems().clear();
                duplicatesTable.setPlaceholder(new Label("Please scan files first."));
                return;
            }

            List<List<FileInfo>> duplicates = controller.findDuplicates(new ArrayList<>(currentFiles));
            duplicatesTable.getItems().clear();

            if (duplicates.isEmpty()) {
                duplicatesTable.setPlaceholder(new Label("No duplicates found."));
            } else {
                List<FileInfo> flattened = new ArrayList<>();
                for (List<FileInfo> group : duplicates) flattened.addAll(group);
                duplicatesTable.getItems().addAll(flattened);
            }
        });
        
        organizeFiles.setOnAction(e -> {
        	// Deschide un selector de folder
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select folder to organize");
            File selectedDir = chooser.showDialog(stage);

            // Verifica daca s-a selectat un folder
            if (selectedDir != null) {
                // Creeaza un obiect din clasa FileOrganizer
                com.fileorganizer.services.FileOrganizer organizer = new com.fileorganizer.services.FileOrganizer();

                // Apeleaza metoda organizeFiles()
                organizer.organizeFiles(selectedDir.getAbsolutePath());

                // Afiseaza mesaj de confirmare
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.INFORMATION,
                    "Files organized successfully in " + selectedDir.getName()
                );
                alert.setHeaderText("Success");
                alert.show();
            } else {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.WARNING,
                    "No folder selected for organization."
                );
                alert.setHeaderText("Warning");
                alert.show();
            }
        });
        // Asamblare layout
        layout.getChildren().addAll(title, buttonsBar, table, dupLabel, duplicatesTable);

        Scene scene = new Scene(layout, 700, 500);
        stage.setScene(scene);
        stage.setTitle("File Organizer");
        stage.show();
    }

    /**
     * Punct de pornire aplicatie.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
