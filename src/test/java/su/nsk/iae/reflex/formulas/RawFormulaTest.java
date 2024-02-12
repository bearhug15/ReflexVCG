package su.nsk.iae.reflex.formulas;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RawFormulaTest {
    RawFormula test = new RawFormula("test","value");

    @Test
    void testToString() {
        assertEquals("value",test.toString());
    }

    @Test
    void toStrings() {
        assertEquals(List.of("value"),test.toStrings());
    }

    @Test
    void toNamedString() {
        assertEquals("test:\"value\"",test.toNamedString());
    }

    @Test
    void toNamedStrings() {
        assertEquals(List.of("test:\"value\""),test.toNamedStrings());
    }

    @Test
    void trim() {
        assertEquals(test,test.trim());
    }
}