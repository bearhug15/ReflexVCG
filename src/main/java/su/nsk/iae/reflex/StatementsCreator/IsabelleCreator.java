package su.nsk.iae.reflex.StatementsCreator;

import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.ops.UnaryOp;
import su.nsk.iae.reflex.expression.types.ExprType;
import su.nsk.iae.reflex.vcg.ProgramMetaData;

import java.util.ArrayList;
import java.util.StringJoiner;

public class IsabelleCreator implements IStatementCreator {
    ProgramMetaData metaData;
    public IsabelleCreator(){}

    @Override
    public String createInputVarInitStatement(int stateNumber, String variableName) {
        String res = variableName.replace(createPlaceHolder(),createStateName(stateNumber));
        res = createStateName(stateNumber+1)+":"+"\""+createStateName(stateNumber+1)+"="+res+"\"";
        return res;
    }

    @Override
    public String createExpressionStatement(int stateNumber, String value) {
        String res = value.replace(createPlaceHolder(),createStateName(stateNumber));
        res = createStateName(stateNumber+1)+":"+"\""+createStateName(stateNumber+1)+"="+res+"\"";
        return res;
    }

    @Override
    public String createConditionStatement(int stateNumber, String value) {
        String res = value.replace(createPlaceHolder(),createStateName(stateNumber));
        res = createStateName(stateNumber)+"_condition:"+"\""+res+"\"";
        return res;
    }

    @Override
    public String createStateStatement(int stateNumber, String processName, String stateName) {
        return createStateName(stateNumber)+"_state:"+"\""+"getPstate "+createStateName(stateNumber)+"''"+processName+"'' = ''"+stateName+"''\"";
    }

    @Override
    public String createSetStateStatement(int stateNumber, String processName, String stateName) {
        return createStateName(stateNumber+1)+":"+"\""+createStateName(stateNumber+1)+"="+"setPstate "+createStateName(stateNumber)+"''"+processName+"'' = ''"+stateName+"''\"";
    }

    @Override
    public String createStartProcessStatement(int stateNumber, String processName, String stateName) {
        return createStateName(stateNumber+1)+":"+"\""+createStateName(stateNumber+1)+"="+"setPstate "+createStateName(stateNumber)+"''"+processName+"'' = ''"+stateName+"''\"";
    }

    @Override
    public String createStopProcessStatement(int stateNumber, String processName) {
        return createStateName(stateNumber+1)+":"+"\""+createStateName(stateNumber+1)+"="+"setPstate "+createStateName(stateNumber)+"''"+processName+"'' = ''stop''\"";
    }

    @Override
    public String createErrorProcessStatement(int stateNumber, String processName) {
        return createStateName(stateNumber+1)+":"+"\""+createStateName(stateNumber+1)+"="+"setPstate "+createStateName(stateNumber)+"''"+processName+"'' = ''error''\"";
    }

    @Override
    public String createResetStatement(int stateNumber, String processName) {
        return createStateName(stateNumber+1)+":"+"\""+createStateName(stateNumber+1)+"="+"reset "+createStateName(stateNumber)+"\"";
    }

    @Override
    public String createFinalStatement(int stateNumber) {
        return createFinalStatementName()+"\""+createFinalStatementName()+"="+createStateName(stateNumber)+"\"";
    }

    @Override
    public String createFinalStatementName(){
        return "st_final";
    }

    @Override
    public String createInitInvariantStatement() {
        return "base_inv:\""+createInvariant(createStateName(0))+"\"";
    }


    @Override
    public String createImplInvariantStatement(String state) {
        return "\""+createInvariant(state)+"\"";
    }

    @Override
    public String createLemma(String init, ArrayList<String> inter, String impl) {
        StringJoiner joiner = new StringJoiner("\n\tand ","lemma\n","\n");
        if(init!=null){
            joiner.add(init);
        }
        for(String line :inter){
            joiner.add(line);
        }
        StringBuilder builder = new StringBuilder();
        builder.append(joiner);
        builder.append("shows ");
        builder.append(impl);
        return builder.toString();
    }

    @Override
    public String createTimeoutExceed(String stateHolder, String condition, String processName) {
        return "(ltime "+stateHolder+" "+IString(processName)+" "+getBiOp(BinaryOp.MoreEq)+" "+condition+")";

    }

    @Override
    public String createTimeoutLose(String stateHolder, String condition, String processName) {
        return "(ltime "+stateHolder+" "+IString(processName)+" "+getBiOp(BinaryOp.Less)+" "+condition+")";
    }

    @Override
    public String createTrue() {
        return "True";
    }

    @Override
    public String createFalse() {
        return "False";
    }

    @Override
    public String createBinaryExpression(String left, String right, BinaryOp op) {
        return "("+left+getBiOp(op)+right+")";
    }

    @Override
    public String createUnaryExpression(String exp, UnaryOp op) {
        return "("+getUnOp(op)+exp+")";
    }

    @Override
    public String createInvariant(String state) {
        return "inv("+state+")";
    }


    public String createGetter(ExprType type, String state, String variable){
        return "("+type.takeGetter()+" "+state+" ''"+variable+"'')";
    }
    public String createSetter(ExprType type, String state, String variable, String value){
        return "("+type.takeSetter()+" "+state+" ''"+variable+"'' "+value+")";
    }

    String createStateName(int stateNumber){
        return "st"+stateNumber;
    }

    public String getBiOp(BinaryOp op){
        return switch (op){
            case Sum ->"+";
            case Sub -> "-";
            case Mul -> "*";
            case Div -> "div";
            case Mod -> "mod";
            case BitAnd -> "bit&";
            case BitOr -> "bit|";
            case BitXor -> "bit\\<oplus>";
            case BitRShift -> ">>";
            case BitLShift -> "<<";
            case And -> "\\<and>";
            case Or -> "\\<or>";
            case Eq -> "=";
            case NotEq -> "\\<noteq>";
            case More -> ">";
            case MoreEq -> "\\<ge>";
            case Less -> "<";
            case LessEq -> "\\<le>";
            case Implication -> "\\<longrightarrow>";

        };
    }
    public String getUnOp(UnaryOp op){
        return null;
    }

    public String IString(String str){
        return "''"+str+"''";
    }

    public String createPlaceHolder(){return "###";}
}
