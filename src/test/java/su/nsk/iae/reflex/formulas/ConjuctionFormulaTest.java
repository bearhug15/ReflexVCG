package su.nsk.iae.reflex.formulas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConjuctionFormulaTest {

    ConjuctionFormula test1,test2,test3,test4;

    @BeforeEach
    void prepare(){
        test1 = new ConjuctionFormula();
        LinkedList<Formula> fs1 = new LinkedList<>();
        fs1.add(new RawFormula("f1","val1"));
        fs1.add(new RawFormula("f2","val2"));
        test1.formulas = fs1;

        test2 = new ConjuctionFormula();
        LinkedList<Formula> fs2 = new LinkedList<>();
        fs2.add(new RawFormula("f1","val1"));
        fs2.add(new TrueFormula());
        test2.formulas = fs2;


        test3 = new ConjuctionFormula();
        LinkedList<Formula> fs3 = new LinkedList<>();
        fs3.add(new RawFormula("f1","val1"));
        fs3.add(new MarkSetState());
        fs3.add(new MarkReset());
        test3.formulas = fs3;

        test4 = new ConjuctionFormula();
        LinkedList<Formula> fs4 = new LinkedList<>();
        fs4.add(new RawFormula("f1","val1"));
        fs4.add(new MarkSetState());
        fs4.add(new MarkReset());
        fs4.add(new UnmarkReset());
        test4.formulas = fs4;
    }

    @Test
    void addConjunct() {
        test1.addConjunct(new RawFormula("f3","val3"));
        assertEquals(List.of(
                new RawFormula("f1","val1"),
                new RawFormula("f2","val2"),
                new RawFormula("f3","val3")
        ),test1.formulas);
    }

    @Test
    void testToString() {
        assertEquals("val1 \\<and> val2",test1.toString());
        assertEquals("val1",test2.toString());
        assertEquals("val1",test3.toString());
        assertEquals("val1",test4.toString());
    }

    @Test
    void toStrings() {
        assertEquals(List.of("val1","val2"),test1.toStrings());
        assertEquals(List.of("val1"),test2.toStrings());
    }

    @Test
    void toNamedStrings() {
        assertEquals(List.of("f1:\"val1\"","f2:\"val2\""),test1.toNamedStrings());
        assertEquals(List.of("f1:\"val1\""),test2.toNamedStrings());
    }

    @Test
    void trim() {
    }

    @Test
    void toNamedString() {
        assertEquals("conj"+test1.formulas.hashCode()+":\"val1 \\<and> val2\"",test1.toNamedString());
        assertEquals("conj"+test2.formulas.hashCode()+":\"val1\"",test2.toNamedString());
    }

    @Test
    void trimByFormula() {
        test1.trimByFormula(new RawFormula("f1","val1"));
        assertEquals(List.of(new RawFormula("f1","val1")),test1.formulas);
    }

    @Test
    void isMarkedSetState() {
        assertFalse(test2.isMarkedSetState());
        assertTrue(test3.isMarkedSetState());
        assertTrue(test4.isMarkedSetState());
    }

    @Test
    void isMarkedReset() {
        assertFalse(test2.isMarkedReset());
        assertTrue(test3.isMarkedReset());
        assertFalse(test4.isMarkedReset());
    }


    @Test
    void peekLastConjunct() {
        assertEquals(new RawFormula("f2","val2"),test1.peekLastConjunct());
    }
}