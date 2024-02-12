package su.nsk.iae.reflex.formulas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GreaterFormulaTest {

    GreaterFormula test1,test2;

    @BeforeEach
    void prepare(){
        test1 = new GreaterFormula(
                "test",
                true,
                "value1",
                "value2"
        );
        test2 = new GreaterFormula(
                "test",
                false,
                "value1",
                "value2"
        );
    }

    @Test
    void testToString() {
        assertEquals("(value1>value2)",test1.toString());
        assertEquals("(value1\\<le>value2)",test2.toString());
    }

    @Test
    void toStrings() {
        assertEquals(List.of("(value1>value2)"),test1.toStrings());
        assertEquals(List.of("(value1\\<le>value2)"),test2.toStrings());
    }

    @Test
    void toNamedString() {
        assertEquals("test:\"value1>value2\"",test1.toNamedString());
        assertEquals("test:\"value1\\<le>value2\"",test2.toNamedString());
    }

    @Test
    void toNamedStrings() {
        assertEquals(List.of("test:\"value1>value2\""),test1.toNamedStrings());
        assertEquals(List.of("test:\"value1\\<le>value2\""),test2.toNamedStrings());
    }


}