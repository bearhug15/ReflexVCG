package su.nsk.iae.reflex.expression.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RealTypeTest {

    RealType test = new RealType();

    @Test
    void testToString() {
        assertEquals("real",test.toString());
    }

    @Test
    void takeGetter() {
        assertEquals("getVarReal",test.takeGetter());
    }

    @Test
    void takeSetter() {
        assertEquals("setVarReal",test.takeSetter());
    }

    @Test
    void invertBorder() {
        assertThrowsExactly(RuntimeException.class,()->test.invertBorder());
    }

    @Test
    void defaultValue() {
        assertEquals("0.0",test.defaultValue());
    }
}