package su.nsk.iae.reflex.StatementsCreator;

import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.ops.UnaryOp;
import su.nsk.iae.reflex.expression.types.ExprType;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ProgramMetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class IsabelleCreator implements IStatementCreator {
    ProgramMetaData metaData;
    int conditionCounter=0;

    public void setConditionCounter(int conditionCounter) {
        this.conditionCounter = conditionCounter;
    }

    public IsabelleCreator(){}

    @Override
    public String InputVarInitStatement(int stateNumber, String variableName) {
        String res = variableName.replace(PlaceHolder(),createStateName(stateNumber));
        res = createStateName(stateNumber+1)+":"+"\""+createStateName(stateNumber+1)+"="+res+"\"";
        return res;
    }

    @Override
    public String ExpressionStatement(int stateNumber, String value) {
        String res = value.replace(PlaceHolder(),createStateName(stateNumber));
        res = createStateName(stateNumber+1)+":"+"\""+createStateName(stateNumber+1)+"="+res+"\"";
        return res;
    }

    @Override
    public String ConditionStatement(int stateNumber, String value) {
        String res = value.replace(PlaceHolder(),createStateName(stateNumber));
        res = createStateName(stateNumber)+"_condition_"+conditionCounter+":"+"\""+res+"\"";
        conditionCounter++;
        return res;
    }

    @Override
    public String StateStatement(int stateNumber, String processName, String stateName) {
        return createStateName(stateNumber)+"_state:\""+ PstateGetter(createStateName(stateNumber),processName)+"="+IString(stateName)+"\"";
    }

    @Override
    public String SetStateStatement(int stateNumber, String processName, String stateName) {
        return createStateName(stateNumber+1)+":\""+createStateName(stateNumber+1)+"="+ PstateSetter(createStateName(stateNumber),processName,stateName)+"\"";
    }

    @Override
    public String StartProcessStatement(int stateNumber, String processName, String stateName) {
        return createStateName(stateNumber+1)+":\""+createStateName(stateNumber+1)+"="+"setPstate "+ PstateSetter(createStateName(stateNumber),processName,stateName)+"\"";
    }

    @Override
    public String StopProcessStatement(int stateNumber, String processName) {
        return createStateName(stateNumber+1)+":\""+createStateName(stateNumber+1)+"="+ PstateSetter(createStateName(stateNumber),processName,"stop")+"\"";
    }

    @Override
    public String ErrorProcessStatement(int stateNumber, String processName) {
        return createStateName(stateNumber+1)+":\""+createStateName(stateNumber+1)+"="+ PstateSetter(createStateName(stateNumber),processName,"error")+"\"";
    }

    @Override
    public String ResetStatement(int stateNumber, String processName) {
        return createStateName(stateNumber+1)+":\""+createStateName(stateNumber+1)+"="+"reset "+createStateName(stateNumber)+"\"";
    }

    @Override
    public String EnvStatement(int stateNumber) {
        return createStateName(stateNumber+1)+":\""+createStateName(stateNumber+1)+"="+"toEnv "+createStateName(stateNumber)+"\"";
    }

    @Override
    public String FinalStatement(int stateNumber) {
        return FinalStatementName()+":\""+ FinalStatementName()+"="+createStateName(stateNumber)+"\"";
    }

    @Override
    public String FinalStatementName(){
        return "st_final";
    }

    @Override
    public String EmptyStateStatement() {
        return createStateName(0)+":\""+createStateName(0)+"="+ EmptyState()+"\"";
    }

    @Override
    public String InitInvariantStatement() {
        return "base_inv:\""+ Invariant(createStateName(0))+"\"";
    }


    @Override
    public String ImplInvariantStatement(String state) {
        return "\""+ Invariant(state)+"\"";
    }

    @Override
    public String Lemma(String init, ArrayList<String> inter, String impl) {
        StringJoiner joiner = new StringJoiner("\n\tand ","lemma\nassumes ","\n");
        if(init!=null){
            joiner.add(init);
        }
        String holder = inter.get(0);
        for(String line :inter.subList(1,inter.size())){
            if(line.equals("")){
                holder+="\n";
            }else{
                joiner.add(holder);
                holder=line;
            }
        }
        joiner.add(holder);
        StringBuilder builder = new StringBuilder();
        builder.append(joiner);
        builder.append("shows ");
        builder.append(impl);
        return builder.toString();
    }

    @Override
    public String TimeoutExceed(String stateHolder, String condition, String processName) {
        return "(ltime "+stateHolder+" "+IString(processName)+" "+ getBinOp(BinaryOp.MoreEq)+" "+condition+")";

    }

    @Override
    public String TimeoutLose(String stateHolder, String condition, String processName) {
        return "(ltime "+stateHolder+" "+IString(processName)+" "+ getBinOp(BinaryOp.Less)+" "+condition+")";
    }

    @Override
    public String True() {
        return "True";
    }

    @Override
    public String False() {
        return "False";
    }

    @Override
    public String BinaryExpression(String left, String right, BinaryOp op) {
        return "("+left+" "+ getBinOp(op)+" "+right+")";
    }

    @Override
    public String UnaryExpression(String exp, ExprType type, UnaryOp op) {

        String res = switch(op){
            case Neg -> "\\<not>";
            case Plus -> "+";
            case Minus -> "-";
            case Invert -> type.invertBorder() + "-";
        };
        return "("+res+" "+exp+")";
    }

    @Override
    public String CastExpression(String exp, ExprType type) {
        return "("+type.toString(this)+" "+exp+")";
    }

    @Override
    public String Invariant(String state) {
        return "inv("+state+")";
    }

    @Override
    public String EmptyState() {
        return "emptyState";
    }




    @Override
    public String PstateSetter(String stateHolder, String processName, String stateName) {
        return "setPstate " + stateHolder +" "+IString(processName)+" "+IString(stateName);
    }

    @Override
    public String PstateGetter(String stateHolder, String processName) {
        return "getPstate " + stateHolder +" "+IString(processName);
    }

    public String Getter(ExprType type, String state, String variableName){
        return "("+type.takeGetter()+" "+state+" "+IString(variableName)+")";
    }
    public String Setter(ExprType type, String state, String variableName, String value){
        return "("+type.takeSetter()+" "+state+" "+IString(variableName)+" "+value+")";
    }

    String createStateName(int stateNumber){
        return "st"+stateNumber;
    }

    public String getBinOp(BinaryOp op){
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
    /*public String getUnOp(UnaryOp op){
        return switch (op);
        return null;
    }*/

    public String IString(String str){
        return "''"+str+"''";
    }

    public String PlaceHolder(){return "###";}

    @Override
    public String Substate(String state1, String state2) {
        return null;
    }

    @Override
    public String ToEnvNum(String state1, String state2) {
        return null;
    }

    @Override
    public String ToEnvP(String state) {
        return null;
    }

    @Override
    public String ForAll(List<String> states, String condition) {
        return null;
    }

    @Override
    public String Exist(List<String> states, String condition) {
        return null;
    }

    @Override
    public String Conjunction(List<String> args) {
        StringJoiner sj = new StringJoiner(" \\<and> ");
        args.forEach(arg->{if(arg!=null&&!arg.isEmpty())sj.add(arg);});
        return sj.toString();
    }

    @Override
    public String Disjunction(List<String> args) {
        StringJoiner sj = new StringJoiner(" \\<or> ");
        args.forEach(arg->{if(arg!=null&&!arg.isEmpty())sj.add(arg);});
        return sj.toString();
    }

    @Override
    public String Implication(String from, String to) {
        return from + " \\<longrightarrow> " + to;
    }

    @Override
    public String PartOfSet(String value, List<String> set) {
        StringJoiner sj = new StringJoiner(",");
        set.forEach(sj::add);
        return value + "\\<in>"+"{"+sj.toString()+"}";
    }

    @Override
    public String Definition(String name, List<String> variables, String definition) {
        StringJoiner builder = new StringJoiner("\n","","\n");
        builder.add("definition "+ name + " where");
        StringJoiner vars = new StringJoiner(" ");
        variables.forEach(vars::add);
        builder.add("\""+name+" "+vars.toString()+" =");
        builder.add(definition);
        builder.add("\"");
        return builder.toString();
    }

    @Override
    public String TypedDefinition(String name, List<Map.Entry<String, String>> variablesWithTypes, String definition) {
        //TODO
        return null;
    }

    @Override
    public String Theory(String name, List<String> imports, String inner) {
        StringJoiner builder = new StringJoiner("\n");
        builder.add("theory " +name);
        StringJoiner sj = new StringJoiner(" ");
        imports.forEach(sj::add);
        builder.add("\timports "+sj.toString());
        builder.add("begin");
        builder.add(inner);
        builder.add("end");
        return builder.toString();
    }
}
