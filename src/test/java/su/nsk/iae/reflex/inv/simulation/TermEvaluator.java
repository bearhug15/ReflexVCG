package su.nsk.iae.reflex.inv.simulation;

import su.nsk.iae.reflex.term.Term;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a {@link Term} - an invariant's formula - on a concrete run: the Isabelle functions
 * it mentions are answered by {@link History}, and a {@link Term.Expr} by running the
 * program expression it carries.
 *
 * <p>Covers what the structural invariants are written with. The quantifiers and Hilbert
 * choice the annotation translator produces are not needed for them, and are refused
 * rather than guessed at.
 */
final class TermEvaluator {

    private final long clock;

    TermEvaluator(long clock) {
        this.clock = clock;
    }

    boolean holds(Term formula, String stateName, History.Node state) {
        Map<String, Object> env = new HashMap<>();
        env.put(stateName, state);
        return (Boolean) evaluate(formula, env);
    }

    Object evaluate(Term term, Map<String, Object> env) {
        if (term instanceof Term.Var var) {
            return variable(var.name(), env);
        }
        if (term instanceof Term.Quoted quoted) {
            return quoted.text();
        }
        if (term instanceof Term.Expr expr) {
            return Machine.evaluate(expr.expression(), (History.Node) env.get(expr.state()));
        }
        if (term instanceof Term.Let let) {
            Map<String, Object> inner = new HashMap<>(env);
            inner.put(let.variable(), evaluate(let.value(), env));
            return evaluate(let.body(), inner);
        }
        if (term instanceof Term.Prefix prefix) {
            Object operand = evaluate(prefix.operand(), env);
            return switch (prefix.operator()) {
                case "\\<not>" -> !(Boolean) operand;
                case "-" -> Val.asInteger(operand).negate();
                default -> throw new UnsupportedOperationException(prefix.operator());
            };
        }
        if (term instanceof Term.Infix infix) {
            return infix(infix, env);
        }
        if (term instanceof Term.App app) {
            return application(app, env);
        }
        if (term instanceof Term.ListTerm list) {
            if (!list.elements().isEmpty()) {
                throw new UnsupportedOperationException("non-empty access path");
            }
            return List.of();
        }
        throw new UnsupportedOperationException(term.getClass().getSimpleName());
    }

    private Object variable(String name, Map<String, Object> env) {
        if (env.containsKey(name)) {
            return env.get(name);
        }
        return switch (name) {
            case "True" -> true;
            case "False" -> false;
            case "emptyState" -> null;
            default -> new BigInteger(name);
        };
    }

    private Object infix(Term.Infix infix, Map<String, Object> env) {
        String op = infix.operator();
        if (op.equals("\\<and>")) {
            return (Boolean) evaluate(infix.left(), env) && (Boolean) evaluate(infix.right(), env);
        }
        if (op.equals("\\<or>")) {
            return (Boolean) evaluate(infix.left(), env) || (Boolean) evaluate(infix.right(), env);
        }
        if (op.equals("\\<longrightarrow>")) {
            return !(Boolean) evaluate(infix.left(), env) || (Boolean) evaluate(infix.right(), env);
        }
        Object left = evaluate(infix.left(), env);
        Object right = evaluate(infix.right(), env);
        return switch (op) {
            case "=" -> same(left, right);
            case "\\<noteq>" -> !same(left, right);
            case "<" -> Val.asInteger(left).compareTo(Val.asInteger(right)) < 0;
            case "\\<le>" -> Val.asInteger(left).compareTo(Val.asInteger(right)) <= 0;
            case ">" -> Val.asInteger(left).compareTo(Val.asInteger(right)) > 0;
            case "\\<ge>" -> Val.asInteger(left).compareTo(Val.asInteger(right)) >= 0;
            case "+" -> Val.asInteger(left).add(Val.asInteger(right));
            case "*" -> Val.asInteger(left).multiply(Val.asInteger(right));
            default -> throw new UnsupportedOperationException(op);
        };
    }

    private static boolean same(Object left, Object right) {
        if (left instanceof BigInteger || right instanceof BigInteger) {
            if (left instanceof Double || right instanceof Double) {
                return Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue()) == 0;
            }
            return Val.asInteger(left).equals(Val.asInteger(right));
        }
        return java.util.Objects.equals(left, right);
    }

    private Object application(Term.App app, Map<String, Object> env) {
        List<Term> args = app.arguments();
        return switch (app.function()) {
            case "getPstate" -> state(args.get(0), env).getPstate((String) evaluate(args.get(1), env));
            case "getVarVal" -> state(args.get(0), env).getVarVal((String) evaluate(args.get(1), env), List.of());
            case "theBool" -> Val.theBool((Val) evaluate(args.get(0), env));
            case "theInt" -> Val.theInt((Val) evaluate(args.get(0), env));
            case "theNat" -> Val.theNat((Val) evaluate(args.get(0), env));
            case "theReal" -> Val.theReal((Val) evaluate(args.get(0), env));
            case "toEnvP" -> state(args.get(0), env).toEnvP();
            case "predEnv" -> state(args.get(0), env).predEnv();
            case "prevProcState" -> state(args.get(0), env).prevProcState((String) evaluate(args.get(1), env));
            case "ltime" -> state(args.get(0), env).ltime((String) evaluate(args.get(1), env), clock);
            case "toEnvNum" -> {
                if (evaluate(args.get(0), env) != null) {
                    throw new UnsupportedOperationException("toEnvNum from anything but emptyState");
                }
                yield BigInteger.valueOf(state(args.get(1), env).boundariesSoFar());
            }
            default -> throw new UnsupportedOperationException(app.function());
        };
    }

    private History.Node state(Term term, Map<String, Object> env) {
        Object value = evaluate(term, env);
        if (!(value instanceof History.Node node)) {
            throw new UnsupportedOperationException("not a state: " + term);
        }
        return node;
    }
}
