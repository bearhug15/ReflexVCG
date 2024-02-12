package su.nsk.iae.reflex.expression.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Int32TypeTest {
    Int32Type test = new Int32Type();
    @Test
    void invertBorder() {
        assertEquals("-1",test.invertBorder());
    }
}