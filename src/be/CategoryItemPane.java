package be;

import gui.MovieListController;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class CategoryItemPane extends Region {
    private Label label;
    private long categoryID = 0;

    public CategoryItemPane(MovieListController refController) {
        super();
        getStyleClass().add("category");
        label = new Label();
        label.setStyle("-fx-text-fill: #dadada;");
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setMinWidth(150);
        label.setMaxWidth(150);
        getChildren().add(label);

        /*this.setOnMouseClicked(event -> {
            refController.showCategory(this.categoryID);
        });*/
    }

    public void toggleSelection() {
        if (getStyleClass().contains("selected")) {
            getStyleClass().remove("selected");
        } else {
            getStyleClass().add("selected");
        }
    }


    public void setLabel(String text) {
        label.setText(text);
    }
}
