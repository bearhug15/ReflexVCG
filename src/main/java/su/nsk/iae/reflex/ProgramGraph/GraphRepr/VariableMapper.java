package su.nsk.iae.reflex.ProgramGraph.GraphRepr;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Pair;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ExprGenRes;
import su.nsk.iae.reflex.ProgramGraph.GraphRepr.ExpressionVisitor;
import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;
import su.nsk.iae.reflex.antlr.ReflexParser;
import su.nsk.iae.reflex.expression.types.ExprType;
import su.nsk.iae.reflex.expression.types.TypeUtils;

import java.util.HashMap;
import java.util.HashSet;

public class VariableMapper {
    //В качестве ключа используеися пара из имени процесса и переменной, в качестве значения новое имя и тип
    HashMap<Pair<String,String>,Pair<String,ExprType>> variables;
    HashSet<String> globalVariables;
    HashMap<String,Pair<String,ExprType>> constants;

    HashMap<Pair<String,String>,Pair<String,ExprType>> enums;
    IStatementCreator creator;
    public VariableMapper(ReflexParser.ProgramContext ctx, IStatementCreator creator) {
        variables = new HashMap<>();
        constants = new HashMap<>();
        enums = new HashMap<>();
        globalVariables = new HashSet<>();
        this.creator = creator;

        prepareVariableMapper(ctx,0);
    }

    public void prepareVariableMapper(ReflexParser.ProgramContext ctx,int stateCount){
        for (ReflexParser.GlobalVariableContext variable: ctx.globalVars){
            if(variable.programVariable()!=null){
                ReflexParser.ProgramVariableContext progVariable = variable.programVariable();
                String name = progVariable.name.getText();
                this.addGlobalVariable(name,"_"+name, TypeUtils.defineType(progVariable.varType.getText()));
            }else{
                ReflexParser.PhysicalVariableContext physVariable = variable.physicalVariable();
                String name = physVariable.name.getText();
                ReflexParser.PortMappingContext pmap = physVariable.mapping;
                String portId = pmap.portId.getText();
                String qualName = portId;
                if (pmap.bit!=null){
                    qualName += "_"+pmap.bit.getText();
                }
                this.addGlobalVariable(
                        name,
                        qualName,
                        TypeUtils.defineType(physVariable.varType.getText()));
            }
        }
        for (ReflexParser.ConstContext con: ctx.consts){
            ExpressionVisitor vis = new ExpressionVisitor(this,"_",stateName(stateCount),creator);
            ExprGenRes res = vis.visitExpression(con.expression());
            this.addConstant(
                    con.name.getText(),
                    res.getExpr().toString(creator),
                    TypeUtils.defineType(con.varType.getText()));
        }

        for (ReflexParser.ProcessContext process: ctx.processes){
            for(ReflexParser.ProcessVariableContext variable: process.variables){
                String processName = process.name.getText();
                if(variable.programVariable()!=null){
                    ReflexParser.ProgramVariableContext progVariable = variable.programVariable();
                    String name = progVariable.name.getText();
                    this.addVariable(
                            processName,
                            name,
                            constructProcessVariableName(processName,name),
                            TypeUtils.defineType(progVariable.varType.getText()));
                }else{
                    ReflexParser.PhysicalVariableContext physVariable = variable.physicalVariable();
                    String name = physVariable.name.getText();
                    ReflexParser.PortMappingContext pmap = physVariable.mapping;
                    String portId = pmap.portId.getText();
                    String qualName = portId;
                    if (pmap.bit!=null){
                        qualName += "_"+pmap.bit.getText();
                    }
                    this.addVariable(
                            processName,
                            name,
                            qualName,
                            TypeUtils.defineType(physVariable.varType.getText()));
                }
            }
        }

        for (ReflexParser.ProcessContext process: ctx.processes){
            for(ReflexParser.ImportedVariableListContext imported: process.imports){
                String providerProcess = imported.processID.getText();
                String acceptorProcess = process.name.getText();
                for (Token id: imported.variables){
                    String name=id.getText();
                    this.addVariable(
                            acceptorProcess,
                            name,
                            this.mapVariable(providerProcess,name),
                            this.variableType(providerProcess,name));
                }
            }
        }
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

    private String constructProcessVariableName(String processName,String variable){
        return "_p_"+processName+"_v_"+variable;
    }

    private String stateName(int stateCount){
        return "st"+stateCount;
    }

}
