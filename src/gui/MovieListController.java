package gui;

import be.Category;
import be.CategoryItemPane;
import be.Movie;
import be.MovieItemPane;
import bl.CategoryService;
import bl.MovieService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MovieListController {
    @FXML
    public CheckBox createCategoriesCheck;
    @FXML
    public TilePane tilePane;
    @FXML
    public HBox topPanel;
    @FXML
    public BorderPane borderPane;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public HBox categoriesPane;
    @FXML
    public ChoiceBox<String> sortingList;
    @FXML
    public TextField filterField;
    @FXML
    public Button editMovieButton;
    @FXML
    public Button editCategoryButton;
    @FXML
    public HBox centeringContainer;
    @FXML
    public VBox mainContent;
    @FXML
    public TextField minRatingValue;
    @FXML
    public TextField maxRatingValue;
    @FXML
    public Button filterButton;

    private Stage stage;
    private MovieItemPane selectedItem;
    private CategoryItemPane selectedCategory;
    private long currentCategory = -1;
    private Category currentCategoryItem;
    private boolean hasCheckedMovieAge = false;
    private final MovieService movieService = new MovieService();
    private final CategoryService categoryService = new CategoryService();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void selectSyncFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Sync Folders and Files from");

        File selectedDirectory = directoryChooser.showDialog(stage);

        if (selectedDirectory != null) {
            File[] firstLevel = selectedDirectory.listFiles();

            if (firstLevel != null) {
                movieService.syncFolder(firstLevel, createCategoriesCheck.isSelected(), true, -1);
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
        pane.setMinWidth(value);
        pane.setPrefWidth(value);
        pane.setMaxWidth(value);
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
        sortingList.getItems().addAll("Name A-Z", "Name Z-A", "Rating ASC", "Rating DESC", "Genre ASC", "Genre DESC");
        sortingList.setValue("Name A-Z");

        sortingList.valueProperty().addListener((obs, oldVal, newVal) -> showCategory(currentCategory));

        filterButton.setOnAction(event -> showCategory(currentCategory));
        filterField.setOnAction(event -> showCategory(currentCategory));
        minRatingValue.setOnAction(event -> showCategory(currentCategory));
        maxRatingValue.setOnAction(event -> showCategory(currentCategory));

        respondToWidthAndHeightChanges();

        minRatingValue.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[0-1]?\\d?.?\\d?")) {
                minRatingValue.setText(oldValue);
            }
            try {
                double value = Double.parseDouble(newValue);
                if (value < 0.0 || value > Double.parseDouble(maxRatingValue.getText())) {
                    minRatingValue.setText(oldValue);
                }
            } catch (NumberFormatException e) {
                if (!newValue.isEmpty()) {
                    minRatingValue.setText(oldValue);
                }
            }
        });
        maxRatingValue.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[0-1]?\\d?.?\\d?")) {
                maxRatingValue.setText(oldValue);
            }
            try {
                double value = Double.parseDouble(newValue);
                if (value < Double.parseDouble(minRatingValue.getText()) || value > 10.0) {
                    maxRatingValue.setText(oldValue);
                }
            } catch (NumberFormatException e) {
                if (!newValue.isEmpty()) {
                    maxRatingValue.setText(oldValue);
                }
            }
        });

        stage.maximizedProperty().addListener((observable, oldValue, newValue) -> respondToWidthAndHeightChanges());

        stage.widthProperty().addListener((obs, oldVal, newVal) -> respondToWidthAndHeightChanges());

        stage.heightProperty().addListener((obs, oldVal, newVal) -> respondToWidthAndHeightChanges());

        tilePane.setOnMouseClicked(event -> {
            if (selectedItem != null && selectedItem.checkSelection()) {
                selectedItem.deselect();
                selectedItem = null;
                editMovieButton.setDisable(true);
            }
        });

        updateCategories();
        showCategory(currentCategory);
    }

    private void respondToWidthAndHeightChanges() {
        double offset = 39;
        double width = stage.getWidth();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        if (screenBounds.getWidth() == stage.getWidth() && screenBounds.getHeight() == stage.getHeight()) {
            offset = 30;
        }

        double contentHeight = stage.getHeight() - offset;
        double contentMainHeight = contentHeight - topPanel.getHeight();
        double contentMainNoCategoriesHeight = contentHeight - topPanel.getHeight() - categoriesPane.getHeight();

        setPaneHeight(borderPane, contentHeight);
        setPaneWidth(borderPane, width);

        setControlHeight(scrollPane, contentMainNoCategoriesHeight);
        setControlWidth(scrollPane, width);

        setPaneHeight(mainContent, contentMainHeight);
        setPaneWidth(mainContent, width);

        int columns = Math.max(1, (int)(width / 200));
        double gapH = tilePane.getHgap();
        double totalGap = gapH * (columns - 1);
        double tilePaneWidth = totalGap + columns * 150;
        setPaneWidth(tilePane, tilePaneWidth + 20);

        int rows = (int)Math.ceil((double) tilePane.getChildren().size() / columns);
        double gapV = tilePane.getHgap();
        double totalGapV = gapV * (rows - 1);
        double tilePaneHeight = totalGapV + rows * 240;
        setPaneHeight(tilePane, tilePaneHeight + 20);

        tilePane.setPrefColumns(columns);

        setPaneHeight(centeringContainer, tilePaneHeight + 20);
        setPaneWidth(centeringContainer, width - 20);

        setPaneWidth(topPanel, width);

        int catItem = categoriesPane.getChildren().size();
        double catWidth = 105 * catItem - 10;
        categoriesPane.setMinWidth(catWidth);
        catWidth = 155 * catItem - 10;
        categoriesPane.setMaxWidth(catWidth);
    }

    private void updateMovieList(long categoryID) throws SQLException {
        List<Movie> movies;
        currentCategory = categoryID;
        if (categoryID >= 0) {
            movies = movieService.getAllMoviesInCategory(categoryID);

        } else {
            movies = movieService.getAllMovies();
        }

        if (movies == null) {
            return;
        }

        switch (sortingList.getValue()) {
            case "Name A-Z":
                movies.sort((u1, u2) -> u1.getTitle().compareToIgnoreCase(u2.getTitle()));
                break;
            case "Name Z-A":
                movies.sort((u1, u2) -> u2.getTitle().compareToIgnoreCase(u1.getTitle()));
                break;
            case "Rating ASC":
                movies.sort((u1, u2) -> (int) (u1.getRating() - u2.getRating()));
                break;
            case "Rating DESC":
                movies.sort((u1, u2) -> (int) (u2.getRating() - u1.getRating()));
                break;
            case "Genre ASC":
                movies.sort((u1, u2) -> u1.getCategories().compareToIgnoreCase(u2.getCategories()));
                break;
            case "Genre DESC":
                movies.sort((u1, u2) -> u2.getCategories().compareToIgnoreCase(u1.getCategories()));
                break;
            default:
                break;
        }

        List<Movie> filteredMovies = movies.stream()
                .filter(movie -> movie.getTitle().toLowerCase().contains(filterField.getText().toLowerCase()) | movie.getCategories().toLowerCase().contains(filterField.getText().toLowerCase()))
                .filter(movie -> movie.getRating() >= Double.parseDouble(minRatingValue.getText()) && movie.getRating() <= Double.parseDouble(maxRatingValue.getText()))
                .toList();

        tilePane.getChildren().clear();

        List<Movie> oldMovies = new ArrayList<>();

        for (Movie movie : filteredMovies) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem editAction = new MenuItem("Edit Movie");
            MenuItem addAction = new MenuItem("Add Movie");
            MenuItem deleteAction = new MenuItem("Delete Movie");
            MenuItem generateImageAction = new MenuItem("Generate Thumbnail");
            MenuItem openImage = new MenuItem("Open image");
            MenuItem openFolder = new MenuItem("Open main folder");
            SeparatorMenuItem separator = new SeparatorMenuItem();
            SeparatorMenuItem separator2 = new SeparatorMenuItem();

            if (movie.getImage() != null) {
                contextMenu.getItems().addAll(editAction, deleteAction, separator, addAction, separator2, generateImageAction, openImage, openFolder);
            } else {
                contextMenu.getItems().addAll(editAction, deleteAction, separator, addAction, separator2, generateImageAction, openFolder);
            }

            editAction.setOnAction(e -> {
                try {
                    createEditModal(movie);
                } catch (IOException error) {
                    System.out.println(error.getMessage());
                }
            });
            addAction.setOnAction(e -> {
                try {
                    createAddModal();
                } catch (IOException error) {
                    System.out.println(error.getMessage());
                }
            });
            deleteAction.setOnAction(e -> {
                if (movieService.createDeleteConfirmation(movie.getTitle())) {
                    movieService.deleteMovie(movie.getId());
                    showCategory(currentCategory);
                }
            });
            generateImageAction.setOnAction(e -> movieService.generateThumbnail(movie));
            openFolder.setOnAction(e -> movieService.openImageFolder());
            openImage.setOnAction(e -> movieService.openImage(movie.getImage()));

            MovieItemPane item = new MovieItemPane();
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/File@32x.png")));
            item.setMovie(movie);

            if (!hasCheckedMovieAge) {
                if (movie.getLastOpened() != null && movie.getRating() < 6.0 && movieService.beenTwoYearsSinceOpened(movie.getLastOpened())) {
                    oldMovies.add(movie);
                }
            }

            if (!Objects.equals(movie.getImage(), "") && movie.getImage() != null) {
                image = new Image("file:" + movie.getImage());
            }

            item.setImage(image);
            item.setLabel(movie.getTitle());
            item.setLabelCat(movie.getCategories());
            item.setLabelRating(movie.getRatingString());

            item.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY) {
                    if (event.getClickCount() == 1) {
                        if (selectedItem != null && selectedItem.equals(item)) {
                            selectedItem.deselect();
                            selectedItem = null;
                        } else {
                            if (selectedItem != null) {
                                selectedItem.deselect();
                            }

                            selectedItem = item;
                            selectedItem.select();
                        }

                        editMovieButton.setDisable(selectedItem == null);
                    } else if (event.getClickCount() == 2) {
                        Desktop desktop = Desktop.getDesktop();
                        try {
                            desktop.open(new File(item.getMovie().getFilelink()));
                            movieService.setMovieOpened(item.getMovie().getId());
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(item, event.getScreenX(), event.getScreenY());
                }

                event.consume();
            });

            tilePane.getChildren().add(item);
        }

        respondToWidthAndHeightChanges();

        if (!hasCheckedMovieAge) {
            hasCheckedMovieAge = true;

            if (!oldMovies.isEmpty()) {
                StringBuilder message = new StringBuilder();
                for (Movie mov : oldMovies) {
                    message.append(mov.getTitle()).append("\n");
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Movies that you might want to remove");
                alert.setHeaderText("There are " + oldMovies.size() + " movie(s), that have not been opened in 2 years or more and have lower than 6 in rating.");
                alert.setContentText(message.toString());

                alert.showAndWait();
            }
        }
    }

    private CategoryItemPane createCategoryItem(Category category) {
        CategoryItemPane item = new CategoryItemPane();
        item.setCategory(category);
        item.setLabel(category.getTitle());

        if (category.getId() == currentCategory && currentCategory != -1) {
            item.select();
            currentCategoryItem = item.getCategory();
            selectedCategory = item;
        }

        ContextMenu contextMenu = new ContextMenu();
        MenuItem editAction = new MenuItem("Edit Category");
        MenuItem deleteAction = new MenuItem("Delete Category");
        SeparatorMenuItem separator = new SeparatorMenuItem();
        MenuItem addAction = new MenuItem("Add Category");

        if (Objects.equals(category.getTitle(), "All")) {
            contextMenu.getItems().addAll(addAction);
        } else {
            contextMenu.getItems().addAll(editAction, deleteAction, separator, addAction);
        }

        editAction.setOnAction(e -> {
            try {
                createEditCategoryModal(category);
            } catch (IOException error) {
                System.out.println(error.getMessage());
            }
        });
        addAction.setOnAction(e -> {
            try {
                createAddCategoryModal();
            } catch (IOException error) {
                System.out.println(error.getMessage());
            }
        });
        deleteAction.setOnAction(e -> {
            if (movieService.createDeleteConfirmation(category.getTitle())) {
                if (currentCategory == category.getId()) {
                    currentCategory = -1;
                    currentCategoryItem = null;
                }
                categoryService.deleteCategory(category.getId());
                updateCategories();
            }
        });

        item.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (selectedCategory != null && selectedCategory.equals(item)) {
                    selectedCategory.deselect();
                    selectedCategory = null;
                    currentCategoryItem = null;
                    currentCategory = -1;

                } else {
                    if (selectedCategory != null) {
                        selectedCategory.deselect();
                    }

                    selectedCategory = item;
                    selectedCategory.select();
                    currentCategoryItem = item.getCategory();
                    currentCategory = currentCategoryItem.getId();
                }

                showCategory(currentCategory);

                editCategoryButton.setDisable(currentCategoryItem == null || Objects.equals(currentCategoryItem.getTitle(), "All") || currentCategory < 0);
            } else if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(item, event.getScreenX(), event.getScreenY());
            }
        });

        return item;
    }

    public void updateCategories() {
        List<Category> categories = categoryService.getAllCategories();
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

        respondToWidthAndHeightChanges();
    }

    public void showCategory(long categoryID)  {
        long usedLong = (categoryID == -99) ? currentCategory : categoryID;

        try {
            updateMovieList(usedLong);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createAddModal() throws IOException {
        createModal("MovieModal.fxml", "Add movie", "movie", null);
    }

    public void editMovie() {
        try {
            if (selectedItem != null && selectedItem.getMovie() != null) {
                createModal("MovieModal.fxml", "Edit movie", "movie", selectedItem.getMovie());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createEditModal(Movie movieToEdit) throws IOException {
        createModal("MovieModal.fxml", "Edit movie", "movie", movieToEdit);
    }

    public void createAddCategoryModal() throws IOException {
        createModal("CategoryModal.fxml", "Add Category", "category", null);
    }

    public void editCategory() {
        try {
            if (currentCategoryItem != null) {
                createModal("CategoryModal.fxml", "Edit Category", "category", currentCategoryItem);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void createEditCategoryModal(Category categoryToEdit) throws IOException {
        createModal("CategoryModal.fxml", "Edit Category", "category", categoryToEdit);
    }

    private void createModal(String fxml, String title, String type, Object value) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Parent root = loader.load();

        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        modalStage.setTitle(title);

        switch (type) {
            case "movie":
                MovieModalController movieModalController = loader.getController();
                movieModalController.setModalStage(modalStage);
                movieModalController.setListController(this);
                movieModalController.init();

                movieModalController.setSelectedMovie((Movie)value);
                movieModalController.initEdit();
                movieModalController.setupCategories();
                break;
            case "category":
                CategoryModalController controller = loader.getController();
                controller.setListController(this);
                controller.setModalStage(modalStage);
                controller.setSelectedCategory((Category)value);
                controller.initEdit();
                break;
            default:
                break;
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add("gui/main.css");

        modalStage.setScene(scene);
        modalStage.showAndWait();
    }
}