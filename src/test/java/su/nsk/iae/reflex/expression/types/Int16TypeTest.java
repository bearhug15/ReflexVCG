package su.nsk.iae.reflex.expression.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Int16TypeTest {
    Int16Type test = new Int16Type();
    @Test
    void invertBorder() {
        assertEquals("-1",test.invertBorder());
    }
}