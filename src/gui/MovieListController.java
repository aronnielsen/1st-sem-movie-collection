package gui;

import be.Category;
import be.CategoryItemPane;
import be.Movie;
import be.MovieItemPane;
import da.DBOperations;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class MovieListController {
     @FXML
    public CheckBox createCategoriesCheck;
    public TilePane tilePane;
    public HBox topPanel;
    public BorderPane borderPane;
    public ScrollPane scrollPane;
    public HBox categoriesPane;
    public ChoiceBox<String> sortingList;
    public TextField filterField;
    private Stage stage;

    private MovieItemPane selectedItem;
    private CategoryItemPane selectedCategory;
    private long currentCategory = -1;

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    public void selectSyncFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Sync Folders and Files from");



        // Optionally set an initial directory
        // directoryChooser.setInitialDirectory(new File("path/to/initial/directory"));

        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            System.out.println("Selected folder: " + selectedDirectory.getAbsolutePath());

            File[] firstLevel = selectedDirectory.listFiles();

            if (firstLevel != null) {
                syncFolder(firstLevel, true, -1);
            }


            try {
                updateMovieList(-1);
                updateCategories();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void setPaneHeight(Pane pane, double value) {
        pane.setMinHeight(value);
        pane.setPrefHeight(value);
        pane.setMaxHeight(value);
    }
    private void setPaneWidth(Pane pane, double value) {
        pane.setMinHeight(value);
        pane.setPrefHeight(value);
        pane.setMaxHeight(value);
    }

    private void setControlHeight(Control pane, double value) {
        pane.setMinHeight(value);
        pane.setPrefHeight(value);
        pane.setMaxHeight(value);
    }
    private void setControlWidth(Control pane, double value) {
        pane.setMinWidth(value);
        pane.setPrefWidth(value);
        pane.setMaxWidth(value);
    }
    
    public void setupListeners() {
        sortingList.getItems().addAll("Name A-Z", "Name Z-A");
        sortingList.setValue("Name A-Z");

        sortingList.valueProperty().addListener((obs, oldVal, newVal) -> showCategory(currentCategory));

        filterField.setOnAction(event -> showCategory(currentCategory));

        setPaneHeight(borderPane, stage.getHeight());
        setPaneWidth(borderPane, stage.getWidth());
        setControlHeight(scrollPane, stage.getHeight() - topPanel.getHeight());
        setControlWidth(scrollPane, stage.getWidth());

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            setPaneWidth(borderPane, stage.getWidth());
            setControlWidth(scrollPane, stage.getWidth());

            double width = newVal.doubleValue();
            int columns = Math.max(1, (int)(width / 200)); // 200 is an arbitrary base width per item
            tilePane.setPrefColumns(columns);

        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            setPaneHeight(borderPane, stage.getHeight());
            setControlHeight(scrollPane, stage.getHeight() - topPanel.getHeight());
        });

        tilePane.setOnMouseClicked(event -> {
            if (selectedItem != null && selectedItem.checkSelection()) {
                selectedItem.toggleSelection();
                selectedItem = null;
            }
        });
    }

    private void updateMovieList(long categoryID) throws SQLException {
        List<Movie> movies;
        currentCategory = categoryID;
        if (categoryID >= 0) {
            movies = DBOperations.getAllMoviesInCategory(categoryID);

        } else {
            movies = DBOperations.getAllMovies();
        }

        assert movies != null;
        switch (sortingList.getValue()) {
            case "Name A-Z":
                movies.sort((u1, u2) -> u1.getTitle().compareToIgnoreCase(u2.getTitle()));
                break;
            case "Name Z-A":
                movies.sort((u1, u2) -> u2.getTitle().compareToIgnoreCase(u1.getTitle()));
                break;
            default:
                break;
        }

        List<Movie> filteredMovies = movies.stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(filterField.getText().toLowerCase()))
                .toList();

        System.out.println("Updating");

        tilePane.getChildren().clear();

        for (Movie movie : filteredMovies) {
            MovieItemPane item = new MovieItemPane(false);
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/File@32x.png")));
            item.setMovie(movie);
            System.out.println(movie.getImage());

            if (!Objects.equals(movie.getImage(), "")) {
                image = new Image("file:" + movie.getImage());
            }

            item.setImage(image);
            item.setLabel(movie.getTitle());

            item.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1) {
                    if (selectedItem != null) {
                        selectedItem.toggleSelection();
                    }

                    if (selectedItem != item) {
                        selectedItem = item;
                        item.toggleSelection();
                    }
                }

                event.consume();
            });

            tilePane.getChildren().add(item);
        }
    }

    private CategoryItemPane createCategoryItem(Category category) {
        CategoryItemPane item = new CategoryItemPane(this);
        item.setLabel(category.getTitle());

        item.setOnMouseClicked(event -> {
            showCategory(category.getId());

            if (selectedCategory != null) {
                selectedCategory.toggleSelection();
            }

            selectedCategory = item;
            item.toggleSelection();
        });

        return item;
    }

    private void updateCategories() {
        List<Category> categories = DBOperations.getAllCategories();
        categoriesPane.getChildren().clear();

        if (categories != null) {
            Category tempCat = new Category();
            tempCat.setTitle("All");
            tempCat.setId(-1);
            categoriesPane.getChildren().add(createCategoryItem(tempCat));

            for (Category category : categories) {
                categoriesPane.getChildren().add(createCategoryItem(category));
            }
        }
    }

    private void syncFolder(File[] files, boolean root, long catID) {
        String userHome = System.getProperty("user.home");
        File destinationFolder = new File(userHome, "Pictures/MovieCollection_AN_Posters");

        assert files != null;
        for (File file : files) {
            if (file.isDirectory() && !file.isHidden()) {
                File[] filesInFolder = (file.listFiles() != null) ? file.listFiles() : null;

                assert filesInFolder != null;
                if (root && createCategoriesCheck.isSelected()) {
                    long categoryID = DBOperations.addCategory(file.getName(), "");
                    System.out.println("Created category " + file.getName() + " - with id: " + categoryID);

                    syncFolder(filesInFolder, false, categoryID);
                } else {
                    syncFolder(filesInFolder, false, -1);
                }
            } else if (!file.isHidden() && file.getName().contains(".mp4")) {
                System.out.println(destinationFolder + "/" + file.getName(). replace(".mp4", ".jpg").replace(" ", "_"));
                File imageCheck = new File(destinationFolder + "/" + file.getName(). replace(".mp4", ".jpg").replace(" ", "_"));
                String imagePath = "";
                if (imageCheck.exists()) {
                    imagePath = imageCheck.getPath();
                }

                if (catID > -1) {
                    DBOperations.addMovieWithCategories(file.getName().replace(".mp4", ""), imagePath, new long[]{catID});
                } else {
                    DBOperations.addMovie(file.getName().replace(".mp4", ""), imagePath);
                }
            }
        }
    }

    public void showCategory(long categoryID)  {
        try {
            updateMovieList(categoryID);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private File selectedImage;
    private File oldImage;

    public void createAddModal() {
        selectedImage = null;
        // Create a new stage for the modal
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL); // Set the modality
        modalStage.setTitle("Add Movie");

        // VBox as the container
        VBox vBox = new VBox(10); // 10 is the spacing between elements
        vBox.setPadding(new Insets(25)); // Padding around the VBox
        vBox.getStyleClass().add("modal");

        // Create UI elements
        // Title input
        Label titleLabel = new Label("Title:");
        TextField titleInput = new TextField();
        titleInput.setPromptText("Enter movie title...");

        // Image picker
        Label imageLabel = new Label("Image:");
        FileChooser fileChooser = new FileChooser();
        Button imageButton = new Button("Select Image");



        imageButton.setOnAction(e -> {
            // Handle file selection
            selectedImage = fileChooser.showOpenDialog(modalStage);
        });

        // Categories ChoiceBox
        Label categoryLabel = new Label("Category:");
        ChoiceBox<String> categoryChoiceBox = new ChoiceBox<>();

        String firstCategory = "";

        List<Category> categories = DBOperations.getAllCategories();

        assert categories != null;
        for (Category category : categories) {
            if (firstCategory.isEmpty()) {
                firstCategory = category.getTitle();
            }
            categoryChoiceBox.getItems().add(category.getTitle());
        }

        categoryChoiceBox.setValue(firstCategory);
        // Populate categoryChoiceBox with items
        // categoryChoiceBox.getItems().addAll("Category 1", "Category 2", ...);

        Button button = new Button("Add movie");
        button.setOnAction(event -> {
            addMovie(titleInput.getText() ,selectedImage);
            modalStage.close();
            showCategory(currentCategory);
        });

        // Add elements to VBox
        vBox.getChildren().addAll(titleLabel, titleInput, imageLabel, imageButton, categoryLabel, categoryChoiceBox, button);

        // Create Scene and show Stage
        Scene scene = new Scene(vBox);
        modalStage.setScene(scene);
        modalStage.showAndWait(); // Show and wait until it is closed
    }

    public void createEditModal() {
        Movie selectedMovie = selectedItem.getMovie();

        selectedImage = null;
        oldImage = null;
        // Create a new stage for the modal
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL); // Set the modality
        modalStage.setTitle("Edit Movie");

        // VBox as the container
        VBox vBox = new VBox(10); // 10 is the spacing between elements
        vBox.setPadding(new Insets(25)); // Padding around the VBox
        vBox.getStyleClass().add("modal");

        // Create UI elements
        // Title input
        Label titleLabel = new Label("Title:");
        TextField titleInput = new TextField();
        titleInput.setPromptText("Enter movie title...");
        titleInput.setText(selectedMovie.getTitle());

        // Image picker
        Label imageLabel = new Label("Image:");
        FileChooser fileChooser = new FileChooser();
        Button imageButton = new Button("Select Image");

        oldImage = new File(selectedMovie.getImage());

        imageButton.setOnAction(e -> {
            // Handle file selection

            selectedImage = fileChooser.showOpenDialog(modalStage);
        });

        // Categories ChoiceBox
        Label categoryLabel = new Label("Category:");
        /*ChoiceBox<String> categoryChoiceBox = new ChoiceBox<>();

        String firstCategory = "";

        for (Category category : DBOperations.getAllCategories()) {
            if (firstCategory.equals("")) {
                firstCategory = category.getTitle();
            }

            if (category.getTitle().equals(selectedMovie.getCategory())) {
                firstCategory = category.getTitle();
            }

            categoryChoiceBox.getItems().add(category.getTitle());
        }

        categoryChoiceBox.setValue(firstCategory);
        */
        // Populate categoryChoiceBox with items
        // categoryChoiceBox.getItems().addAll("Category 1", "Category 2", ...);

        ListView<String> listView = new ListView<>();

        List<Category> categories = DBOperations.getAllCategories();

        assert categories != null;
        for (Category category : categories) {
            listView.getItems().add(category.getTitle());
        }

        /*ObservableList<String> items = FXCollections.observableArrayList(
                "Option 1", "Option 2", "Option 3", "Option 4"
        );*/
        //listView.setItems(items);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);


        Button button = new Button("Edit movie");
        button.setOnAction(event -> {
            if (selectedImage != null) {
                editMovie(selectedMovie.getId(), titleInput.getText(), selectedImage);
            } else {
                editMovie(selectedMovie.getId(), titleInput.getText(), oldImage);
            }

            modalStage.close();
            showCategory(currentCategory);
        });

        // Add elements to VBox
        vBox.getChildren().addAll(titleLabel, titleInput, imageLabel, imageButton, categoryLabel, listView, button);

        // Create Scene and show Stage
        Scene scene = new Scene(vBox);
        modalStage.setScene(scene);
        modalStage.showAndWait(); // Show and wait until it is closed
    }

    public static String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        if (lastIndexOfDot > 0) { // Ensure there is a dot, and it's not the first character
            return fileName.substring(lastIndexOfDot + 1).toLowerCase();
        } else {
            return ""; // No extension found
        }
    }

    private String saveImage(File sourceFile, String imageName) {
        String userHome = System.getProperty("user.home");
        File destinationFolder = new File(userHome, "Pictures/MovieCollection_AN_Posters");

        System.out.println(destinationFolder);

        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        System.out.println("Documents Directory: " + destinationFolder.getAbsolutePath());

        String extension = getFileExtension(sourceFile.getName());

        File destFile = new File(destinationFolder, imageName + "." + extension);
        try {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Image saved successfully.");
            return destFile.getPath();
        } catch (IOException ex) {
            System.err.println("Error saving the image: " + ex.getMessage());
            return "";
        }
    }

    private void addMovie(String title, File image) {
        String path = saveImage(image, title.replace(" ", "_"));

        DBOperations.addMovie(title, path);
    }

    private void editMovie(long id, String title, File image) {
        String path = "";
        if (image.exists()) {
            path = saveImage(image, title.replace(" ", "_"));
        }

        DBOperations.editMovie(id, title, path);
    }
}