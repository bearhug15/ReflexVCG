package su.nsk.iae.reflex.expression.types;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

public class StateType implements ExprType{
    String name;
    public StateType(String name){
        this.name = name;
    }
    @Override
    public String takeGetter() {
        throw new RuntimeException("Trying to create getter for state variable");
    }

    @Override
    public String takeSetter() {
        throw new RuntimeException("Trying to create setter for state variable");
    }

    @Override
    public String invertBorder() {
        throw new RuntimeException("Trying to invert state");
    }

    @Override
    public String defaultValue() {
        return "emptyState";
    }


    public String toString(IStatementCreator creator){
        return this.name;
    }
}
