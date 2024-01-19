package bl;

import be.Movie;
import be.MovieCategoryConnection;
import be.ThumbnailGenerator;
import da.DAHelper;
import da.MovieDA;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class MovieService {
    private final CategoryService categoryService = new CategoryService();

    private final String userHome = System.getProperty("user.home");
    private final File destinationFolder = new File(userHome, "Pictures/MovieCollection_AN_Posters");

    public void editMovieWithCategories(long id, String title, File image, long[] categories, double rating, String filelink) {
        String path = null;

        if (image != null && image.exists()) {
            path = saveImage(image, title.replace(" ", "_"));
        }

        MovieDA.removeCategoriesFromMovie(id);
        MovieDA.editMovieWithCategories(id, title, path, categories, DAHelper.roundToDecimalPlaces(rating, 1), filelink);
    }

    public List<Movie> getAllMovies() {
        return MovieDA.getAllMovies();
    }

    public List<Movie> getAllMoviesInCategory(long categoryID) {
        return MovieDA.getAllMoviesInCategory(categoryID);
    }

    public List<MovieCategoryConnection> getCategoriesForMovie(long movieID) {
        return MovieDA.getCategoriesForMovie(movieID);
    }

    public void addMovieWithCategories(String title, File image, long[] categories, double rating, String filelink) {
        String path = null;

        if (image != null && image.exists()) {
            path = saveImage(image, title.replace(" ", "_"));
        }

        MovieDA.addMovieWithCategories(title, path, categories, DAHelper.roundToDecimalPlaces(rating, 1), filelink);
    }

    public void addMovie(String title, File image, double rating, String filelink) {
        String path = null;

        if (image != null && image.exists()) {
            path = saveImage(image, title.replace(" ", "_"));
        }

        MovieDA.addMovie(title, path, DAHelper.roundToDecimalPlaces(rating, 1), filelink);
    }

    public static String getFileExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");

        return (lastIndexOfDot > 0) ? fileName.substring(lastIndexOfDot + 1).toLowerCase() : "";
    }

    public boolean beenTwoYearsSinceOpened(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(date, formatter);

        LocalDate twoYearsAgo = LocalDate.now().minusYears(2);

        return dateTime.toLocalDate().isBefore(twoYearsAgo);
    }

    private String saveImage(File sourceFile, String imageName) {
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        String extension = getFileExtension(sourceFile.getName());

        File destFile = new File(destinationFolder, imageName + "." + extension);
        try {
            Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return destFile.getPath();
        } catch (IOException ex) {
            System.err.println("Error saving the image: " + ex.getMessage());
            return "";
        }
    }

    public void syncFolder(File[] files, boolean createCategories, boolean root, long catID) {
        assert files != null;
        for (File file : files) {
            if (file.isDirectory() && !file.isHidden()) {
                File[] filesInFolder = (file.listFiles() != null) ? file.listFiles() : null;

                assert filesInFolder != null;
                if (root && createCategories) {
                    long categoryID = categoryService.addCategory(file.getName());
                    syncFolder(filesInFolder, false, false, categoryID);
                } else {
                    syncFolder(filesInFolder, false, false, -1);
                }
            } else if (!file.isHidden() && getFileExtension(file.getName()).equals("mp4")) {
                System.out.println(destinationFolder + "/" + file.getName(). replace(".mp4", ".jpg").replace(" ", "_"));
                File imageCheck = new File(destinationFolder + "/" + file.getName(). replace(".mp4", ".jpg").replace(" ", "_"));

                if (catID > -1) {
                    addMovieWithCategories(file.getName().replace(".mp4", ""), imageCheck, new long[]{catID}, 0.0, file.getPath());
                } else {
                    addMovie(file.getName().replace(".mp4", ""), imageCheck, 0.0, file.getPath());
                }
            }
        }
    }

    public void generateThumbnail(Movie movie) {
        String videoFilePath = movie.getFilelink();
        String thumbnailPath = destinationFolder.getPath() + "/" + movie.getTitle().replace(" ", "_") + ".jpg";
        String time = "00:02:00";

        ThumbnailGenerator creator = new ThumbnailGenerator();
        creator.createThumbnail(videoFilePath, thumbnailPath, time);

        MovieDA.updateImageOnMovie(movie.getId(), thumbnailPath);
    }

    public void setMovieOpened(long movieID) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String formattedDate = now.format(dtf);

        MovieDA.movieOpened(movieID, formattedDate);
    }

    public void deleteMovie(int id) {
        MovieDA.deleteMovie(id);
    }

    public void openImageFolder() {
        if (destinationFolder.exists() && destinationFolder.isDirectory()) {
            Desktop desktop = Desktop.getDesktop();

            try {
                desktop.open(destinationFolder);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void openImage(String image) {
        if (destinationFolder.exists() && destinationFolder.isDirectory()) {
            Desktop desktop = Desktop.getDesktop();

            try {
                desktop.open(new File(image));
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public boolean createDeleteConfirmation(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Do you really want to delete this item?");
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();

        return (result.isPresent() && result.get() == ButtonType.OK);
    }
}