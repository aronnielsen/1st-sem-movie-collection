package da;

import java.sql.*;

public class DBOperations {
    public static void setupDatabaseTables() {
        try (Connection conn = DBConnection.connect();
            Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS movie (\n"
                    + " id integer PRIMARY KEY,\n"
                    + " title text NOT NULL,\n"
                    + " rating double NOT NULL,\n"
                    + " filelink text NOT NULL,\n"
                    + " lastopen text NULL,\n"
                    + " image text \n"
                    + ");");
            stmt.execute("CREATE TABLE IF NOT EXISTS category (\n"
                    + " id integer PRIMARY KEY,\n"
                    + " title text NOT NULL\n"
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
}
