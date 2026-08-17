package su.nsk.iae.reflex.ir;

import java.util.List;
import java.util.Objects;

/**
 * Types in the Reflex IR.
 *
 * <p>Two kinds of type exist that a source program never spells out. {@link Named} is a
 * reference to a struct or enum whose declaration has not been resolved yet, produced by
 * AST lowering and replaced by {@link Struct} or {@link Enum} during resolution.
 * {@link Undefined} is the type of a bare literal: the cast-insertion pass of
 * Preprocessing.tex gives integer and float literals a type that adapts to their context
 * rather than forcing an early commitment, so {@code x + 1} does not widen {@code x}.
 */
public sealed interface IrType
        permits IrType.Builtin, IrType.Named, IrType.Struct, IrType.Enum, IrType.Array, IrType.Undefined {

    /** Builtin scalar kinds, in the widening order used by orderIntTypes. */
    enum BuiltinKind {
        VOID("void"),
        BOOL("bool"),
        INT8("int8"), UINT8("uint8"),
        INT16("int16"), UINT16("uint16"),
        INT32("int32"), UINT32("uint32"),
        INT64("int64"), UINT64("uint64"),
        TIME("time"),
        FLOAT("float"), DOUBLE("double");

        private final String keyword;

        BuiltinKind(String keyword) {
            this.keyword = keyword;
        }

        public String keyword() {
            return keyword;
        }

        public static BuiltinKind fromKeyword(String keyword) {
            for (BuiltinKind kind : values()) {
                if (kind.keyword.equals(keyword)) {
                    return kind;
                }
            }
            throw new IllegalArgumentException("Not a builtin Reflex type: " + keyword);
        }
    }

    /** The adaptive types carried by literals until the cast pass fixes them. */
    enum UndefinedKind { INT, FLOAT }

    record Builtin(BuiltinKind kind) implements IrType {
        public Builtin {
            Objects.requireNonNull(kind);
        }

        @Override
        public String toString() {
            return kind.keyword();
        }
    }

    /** An as-yet unresolved reference to a declared struct or enum. */
    record Named(String name) implements IrType {
        @Override
        public String toString() {
            return name;
        }
    }

    record Struct(String name) implements IrType {
        @Override
        public String toString() {
            return "struct " + name;
        }
    }

    record Enum(String name) implements IrType {
        @Override
        public String toString() {
            return "enum " + name;
        }
    }

    /** {@code size} is null when the extent is to be taken from an initialiser. */
    record Array(IrType element, Integer size) implements IrType {
        @Override
        public String toString() {
            return element + "[" + (size == null ? "" : size) + "]";
        }
    }

    record Undefined(UndefinedKind kind) implements IrType {
        @Override
        public String toString() {
            return kind == UndefinedKind.INT ? "<int literal>" : "<float literal>";
        }
    }

    // ------------------------------------------------------------------ helpers

    IrType VOID = new Builtin(BuiltinKind.VOID);
    IrType BOOL = new Builtin(BuiltinKind.BOOL);
    IrType INT32 = new Builtin(BuiltinKind.INT32);
    IrType TIME = new Builtin(BuiltinKind.TIME);
    IrType UNDEFINED_INT = new Undefined(UndefinedKind.INT);
    IrType UNDEFINED_FLOAT = new Undefined(UndefinedKind.FLOAT);

    /**
     * Integer types in widening order. bool participates because Reflex, like C, promotes
     * it in arithmetic; time and the float types are deliberately absent.
     */
    List<BuiltinKind> INTEGER_ORDER = List.of(
            BuiltinKind.BOOL,
            BuiltinKind.INT8, BuiltinKind.UINT8,
            BuiltinKind.INT16, BuiltinKind.UINT16,
            BuiltinKind.INT32, BuiltinKind.UINT32,
            BuiltinKind.INT64, BuiltinKind.UINT64);

    static IrType of(BuiltinKind kind) {
        return new Builtin(kind);
    }

    default boolean isBuiltin(BuiltinKind kind) {
        return this instanceof Builtin b && b.kind() == kind;
    }

    default boolean isUndefined() {
        return this instanceof Undefined;
    }

    default boolean isBool() {
        return isBuiltin(BuiltinKind.BOOL);
    }

    default boolean isFloating() {
        return this instanceof Builtin b
                && (b.kind() == BuiltinKind.FLOAT || b.kind() == BuiltinKind.DOUBLE);
    }

    /** True for the types that take part in integer widening (see INTEGER_ORDER). */
    default boolean isInteger() {
        return this instanceof Builtin b && INTEGER_ORDER.contains(b.kind());
    }

    /**
     * Position in the widening order, or -1 for a type that does not participate.
     * This is the {@code orderIntTypes} comparison of Preprocessing.tex.
     */
    default int integerRank() {
        return this instanceof Builtin b ? INTEGER_ORDER.indexOf(b.kind()) : -1;
    }
}
