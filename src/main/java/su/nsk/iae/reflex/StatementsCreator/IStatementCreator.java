package su.nsk.iae.reflex.StatementsCreator;

import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.ops.UnaryOp;
import su.nsk.iae.reflex.expression.types.ExprType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface IStatementCreator {

     String InputVarInitStatement(int stateNumber, String variableName);
     String ExpressionStatement(int stateNumber, String value);
     String ConditionStatement(int stateNumber, String value);
     String StateStatement(int stateNumber, String processName, String stateName);
     String SetStateStatement(int stateNumber, String processName, String stateName);
     String StartProcessStatement(int stateNumber, String processName, String stateName);
     String StopProcessStatement(int stateNumber, String processName);
     String ErrorProcessStatement(int stateNumber, String processName);
     String ResetStatement(int stateNumber, String processName);
     String EnvStatement(int stateNumber);
     String FinalStatement(int stateNumber);
     String FinalStatementName();
     String EmptyStateStatement();
     String InitInvariantStatement();
     String ImplInvariantStatement(String state);
     String Lemma(String init, ArrayList<String> inter, String impl);
     String TimeoutExceed(String stateHolder, String condition, String processName);
     String TimeoutLose(String stateHolder, String condition, String processName);

<<<<<<< HEAD
     String True();
     String False();
     String BinaryExpression(String left, String right, BinaryOp op);
     String UnaryExpression(String exp, ExprType type, UnaryOp op);
     String CastExpression(String exp, ExprType type);
     String Invariant(String state);
     String EmptyState();
=======
     String createTrue();
     String createFalse();
     String createBinaryExpression(String left, String right, BinaryOp op);
     String createUnaryExpression(String exp, ExprType type, UnaryOp op);
     String createCastExpression(String exp, ExprType type);
     String createInvariant(String state);
     String createEmptyState();
>>>>>>> 88a1068e3410f8636250832f5ed3206caf7ea7c7

     String Setter(ExprType type, String stateHolder, String variable, String value);
     String Getter(ExprType type, String stateHolder, String variable);
     String PstateSetter(String stateHolder, String processName, String stateName);
     String PstateGetter(String stateHolder, String processName);

     String PlaceHolder();

     String Substate(String state1, String state2);
     String ToEnvNum(String state1, String state2);

     String ToEnvP(String state);
     String ForAll(List<String> states, String condition);
     String Exist(List<String> states, String condition);

     String Conjunction(List<String> args);
     String Disjunction(List<String> args);
     String Implication(String from, String to);

     String PartOfSet(String value, List<String> set);
     String Definition(String name, List<String> variables, String definition);
     String TypedDefinition(String name, List<Map.Entry<String,String>> variablesWithTypes, String definition);
     String Theory(String name, List<String> imports, String inner);
}
