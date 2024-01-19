package da;

import java.sql.*;

public class DAHelper {
    public static long getLastID(Statement stmt) throws java.sql.SQLException {
        try (ResultSet rs = stmt.executeQuery("SELECT last_insert_rowid()")) {
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("Unable to retrieve last inserted ID.");
            }
        }
    }

    public static double roundToDecimalPlaces(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }

    public static void deleteHelper(long categoryID, String sql) {
        try (Connection conn = DBConnection.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, categoryID);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.out.println("Deleting error");
        }
    }
}
