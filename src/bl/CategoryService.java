package bl;

import be.Category;
import da.CategoryDA;
import java.util.List;

public class CategoryService {
    public long addCategory(String title) {
        return CategoryDA.addCategory(title);
    }

    public void editCategory(long id, String title) {
        CategoryDA.editCategory(id, title);
    }

    public List<Category> getAllCategories() {
        return CategoryDA.getAllCategories();
    }

    public void deleteCategory(long categoryID) {
        CategoryDA.deleteCategory(categoryID);
    }
}
