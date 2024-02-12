package su.nsk.iae.reflex.expression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RawExpressionTest {
    RawExpression test = new RawExpression("test");

    @Test
    void testToString() {
        assertEquals("test",test.toString());
    }

    @Test
    void exprType() {
        assertNull(test.exprType());
    }


    @Test
    void isActuated() {
        assertTrue(test.isActuated());
    }
}