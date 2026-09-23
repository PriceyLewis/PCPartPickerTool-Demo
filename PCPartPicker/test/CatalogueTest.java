import org.junit.jupiter.api.Test;
import io.github.spannm.jackcess.*;
import java.io.File;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class CatalogueTest {
    @Test void sharedCatalogueHasCoverageAndValidUniqueRows() {
        assertEquals(130, DemoCatalogue.ENTRIES.size());
        assertEquals(130, DemoCatalogue.ENTRIES.stream().map(DemoCatalogue.Entry::name).distinct().count());
        assertEquals(8, DemoCatalogue.ENTRIES.stream().map(DemoCatalogue.Entry::category).distinct().count());
        for (var part : DemoCatalogue.ENTRIES) {
            assertTrue(part.price() > 3); assertFalse(part.brand().isBlank());
        }
    }
    @Test void desktopCatalogueAndSharedStoreListingsMatchSeedExactly() throws Exception {
        try(var db = DatabaseBuilder.open(new File("PCPartPicker/Database for App.accdb"))) {
            Map<String,Integer> parts = new HashMap<>(), stores = new HashMap<>();
            for(var row:db.getTable("Parts")) parts.put(row.getString("PartName"),row.getInt("PartID"));
            for(var row:db.getTable("Stores")) stores.put(row.getString("StoreName"),row.getInt("StoreID"));
            Map<String,Row> inventory = new HashMap<>();
            for(var row:db.getTable("Inventory")) inventory.put(row.getInt("PartID")+":"+row.getInt("StoreID"),row);
            assertEquals(130,parts.size());
            for(int p=0;p<DemoCatalogue.ENTRIES.size();p++) {
                var entry=DemoCatalogue.ENTRIES.get(p);
                assertNotNull(parts.get(entry.name()),entry.name());
                for(int s=0;s<3;s++) {
                    var row=inventory.get(parts.get(entry.name())+":"+stores.get(DemoCatalogue.STORES[s]));
                    assertNotNull(row,entry.name());
                    assertEquals(DemoCatalogue.price(entry,s),((Number)row.get("Price")).doubleValue(),0.001);
                    assertEquals(DemoCatalogue.stock(p,s),row.getInt("StockLevel"));
                }
            }
        }
    }
    @Test void browserAndDesktopHaveMatchingInStockSharedOffers() throws Exception {
        var rows=BrowserDemoData.query("SELECT Parts.PartName, Parts.Brand, Stores.StoreName, Inventory.Price, Inventory.StockLevel FROM (Parts INNER JOIN Inventory ON Parts.PartID = Inventory.PartID) INNER JOIN Stores ON Inventory.StoreID = Stores.StoreID WHERE Inventory.StockLevel > 0 ORDER BY Inventory.Price ASC");
        Set<String> seen = new HashSet<>();
        double previous=0;
        while(rows.next()) {
            assertTrue(rows.getInt("StockLevel")>0);
            assertTrue(rows.getDouble("Price")>=previous); previous=rows.getDouble("Price");
            seen.add(rows.getString("PartName"));
        }
        assertEquals(129,seen.size());
    }
    @Test void expensiveGpuIsStillRecommendedAndWrongCategoryIsExcluded() {
        System.setProperty("portfolio.browser","true");
        var ai = new PCPartAI(new DatabaseAccess());
        var reply = ai.respond("recommend RTX 4090 GPU under £1500",Map.of());
        assertEquals("GeForce RTX 4090",reply.getPartName());
        assertFalse(ai.respond("recommend GPU under £5",Map.of()).hasRecommendation());
        var storage=ai.respond("recommend SSD under £100",Map.of());
        assertTrue(storage.hasRecommendation());
        assertTrue(storage.getPartName().contains("SSD"));
    }
}
