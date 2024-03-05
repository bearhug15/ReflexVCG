package su.nsk.iae.reflex.vcg;

import org.antlr.v4.runtime.misc.Pair;
import su.nsk.iae.reflex.expression.types.ExprType;

import java.util.HashMap;
import java.util.HashSet;

public class VariableMapper {
    //В качестве ключа используеися пара из имени процесса и переменной, в качестве значения новое имя и тип
    HashMap<Pair<String,String>,Pair<String,ExprType>> variables;
    HashSet<String> globalVariables;
    HashMap<String,Pair<String,ExprType>> constants;

    HashMap<Pair<String,String>,Pair<String,ExprType>> enums;

    public VariableMapper() {
        variables = new HashMap<>();
        constants = new HashMap<>();
        enums = new HashMap<>();
        globalVariables = new HashSet<>();
    }

    public String mapVariable(String process, String variable){
        if (globalVariables.contains(variable)){
            return variables.get(new Pair<>("_",variable)).a;
        }else{
            return variables.get(new Pair<>(process,variable)).a;
        }

    }
    public ExprType variableType(String process,String variable){
        if (globalVariables.contains(variable)){
            return variables.get(new Pair<>("_",variable)).b;
        }else{
            return variables.get(new Pair<>(process,variable)).b;
        }
    }
    public boolean is_variable(String process,String variable){
        return variables.containsKey(new Pair<>(process,variable)) ||globalVariables.contains(variable);
    }
    public boolean is_const(String variable){
        return constants.containsKey(variable);
    }
    public boolean is_enum(String process,String variable){
        return enums.containsKey(new Pair<>(process,variable));
    }

    public boolean is_global(String process, String variable){
        return !variables.containsKey(new Pair<>(process,variable)) && globalVariables.contains(variable);
    }

    public String constantValue(String variable){
        return constants.get(variable).a;
    }
    public ExprType constantType(String variable){
        return constants.get(variable).b;
    }

    public void addVariable(String process,String variable,String variableName,ExprType type){

        variables.put(new Pair<>(process,variable),new Pair<>(variableName,type));
    }
    public void addGlobalVariable(String variable, String variableName,ExprType type){
        globalVariables.add(variable);
        addVariable("_",variable,variableName,type);
    }
    public void addConstant(String variable,String value,ExprType type){
        constants.put(variable,new Pair<>(value,type));
    }
    public void addEnum(String process,String variable,ExprType type){
        String variableName = "_e_"+variable;
        variables.put(new Pair<>(process,variable),new Pair<>(variableName,type));
    }
    public boolean is_global(String variable){
        return globalVariables.contains(variable);
    }
}
