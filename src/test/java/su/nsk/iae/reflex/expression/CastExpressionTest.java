package su.nsk.iae.reflex.expression;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.expression.types.BoolType;
import su.nsk.iae.reflex.expression.types.Int8Type;
import su.nsk.iae.reflex.expression.types.RealType;
import su.nsk.iae.reflex.expression.types.UInt8Type;

import static org.junit.jupiter.api.Assertions.*;

class CastExpressionTest {
    static CastExpression test1,test2,test3,test4;
    @BeforeAll
    static void prepare(){
        test1 = new CastExpression(new UInt8Type(), new VariableExpression("name",new UInt8Type(),false));
        test2 = new CastExpression(new UInt8Type(), new VariableExpression("name",new Int8Type(),false));
        test3 =  new CastExpression(new BoolType(), new RawExpression("value"));
        test4 = new CastExpression(new RealType(), new VariableExpression("name",new Int8Type(),true));
    }

    @Test
    void exprType() {
        assertTrue(test1.exprType() instanceof UInt8Type);
        assertTrue(test2.exprType() instanceof UInt8Type);
        assertTrue(test3.exprType() instanceof BoolType);
        assertTrue(test4.exprType() instanceof RealType);

    }

    @Test
    void isActuated() {
        assertTrue(test1.isActuated());
        assertTrue(test2.isActuated());
        assertTrue(test3.isActuated());
        assertFalse(test4.isActuated());
    }

    @Test
    void testToString() {
        assertEquals("name",test1.toString());
        assertEquals("(nat name)",test2.toString());
        assertEquals("(bool value)",test3.toString());
        assertThrows(RuntimeException.class,()->test4.toString());
    }
}