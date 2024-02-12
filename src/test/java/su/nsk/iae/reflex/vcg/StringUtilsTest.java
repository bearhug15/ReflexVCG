package su.nsk.iae.reflex.vcg;

import org.junit.jupiter.api.Test;
import su.nsk.iae.reflex.expression.types.BoolType;
import su.nsk.iae.reflex.expression.types.NatType;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    void constructGetter() {
        assertEquals("(getVarNat s0 ''var'')",StringUtils.constructGetter(new NatType(),"s0", "var"));
        assertEquals("(getVarBool s0 ''var'')",StringUtils.constructGetter(new BoolType(),"s0", "var"));
    }

    @Test
    void constructSetter() {
        assertEquals("(setVarNat s0 ''var'' 10)",StringUtils.constructSetter(new NatType(),"s0", "var","10"));
        assertEquals("(setVarBool s0 ''var'' True)",StringUtils.constructSetter(new BoolType(),"s0", "var","True"));
    }

    @Test
    void parseInteger() {
        assertEquals("3",StringUtils.parseInteger("3"));
        assertEquals("-11",StringUtils.parseInteger("-013"));
        assertEquals("15",StringUtils.parseInteger("0XF"));
    }

    @Test
    void parseHex() {
        assertEquals("10",StringUtils.parseHex("0xA"));
        assertEquals("-15",StringUtils.parseHex("-0Xf"));
    }

    @Test
    void parseOctal() {
        assertEquals("10",StringUtils.parseOctal("012"));
        assertEquals("-10",StringUtils.parseOctal("-012"));
    }

    @Test
    void parseInt() {
        assertEquals("+10",StringUtils.parseInt("+10"));
        assertEquals("2",StringUtils.parseInt("2"));
        assertEquals("-5",StringUtils.parseInt("-5"));
    }


    @Test
    void parseDecimalFloat() {
        assertEquals("0.0",StringUtils.parseFloat("0."));
        assertEquals("0.1",StringUtils.parseFloat("0.1"));
        assertEquals("1.1",StringUtils.parseFloat(".11e1"));
        assertEquals("0.11",StringUtils.parseFloat("1.1E-1"));
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
        assertEquals("True",StringUtils.parseBool("true"));
        assertEquals("False",StringUtils.parseBool("false"));
    }

    @Test
    void parseTime() {
        assertEquals(String.valueOf(24*60*60*1000),StringUtils.parseTime("0t1d"));
        assertEquals(String.valueOf(3*60*60*1000+60*1000+10),StringUtils.parseTime("0t3h1m10ms"));
        assertEquals(String.valueOf(3*1000+5),StringUtils.parseTime("0t3s5ms"));
    }
}