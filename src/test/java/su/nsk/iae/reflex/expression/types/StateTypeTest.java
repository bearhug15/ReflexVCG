package su.nsk.iae.reflex.expression.types;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StateTypeTest {
    StateType test = new StateType("test");

    @Test
    void takeGetter() {
        assertThrowsExactly(RuntimeException.class,()->test.takeGetter());
    }

    @Test
    void takeSetter() {
        assertThrowsExactly(RuntimeException.class,()->test.takeSetter());
    }

    @Test
    void invertBorder() {
        assertThrowsExactly(RuntimeException.class,()->test.invertBorder());
    }

    @Test
    void defaultValue() {
        assertEquals("emptyState",test.defaultValue());
    }

    @Test
    void testToString() {
        assertEquals("test",test.toString());
    }
}