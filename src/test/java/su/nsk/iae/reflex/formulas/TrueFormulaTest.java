package su.nsk.iae.reflex.formulas;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TrueFormulaTest {
    TrueFormula test = new TrueFormula();

    @Test
    void toStrings() {
    assertEquals(List.of("True"),test.toStrings());
    }

    @Test
    void toNamedString() {
        assertEquals("true:\"True\"",test.toNamedString());
    }

    @Test
    void toNamedStrings() {
        assertEquals(List.of("true:\"True\""),test.toNamedStrings());
    }

    @Test
    void testToString() {
        assertEquals("True",test.toString());
    }
}