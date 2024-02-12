package su.nsk.iae.reflex.expression;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.expression.types.Int8Type;
import su.nsk.iae.reflex.expression.types.UInt8Type;

import static org.junit.jupiter.api.Assertions.*;

class VariableExpressionTest {

    VariableExpression test1,test2;

    @BeforeEach
    void prepare(){
        test1 = new VariableExpression("name", new UInt8Type(),false);
        test2 = new VariableExpression("name", new Int8Type(),true);
    }

    @Test
    void testToString() {
        assertEquals("name",test1.toString());

        assertThrows(RuntimeException.class,()->test2.toString());
        test2.actuate("s");
        assertEquals("(getVarInt s ''name'')",test2.toString());
    }

    @Test
    void actuate() {
        assertFalse(test2.isActuated());
        test2.actuate("S");
        assertTrue(test2.isActuated());
    }

    @Test
    void isActuated() {
        assertTrue(test1.isActuated());
        assertFalse(test2.isActuated());
    }

    @Test
    void exprType() {
        assertTrue(test1.exprType() instanceof UInt8Type);
        assertTrue(test2.exprType() instanceof Int8Type);
    }
}