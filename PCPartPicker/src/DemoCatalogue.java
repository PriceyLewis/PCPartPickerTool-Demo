import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** Shared fictional pricing fixture for the desktop database and browser adapter. */
final class DemoCatalogue {
    record Entry(String name, String brand, String category, double price) { }
    static final List<Entry> ENTRIES = load();
    static final String[] STORES = {"Scan Computers", "Overclockers UK", "CCL Computers"};

    private static List<Entry> load() {
        var stream = DemoCatalogue.class.getResourceAsStream("/Assets/catalogue.tsv");
        if (stream == null) throw new IllegalStateException("Missing demo catalogue resource");
        List<Entry> entries = new ArrayList<>();
        try (var reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split("\t", -1);
                if (fields.length != 4) throw new IllegalStateException("Invalid catalogue row: " + line);
                entries.add(new Entry(fields[0], fields[1], fields[2], Double.parseDouble(fields[3])));
            }
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Cannot read demo catalogue", ex);
        }
        return List.copyOf(entries);
    }

    static double price(Entry entry, int store) {
        return Math.round((entry.price() + new double[]{0, 5, -3}[store]) * 100) / 100.0;
    }

    static int stock(int part, int store) {
        // Includes a completely sold-out part, isolated sold-out listings and low stock.
        if (part == ENTRIES.size() - 1 || (part + store) % 17 == 0) return 0;
        return 1 + (part * 7 + store * 3) % 24;
    }
}
