package su.nsk.iae.reflex.expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.types.Int8Type;
import su.nsk.iae.reflex.expression.types.RealType;

import static org.junit.jupiter.api.Assertions.*;

class BinaryExpressionTest {

    BinaryExpression test1,test2,test3;

    @BeforeEach
    void prepare(){
        test1 = new BinaryExpression(
                BinaryOp.Sum,
                new RawExpression("val1"),
                new RawExpression("val2"),
                new RealType());
        test2 = new BinaryExpression(
                BinaryOp.BitRShift,
                new RawExpression("val1"),
                new RawExpression("val2"),
                new Int8Type());
    }

    @Test
    void exprType() {
        assertTrue(test1.exprType() instanceof RealType);
        assertTrue(test2.exprType() instanceof Int8Type);
    }

    @Test
    void testToString() {
        assertEquals("(val1 + val2)",test1.toString());
        assertEquals("(val1 >> val2)",test2.toString());
    }

    @Test
    void isActuated() {
        assertTrue(test1.isActuated());
        assertTrue(test2.isActuated());
    }
}