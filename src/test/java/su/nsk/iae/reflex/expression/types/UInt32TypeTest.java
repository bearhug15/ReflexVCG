package su.nsk.iae.reflex.expression.types;

import org.junit.jupiter.api.Test;

import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static org.junit.jupiter.api.Assertions.*;

class UInt32TypeTest {
    UInt32Type test = new UInt32Type();

    @Test
    void invertBorder() {
        assertEquals(Long.toString(Double.valueOf(pow(2,32)).longValue()-1),test.invertBorder());
    }
}