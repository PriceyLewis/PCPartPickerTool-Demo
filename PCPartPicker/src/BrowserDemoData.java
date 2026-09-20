import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class BrowserDemoData {
    private static final List<Part> PARTS = new ArrayList<>();
    private static final List<Store> STORES = new ArrayList<>();
    private static final List<InventoryLine> INVENTORY = new ArrayList<>();
    private static final List<Review> REVIEWS = new ArrayList<>();
    private static final List<SearchLog> SEARCH_LOGS = new ArrayList<>();
    private static final Map<Integer, Map<String, Object>> USERS = new LinkedHashMap<>();
    private static int nextPartId = 1;
    private static int nextUserId = 1;

    static {
        seed();
    }

    private BrowserDemoData() {
    }

    static synchronized Map<String, Object> demoUser() {
        return copyUser(USERS.get(1));
    }

    static synchronized Map<String, Object> addUser(String username, String preferences, String location) {
        int id = nextUserId++;
        Map<String, Object> user = new HashMap<>();
        user.put("UserID", id);
        user.put("Username", username);
        user.put("Location", location);
        user.put("Preferences", preferences);
        USERS.put(id, user);
        return copyUser(user);
    }

    static synchronized Map<String, Object> retrieveUser(String userId) {
        try {
            return copyUser(USERS.get(Integer.parseInt(userId)));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    static synchronized void addSearchLog(String partName, Map<String, Object> userDetails) {
        if (userDetails == null) {
            return;
        }
        int userId = Integer.parseInt(String.valueOf(userDetails.getOrDefault("UserID", 1)));
        String location = String.valueOf(userDetails.getOrDefault("Location", "Demo"));
        SEARCH_LOGS.add(new SearchLog(userId, partName, LocalDate.now(), location));
    }

    static synchronized void addReview(String storeId, String userId, String comment, int rating) {
        REVIEWS.add(new Review(
            Integer.parseInt(storeId),
            Integer.parseInt(userId),
            rating,
            comment,
            LocalDate.now()
        ));
    }

    static synchronized String storeIdFor(String name) {
        String needle = name == null ? "" : name.trim().toLowerCase(Locale.UK);
        return STORES.stream()
            .filter(store -> store.name().toLowerCase(Locale.UK).contains(needle)
                || needle.contains(store.name().toLowerCase(Locale.UK)))
            .map(store -> String.valueOf(store.id()))
            .findFirst()
            .orElse("");
    }

    static synchronized void decrementStock(String partName, String storeId) {
        int parsedStoreId;
        try {
            parsedStoreId = Integer.parseInt(storeId);
        } catch (NumberFormatException ex) {
            return;
        }

        Part part = PARTS.stream()
            .filter(candidate -> candidate.name().equalsIgnoreCase(partName))
            .findFirst()
            .orElse(null);
        if (part == null) {
            return;
        }

        INVENTORY.stream()
            .filter(line -> line.partId == part.id() && line.storeId == parsedStoreId && line.stock > 0)
            .findFirst()
            .ifPresent(line -> line.stock--);
    }

    static synchronized CachedRowSet query(String sql, Object... parameters) {
        String normalized = normalize(sql);

        if (normalized.contains("select partid, partname, brand from parts")) {
            List<Part> rows = PARTS.stream()
                .sorted(Comparator.comparing(Part::name))
                .toList();
            return rowSet(
                new String[]{"PartID", "PartName", "Brand"},
                new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR},
                rows.stream().map(part -> new Object[]{part.id(), part.name(), part.brand()}).toList()
            );
        }

        if (normalized.contains("from stores") && !normalized.contains("storereviews")) {
            List<Store> stores = new ArrayList<>(STORES);
            if (normalized.contains("where storename = ?") && parameters.length > 0) {
                String needle = String.valueOf(parameters[0]).toLowerCase(Locale.UK);
                stores.removeIf(store -> !store.name().toLowerCase(Locale.UK).equals(needle)
                    && !store.name().toLowerCase(Locale.UK).contains(needle));
            }
            return rowSet(
                new String[]{"StoreID", "StoreName"},
                new int[]{Types.INTEGER, Types.VARCHAR},
                stores.stream().map(store -> new Object[]{store.id(), store.name()}).toList()
            );
        }

        if (normalized.contains("from storereviews")) {
            int storeId = parameters.length > 0 ? Integer.parseInt(String.valueOf(parameters[0])) : -1;
            int limit = normalized.contains("top 5") ? 5 : 10;
            List<Review> reviews = REVIEWS.stream()
                .filter(review -> storeId < 0 || review.storeId() == storeId)
                .sorted(Comparator.comparing(Review::date).reversed())
                .limit(limit)
                .toList();
            return rowSet(
                new String[]{"Rating", "Comment", "ReviewDate"},
                new int[]{Types.INTEGER, Types.VARCHAR, Types.DATE},
                reviews.stream().map(review -> new Object[]{
                    review.rating(), review.comment(), Date.valueOf(review.date())
                }).toList()
            );
        }

        // Match the Users table as a complete identifier; "UserSearches" also starts with "Users".
        if (normalized.matches(".*\\bfrom users\\b.*")) {
            if (normalized.contains("count(*)")) {
                return rowSet(
                    new String[]{"TotalUsers"},
                    new int[]{Types.INTEGER},
                    List.<Object[]>of(new Object[]{USERS.size()})
                );
            }
            return rowSet(
                new String[]{"UserID", "Username", "Location", "Preferences"},
                new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR},
                USERS.values().stream().map(user -> new Object[]{
                    user.get("UserID"), user.get("Username"), user.get("Location"), user.get("Preferences")
                }).toList()
            );
        }

        if (normalized.contains("from usersearches")) {
            if (normalized.contains("group by searchedpart")) {
                Map<String, Integer> counts = new HashMap<>();
                for (SearchLog log : SEARCH_LOGS) {
                    counts.merge(log.partName(), 1, Integer::sum);
                }
                Map.Entry<String, Integer> top = counts.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(Map.entry("No searches yet", 0));
                return rowSet(
                    new String[]{"SearchedPart", "SearchCount"},
                    new int[]{Types.VARCHAR, Types.INTEGER},
                    List.<Object[]>of(new Object[]{top.getKey(), top.getValue()})
                );
            }

            Integer userId = null;
            if (normalized.contains("where userid = ?") && parameters.length > 0) {
                userId = Integer.parseInt(String.valueOf(parameters[0]));
            }
            final Integer filterUserId = userId;
            List<SearchLog> logs = SEARCH_LOGS.stream()
                .filter(log -> filterUserId == null || log.userId() == filterUserId)
                .sorted(Comparator.comparing(SearchLog::date).reversed())
                .limit(normalized.contains("top 25") ? 25 : 50)
                .toList();

            if (normalized.startsWith("select top 25 searchedpart")) {
                return rowSet(
                    new String[]{"SearchedPart", "SearchDate", "UserLocation"},
                    new int[]{Types.VARCHAR, Types.DATE, Types.VARCHAR},
                    logs.stream().map(log -> new Object[]{
                        log.partName(), Date.valueOf(log.date()), log.location()
                    }).toList()
                );
            }

            return rowSet(
                new String[]{"UserID", "SearchedPart", "SearchDate", "UserLocation"},
                new int[]{Types.INTEGER, Types.VARCHAR, Types.DATE, Types.VARCHAR},
                logs.stream().map(log -> new Object[]{
                    log.userId(), log.partName(), Date.valueOf(log.date()), log.location()
                }).toList()
            );
        }

        if (normalized.contains("from inventory") && normalized.contains("count(*)")) {
            long count = INVENTORY.stream().filter(line -> line.stock < 5).count();
            return rowSet(
                new String[]{"LowStockCount"},
                new int[]{Types.INTEGER},
                List.<Object[]>of(new Object[]{(int) count})
            );
        }

        if (normalized.contains("from (parts inner join inventory")) {
            List<JoinedPart> joined = joinedInventory();

            if (normalized.contains("where inventory.stocklevel < 5")) {
                joined.removeIf(row -> row.stock() >= 5);
                joined.sort(Comparator.comparingInt(JoinedPart::stock));
                return rowSet(
                    new String[]{"PartName", "StoreName", "StockLevel"},
                    new int[]{Types.VARCHAR, Types.VARCHAR, Types.INTEGER},
                    joined.stream().map(row -> new Object[]{row.partName(), row.storeName(), row.stock()}).toList()
                );
            }

            joined.removeIf(row -> row.stock() <= 0);
            int paramIndex = 0;

            if (normalized.contains("parts.partname like ?") && paramIndex < parameters.length) {
                String keyword = cleanLike(parameters[paramIndex++]);
                joined.removeIf(row -> !row.partName().toLowerCase(Locale.UK).contains(keyword));
            }

            if (normalized.contains("parts.brand = ?") && paramIndex < parameters.length) {
                String brand = String.valueOf(parameters[paramIndex++]);
                joined.removeIf(row -> !row.brand().equalsIgnoreCase(brand));
            }

            if (normalized.contains("inventory.price >= ?") && paramIndex < parameters.length) {
                double min = Double.parseDouble(String.valueOf(parameters[paramIndex++]));
                joined.removeIf(row -> row.price() < min);
            }

            if (normalized.contains("inventory.price <= ?") && paramIndex < parameters.length) {
                double max = Double.parseDouble(String.valueOf(parameters[paramIndex++]));
                joined.removeIf(row -> row.price() > max);
            }

            if (normalized.contains("order by inventory.price desc")) {
                joined.sort(Comparator.comparingDouble(JoinedPart::price).reversed());
            } else if (normalized.contains("order by inventory.price asc")) {
                joined.sort(Comparator.comparingDouble(JoinedPart::price));
            } else {
                joined.sort(Comparator.comparing(JoinedPart::partName));
            }

            if (normalized.contains("top 30") && joined.size() > 30) {
                joined = new ArrayList<>(joined.subList(0, 30));
            }

            if (normalized.contains("parts.brand") && normalized.contains("inventory.stocklevel")) {
                return rowSet(
                    new String[]{"PartName", "Brand", "StoreName", "Price", "StockLevel"},
                    new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.DOUBLE, Types.INTEGER},
                    joined.stream().map(row -> new Object[]{
                        row.partName(), row.brand(), row.storeName(), row.price(), row.stock()
                    }).toList()
                );
            }

            if (normalized.contains("parts.brand")) {
                return rowSet(
                    new String[]{"PartName", "StoreName", "Price", "Brand"},
                    new int[]{Types.VARCHAR, Types.VARCHAR, Types.DOUBLE, Types.VARCHAR},
                    joined.stream().map(row -> new Object[]{
                        row.partName(), row.storeName(), row.price(), row.brand()
                    }).toList()
                );
            }

            return rowSet(
                new String[]{"PartName", "StoreName", "Price"},
                new int[]{Types.VARCHAR, Types.VARCHAR, Types.DOUBLE},
                joined.stream().map(row -> new Object[]{
                    row.partName(), row.storeName(), row.price()
                }).toList()
            );
        }

        throw new IllegalArgumentException("Browser demo query is not supported: " + sql);
    }

    static synchronized int update(String sql, Object... parameters) {
        String normalized = normalize(sql);

        if (normalized.startsWith("insert into parts")) {
            PARTS.add(new Part(nextPartId++, String.valueOf(parameters[0]), String.valueOf(parameters[1])));
            return 1;
        }

        if (normalized.startsWith("delete from parts")) {
            int id = Integer.parseInt(String.valueOf(parameters[0]));
            PARTS.removeIf(part -> part.id() == id);
            INVENTORY.removeIf(line -> line.partId == id);
            return 1;
        }

        if (normalized.startsWith("insert into usersearches")) {
            SEARCH_LOGS.add(new SearchLog(
                Integer.parseInt(String.valueOf(parameters[0])),
                String.valueOf(parameters[1]),
                LocalDate.now(),
                String.valueOf(parameters[3])
            ));
            return 1;
        }

        if (normalized.startsWith("insert into storereviews")) {
            addReview(
                String.valueOf(parameters[0]),
                String.valueOf(parameters[1]),
                String.valueOf(parameters[3]),
                Integer.parseInt(String.valueOf(parameters[2]))
            );
            return 1;
        }

        return 0;
    }

    private static void seed() {
        PARTS.clear();
        STORES.clear();
        INVENTORY.clear();
        REVIEWS.clear();
        SEARCH_LOGS.clear();
        USERS.clear();

        addPart("Ryzen 7 7800X3D", "AMD");
        addPart("Core i7-14700K", "Intel");
        addPart("GeForce RTX 4070 Super", "NVIDIA");
        addPart("Radeon RX 7800 XT", "AMD");
        addPart("Vengeance 32GB DDR5", "Corsair");
        addPart("MAG B650 Tomahawk WiFi", "MSI");
        addPart("TUF Gaming B650-PLUS", "ASUS");

        STORES.add(new Store(1, "Scan Computers"));
        STORES.add(new Store(2, "Overclockers UK"));
        STORES.add(new Store(3, "CCL Computers"));

        addInventory(1, 1, 329.99, 7);
        addInventory(1, 2, 334.99, 3);
        addInventory(2, 1, 349.99, 5);
        addInventory(3, 1, 579.99, 8);
        addInventory(3, 2, 569.99, 4);
        addInventory(4, 3, 449.99, 6);
        addInventory(5, 1, 109.99, 12);
        addInventory(6, 2, 179.99, 2);
        addInventory(7, 3, 169.99, 9);

        Map<String, Object> demo = new HashMap<>();
        demo.put("UserID", 1);
        demo.put("Username", "Demo Builder");
        demo.put("Location", "Manchester");
        demo.put("Preferences", "NVIDIA quiet gaming");
        USERS.put(1, demo);
        nextUserId = 2;

        REVIEWS.add(new Review(1, 1, 5, "Fast dispatch and clear stock information.", LocalDate.now().minusDays(4)));
        REVIEWS.add(new Review(2, 1, 4, "Good enthusiast range and helpful product pages.", LocalDate.now().minusDays(8)));
        REVIEWS.add(new Review(3, 1, 4, "Straightforward ordering experience in the demo data.", LocalDate.now().minusDays(13)));

        SEARCH_LOGS.add(new SearchLog(1, "GeForce RTX 4070 Super", LocalDate.now().minusDays(1), "Manchester"));
        SEARCH_LOGS.add(new SearchLog(1, "Ryzen 7 7800X3D", LocalDate.now().minusDays(2), "Manchester"));
        SEARCH_LOGS.add(new SearchLog(1, "GeForce RTX 4070 Super", LocalDate.now().minusDays(3), "Manchester"));
    }

    private static void addPart(String name, String brand) {
        PARTS.add(new Part(nextPartId++, name, brand));
    }

    private static void addInventory(int partId, int storeId, double price, int stock) {
        INVENTORY.add(new InventoryLine(partId, storeId, price, stock));
    }

    private static List<JoinedPart> joinedInventory() {
        List<JoinedPart> rows = new ArrayList<>();
        for (InventoryLine line : INVENTORY) {
            Part part = PARTS.stream().filter(item -> item.id() == line.partId).findFirst().orElse(null);
            Store store = STORES.stream().filter(item -> item.id() == line.storeId).findFirst().orElse(null);
            if (part != null && store != null) {
                rows.add(new JoinedPart(
                    part.id(), part.name(), part.brand(), store.id(), store.name(), line.price, line.stock
                ));
            }
        }
        return rows;
    }

    private static CachedRowSet rowSet(String[] columns, int[] types, List<Object[]> rows) {
        try {
            CachedRowSet rowSet = RowSetProvider.newFactory().createCachedRowSet();
            RowSetMetaDataImpl metadata = new RowSetMetaDataImpl();
            metadata.setColumnCount(columns.length);
            for (int i = 0; i < columns.length; i++) {
                metadata.setColumnName(i + 1, columns[i]);
                metadata.setColumnLabel(i + 1, columns[i]);
                metadata.setColumnType(i + 1, types[i]);
            }
            rowSet.setMetaData(metadata);

            for (int rowIndex = rows.size() - 1; rowIndex >= 0; rowIndex--) {
                Object[] values = rows.get(rowIndex);
                rowSet.moveToInsertRow();
                for (int column = 0; column < values.length; column++) {
                    rowSet.updateObject(column + 1, values[column]);
                }
                rowSet.insertRow();
                rowSet.moveToCurrentRow();
            }
            rowSet.beforeFirst();
            return rowSet;
        } catch (SQLException ex) {
            throw new RuntimeException("Could not build browser demo data.", ex);
        }
    }

    private static String cleanLike(Object value) {
        return String.valueOf(value)
            .replace("%", "")
            .trim()
            .toLowerCase(Locale.UK);
    }

    private static String normalize(String sql) {
        return sql.toLowerCase(Locale.UK).replaceAll("\\s+", " ").trim();
    }

    private static Map<String, Object> copyUser(Map<String, Object> user) {
        return user == null ? null : new HashMap<>(user);
    }

    private record Part(int id, String name, String brand) {
    }

    private record Store(int id, String name) {
    }

    private static final class InventoryLine {
        private final int partId;
        private final int storeId;
        private final double price;
        private int stock;

        private InventoryLine(int partId, int storeId, double price, int stock) {
            this.partId = partId;
            this.storeId = storeId;
            this.price = price;
            this.stock = stock;
        }
    }

    private record Review(int storeId, int userId, int rating, String comment, LocalDate date) {
    }

    private record SearchLog(int userId, String partName, LocalDate date, String location) {
    }

    private record JoinedPart(
        int partId,
        String partName,
        String brand,
        int storeId,
        String storeName,
        double price,
        int stock
    ) {
    }
}
