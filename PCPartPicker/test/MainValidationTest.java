import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MainValidationTest {
    @Test
    void parsesOptionalPriceFilters() {
        assertNull(Main.parseOptionalPriceFilter(""));
        assertNull(Main.parseOptionalPriceFilter("   "));
        assertEquals(199.99, Main.parseOptionalPriceFilter("199.99"));
        assertEquals(0.0, Main.parseOptionalPriceFilter("0"));
    }

    @Test
    void rejectsInvalidPriceFilters() {
        assertThrows(IllegalArgumentException.class, () -> Main.parseOptionalPriceFilter("NaN"));
        assertThrows(IllegalArgumentException.class, () -> Main.parseOptionalPriceFilter("Infinity"));
        assertThrows(IllegalArgumentException.class, () -> Main.parseOptionalPriceFilter("-1"));
        assertThrows(IllegalArgumentException.class, () -> Main.parseOptionalPriceFilter("abc"));
    }

    @Test
    void parsesBasketPriceFormatsAndRejectsNonFiniteValues() {
        assertEquals(579.99, Main.parsePriceValue("GBP 579.99"));
        assertEquals(579.99, Main.parsePriceValue("£579.99"));
        assertEquals(579.99, Main.parsePriceValue("$579.99"));

        assertThrows(IllegalArgumentException.class, () -> Main.parsePriceValue(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> Main.parsePriceValue(Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> Main.parsePriceValue("-10"));
    }
}
