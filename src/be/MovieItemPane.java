package be;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MovieItemPane extends Region {
    private static final double RATIO = 16.0 / 9.0;
    private ImageView imageView;
    private Label label;

    private VBox vertical;

    private Movie movie;

    public MovieItemPane(boolean folder) {
        super();

        vertical = new VBox();
        vertical.setSpacing(10.0);
        vertical.setPadding(new Insets(10.0, 10.0, 10.0, 10.0));

        HBox horizontal = new HBox();
        horizontal.setAlignment(Pos.CENTER);


        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        horizontal.getChildren().add(imageView);

        vertical.getChildren().add(horizontal);

        label = new Label();
        label.setStyle("-fx-text-fill: #dadada;");
        label.setWrapText(true);
        label.setMinWidth(150);
        label.setMaxWidth(150);
        vertical.getChildren().add(label);

        // Set preferred width and height based on aspect ratio
        setPrefWidth(150); // Arbitrary value
        setPrefHeight(75);
        //setPrefHeight(270 / RATIO);

        //getStyleClass().add("folder");

        getChildren().add(vertical);
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public void setImage(Image image) {
        imageView.setImage(image);
        imageView.fitWidthProperty().bind(this.widthProperty());
        imageView.fitHeightProperty().bind(this.heightProperty());
    }

    public void setLabel(String text) {
        label.setText(text);
    }

    public boolean checkSelection() {
        return vertical.getStyleClass().contains("selected");
    }

    public void toggleSelection() {
        if (vertical.getStyleClass().contains("selected")) {
            vertical.getStyleClass().remove("selected");
        } else {
            vertical.getStyleClass().add("selected");
        }
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = width / RATIO;
        setPrefHeight(height);

        super.layoutChildren();
    }
}
