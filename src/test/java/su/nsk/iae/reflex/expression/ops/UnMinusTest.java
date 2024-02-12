package su.nsk.iae.reflex.expression.ops;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnMinusTest {
    UnMinus test = new UnMinus();
    @Test
    void testToString() {
        assertEquals("-",test.toString());
    }
}