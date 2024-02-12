package su.nsk.iae.reflex.expression.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoolTypeTest {

    BoolType test = new BoolType();

    @Test
    void testToString() {
        assertEquals("bool",test.toString());
    }

    @Test
    void takeGetter() {
        assertEquals("getVarBool",test.takeGetter());
    }

    @Test
    void takeSetter() {
        assertEquals("setVarBool",test.takeSetter());
    }

    @Test
    void invertBorder() {
        assertEquals("True",test.invertBorder());
    }

    @Test
    void defaultValue() {
        assertEquals("False",test.defaultValue());
    }
}