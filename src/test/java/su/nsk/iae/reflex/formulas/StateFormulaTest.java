package su.nsk.iae.reflex.formulas;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StateFormulaTest {
    StateFormula test = new StateFormula("s","value");

    @Test
    void testToString() {
        assertEquals("(s=value)",test.toString());
    }

    @Test
    void toStrings() {
        assertEquals(List.of("(s=value)"),test.toStrings());
    }

    @Test
    void toNamedString() {
        assertEquals("s:\"(s=value)\"",test.toNamedString());
    }

    @Test
    void toNamedStrings() {
        assertEquals(List.of("s:\"(s=value)\""),test.toNamedStrings());
    }


}