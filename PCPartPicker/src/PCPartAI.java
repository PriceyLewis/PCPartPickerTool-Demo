import javax.sql.rowset.CachedRowSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PCPartAI {
    private final DatabaseAccess database;

    public PCPartAI(DatabaseAccess database) {
        this.database = database;
    }

    public AssistantReply respond(String prompt, Map<String, Object> userDetails) {
        String cleanedPrompt = prompt == null ? "" : prompt.trim();
        if (cleanedPrompt.isEmpty()) {
            return new AssistantReply("Tell me what kind of component you want, your budget, or the store you want reviewed.");
        }

        if (looksLikeStoreQuestion(cleanedPrompt)) {
            return buildStoreReply(cleanedPrompt);
        }

        return buildPartReply(cleanedPrompt, userDetails);
    }

    private boolean looksLikeStoreQuestion(String prompt) {
        String normalized = prompt.toLowerCase(Locale.UK);
        return normalized.contains("store") || normalized.contains("review") || normalized.contains("retailer");
    }

    private AssistantReply buildStoreReply(String prompt) {
        try {
            CachedRowSet stores = database.executeQuery("SELECT StoreID, StoreName FROM Stores");
            String matchedStoreName = null;
            String matchedStoreId = null;
            String normalizedPrompt = prompt.toLowerCase(Locale.UK);

            while (stores.next()) {
                String storeName = stores.getString("StoreName");
                if (normalizedPrompt.contains(storeName.toLowerCase(Locale.UK))) {
                    matchedStoreName = storeName;
                    matchedStoreId = String.valueOf(stores.getInt("StoreID"));
                    break;
                }
            }

            if (matchedStoreId == null) {
                return new AssistantReply("I could not match that request to a store in the demo database. Try using the exact store name.");
            }

            CachedRowSet reviews = database.executeQuery(
                "SELECT TOP 5 [Rating(1-5)] AS Rating, Comment, ReviewDate FROM StoreReviews WHERE StoreID = ? ORDER BY ReviewDate DESC",
                matchedStoreId
            );

            int reviewCount = 0;
            int ratingTotal = 0;
            List<String> comments = new ArrayList<>();
            while (reviews.next()) {
                reviewCount++;
                ratingTotal += reviews.getInt("Rating");
                String comment = reviews.getString("Comment");
                if (comment != null && !comment.isBlank()) {
                    comments.add(comment.trim());
                }
            }

            if (reviewCount == 0) {
                return new AssistantReply(matchedStoreName + " has no reviews in the current demo data.");
            }

            double average = (double) ratingTotal / reviewCount;
            StringBuilder message = new StringBuilder();
            message.append(matchedStoreName)
                .append(" has ")
                .append(reviewCount)
                .append(" recent review(s) in this demo with an average rating of ")
                .append(String.format(Locale.UK, "%.1f/5", average))
                .append('.');

            if (!comments.isEmpty()) {
                message.append("\nRecent feedback: ").append(comments.get(0));
            }

            return new AssistantReply(message.toString());
        } catch (SQLException e) {
            throw new RuntimeException("Could not summarise store reviews.", e);
        }
    }

    private AssistantReply buildPartReply(String prompt, Map<String, Object> userDetails) {
        try {
            CachedRowSet parts = database.executeQuery(
                "SELECT Parts.PartName, Parts.Brand, Stores.StoreName, Inventory.Price, Inventory.StockLevel " +
                    "FROM (Parts INNER JOIN Inventory ON Parts.PartID = Inventory.PartID) " +
                    "INNER JOIN Stores ON Inventory.StoreID = Stores.StoreID " +
                    "WHERE Inventory.StockLevel > 0 ORDER BY Inventory.Price ASC"
            );

            String preferenceText = userDetails == null ? "" : String.valueOf(userDetails.getOrDefault("Preferences", ""));
            double maxBudget = extractBudget(prompt);
            String normalizedPrompt = prompt.toLowerCase(Locale.UK);
            String normalizedPreference = preferenceText.toLowerCase(Locale.UK);
            String requestedCategory = requestedCategory(normalizedPrompt);
            List<ScoredPart> candidates = new ArrayList<>();

            while (parts.next()) {
                String partName = parts.getString("PartName");
                String brand = parts.getString("Brand");
                String storeName = parts.getString("StoreName");
                double price = parts.getDouble("Price");
                String searchable = (partName + " " + brand).toLowerCase(Locale.UK);

                if (maxBudget > 0 && price > maxBudget) {
                    continue;
                }

                if (requestedCategory != null && DemoCatalogue.ENTRIES.stream().noneMatch(
                    entry -> entry.name().equals(partName) && entry.category().equals(requestedCategory))) {
                    continue;
                }

                int score = 0;
                for (String token : normalizedPrompt.split("[^a-z0-9]+")) {
                    if (token.length() > 2 && searchable.contains(token)) {
                        score += 3;
                    }
                }
                for (String token : normalizedPreference.split("[^a-z0-9]+")) {
                    if (token.length() > 2 && searchable.contains(token)) {
                        score += 2;
                    }
                }
                if (score == 0 && normalizedPrompt.contains("recommend")) {
                    score = 1;
                }

                candidates.add(new ScoredPart(partName, brand, storeName, price, score));
            }

            if (candidates.isEmpty()) {
                return new AssistantReply("I could not find an in-stock part that matches that budget or request.");
            }

            candidates.sort(Comparator
                .comparingInt(ScoredPart::score).reversed()
                .thenComparingDouble(ScoredPart::price));

            ScoredPart best = candidates.get(0);
            StringBuilder message = new StringBuilder();
            message.append("Best match: ")
                .append(best.partName)
                .append(" by ")
                .append(best.brand)
                .append(" at ")
                .append(best.storeName)
                .append(" for ")
                .append(String.format(Locale.UK, "GBP %.2f", best.price))
                .append('.');

            int extraCount = Math.min(2, candidates.size() - 1);
            if (extraCount > 0) {
                message.append("\nOther solid options:");
                for (int i = 1; i <= extraCount; i++) {
                    ScoredPart option = candidates.get(i);
                    message.append("\n- ")
                        .append(option.partName)
                        .append(" at ")
                        .append(option.storeName)
                        .append(" for ")
                        .append(String.format(Locale.UK, "GBP %.2f", option.price));
                }
            }

            return new AssistantReply(message.toString(), best.partName, best.storeName, best.price);
        } catch (SQLException e) {
            throw new RuntimeException("Could not build part recommendation.", e);
        }
    }

    private static String requestedCategory(String prompt) {
        String[][] categories = {
            {"GPU", "gpu", "graphics card"}, {"CPU", "cpu", "processor"},
            {"Motherboard", "motherboard"}, {"RAM", "ram", "memory"},
            {"Storage", "ssd", "hdd", "storage"}, {"Case", "case"},
            {"PSU", "psu", "power supply"}, {"Cooling", "cooler", "cooling"}
        };
        for (String[] category : categories) {
            for (int i = 1; i < category.length; i++) {
                if (Pattern.compile("\\b" + Pattern.quote(category[i]) + "\\b").matcher(prompt).find()) return category[0];
            }
        }
        return null;
    }

    static double extractBudget(String prompt) {
        Pattern pattern = Pattern.compile("(?:under|below|max|budget)\\s*(?:gbp|usd|£|\\$)?\\s*(\\d+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(prompt);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        }
        return -1;
    }

    private record ScoredPart(String partName, String brand, String storeName, double price, int score) {
    }

    public static final class AssistantReply {
        private final String message;
        private final String partName;
        private final String storeName;
        private final double price;

        public AssistantReply(String message) {
            this(message, null, null, 0);
        }

        public AssistantReply(String message, String partName, String storeName, double price) {
            this.message = message;
            this.partName = partName;
            this.storeName = storeName;
            this.price = price;
        }

        public String getMessage() {
            return message;
        }

        public boolean hasRecommendation() {
            return partName != null && storeName != null;
        }

        public String getPartName() {
            return partName;
        }

        public String getStoreName() {
            return storeName;
        }

        public double getPrice() {
            return price;
        }
    }
}
