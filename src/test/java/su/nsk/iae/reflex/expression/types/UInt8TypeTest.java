package su.nsk.iae.reflex.expression.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UInt8TypeTest {
    UInt8Type test = new UInt8Type();

    @Test
    void invertBorder() {
        assertEquals("255",test.invertBorder());
    }
}