package com.fileorganizer;

import com.fileorganizer.controllers.DataBaseManager;
import com.fileorganizer.controllers.MainController;
import com.fileorganizer.models.FileInfo;
import com.fileorganizer.models.FileRecord;
import com.fileorganizer.services.CustomOrganizer;
import com.fileorganizer.services.FileOrganizer;
import com.fileorganizer.services.SmartOrganizer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    // Datele aplicației
    private final ObservableList<FileInfo> allFiles = FXCollections.observableArrayList();
    private final ObservableList<FileInfo> duplicateFiles = FXCollections.observableArrayList();

    // Componente UI (declare aici ca să le pot modifica din butoane)
    private Label statusLabel;
    private Label filesCountLabel;
    private Label duplicatesCountLabel;
    private Label currentFolderLabel;
    private ProgressBar progressBar;
    
    // Referințe pentru Tab-uri
    private TabPane mainTabPane;
    private Tab filesTab;
    private Tab dupTab;

    @Override
    public void start(Stage stage) {
        DataBaseManager.createNewDataBase(); // Asigură-te că baza de date există
        MainController controller = new MainController();

        // 1. PREGĂTIRE DATE (Rezolvarea erorilor anterioare)
        // Creăm lista filtrată AICI pentru a o pasa curat metodelor UI
        FilteredList<FileInfo> filteredData = new FilteredList<>(allFiles, f -> true);

        // ====== Layout Principal ======
        BorderPane root = new BorderPane();
        root.getStyleClass().add("root-pane");

        // ====== 2. SIDEBAR (Stânga) ======
        VBox sidebar = createSidebar(stage, controller);
        root.setLeft(sidebar);

        // ====== 3. TOP DASHBOARD (Sus - Statistici & Search) ======
        // Pasăm filteredData ca parametru -> Elimină erorile de tip
        VBox topContainer = createTopDashboard(filteredData);
        root.setTop(topContainer);

        // ====== 4. CENTER CONTENT (Tabele) ======
        // Pasăm filteredData ca parametru
        StackPane centerContent = createCenterContent(filteredData);
        root.setCenter(centerContent);

        // ====== 5. BOTTOM STATUS BAR (Jos) ======
        HBox statusBar = createStatusBar();
        root.setBottom(statusBar);

        // ====== SCENE SETUP ======
        Scene scene = new Scene(root, 1100, 700);
        
        // Încarcă CSS (Asigură-te că styles.css e în src/main/resources)
        if (getClass().getResource("/styles.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        }
        
        stage.setScene(scene);
        stage.setTitle("File Organizer Pro");
        stage.show();
    }

    /**
     * Construiește meniul din stânga.
     */
    private VBox createSidebar(Stage stage, MainController controller) {
        VBox sidebar = new VBox(15);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(260);

        // Titlu
        Label appTitle = new Label("File Organizer");
        appTitle.getStyleClass().add("sidebar-title");
        Label appSubtitle = new Label("PRO EDITION");
        appSubtitle.getStyleClass().add("sidebar-subtitle");
        
        VBox titleBox = new VBox(2, appTitle, appSubtitle);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.setPadding(new Insets(0, 0, 20, 0));

        // Butoane Acțiuni
        Button scanBtn = createMenuButton("Scan Folder", "🔍");
        Button findDupBtn = createMenuButton("Find Duplicates", "🧩");
        Button smartOrgBtn = createMenuButton("Smart AI Organize", "🧠");
        Button standardOrgBtn = createMenuButton("Standard Organize", "🧹");
        Button historyBtn = createMenuButton("View History", "📜");
        Button clearBtn = createMenuButton("Clear All", "♻");

        // Secțiune Custom Organize (Collapsible)
        TitledPane customPane = new TitledPane();
        customPane.setText("Custom Rules");
        customPane.setCollapsible(true);
        customPane.setExpanded(false);
        
        TextField ruleField = new TextField();
        ruleField.setPromptText("Ext (e.g., pdf)");
        TextField destField = new TextField();
        destField.setPromptText("Folder Name");
        Button applyCustomBtn = new Button("Apply Rule");
        applyCustomBtn.getStyleClass().add("action-button-small");
        
        VBox customBox = new VBox(10, 
            new Label("File contains/ext:"), ruleField, 
            new Label("Move to:"), destField, 
            applyCustomBtn
        );
        customPane.setContent(customBox);

        // --- Logica Butoane ---
        
        // 1. Scan
        scanBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Select Folder to Scan");
            File selectedDir = chooser.showDialog(stage);
            if (selectedDir != null) {
                simulateProcessing(() -> {
                    List<FileInfo> files = controller.scanFiles(selectedDir.getAbsolutePath());
                    Platform.runLater(() -> {
                        allFiles.setAll(files);
                        duplicateFiles.clear();
                        
                        // Actualizare label-uri
                        currentFolderLabel.setText(selectedDir.getName());
                        filesCountLabel.setText(String.valueOf(files.size()));
                        duplicatesCountLabel.setText("0");
                        statusLabel.setText("Scanned: " + selectedDir.getAbsolutePath());
                        
                        mainTabPane.getSelectionModel().select(filesTab);
                    });
                });
            }
        });

        // 2. Find Duplicates
        findDupBtn.setOnAction(e -> {
            if(allFiles.isEmpty()) { showToast("Scan a folder first!", true); return; }
            
            simulateProcessing(() -> {
                List<List<FileInfo>> duplicates = controller.findDuplicates(new ArrayList<>(allFiles));
                Platform.runLater(() -> {
                    duplicateFiles.clear();
                    List<FileInfo> flattened = new ArrayList<>();
                    for(List<FileInfo> group : duplicates) {
                        flattened.addAll(group);
                    }
                    duplicateFiles.addAll(flattened);
                    
                    duplicatesCountLabel.setText(String.valueOf(flattened.size()));
                    statusLabel.setText("Found " + flattened.size() + " duplicates.");
                    mainTabPane.getSelectionModel().select(dupTab);
                });
            });
        });

        // 3. Organizers
        smartOrgBtn.setOnAction(e -> handleOrganization(stage, "smart", null, null));
        standardOrgBtn.setOnAction(e -> handleOrganization(stage, "standard", null, null));
        applyCustomBtn.setOnAction(e -> handleOrganization(stage, "custom", ruleField.getText(), destField.getText()));

        // 4. History
        historyBtn.setOnAction(e -> showHistoryWindow());

        // 5. Clear
        clearBtn.setOnAction(e -> {
            allFiles.clear();
            duplicateFiles.clear();
            filesCountLabel.setText("0");
            duplicatesCountLabel.setText("0");
            currentFolderLabel.setText("None");
            statusLabel.setText("Ready");
        });

        sidebar.getChildren().addAll(
            titleBox, 
            new Label("MAIN ACTIONS"), 
            scanBtn, findDupBtn, 
            new Separator(),
            new Label("TOOLS"),
            smartOrgBtn, standardOrgBtn, customPane,
            new Region(), // spacer
            historyBtn, clearBtn
        );
        VBox.setVgrow(sidebar.getChildren().get(10), Priority.ALWAYS); // Push history/clear down

        return sidebar;
    }

    /**
     * Construiește Dashboard-ul de sus (Carduri statistici + Search).
     */
    private VBox createTopDashboard(FilteredList<FileInfo> filteredFiles) {
        VBox topBox = new VBox(15);
        topBox.setPadding(new Insets(20, 20, 10, 20));
        topBox.getStyleClass().add("top-dashboard");

        // Linie Header
        HBox headerLine = new HBox(15);
        headerLine.setAlignment(Pos.CENTER_LEFT);

        // Carduri Statistici
        // Notă: Salvăm referințele la Label-uri pentru a le putea modifica mai târziu
        HBox folderCard = createStatCard("Current Folder", "None", "📁");
        currentFolderLabel = (Label) ((VBox)folderCard.getChildren().get(1)).getChildren().get(0); 

        HBox fileCard = createStatCard("Total Files", "0", "📄");
        filesCountLabel = (Label) ((VBox)fileCard.getChildren().get(1)).getChildren().get(0);

        HBox dupCard = createStatCard("Duplicates", "0", "⚠️");
        duplicatesCountLabel = (Label) ((VBox)dupCard.getChildren().get(1)).getChildren().get(0);

        // Search Box
        TextField searchField = new TextField();
        searchField.setPromptText("Search files...");
        searchField.getStyleClass().add("search-field");
        searchField.setPrefWidth(250);
        
        // --- LOGICA DE SEARCH ---
        searchField.textProperty().addListener((obs, old, text) -> {
            String filter = text == null ? "" : text.trim().toLowerCase();
            filteredFiles.setPredicate(file -> {
                if (filter.isEmpty()) return true;
                return file.getName().toLowerCase().contains(filter);
            });
        });
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerLine.getChildren().addAll(folderCard, fileCard, dupCard, spacer, searchField);
        topBox.getChildren().add(headerLine);

        return topBox;
    }

    /**
     * Construiește zona centrală cu Tab-uri.
     */
    private StackPane createCenterContent(FilteredList<FileInfo> filteredFiles) {
        StackPane center = new StackPane();
        center.setPadding(new Insets(10, 20, 20, 20));

        mainTabPane = new TabPane();
        mainTabPane.getStyleClass().add("floating-tabs");

        // --- Files Table (Folosește lista filtrată primită ca parametru) ---
        TableView<FileInfo> filesTable = createStyledTable();
        
        SortedList<FileInfo> sorted = new SortedList<>(filteredFiles);
        sorted.comparatorProperty().bind(filesTable.comparatorProperty());
        filesTable.setItems(sorted);

        filesTab = new Tab("All Files", filesTable);
        filesTab.setClosable(false);

        // --- Duplicates Table ---
        TableView<FileInfo> dupTable = createStyledTable();
        dupTable.setItems(duplicateFiles);
        dupTab = new Tab("Duplicates", dupTable);
        dupTab.setClosable(false);

        mainTabPane.getTabs().addAll(filesTab, dupTab);
        center.getChildren().add(mainTabPane);
        
        return center;
    }

    /**
     * Bara de jos cu status și progress bar.
     */
    private HBox createStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setPadding(new Insets(5, 20, 5, 20));
        statusBar.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("Ready");
        statusLabel.setStyle("-fx-text-fill: #888;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(150);

        statusBar.getChildren().addAll(statusLabel, spacer, progressBar);
        return statusBar;
    }

    // ====== Helper Methods ======

    private Button createMenuButton(String text, String icon) {
        Button btn = new Button(icon + "  " + text);
        btn.getStyleClass().add("menu-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private HBox createStatCard(String title, String value, String iconEmoji) {
        HBox card = new HBox(15);
        card.getStyleClass().add("stat-card");
        card.setAlignment(Pos.CENTER_LEFT);

        Label iconLbl = new Label(iconEmoji);
        iconLbl.setStyle("-fx-font-size: 24px;");

        VBox textBox = new VBox(2);
        Label valueLbl = new Label(value);
        valueLbl.getStyleClass().add("stat-value");
        Label titleLbl = new Label(title);
        titleLbl.getStyleClass().add("stat-title");
        textBox.getChildren().addAll(valueLbl, titleLbl);

        card.getChildren().addAll(iconLbl, textBox);
        return card;
    }

    private TableView<FileInfo> createStyledTable() {
        TableView<FileInfo> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<FileInfo, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        
        TableColumn<FileInfo, String> sizeCol = new TableColumn<>("Size");
        sizeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSize()));
        sizeCol.setMaxWidth(100);
        sizeCol.setMinWidth(100);

        TableColumn<FileInfo, String> dateCol = new TableColumn<>("Date Modified");
        dateCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLastModified()));
        dateCol.setMaxWidth(180);
        dateCol.setMinWidth(180);

        table.getColumns().addAll(nameCol, sizeCol, dateCol);
        
        // Context Menu (Copy Name)
        table.setRowFactory(tv -> {
            TableRow<FileInfo> row = new TableRow<>();
            MenuItem copyItem = new MenuItem("Copy Name");
            copyItem.setOnAction(e -> {
                if(row.getItem() != null) {
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(row.getItem().getName());
                    Clipboard.getSystemClipboard().setContent(cc);
                }
            });
            row.setContextMenu(new ContextMenu(copyItem));
            return row;
        });

        return table;
    }

    private void handleOrganization(Stage stage, String type, String rule, String dest) {
        if ("custom".equals(type) && (rule == null || rule.isEmpty() || dest == null || dest.isEmpty())) {
            showToast("Please fill Rule and Destination!", true);
            return;
        }

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Folder to Organize");
        File selectedDir = chooser.showDialog(stage);

        if (selectedDir != null) {
            simulateProcessing(() -> {
                try {
                    if ("smart".equals(type)) {
                        SmartOrganizer so = new SmartOrganizer();
                        // Iterăm și clasificăm fișierele
                        for(File f : selectedDir.listFiles()) {
                            if(f.isFile()) {
                                String target = so.classifyFile(f);
                                File d = new File(selectedDir, target);
                                if(!d.exists()) d.mkdir();
                                Files.move(f.toPath(), d.toPath().resolve(f.getName()));
                            }
                        }
                    } else if ("standard".equals(type)) {
                        new FileOrganizer().organizeFiles(selectedDir.getAbsolutePath());
                    } else if ("custom".equals(type)) {
                        new CustomOrganizer().customOrganizer(selectedDir.getAbsolutePath(), rule, dest);
                    }
                    
                    Platform.runLater(() -> {
                        showToast("Organization Complete!", false);
                        statusLabel.setText("Organized: " + selectedDir.getName());
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showToast("Error organizing files", true));
                    ex.printStackTrace();
                }
            });
        }
    }

    private void showHistoryWindow() {
        Stage historyStage = new Stage();
        historyStage.setTitle("Database History");

        TableView<FileRecord> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        
        TableColumn<FileRecord, String> nameCol = new TableColumn<>("File");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<FileRecord, String> srcCol = new TableColumn<>("Source");
        srcCol.setCellValueFactory(new PropertyValueFactory<>("source"));
        
        TableColumn<FileRecord, String> dstCol = new TableColumn<>("Destination");
        dstCol.setCellValueFactory(new PropertyValueFactory<>("destination"));
        
        TableColumn<FileRecord, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));

        table.getColumns().addAll(nameCol, srcCol, dstCol, dateCol);
        table.setItems(DataBaseManager.getHistory());

        VBox root = new VBox(table);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 800, 500);
        // Reutilizăm stilurile
        if (getClass().getResource("/styles.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        }
        historyStage.setScene(scene);
        historyStage.show();
    }

    private void simulateProcessing(Runnable task) {
        progressBar.setVisible(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        statusLabel.setText("Processing...");
        
        new Thread(() -> {
            try {
                Thread.sleep(500); // Mic delay vizual
                task.run();
            } catch (InterruptedException e) { e.printStackTrace(); }
            finally {
                Platform.runLater(() -> {
                    progressBar.setVisible(false);
                    progressBar.setProgress(0);
                });
            }
        }).start();
    }

    private void showToast(String message, boolean isError) {
        Alert alert = new Alert(isError ? Alert.AlertType.ERROR : Alert.AlertType.INFORMATION, message);
        alert.setHeaderText(null);
        alert.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}