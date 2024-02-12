package su.nsk.iae.reflex.expression.types;

import su.nsk.iae.reflex.expression.ops.BinaryOp;

public class TypeUtils {
    public static ExprType castType(ExprType type1, ExprType type2, BinaryOp op){
        if(!isPossible(type1,type2,op)){
            throw new RuntimeException("Operation "+op+" not possible on args with types: "+type1+" "+type2);
        }
        if (type1 instanceof DoubleType){
            return type1;
        }
        if (type2 instanceof DoubleType){
            return type2;
        }
        if (type1 instanceof FloatType){
            return type1;
        }
        if (type2 instanceof FloatType){
            return type2;
        }
        if (type1 instanceof IntType){
            return type1;
        }
        if (type2 instanceof IntType){
            return type2;
        }
        if (type1 instanceof NatType){
            return type1;
        }
        if (type2 instanceof NatType){
            return type2;
        }
        return type1;
    }
    public static ExprType resultType(ExprType type1, ExprType type2, BinaryOp op){
        return switch (op){
            case And,Or,Eq,NotEq,More,MoreEq,Less,LessEq -> new BoolType();
            default -> castType(type1,type2,op);
        };
    }
    public static ExprType defineType(String type){
        return switch (type) {
            case "void" -> new VoidType();
            case "bool" -> new BoolType();
            case "time" -> new TimeType();
            case "float" -> new FloatType();
            case "double" -> new DoubleType();
            case "int8" -> new Int8Type();
            case "uint8" -> new UInt8Type();
            case "int16" -> new Int16Type();
            case "uint16" -> new UInt16Type();
            case "int32" -> new Int32Type();
            case "uint32" -> new UInt32Type();
            case "int64" -> new Int64Type();
            case "uint64" -> new UInt64Type();
            default -> null;
        };
    }

    public static boolean isPossible(ExprType type1, ExprType type2, BinaryOp op){
        return switch (op){
            case Mod,BitLShift,BitRShift,BitOr,BitAnd,BitXor ->
                    (type1 instanceof NatType || type1 instanceof IntType)
                    && (type2 instanceof NatType || type2 instanceof IntType);
            case And,Or -> (type1 instanceof BoolType) && (type2 instanceof BoolType);
            case Eq,NotEq -> (type1 instanceof BoolType) && (type2 instanceof BoolType)
                    || !(type1 instanceof BoolType) && !(type2 instanceof BoolType);
            default -> !(type1 instanceof BoolType) && !(type2 instanceof BoolType);
        };

    }
}
