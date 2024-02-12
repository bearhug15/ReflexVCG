package su.nsk.iae.reflex.expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.expression.ops.UnMinus;
import su.nsk.iae.reflex.expression.ops.UnPlus;
import su.nsk.iae.reflex.expression.types.Int8Type;
import su.nsk.iae.reflex.expression.types.IntType;
import su.nsk.iae.reflex.expression.types.NatType;
import su.nsk.iae.reflex.expression.types.UInt8Type;

import static org.junit.jupiter.api.Assertions.*;

class UnaryExpressionTest {

    UnaryExpression test1,test2,test3;

    @BeforeEach
    void prepare(){
        test1 = new UnaryExpression(
                new UnMinus(),
                new ConstantExpression("10", new UInt8Type()),
                new NatType()
        );
        test2 = new UnaryExpression(
                new UnMinus(),
                new VariableExpression("name", new Int8Type(), false),
                new IntType()
        );
        test3 = new UnaryExpression(
                new UnPlus(),
                new VariableExpression("name", new Int8Type(), true),
                new NatType()
        );
    }

    @Test
    void exprType() {
        assertTrue(test1.exprType() instanceof  NatType);
        assertTrue(test2.exprType() instanceof  IntType);
        assertTrue(test3.exprType() instanceof  NatType);
    }

    @Test
    void testToString() {
        assertEquals("(-10)",test1.toString());
        assertEquals("(-name)",test2.toString());

        assertThrows(RuntimeException.class,()->test3.toString());
        test3.actuate("s");
        assertEquals("(+(getVarInt s ''name''))",test3.toString());
    }

    @Test
    void isActuated() {
        assertTrue(test1.isActuated());
        assertTrue(test2.isActuated());
        assertFalse(test3.isActuated());
    }
}