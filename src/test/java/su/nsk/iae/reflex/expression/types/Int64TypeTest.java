package su.nsk.iae.reflex.expression.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Int64TypeTest {
    Int64Type test = new Int64Type();

    @Test
    void invertBorder() {
        assertEquals("-1",test.invertBorder());
    }
}