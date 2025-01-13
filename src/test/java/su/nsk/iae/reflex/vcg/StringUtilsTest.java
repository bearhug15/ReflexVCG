package su.nsk.iae.reflex.vcg;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ASTGraphProjection;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void parseInteger() {
        assertEquals("3", ASTGraphProjection.ValueParser.parseInteger("3"));
        assertEquals("-11", ASTGraphProjection.ValueParser.parseInteger("-013"));
        assertEquals("15", ASTGraphProjection.ValueParser.parseInteger("0XF"));
    }

    @Test
    void parseHex() {
        assertEquals("10", ASTGraphProjection.ValueParser.parseHex("0xA"));
        assertEquals("-15", ASTGraphProjection.ValueParser.parseHex("-0Xf"));
    }

    @Test
    void parseOctal() {
        assertEquals("10", ASTGraphProjection.ValueParser.parseOctal("012"));
        assertEquals("-10", ASTGraphProjection.ValueParser.parseOctal("-012"));
    }

    @Test
    void parseInt() {
        assertEquals("+10", ASTGraphProjection.ValueParser.parseInt("+10"));
        assertEquals("2", ASTGraphProjection.ValueParser.parseInt("2"));
        assertEquals("-5", ASTGraphProjection.ValueParser.parseInt("-5"));
    }


    @Test
    void parseDecimalFloat() {
        assertEquals("0.0", ASTGraphProjection.ValueParser.parseFloat("0."));
        assertEquals("0.1", ASTGraphProjection.ValueParser.parseFloat("0.1"));
        assertEquals("1.1", ASTGraphProjection.ValueParser.parseFloat(".11e1"));
        assertEquals("0.11", ASTGraphProjection.ValueParser.parseFloat("1.1E-1"));
    }

    @Test
    void parseHexFloat() {
        //TODO
        /*assertEquals("0.0",StringUtils.parseFloat("0x0."));
        assertEquals("0.1",StringUtils.parseFloat("0x0.1"));
        assertEquals("1.1",StringUtils.parseFloat("0x.11p1"));
        assertEquals("0.11",StringUtils.parseFloat("1.1P-1"));*/
    }

    @Test
    void parseBool() {
        assertEquals("True", ASTGraphProjection.ValueParser.parseBool("true"));
        assertEquals("False", ASTGraphProjection.ValueParser.parseBool("false"));
    }

    @Test
    void parseTime() {
        assertEquals(String.valueOf(24*60*60*1000), ASTGraphProjection.ValueParser.parseTime("0t1d"));
        assertEquals(String.valueOf(3*60*60*1000+60*1000+10), ASTGraphProjection.ValueParser.parseTime("0t3h1m10ms"));
        assertEquals(String.valueOf(3*60*60*1000+60*1000+3*1000+10), ASTGraphProjection.ValueParser.parseTime("0t3h1m3s10ms"));
        assertEquals(String.valueOf(3*1000+5), ASTGraphProjection.ValueParser.parseTime("0t3s5ms"));
    }
}