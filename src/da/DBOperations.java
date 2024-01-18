package da;

import be.Category;
import be.Movie;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBOperations {
    public static void setupDatabaseTables() {
        try (Connection conn = DBConnection.connect();
            Statement stmt = conn.createStatement()) {

            stmt.execute("DROP TABLE IF EXISTS movie_category_connection;");
            stmt.execute("DROP TABLE IF EXISTS movie;");
            stmt.execute("DROP TABLE IF EXISTS category;");

            stmt.execute("CREATE TABLE IF NOT EXISTS movie (\n"
                    + " id integer PRIMARY KEY,\n"
                    + " title text NOT NULL,\n"
                    + " image text NOT NULL\n"
                    + ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS category (\n"
                    + " id integer PRIMARY KEY,\n"
                    + " title text NOT NULL,\n"
                    + " image text NOT NULL\n"
                    + ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS movie_category_connection (\n"
                    + " id integer PRIMARY KEY,\n"
                    + " movie integer NOT NULL,\n"
                    + " category integer NOT NULL,\n"
                    + " FOREIGN KEY (movie) REFERENCES movie(id),\n"
                    + " FOREIGN KEY (category) REFERENCES category(id)\n"
                    + ");");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void addMovieWithCategories(String name, String imagePath, long[] categories) {
        long lastID = addMovie(name, imagePath);

        for (long category : categories) {
            if (!checkIfMovieHasCategory(lastID, category)) {
                addCategoryToMovie(lastID, category);
            }
        }
    }

    public static boolean checkIfMovieHasCategory(long movieID, long categoryID) {
        String sql = "SELECT * FROM movie_category_connection WHERE movie=? AND category=?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, movieID);
            pstmt.setLong(2, categoryID);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return true;
        }
    }

    public static void addCategoryToMovie(long movieID, long categoryID) {
        String sql = "INSERT INTO movie_category_connection(movie, category) VALUES(?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, movieID);
            pstmt.setLong(2, categoryID);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static long addCategory(String name, String imagePath) {
        String sql = "INSERT INTO category(title, image) VALUES(?,?)";

        return addItemWithTwoStrings(name, imagePath, sql);
    }

    private static long addItemWithTwoStrings(String name, String imagePath, String sql) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             Statement stmt = conn.createStatement()) {

            pstmt.setString(1, name);
            pstmt.setString(2, imagePath);

            pstmt.executeUpdate();

            return getLastID(stmt);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public static long addMovie(String name, String imagePath) {
        String sql = "INSERT INTO movie(title, image) VALUES(?,?)";

        return addItemWithTwoStrings(name, imagePath, sql);
    }

    private static long getLastID(Statement stmt) throws java.sql.SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("Unable to retrieve last inserted ID.");
            }
        }
    }

    public static List<Movie> getAllMovies() {
        String sql = "SELECT * FROM movie";
        List<Movie> movies = new ArrayList<>();

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Movie movie = new Movie();
                    movie.setId(rs.getInt("id"));
                    movie.setTitle(rs.getString("title"));
                    movie.setImage(rs.getString("image"));
                    movies.add(movie);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return movies;
    }

    public static List<Category> getAllCategories() {
        String sql = "SELECT * FROM category";
        List<Category> categories = new ArrayList<>();

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Category category = new Category();
                    category.setId(rs.getInt("id"));
                    category.setTitle(rs.getString("title"));
                    category.setImage(rs.getString("image"));
                    categories.add(category);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return categories;
    }

    public static List<Movie> getAllMoviesInCategory(long categoryID) {
        String sql = "SELECT * FROM movie_category_connection LEFT JOIN movie ON movie_category_connection.movie = movie.id WHERE category = ?";
        List<Movie> movies = new ArrayList<>();

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, categoryID);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Movie movie = new Movie();
                    movie.setId(rs.getInt("id"));
                    movie.setTitle(rs.getString("title"));
                    movie.setImage(rs.getString("image"));
                    movies.add(movie);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return movies;
    }

    public static void editMovie(long id, String title, String path) {
        String sql = "UPDATE movie SET title=?, image=? WHERE id=?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             Statement stmt = conn.createStatement()) {

            pstmt.setString(1, title);
            pstmt.setString(2, path);
            pstmt.setLong(3, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
