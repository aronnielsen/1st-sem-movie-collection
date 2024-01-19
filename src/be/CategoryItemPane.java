package be;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class CategoryItemPane extends Region {
    private final Label label;
    private Category category;

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public CategoryItemPane() {
        super();

        VBox vertical = new VBox();
        vertical.setAlignment(Pos.CENTER);

        label = new Label();
        label.setStyle("-fx-text-fill: #dadada;");
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setMinWidth(100);
        label.setMaxWidth(150);
        label.getStyleClass().add("category");
        vertical.getChildren().add(label);

        getChildren().add(vertical);
    }

    public void select() {
        getStyleClass().add("selected");
    }
    public void deselect() {
        getStyleClass().remove("selected");
    }

    public void setLabel(String text) {
        label.setText(text);
    }
}
