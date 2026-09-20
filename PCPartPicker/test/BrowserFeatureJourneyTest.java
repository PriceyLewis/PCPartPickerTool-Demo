import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.sql.rowset.CachedRowSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BrowserFeatureJourneyTest {
    @BeforeAll
    static void enableBrowserMode() {
        System.setProperty("portfolio.browser", "true");
    }

    @Test
    void accountHistoryReviewInventoryAndAssistantJourneysWorkTogether() throws Exception {
        DatabaseAccess database = new DatabaseAccess();

        Map<String, Object> user = DatabaseAccess.add_User(
            "Journey Tester", "quiet NVIDIA build", "Warrington", database.getURL()
        );
        assertNotNull(user.get("UserID"));
        assertEquals("Journey Tester", DatabaseAccess.retrieve_User(
            String.valueOf(user.get("UserID")), database.getURL()
        ).get("Username"));

        CachedRowSet inventory = database.executeQuery(
            "SELECT Parts.PartName, Stores.StoreName, Inventory.Price, Parts.Brand " +
                "FROM (Parts INNER JOIN Inventory ON Parts.PartID = Inventory.PartID) " +
                "INNER JOIN Stores ON Inventory.StoreID = Stores.StoreID " +
                "WHERE Inventory.StockLevel > 0 AND Parts.PartName LIKE ? " +
                "AND Parts.Brand = ? AND Inventory.Price <= ? ORDER BY Inventory.Price ASC",
            "%RTX%", "NVIDIA", 700.0
        );
        assertTrue(inventory.next(), "The advanced-search journey should return an in-stock GPU");
        String selectedPart = inventory.getString("PartName");

        database.add_Log(selectedPart, user, database.getURL());
        CachedRowSet history = database.executeQuery(
            "SELECT TOP 25 SearchedPart, SearchDate, UserLocation FROM UserSearches " +
                "WHERE UserID = ? ORDER BY SearchDate DESC",
            user.get("UserID")
        );
        assertTrue(history.next());
        assertEquals(selectedPart, history.getString("SearchedPart"));

        String storeId = database.retrieve_StoreID("Scan Computers");
        assertFalse(storeId.isBlank());
        database.insert_Review(storeId, String.valueOf(user.get("UserID")), "Clear journey test review", 5);
        CachedRowSet reviews = database.executeQuery(
            "SELECT TOP 10 [Rating(1-5)] AS Rating, Comment, ReviewDate FROM StoreReviews " +
                "WHERE StoreID = ? ORDER BY ReviewDate DESC", storeId
        );
        boolean reviewFound = false;
        while (reviews.next()) {
            reviewFound |= "Clear journey test review".equals(reviews.getString("Comment"));
        }
        assertTrue(reviewFound, "A submitted review should immediately be readable");

        PCPartAI.AssistantReply recommendation = new PCPartAI(database).respond(
            "recommend an NVIDIA part under £700", user
        );
        assertTrue(recommendation.hasRecommendation());
        assertTrue(recommendation.getPrice() <= 700.0);
    }

    @Test
    void adminCanAddAndRemovePartsInDisposableBrowserData() throws Exception {
        DatabaseAccess database = new DatabaseAccess();
        assertEquals(1, database.executeUpdate(
            "INSERT INTO Parts (PartName, Brand) VALUES (?, ?)", "Portfolio Test Part", "Demo"
        ));

        CachedRowSet parts = database.executeQuery("SELECT PartID, PartName, Brand FROM Parts ORDER BY PartName");
        Integer partId = null;
        while (parts.next()) {
            if ("Portfolio Test Part".equals(parts.getString("PartName"))) {
                partId = parts.getInt("PartID");
            }
        }
        assertNotNull(partId);
        assertEquals(1, database.executeUpdate("DELETE FROM Parts WHERE PartID = ?", partId));
    }
}
