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
    private static final double RATIO = 2.5 / 4.0;
    private final ImageView imageView;
    private final Label label;
    private final Label labelCat;
    private final Label labelRating;

    private final VBox vertical;

    private Movie movie;

    public MovieItemPane() {
        super();

        vertical = new VBox();
        vertical.setSpacing(10.0);
        vertical.setPadding(new Insets(10.0, 10.0, 10.0, 10.0));
        vertical.setAlignment(Pos.CENTER);

        HBox horizontal = new HBox();
        horizontal.setMinSize(150, 150);
        horizontal.setAlignment(Pos.CENTER);

        imageView = new ImageView();
        imageView.setPreserveRatio(true);

        horizontal.getChildren().add(imageView);
        vertical.getChildren().add(horizontal);

        label = labelHelper();
        vertical.getChildren().add(label);

        labelRating = labelHelper();
        vertical.getChildren().add(labelRating);

        labelCat = labelHelper();
        labelCat.setStyle("-fx-font-size: 10.0; -fx-text-fill: #dadada;");
        vertical.getChildren().add(labelCat);

        setPrefWidth(150);
        setPrefHeight(200);
        getChildren().add(vertical);
    }

    private Label labelHelper() {
        Label tempLbl = new Label();

        tempLbl.setStyle("-fx-text-fill: #dadada;");
        tempLbl.setWrapText(true);
        tempLbl.setMinWidth(150);
        tempLbl.setMaxWidth(150);
        return tempLbl;
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
        imageView.fitHeightProperty().bind(this.widthProperty());
    }

    public void setLabelRating(String text) {
        labelRating.setText(text);
    }

    public void setLabel(String text) {
        label.setText(text);
    }
    public void setLabelCat(String text) {
        labelCat.setText(text);
    }

    public boolean checkSelection() {
        return vertical.getStyleClass().contains("selected");
    }

    public void select() {
        vertical.getStyleClass().add("selected");
    }
    public void deselect() {
        vertical.getStyleClass().remove("selected");
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();

        double height = width / RATIO;
        setPrefHeight(height);

        super.layoutChildren();
    }
}
