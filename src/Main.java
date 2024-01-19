import da.DBOperations;
import gui.MovieListController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/MovieList.fxml"));
        Parent root = loader.load();

        primaryStage.initStyle(StageStyle.DECORATED);
        primaryStage.setTitle("Movie Collection");
        Scene scene = new Scene(root, 1280, 720);
        scene.getStylesheets().add("gui/main.css");
        primaryStage.setScene(scene);
        primaryStage.show();

        MovieListController controller = loader.getController();
        controller.setStage(primaryStage);
        controller.setupListeners();

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        double centerXPosition = screenBounds.getMinX() + screenBounds.getWidth() / 2 - primaryStage.getWidth() / 2;
        double centerYPosition = screenBounds.getMinY() + screenBounds.getHeight() / 2 - primaryStage.getHeight() / 2;

        primaryStage.setX(centerXPosition);
        primaryStage.setY(centerYPosition);

        DBOperations.setupDatabaseTables();
    }

    public static void main(String[] args) {
        launch(args);
    }
}