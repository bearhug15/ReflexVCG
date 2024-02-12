package su.nsk.iae.reflex.expression.ops;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.expression.types.Int8Type;
import su.nsk.iae.reflex.expression.types.RealType;
import su.nsk.iae.reflex.expression.types.UInt8Type;

import static org.junit.jupiter.api.Assertions.*;

class UnInvertTest {
    UnInvert test1 = new UnInvert(new UInt8Type());
    UnInvert test2 = new UnInvert(new Int8Type());
    //UnInvert test3 = new UnInvert(new RealType());

    @Test
    void testToString() {
        assertEquals("255-",test1.toString());
        assertEquals("-1-",test2.toString());
        //assertThrows(RuntimeException.class,()->test3.toString());
    }
}