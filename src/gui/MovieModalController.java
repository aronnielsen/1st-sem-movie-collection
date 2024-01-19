package gui;

import be.Category;
import be.Movie;
import be.MovieCategoryConnection;
import bl.CategoryService;
import bl.MovieService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MovieModalController {
    @FXML
    public Label fromHeader;
    @FXML
    public TextField movieTitle;
    @FXML
    public TextField imagePath;
    public TextField moviePath;
    @FXML
    public Button imagePathButton;
    @FXML
    public ListView<Category> categoryList;
    @FXML
    public Button submitButton;
    @FXML
    public Slider movieRating;
    @FXML
    public Label ratingLabel;

    private Movie selectedMovie;
    private final MovieService movieService = new MovieService();
    private final CategoryService categoryService = new CategoryService();
    private Stage modalStage;
    private MovieListController listController;

    private File selectedImage;
    private File selectedMovieFile;

    public void setModalStage(Stage modalStage) {
        this.modalStage = modalStage;
    }
    public void setListController(MovieListController listController) {
        this.listController = listController;
    }

    public void setupCategories() {
        categoryList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        List<Category> categories = categoryService.getAllCategories();
        categoryList.getItems().addAll(categories);

        if (selectedMovie != null) {
            List<MovieCategoryConnection> mcc = movieService.getCategoriesForMovie(selectedMovie.getId());
            List<String> categoriesIds = new ArrayList<>();

            for (MovieCategoryConnection item : mcc) {
                categoriesIds.add(Long.toString(item.getCategoryID()));
            }

            int cnt = 0;
            for (Category cat : categoryList.getItems()) {
                if (categoriesIds.contains(Long.toString(cat.getId()))) {
                    categoryList.getSelectionModel().select(cnt);
                }

                cnt++;
            }
        }

        categoryList.setCellFactory(lv -> new TextFieldListCell<>(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category.getTitle();
            }

            @Override
            public Category fromString(String string) {
                return null;
            }
        }));
    }

    @FXML
    public void submitForm() {
        String movieTitleValue = movieTitle.getText();
        File movieImage = null;
        File movieFile = null;
        long[] movieCategories = new long[categoryList.getSelectionModel().getSelectedItems().size()];

        int cnt = 0;
        for (Category item : categoryList.getSelectionModel().getSelectedItems()) {
            movieCategories[cnt] = item.getId();

            cnt++;
        }

        if (selectedImage != null) {
            movieImage = selectedImage;
        } else if (imagePath != null && imagePath.getText() != null && !imagePath.getText().isEmpty()) {
            File fileCheck = new File(imagePath.getText());

            if (fileCheck.exists()) {
                movieImage = fileCheck;
            }
        }

        if (selectedMovieFile != null) {
            movieFile = selectedMovieFile;
        } else if (!moviePath.getText().isEmpty()) {
            File fileCheck = new File(moviePath.getText());

            if (fileCheck.exists()) {
                movieFile = fileCheck;
            }
        }

        if (!movieTitleValue.isEmpty() && selectedMovie == null && movieFile != null) {
            // Adding a movie
            movieService.addMovieWithCategories(movieTitleValue, movieImage, movieCategories, movieRating.getValue(), movieFile.getPath());
            listController.showCategory(-99);
            modalStage.close();

        } else if (!movieTitleValue.isEmpty() && selectedMovie != null && movieFile != null) {
            // Editing a movie
            movieService.editMovieWithCategories(selectedMovie.getId(), movieTitleValue, movieImage, movieCategories, movieRating.getValue(), movieFile.getPath());
            listController.showCategory(-99);
            modalStage.close();
        }
    }

    public void setSelectedMovie(Movie selectedMovie) {
        if (selectedMovie != null) {
            this.selectedMovie = selectedMovie;
        }
    }

    public void openImagePicker() {
        FileChooser fileChooser = new FileChooser();

        ExtensionFilter extFilter = new ExtensionFilter("Image files (*.png, *.jpg, *.gif)", "*.png", "*.jpg", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);

        selectedImage = fileChooser.showOpenDialog(modalStage);

        if (selectedImage != null) {
            imagePath.setText(selectedImage.getPath());
        }
    }

    public void openMoviePicker() {
        FileChooser fileChooser = new FileChooser();

        ExtensionFilter extFilter = new ExtensionFilter("MP4 files (*.mp4)", "*.mp4");
        fileChooser.getExtensionFilters().add(extFilter);

        selectedMovieFile = fileChooser.showOpenDialog(modalStage);

        if (selectedMovieFile != null) {
            moviePath.setText(selectedMovieFile.getPath());
        }
    }

    public void init() {
        movieRating.valueProperty().addListener((obs, oldVal, newVal) -> ratingLabel.setText(String.format("%.1f", movieRating.getValue())));
    }

    public void initEdit() {
        if (this.selectedMovie != null) {
            fromHeader.setText("Edit: " + selectedMovie.getTitle());
            movieTitle.setText(selectedMovie.getTitle());
            imagePath.setText(selectedMovie.getImage());
            moviePath.setText(selectedMovie.getFilelink());
            movieRating.setValue(selectedMovie.getRating());
            ratingLabel.setText(Double.toString(selectedMovie.getRating()));
            submitButton.setText("Edit movie");
        }
    }
}