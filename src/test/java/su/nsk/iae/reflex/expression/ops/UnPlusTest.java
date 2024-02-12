package su.nsk.iae.reflex.expression.ops;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnPlusTest {
    UnPlus test = new UnPlus();
    @Test
    void testToString() {
        assertEquals("+",test.toString());
    }
}