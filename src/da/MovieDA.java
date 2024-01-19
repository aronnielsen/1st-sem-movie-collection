package da;

import be.Movie;
import be.MovieCategoryConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MovieDA {
    private static Movie createMovieItemHelper(ResultSet rs) throws SQLException {
        Movie movie = new Movie();
        movie.setId(rs.getInt("id"));
        movie.setTitle(rs.getString("title"));
        movie.setImage(rs.getString("image"));
        movie.setRating(rs.getDouble("rating"));
        movie.setFilelink(rs.getString("filelink"));
        movie.setLastOpened(rs.getString("lastopen"));
        String categories = rs.getString("categories");

        movie.setCategories(Objects.requireNonNullElse(categories, ""));

        return movie;
    }

    public static List<Movie> getAllMovies() {
        String sql =
        "SELECT m.id, m.title, m.rating, m.filelink, m.image, m.lastopen, GROUP_CONCAT(c.title) AS categories FROM movie m LEFT JOIN movie_category_connection mcc ON m.id = mcc.movie LEFT JOIN category c ON mcc.category = c.id GROUP BY m.id";

        List<Movie> movies = new ArrayList<>();

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movies.add(createMovieItemHelper(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return movies;
    }

    public static List<Movie> getAllMoviesInCategory(long categoryID) {
        String sql =
        "SELECT m.id, m.title, m.rating, m.filelink, m.image, m.lastopen, GROUP_CONCAT(c.title) AS categories FROM movie m LEFT JOIN movie_category_connection mcc ON m.id = mcc.movie LEFT JOIN category c ON mcc.category = c.id WHERE mcc.category = ? GROUP BY m.id";
        List<Movie> movies = new ArrayList<>();

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, categoryID);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    movies.add(createMovieItemHelper(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return movies;
    }

    public static List<MovieCategoryConnection> getCategoriesForMovie(long movieID) {
        String sql = "SELECT * FROM movie_category_connection WHERE movie=?";

        List<MovieCategoryConnection> categories = new ArrayList<>();

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, movieID);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MovieCategoryConnection mcc = new MovieCategoryConnection();
                    mcc.setId(rs.getInt("id"));
                    mcc.setMovieID(rs.getLong("movie"));
                    mcc.setCategoryID(rs.getLong("category"));
                    categories.add(mcc);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return categories;
    }

    public static void addMovieWithCategories(String name, String imagePath, long[] categories, double rating, String filelink) {
        long lastID = addMovie(name, imagePath, rating, filelink);

        for (long category : categories) {
            if (!checkIfMovieHasCategory(lastID, category)) {
                addCategoryToMovie(lastID, category);
            }
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

    public static long addMovie(String name, String imagePath, double rating, String filelink) {
        String sql = "INSERT INTO movie(title, image, rating, filelink) VALUES(?,?,?,?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             Statement stmt = conn.createStatement()) {

            pstmt.setString(1, name);
            pstmt.setString(2, imagePath);
            pstmt.setDouble(3, rating);
            pstmt.setString(4, filelink);

            pstmt.executeUpdate();

            return DAHelper.getLastID(stmt);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public static void editMovie(long id, String title, String path, double rating, String filelink) {
        String sql = "UPDATE movie SET title=?, image=?, rating=?, filelink=? WHERE id=?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, title);
            pstmt.setString(2, path);
            pstmt.setDouble(3, rating);
            pstmt.setString(4, filelink);
            pstmt.setLong(5, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
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

    public static void editMovieWithCategories(long id, String title, String path, long[] categories, double rating, String filelink) {
        editMovie(id, title, path, rating, filelink);

        for (long category : categories) {
            addCategoryToMovie(id, category);
        }
    }

    public static void removeCategoriesFromMovie(long id) {
        String sql = "DELETE FROM movie_category_connection WHERE movie=?";

        DAHelper.deleteHelper(id, sql);
    }

    public static void deleteMovie(long movieID) {
        removeCategoriesFromMovie(movieID);
        String sql = "DELETE FROM movie WHERE id=?";
        DAHelper.deleteHelper(movieID, sql);
    }

    public static void movieOpened(long movieID, String date) {
        String sql = "UPDATE movie SET lastopen=? WHERE id=?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, date);
            pstmt.setLong(2, movieID);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void updateImageOnMovie(int id, String thumbnailPath) {
        String sql = "UPDATE movie SET image=? WHERE id=?";

        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, thumbnailPath);
            pstmt.setLong(2, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
