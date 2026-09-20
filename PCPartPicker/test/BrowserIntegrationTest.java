import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrowserIntegrationTest {

    @BeforeAll
    static void enableBrowserMode() {
        System.setProperty("portfolio.browser", "true");
    }

    @Test
    void browserDatabaseAndRecommendationAssistantWorkTogether() {
        DatabaseAccess database = new DatabaseAccess();
        Map<String, Object> demoUser = DatabaseAccess.browserDemoUser();
        PCPartAI assistant = new PCPartAI(database);

        PCPartAI.AssistantReply reply = assistant.respond(
            "recommend an NVIDIA graphics card under £600",
            demoUser
        );

        assertNotNull(demoUser);
        assertTrue(reply.hasRecommendation());
        assertTrue(reply.getPrice() <= 600.0);
        assertFalse(reply.getMessage().isBlank());
    }

    @Test
    void browserStoreReviewsAreAvailable() throws Exception {
        DatabaseAccess database = new DatabaseAccess();
        String storeId = database.retrieve_StoreID("Scan Computers");

        assertFalse(storeId.isBlank());
        var reviews = database.executeQuery(
            "SELECT TOP 10 [Rating(1-5)] AS Rating, Comment, ReviewDate " +
                "FROM StoreReviews WHERE StoreID = ? ORDER BY ReviewDate DESC",
            storeId
        );

        assertTrue(reviews.next());
        assertTrue(reviews.getInt("Rating") >= 1);
        assertFalse(reviews.getString("Comment").isBlank());
    }
}
