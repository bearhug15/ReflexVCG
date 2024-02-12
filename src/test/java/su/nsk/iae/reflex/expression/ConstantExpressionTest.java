package su.nsk.iae.reflex.expression;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.expression.types.UInt8Type;

import static org.junit.jupiter.api.Assertions.*;

class ConstantExpressionTest {
    ConstantExpression test = new ConstantExpression("10", new UInt8Type());

    @Test
    void exprType() {
        assertTrue(test.exprType() instanceof UInt8Type);
    }

    @Test
    void isActuated() {
        assertTrue(test.isActuated());
    }

    @Test
    void testToString() {
        assertEquals("10",test.toString());
    }
}