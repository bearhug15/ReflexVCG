package su.nsk.iae.reflex.expression.ops;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnNegTest {
    UnNeg test = new UnNeg();
    @Test
    void testToString() {
        assertEquals("\\<not>",test.toString());
    }
}