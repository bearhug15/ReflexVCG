package su.nsk.iae.reflex.expression.types;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static java.lang.Math.pow;
import static org.junit.jupiter.api.Assertions.*;

class UInt64TypeTest {
    UInt64Type test = new UInt64Type();

    @Test
    void invertBorder() {
        BigDecimal decimal = new BigDecimal(2);
        decimal = decimal.pow(64);
        decimal = decimal.subtract(new BigDecimal(1));
        assertEquals(decimal.toString(),test.invertBorder());
    }
}