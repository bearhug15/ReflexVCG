package su.nsk.iae.reflex.expression.types;

import org.junit.jupiter.api.Test;

import javax.management.RuntimeMBeanException;

import static org.junit.jupiter.api.Assertions.*;

class NatTypeTest {
    NatType test = new NatType();

    @Test
    void testToString() {
        assertEquals("nat", test.toString());
    }

    @Test
    void takeGetter() {
        assertEquals("getVarNat",test.takeGetter());
    }

    @Test
    void takeSetter() {
        assertEquals("setVarNat",test.takeSetter());
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