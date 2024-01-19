package da;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public static Connection connect() {
        Connection conn = null;
        try {
            String userHome = System.getProperty("user.home");
            File destinationFolder = new File(userHome, "Pictures/MovieCollection_AN_Posters/Database");

            if (!destinationFolder.exists()) {
                destinationFolder.mkdirs();
            }

            String url = "jdbc:sqlite:" + destinationFolder + "/movie_collection.db";
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}
