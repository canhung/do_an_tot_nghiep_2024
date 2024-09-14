package org.example.demo;

import javafx.application.Application;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.stage.DirectoryChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

public class App extends Application {

    private TextField filePathField = new TextField();
    private TextFlow resultTextFlow = new TextFlow();
    private String selectedFilePath;
    private String analysisFilePath;
    private TextField analyzedFilePathField = new TextField();

    private BorderPane mainPane = new BorderPane();
    private Stage primaryStage; // Thêm biến toàn cục để lưu trữ primaryStage
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Tweet Analysis");

        // Create MenuBar
        MenuBar menuBar = new MenuBar();

        // Create 'Analyze' menu
        Menu analyzeMenu = new Menu("Analyze");
        MenuItem analyzeMenuItem = new MenuItem("Analyze");
        analyzeMenu.getItems().add(analyzeMenuItem);

        // Create 'Mutual Information' menu
        Menu miMenu = new Menu("Mutual Information");
        MenuItem miMenuItem = new MenuItem("Calculate MI");
        miMenu.getItems().add(miMenuItem);

        // Add menus to the MenuBar
        menuBar.getMenus().addAll(analyzeMenu, miMenu);

        // Event handlers for menu items
        analyzeMenuItem.setOnAction(e -> showAnalyzeScreen()); // go to Analyze Screen
        miMenuItem.setOnAction(e -> showMIScreen()); // go to Mutual Information Screen

        // Main layout
        VBox vbox = new VBox();
        vbox.getChildren().add(menuBar);
        vbox.getChildren().add(mainPane); // Thêm BorderPane vào VBox

        // Scene and Stage
        Scene scene = new Scene(vbox, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Hiển thị màn hình Analyze mặc định
        showAnalyzeScreen();
    }

    private void showAnalyzeScreen() {
        // Layout cho màn hình Analyze
        VBox analyzeScreen = new VBox();
        analyzeScreen.setSpacing(10);

        HBox fileInputBox = new HBox();
        fileInputBox.setSpacing(10);

        filePathField.setPrefWidth(400);

        Button selectFileButton = new Button("Select");
        selectFileButton.setOnAction(e -> selectFile());

        Button analyzeButton = new Button("Analyze");
        analyzeButton.setOnAction(e -> analyzeTweet());

        fileInputBox.getChildren().addAll(filePathField, selectFileButton, analyzeButton);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(resultTextFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);

        BorderPane resultPane = new BorderPane();
        resultPane.setCenter(scrollPane);
        resultPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(1))));

        Label analyzedFileLabel = new Label("Analyzed Folder Path:");
        analyzedFilePathField.setPrefWidth(400);
        analyzedFilePathField.setEditable(false);

        Button openFileButton = new Button("Open Analyzed Folder");
        openFileButton.setOnAction(e -> openFolder(analyzedFilePathField.getText()));

        HBox analyzedFileHBox = new HBox();
        analyzedFileHBox.setSpacing(10);
        analyzedFileHBox.getChildren().addAll(analyzedFilePathField, openFileButton);

        analyzeScreen.getChildren().addAll(fileInputBox, resultPane, analyzedFileLabel, analyzedFileHBox);

        // Đặt nội dung của mainPane là màn hình Analyze
        mainPane.setCenter(analyzeScreen);
    }

    private void showMIScreen() {
        VBox miScreen = new VBox();
        miScreen.setSpacing(10);

        Label miLabel = new Label("Mutual Information Calculation:");

        TextField folderPathField = new TextField();
        folderPathField.setPromptText("Select folder with feature files...");
        folderPathField.setEditable(false);

        Button selectFolderButton = new Button("Select Folder");
        selectFolderButton.setOnAction(e -> selectFolder(folderPathField));

        Button calculateMIButton = new Button("Calculate MI");
        calculateMIButton.setOnAction(e -> {
            try {
                String folderPath = folderPathField.getText();
                if (folderPath != null && !folderPath.isEmpty()) {
                    File folder = new File(folderPath);
                    if (folder.exists() && folder.isDirectory()) {
                        mutualInformation(folder);
                    } else {
                        showError("Selected path is not a valid directory.");
                    }
                } else {
                    showError("Please select a folder.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showError("Error calculating MI: " + ex.getMessage());
            }
        });

        // Create an HBox to place buttons next to each other
        HBox buttonBox = new HBox(10); // 10 is the spacing between buttons
        buttonBox.getChildren().addAll(selectFolderButton, calculateMIButton);

        miScreen.getChildren().addAll(miLabel, folderPathField, buttonBox);

        mainPane.setCenter(miScreen);
    }


    private void selectFolder(TextField folderPathField) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedFolder = directoryChooser.showDialog(primaryStage);
        if (selectedFolder != null) {
            folderPathField.setText(selectedFolder.getAbsolutePath());
        }
    }

    private void showError(String message) {
        Text errorText = new Text(message);
        errorText.setFill(Color.RED);
        resultTextFlow.getChildren().clear();
        resultTextFlow.getChildren().add(errorText);
        Alert alert = new Alert(Alert.AlertType.ERROR); // type Alert ERROR
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void selectFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            selectedFilePath = file.getAbsolutePath();
            filePathField.setText(selectedFilePath);
        }
    }

    private void mutualInformation(File selectedFolder) {
        try {
            MutualInformationProcessor miProcessor = new MutualInformationProcessor();
            Map<String, Integer> allFeatureCounts = miProcessor.loadAllFeatureCounts(selectedFolder);


            // get list folder of authors
            String[] authorFolders = selectedFolder.list((dir, name) -> new File(dir, name).isDirectory() && name.endsWith("_analysis"));

            int NC = 500 * authorFolders.length;  // Sumary of words for all authors
            if (authorFolders == null) {
                System.out.println("Can not find folder of author");
                return;
            }

            for (String authorFolder : authorFolders) {
                String authorFolderPath = selectedFolder.getAbsolutePath() + File.separator + authorFolder;
                // Process feature for file bigram, trigram, fourgram
                miProcessor.processFeatures(authorFolderPath, "char_bigram.txt", allFeatureCounts, NC);
                miProcessor.processFeatures(authorFolderPath, "char_trigram.txt", allFeatureCounts, NC);
                miProcessor.processFeatures(authorFolderPath, "char_fourgram.txt", allFeatureCounts, NC);
            }


            // Thông báo thành công và mở folder
            Desktop.getDesktop().open(selectedFolder);
        } catch (IOException ioException) {
            ioException.printStackTrace();
            showError("Error processing MI: " + ioException.getMessage());
        }
    }

    private void analyzeTweet() {
        if (selectedFilePath != null && !selectedFilePath.isEmpty()) {
            try {
                // Tạo đường dẫn file chuẩn hóa đầu ra
                String normalizedFilePath = selectedFilePath;

                // Chuẩn hóa file đầu vào và lấy thông báo kết quả
                String normalizationMessage = FileNormalization.normalizeFile(selectedFilePath, normalizedFilePath);

                // Hiển thị thông báo kết quả chuẩn hóa
                Text normalizationText = new Text(normalizationMessage + "\n\n");
                normalizationText.setFill(Color.BLUE);
                resultTextFlow.getChildren().clear();
                resultTextFlow.getChildren().add(normalizationText);

                if (normalizationMessage.contains("successfully")) {

                    String content = new String(Files.readAllBytes(Paths.get(normalizedFilePath)));
                    // Split tweets by line
                    String[] tweets = content.split("\\R");

                    // Join tweets with space
                    String joinedTweets = String.join(" ", tweets);

                    // Set joined tweets to TweetProcessor
                    TweetProcessor.setTweetContent(joinedTweets);

                    // Analyze the tweet and get the result file path
                    analysisFilePath = TweetProcessor.analyzeTweet(normalizedFilePath);

                    // Display the tweets and analysis file path
                    StringBuilder resultText = new StringBuilder();
                    for (int i = 0; i < tweets.length; i++) {
                        resultText.append((i + 1) + ". " + tweets[i] + "\n\n");
                    }

                    Text tweetsTextNode = new Text(resultText.toString());
                    Text analysisPathText = new Text("Analysis saved to: " + analysisFilePath);
                    analysisPathText.setFill(Color.RED);

                    resultTextFlow.getChildren().addAll(tweetsTextNode, analysisPathText);

                    // showanalyzedFilePathField
                    analyzedFilePathField.setText(analysisFilePath);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Text errorText = new Text("Error reading the file.");
                resultTextFlow.getChildren().clear();
                resultTextFlow.getChildren().add(errorText);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            Text errorText = new Text("Please select a file first.");
            resultTextFlow.getChildren().clear();
            resultTextFlow.getChildren().add(errorText);
        }
    }

    private void openFolder(String folderPath) {
        if (folderPath != null && !folderPath.isEmpty()) {
            File folder = new File(folderPath);
            if (folder.exists() && folder.isDirectory()) {
                try {
                    Desktop.getDesktop().open(folder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Folder is not existing or this isn't folder.");
            }
        }
    }

   public static void main(String[] args) {
        launch(args);
    }
}
