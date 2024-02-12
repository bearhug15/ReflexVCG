package su.nsk.iae.reflex.expression.types;

import org.junit.jupiter.api.Test;

import javax.management.RuntimeMBeanException;

import static org.junit.jupiter.api.Assertions.*;

class IntTypeTest {
    IntType test = new IntType();

    @Test
    void testToString() {
        assertEquals("int", test.toString());
    }

    @Test
    void takeGetter() {
        assertEquals("getVarInt",test.takeGetter());
    }

    @Test
    void takeSetter() {
        assertEquals("setVarInt",test.takeSetter());
    }

    @Test
    void invertBorder() {
        assertThrows(RuntimeException.class,()->test.invertBorder());
    }

    @Test
    void defaultValue() {
        assertEquals("0",test.defaultValue());
    }
}