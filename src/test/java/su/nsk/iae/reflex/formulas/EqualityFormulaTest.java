package su.nsk.iae.reflex.formulas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.expression.RawExpression;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EqualityFormulaTest {

    EqualityFormula test1,test2;

    @BeforeEach
    void prepare(){
        test1 = new EqualityFormula(
                "test",
                true,
                new RawExpression("value1"),
                new RawExpression("value2")
        );
        test2 = new EqualityFormula(
                "test",
                false,
                new RawExpression("value1"),
                new RawExpression("value2")
        );
    }

    @Test
    void testToString() {
        assertEquals("(value1=value2)",test1.toString());
        assertEquals("(value1\\<noteq>value2)",test2.toString());
    }

    @Test
    void toStrings() {
        assertEquals(List.of("(value1=value2)"),test1.toStrings());
        assertEquals(List.of("(value1\\<noteq>value2)"),test2.toStrings());
    }

    @Test
    void toNamedString() {
        assertEquals("test:\"value1=value2\"",test1.toNamedString());
        assertEquals("test:\"value1\\<noteq>value2\"",test2.toNamedString());
    }

    @Test
    void toNamedStrings() {
        assertEquals(List.of("test:\"value1=value2\""),test1.toNamedStrings());
        assertEquals(List.of("test:\"value1\\<noteq>value2\""),test2.toNamedStrings());
    }


}