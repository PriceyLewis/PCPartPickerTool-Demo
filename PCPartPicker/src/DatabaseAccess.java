import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class DatabaseAccess {
    private final String url;

    public DatabaseAccess() {
        File dbFile = findDatabaseFile("Database for App.accdb");
        if (!dbFile.exists()) {
            throw new IllegalStateException("Database file not found: " + dbFile.getAbsolutePath());
        }
        this.url = "jdbc:ucanaccess://" + dbFile.getAbsolutePath();
    }

    public String getURL() {
        return url;
    }

    private File findDatabaseFile(String name) {
        File root = new File(System.getProperty("user.dir"));
        File result = searchRecursively(root, name);
        return result != null ? result : new File(name);
    }

    private File searchRecursively(File dir, String targetName) {
        File[] files = dir.listFiles();
        if (files == null) {
            return null;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                File result = searchRecursively(file, targetName);
                if (result != null) {
                    return result;
                }
            } else if (file.getName().equalsIgnoreCase(targetName)) {
                return file;
            }
        }

        return null;
    }

    public ResultSet executeCustomQuery(String sql) {
        return executeQuery(sql);
    }

    public CachedRowSet executeQuery(String sql, Object... parameters) {
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = prepareStatement(conn, sql, parameters);
             ResultSet rs = stmt.executeQuery()) {
            CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet();
            rowSet.populate(rs);
            return rowSet;
        } catch (SQLException e) {
            throw new RuntimeException("Database query failed: " + e.getMessage(), e);
        }
    }

    public int executeUpdate(String sql, Object... parameters) {
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = prepareStatement(conn, sql, parameters)) {
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Database update failed: " + e.getMessage(), e);
        }
    }

    private PreparedStatement prepareStatement(Connection conn, String sql, Object... parameters) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(sql);
        for (int i = 0; i < parameters.length; i++) {
            stmt.setObject(i + 1, parameters[i]);
        }
        return stmt;
    }

    public static String retrieve_Recomendations_Reviews(String sql, String url) {
        StringBuilder result = new StringBuilder();
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int columnCount = rs.getMetaData().getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                result.append(rs.getMetaData().getColumnName(i)).append('\t');
            }
            result.append('\n');

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    result.append(rs.getString(i)).append('\t');
                }
                result.append('\n');
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve recommendations or reviews.", e);
        }
        return result.toString();
    }

    public static Map<String, Object> add_User(String username, String preferences, String location, String url) {
        String sql = "INSERT INTO Users (Username, Location, Preferences) VALUES (?, ?, ?)";
        Map<String, Object> userDetails = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, username);
            stmt.setString(2, location);
            stmt.setString(3, preferences);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    userDetails.put("UserID", generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not create user.", e);
        }

        userDetails.put("Username", username);
        userDetails.put("Location", location);
        userDetails.put("Preferences", preferences);
        return userDetails;
    }

    public static Map<String, Object> retrieve_User(String userId, String url) {
        String sql = "SELECT UserID, Username, Location, Preferences FROM Users WHERE UserID = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                Map<String, Object> userDetails = new HashMap<>();
                userDetails.put("UserID", rs.getInt("UserID"));
                userDetails.put("Username", rs.getString("Username"));
                userDetails.put("Location", rs.getString("Location"));
                userDetails.put("Preferences", rs.getString("Preferences"));
                return userDetails;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve user.", e);
        }
    }

    public void add_Log(String pcPart, Map<String, Object> userDetails, String ignoredUrl) {
        String sql = "INSERT INTO UserSearches (UserID, SearchedPart, SearchDate, UserLocation) VALUES (?, ?, ?, ?)";
        executeUpdate(
            sql,
            userDetails.get("UserID"),
            pcPart,
            Date.valueOf(LocalDate.now()),
            String.valueOf(userDetails.getOrDefault("Location", ""))
        );
    }

    public String retrieve_StoreID(String storeName) {
        CachedRowSet rs = executeQuery("SELECT TOP 1 StoreID FROM Stores WHERE StoreName = ? OR StoreName LIKE ?", storeName, "%" + storeName + "%");
        try {
            if (rs.next()) {
                return String.valueOf(rs.getInt("StoreID"));
            }
            return "";
        } catch (SQLException e) {
            throw new RuntimeException("Could not retrieve store ID.", e);
        }
    }

    public void insert_Review(String storeId, String userID, String review, int rating) {
        executeUpdate(
            "INSERT INTO StoreReviews (StoreID, UserID, [Rating(1-5)], Comment, ReviewDate) VALUES (?, ?, ?, ?, ?)",
            storeId,
            userID,
            rating,
            review,
            Date.valueOf(LocalDate.now())
        );
    }

    public static void update_Stock(String partName, String url, String storeID) {
        String selectSql = "SELECT TOP 1 PartID FROM Parts WHERE PartName = ?";
        String updateSql = "UPDATE Inventory SET StockLevel = StockLevel - 1, LastUpdated = ? " +
            "WHERE PartID = ? AND StoreID = ? AND StockLevel > 0";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
            selectStmt.setString(1, partName);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (!rs.next()) {
                    return;
                }

                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setDate(1, Date.valueOf(LocalDate.now()));
                    updateStmt.setInt(2, rs.getInt("PartID"));
                    updateStmt.setString(3, storeID);
                    updateStmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not update stock.", e);
        }
    }
}
