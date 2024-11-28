package su.nsk.iae.reflex.StatementsCreator;

import su.nsk.iae.reflex.expression.ops.BinaryOp;
import su.nsk.iae.reflex.expression.ops.UnaryOp;
import su.nsk.iae.reflex.expression.types.ExprType;

import java.util.ArrayList;

public interface IStatementCreator {

     String createInputVarInitStatement(int stateNumber, String variableName);
     String createExpressionStatement(int stateNumber, String value);
     String createConditionStatement(int stateNumber, String value);
     String createStateStatement(int stateNumber, String processName, String stateName);
     String createSetStateStatement(int stateNumber, String processName, String stateName);
     String createStartProcessStatement(int stateNumber, String processName,String stateName);
     String createStopProcessStatement(int stateNumber, String processName);
     String createErrorProcessStatement(int stateNumber, String processName);
     String createResetStatement(int stateNumber,String processName);
     String createEnvStatement(int stateNumber);
     String createFinalStatement(int stateNumber);
     String createFinalStatementName();
     String createEmptyStateStatement();
     String createInitInvariantStatement();
     String createImplInvariantStatement(String state);
     String createLemma(String init, ArrayList<String> inter, String impl);
     String createTimeoutExceed(String stateHolder,String condition, String processName);
     String createTimeoutLose(String stateHolder,String condition, String processName);

     String createTrue();
     String createFalse();
     String createBinaryExpression(String left, String right, BinaryOp op);
     String createUnaryExpression(String exp, ExprType type, UnaryOp op);
     String createCastExpression(String exp, ExprType type);
     String createInvariant(String state);
     String createEmptyState();

     String createSetter(ExprType type, String stateHolder, String variable, String value);
     String createGetter(ExprType type, String stateHolder, String variable);
     String createPstateSetter(String stateHolder, String processName, String stateName);
     String createPstateGetter(String stateHolder, String processName);

    String createPlaceHolder();


}
