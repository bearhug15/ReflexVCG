package su.nsk.iae.reflex.formulas;

import su.nsk.iae.reflex.StatementsCreator.IStatementCreator;

import java.util.*;
import java.util.stream.Collectors;

public class ConjuctionFormula implements Formula{
    String name;
    LinkedList<Formula> formulas;

    public ConjuctionFormula(){
        name = "conj";
        formulas = new LinkedList<>();
    }

    public void addConjunct(Formula conjunct){
        this.formulas.add(conjunct);
    }

    @Override
    public String toString(IStatementCreator creator) {
        StringJoiner joiner= new StringJoiner(" \\<and> ");
        List<String> strings = toStrings(creator);
        for (String str: strings){
            joiner.add(str);
        }
        return joiner.toString();
    }

    @Override
    public List<String> toStrings(IStatementCreator creator) {

        return formulas.stream().filter(formula ->
                formula.getClass().equals(ConjuctionFormula.class)
                        || formula.getClass().equals(EqualityFormula.class)
                        || formula.getClass().equals(GreaterFormula.class)
                        || formula.getClass().equals(ImplicationFormula.class)
                        || formula.getClass().equals(RawFormula.class)
                        || formula.getClass().equals(StateFormula.class)).map(formula1 -> formula1.toString(creator)).toList();
    }

    @Override
    public List<String> toNamedStrings(IStatementCreator creator) {

        return formulas.stream()
                .filter(formula -> formula.getClass().equals(ConjuctionFormula.class)
                        || formula.getClass().equals(EqualityFormula.class)
                        || formula.getClass().equals(GreaterFormula.class)
                        || formula.getClass().equals(ImplicationFormula.class)
                        || formula.getClass().equals(RawFormula.class)
                        || formula.getClass().equals(StateFormula.class))
                .map(formula1 -> formula1.toNamedString(creator)).toList();
    }

    @Override
    public Formula trim() {
        LinkedList<Formula> trimmed = formulas.stream()
                .map(Formula::trim)
                .filter(formula -> !formula.getClass().equals(TrueFormula.class)).collect(Collectors.toCollection(LinkedList::new));
        if (trimmed.isEmpty()){
            return new TrueFormula();
        }else{
            formulas = trimmed;
            return this;
        }
    }

    public String toNamedString(IStatementCreator creator){
        StringJoiner join = new StringJoiner(" \\<and> ");
        for (String string: this.toStrings(creator))
            join.add(string);
        return name+formulas.hashCode() + ":\""+ join.toString() + "\"";
    }
    public void trimByFormula(Formula formula){
        if (formula == null){
            formulas = new LinkedList<>();
            return;
        }
        int idx = formulas.indexOf(formula);
        if (idx>-1) {
            formulas = new LinkedList<>(formulas.subList(0, idx + 1));
        } else{
            throw new RuntimeException("Formula from stack not found");
        }
    }
    public boolean isMarkedReset(){
        Iterator<Formula> iter = formulas.descendingIterator();
        while (iter.hasNext()){
            Formula formula = iter.next();
            if (formula instanceof MarkRestart){
                return true;
            }
            if (formula instanceof UnmarkReset){
                return false;
            }
        }
        return false;
    }

    public Formula peekLastConjunct(){
        return formulas.peekLast();
    }
}
