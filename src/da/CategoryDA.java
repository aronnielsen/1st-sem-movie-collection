package da;

import be.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDA {
    public static List<Category> getAllCategories() {
        String sql = "SELECT * FROM category ORDER BY title";
        List<Category> categories = new ArrayList<>();

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Category category = new Category();
                    category.setId(rs.getInt("id"));
                    category.setTitle(rs.getString("title"));
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return categories;
    }

    public static long addCategory(String name) {
        String sql = "INSERT INTO category (title) VALUES (?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             Statement stmt = conn.createStatement()) {

            pstmt.setString(1, name);

            pstmt.executeUpdate();

            return DAHelper.getLastID(stmt);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public static void editCategory(long id, String title) {
        String sql = "UPDATE category SET title=? WHERE id=?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setLong(2, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void removeMoviesFromCategory(long categoryID) {
        String sql = "DELETE FROM movie_category_connection WHERE category=?";
        DAHelper.deleteHelper(categoryID, sql);
    }

    public static void deleteCategory(long categoryID) {
        removeMoviesFromCategory(categoryID);
        String sql = "DELETE FROM category WHERE id=?";
        DAHelper.deleteHelper(categoryID, sql);
    }
}
