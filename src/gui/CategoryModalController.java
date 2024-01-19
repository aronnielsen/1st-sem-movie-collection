package gui;

import be.Category;
import bl.CategoryService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CategoryModalController {
    @FXML
    public Label fromHeader;
    @FXML
    public TextField categoryTitle;
    @FXML
    public Button submitButton;

    private final CategoryService categoryService = new CategoryService();
    private Stage modalStage;
    private Category selectedCategory;
    private MovieListController listController;

    public void setModalStage(Stage modalStage) {
        this.modalStage = modalStage;
    }

    @FXML
    public void submitForm() {
        String categoryTitleValue = categoryTitle.getText();

        if (selectedCategory == null) {
            // Adding
            categoryService.addCategory(categoryTitleValue);

        } else {
            // Editing
            categoryService.editCategory(selectedCategory.getId(), categoryTitleValue);
        }
        listController.updateCategories();
        modalStage.close();
    }

    public void setSelectedCategory(Category selectedCategory) {
        if (selectedCategory != null) {
            this.selectedCategory = selectedCategory;
        }
    }

    public void initEdit() {
        if (this.selectedCategory != null) {
            fromHeader.setText("Edit: " + selectedCategory.getTitle());
            categoryTitle.setText(selectedCategory.getTitle());
            submitButton.setText("Edit category");
        }
    }

    public void setListController(MovieListController movieListController) {
        this.listController = movieListController;
    }
}