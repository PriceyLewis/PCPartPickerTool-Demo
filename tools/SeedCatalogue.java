import io.github.spannm.jackcess.*;
import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/** Run explicitly against a COPY of the bundled demo database; never on app startup. */
class SeedCatalogue {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) throw new IllegalArgumentException("Supply a demo .accdb copy to update");
        try (var db = DatabaseBuilder.open(new File(args[0]))) {
            var parts = db.getTable("Parts");
            Map<String, String> aliases = Map.of(
                "AMD Ryzen 9 7900X", "Ryzen 9 7900X", "NVIDIA RTX 4070 Ti", "GeForce RTX 4070 Ti",
                "AMD Radeon RX 7900 XT", "Radeon RX 7900 XT", "Corsair Vengeance 32GB DDR5", "Vengeance 32GB DDR5",
                "Corsair RM850x PSU", "RM850x PSU", "Intel Core i9-13900K", "Core i9-13900K");
            Map<String, Row> byName = new HashMap<>();
            for (var row : parts) {
                String old = row.getString("PartName");
                if (aliases.containsKey(old)) { row.put("PartName", aliases.get(old)); parts.updateRow(row); }
                byName.put(row.getString("PartName"), row);
            }
            Map<String, Integer> stores = new HashMap<>();
            for (var row : db.getTable("Stores")) stores.put(row.getString("StoreName"), row.getInt("StoreID"));
            var inventory = db.getTable("Inventory");
            Map<String, Row> listings = new HashMap<>();
            for (var row : inventory) listings.put(row.getInt("PartID") + ":" + row.getInt("StoreID"), row);
            for (int i = 0; i < DemoCatalogue.ENTRIES.size(); i++) {
                var entry = DemoCatalogue.ENTRIES.get(i);
                Row row = byName.get(entry.name());
                int id;
                if (row == null) {
                    Map<String,Object> data = new HashMap<>();
                    data.put("PartName",entry.name()); data.put("Brand",entry.brand()); data.put("Category",entry.category());
                    data.put("Specs","Portfolio catalogue entry; specifications and compatibility are not verified. Prices and stock are fictional.");
                    parts.addRowFromMap(data); id = ((Number)data.get("PartID")).intValue();
                } else {
                    row.put("Category",entry.category()); row.put("Brand",entry.brand()); parts.updateRow(row); id=row.getInt("PartID");
                }
                for (int s=0; s<DemoCatalogue.STORES.length; s++) {
                    int storeId = stores.get(DemoCatalogue.STORES[s]);
                    Row listing = listings.get(id+":"+storeId);
                    Map<String,Object> data = listing == null ? new HashMap<>() : listing;
                    data.put("PartID",id); data.put("StoreID",storeId);
                    data.put("Price",BigDecimal.valueOf(DemoCatalogue.price(entry,s)));
                    data.put("StockLevel",DemoCatalogue.stock(i,s));
                    data.put("LastUpdated",java.time.LocalDateTime.of(2026,9,23,0,0));
                    if (listing == null) inventory.addRowFromMap(data); else inventory.updateRow(listing);
                }
            }
            for (var row : db.getTable("StoreReviews")) {
                String comment = row.getString("Comment");
                if (comment != null && !comment.startsWith("[Fictional demo review]")) {
                    row.put("Comment","[Fictional demo review] " + comment); db.getTable("StoreReviews").updateRow(row);
                }
            }
            System.out.println("Parts: "+parts.getRowCount()+"; inventory: "+inventory.getRowCount());
        }
    }
}
