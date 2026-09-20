import org.junit.jupiter.api.Test;

import javax.sql.rowset.CachedRowSet;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrowserDemoDataTest {

    @Test
    void providesDisposableDemoUser() {
        Map<String, Object> user = BrowserDemoData.demoUser();

        assertNotNull(user);
        assertEquals(1, user.get("UserID"));
        assertEquals("Demo Builder", user.get("Username"));
    }

    @Test
    void exposesInStockInventoryForBrowserScreens() throws Exception {
        CachedRowSet rows = BrowserDemoData.query(
            "SELECT Parts.PartName, Stores.StoreName, Inventory.Price " +
                "FROM (Parts INNER JOIN Inventory ON Parts.PartID = Inventory.PartID) " +
                "INNER JOIN Stores ON Inventory.StoreID = Stores.StoreID " +
                "WHERE Inventory.StockLevel > 0 ORDER BY Inventory.Price ASC"
        );

        assertTrue(rows.next());
        assertNotNull(rows.getString("PartName"));
        assertNotNull(rows.getString("StoreName"));
        assertTrue(rows.getDouble("Price") > 0);
    }

    @Test
    void appliesBrowserSearchBrandAndBudgetFilters() throws Exception {
        CachedRowSet rows = BrowserDemoData.query(
            "SELECT Parts.PartName, Stores.StoreName, Inventory.Price, Parts.Brand " +
                "FROM (Parts INNER JOIN Inventory ON Parts.PartID = Inventory.PartID) " +
                "INNER JOIN Stores ON Inventory.StoreID = Stores.StoreID " +
                "WHERE Inventory.StockLevel > 0 AND Parts.Brand = ? AND Inventory.Price <= ? " +
                "ORDER BY Inventory.Price ASC",
            "NVIDIA",
            600.0
        );

        assertTrue(rows.next());
        assertEquals("NVIDIA", rows.getString("Brand"));
        assertTrue(rows.getDouble("Price") <= 600.0);
    }
}
