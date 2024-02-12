package su.nsk.iae.reflex.expression.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UInt16TypeTest {
    UInt16Type test = new UInt16Type();

    @Test
    void invertBorder() {
        assertEquals("65535",test.invertBorder());
    }

}