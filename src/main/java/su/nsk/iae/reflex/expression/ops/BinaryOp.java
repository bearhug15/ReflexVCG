package su.nsk.iae.reflex.expression.ops;

import su.nsk.iae.reflex.expression.types.ExprType;

public enum BinaryOp {
    Sum("+"),Sub("-"),Mul("*"),Div("/"),Mod("mod"),
    BitAnd("bit&"),BitOr("bit|"),BitXor("bit\\<oplus>"),BitRShift(">>"),BitLShift("<<"),
    And("\\<and>"),Or("\\<or>"),Eq("="),NotEq("\\<noteq>"),
    More(">"),MoreEq("\\<ge>"),Less("<"),LessEq("\\<le>"),
    Implication("\\<longrightarrow>");

    final String op;

    BinaryOp(String op){
        this.op = op;
    }

    public String toString(){
        return op;
    }

    public static BinaryOp defineOp(String op){
        switch (op){
            case "+":return BinaryOp.Sum;
            case "-":return BinaryOp.Sub;
            case "*":return BinaryOp.Mul;
            case "/":return BinaryOp.Div;
            case "|":return BinaryOp.BitOr;
            case "&":return BinaryOp.BitAnd;
            case "^":return BinaryOp.BitXor;
            case ">>":return BinaryOp.BitRShift;
            case "<<":return BinaryOp.BitLShift;
            case "||":return BinaryOp.Or;
            case "&&":return BinaryOp.And;
            case "==":return BinaryOp.Eq;
            case "!=":return BinaryOp.NotEq;
            case ">":return BinaryOp.More;
            case "<":return BinaryOp.Less;
            case ">=":return BinaryOp.MoreEq;
            case "<=":return BinaryOp.LessEq;
        }
        throw new RuntimeException("Unknown operation "+op);

    }

    public static BinaryOp getAssignSubOp(String op){

        return defineOp(op.replace("=",""));
    }
}
