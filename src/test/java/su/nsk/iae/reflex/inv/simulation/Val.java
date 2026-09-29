package su.nsk.iae.reflex.inv.simulation;

import su.nsk.iae.reflex.ir.IrType;
import su.nsk.iae.reflex.term.Terms;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ReflexBase's {@code val}, concretely: the value a state holds for a variable, with the
 * projections and access paths the theory defines. Kept in step with ReflexBase.thy by
 * hand - each method names the function it mirrors.
 */
sealed interface Val {

    record B(boolean value) implements Val {
    }

    record I(BigInteger value) implements Val {
    }

    record N(BigInteger value) implements Val {
    }

    record R(double value) implements Val {
    }

    record Struct(Map<String, Val> fields) implements Val {
    }

    record Arr(Map<BigInteger, Val> elements) implements Val {
    }

    enum Nil implements Val { NIL }

    /** An element of an access path: {@code AccessField} or {@code AccessIndex}. */
    sealed interface Access {
    }

    record Field(String name) implements Access {
    }

    record Index(BigInteger index) implements Access {
    }

    // ------------------------------------------------------------------ projections

    /** theBool. */
    static boolean theBool(Val v) {
        if (v instanceof B b) {
            return b.value();
        }
        if (v instanceof I i) {
            return i.value().signum() != 0;
        }
        if (v instanceof N n) {
            return n.value().signum() != 0;
        }
        return false;
    }

    /** theInt. */
    static BigInteger theInt(Val v) {
        if (v instanceof I i) {
            return i.value();
        }
        if (v instanceof N n) {
            return n.value();
        }
        if (v instanceof B b) {
            return b.value() ? BigInteger.ONE : BigInteger.ZERO;
        }
        return BigInteger.ZERO;
    }

    /** theNat. */
    static BigInteger theNat(Val v) {
        if (v instanceof N n) {
            return n.value();
        }
        if (v instanceof I i) {
            return i.value().max(BigInteger.ZERO);
        }
        if (v instanceof B b) {
            return b.value() ? BigInteger.ONE : BigInteger.ZERO;
        }
        return BigInteger.ZERO;
    }

    /** theReal. */
    static double theReal(Val v) {
        if (v instanceof R r) {
            return r.value();
        }
        if (v instanceof I i) {
            return i.value().doubleValue();
        }
        if (v instanceof N n) {
            return n.value().doubleValue();
        }
        return 0;
    }

    /** The projection a Reflex type is read through, as the renderer picks it. */
    static Object project(Val v, IrType type) {
        return switch (Terms.sortOf(type)) {
            case BOOL -> theBool(v);
            case INT -> theInt(v);
            case NAT -> theNat(v);
            case REAL -> theReal(v);
        };
    }

    /** The constructor a Reflex type is written through: ValBool, ValInt, ValNat, ValReal. */
    static Val wrap(Object value, IrType type) {
        return switch (Terms.sortOf(type)) {
            case BOOL -> new B((Boolean) value);
            case INT -> new I(asInteger(value));
            case NAT -> new N(asInteger(value));
            case REAL -> new R(value instanceof Double d ? d : asInteger(value).doubleValue());
        };
    }

    static BigInteger asInteger(Object value) {
        if (value instanceof BigInteger i) {
            return i;
        }
        if (value instanceof Boolean b) {
            return b ? BigInteger.ONE : BigInteger.ZERO;
        }
        if (value instanceof Double d) {
            return BigInteger.valueOf((long) Math.floor(d));
        }
        throw new IllegalArgumentException("not a number: " + value);
    }

    // ------------------------------------------------------------------ access paths

    /** defaultVal. */
    static Val defaultVal(Val v) {
        if (v instanceof B) {
            return new B(false);
        }
        if (v instanceof I) {
            return new I(BigInteger.ZERO);
        }
        if (v instanceof N) {
            return new N(BigInteger.ZERO);
        }
        if (v instanceof R) {
            return new R(0);
        }
        if (v instanceof Struct) {
            return new Struct(Map.of());
        }
        if (v instanceof Arr) {
            return new Arr(Map.of());
        }
        return Nil.NIL;
    }

    /** applyAccess. */
    static Val applyAccess(Val v, List<Access> path) {
        if (path.isEmpty()) {
            return v;
        }
        Access first = path.get(0);
        List<Access> rest = path.subList(1, path.size());
        if (v instanceof Struct s && first instanceof Field f) {
            return applyAccess(s.fields().getOrDefault(f.name(), Nil.NIL), rest);
        }
        if (v instanceof Arr a && first instanceof Index i) {
            return applyAccess(a.elements().getOrDefault(i.index(), Nil.NIL), rest);
        }
        return defaultVal(v);
    }

    /** updValPath. */
    static Val updValPath(Val v, List<Access> path, Val newVal) {
        if (path.isEmpty()) {
            return newVal;
        }
        Access first = path.get(0);
        List<Access> rest = path.subList(1, path.size());
        if (v instanceof Struct s && first instanceof Field f) {
            Map<String, Val> fields = new LinkedHashMap<>(s.fields());
            fields.put(f.name(), updValPath(s.fields().getOrDefault(f.name(), Nil.NIL), rest, newVal));
            return new Struct(fields);
        }
        if (v instanceof Arr a && first instanceof Index i) {
            Map<BigInteger, Val> elements = new LinkedHashMap<>(a.elements());
            elements.put(i.index(), updValPath(a.elements().getOrDefault(i.index(), Nil.NIL), rest, newVal));
            return new Arr(elements);
        }
        return newVal;
    }
}
