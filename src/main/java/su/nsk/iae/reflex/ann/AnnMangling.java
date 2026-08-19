package su.nsk.iae.reflex.ann;

import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Rewrites the variable names inside annotations to their mangled form.
 *
 * <p>Annotations are subject to the same scoping as the code they are attached to: an
 * annotation on a state that names {@code x} means that state's {@code x}. The translation
 * to Isabelle reads a variable with {@code getVarVal} under its final name, so this has to
 * have happened before it runs.
 *
 * <p>Driven by {@link su.nsk.iae.reflex.preprocess.NameManglingPass}, which calls
 * {@link #mangle} at each construct with the variable map that is in scope there. Three
 * kinds of name are left alone:
 * <ul>
 *   <li>names the annotation already wrote qualified, {@code node#process#x} - the
 *       annotation grammar spells qualification with the same separator, so such a name is
 *       already final;</li>
 *   <li>variables bound by a quantifier, and the formal parameters of a {@code define},
 *       which are the annotation's own names rather than the program's;</li>
 *   <li>process and state names, which live in their own namespace.</li>
 * </ul>
 */
public final class AnnMangling {

    /** Names bound by an enclosing quantifier or definition, innermost scope last. */
    private final Deque<Set<String>> bound = new ArrayDeque<>();
    private final List<String> unresolved = new ArrayList<>();

    /** Names an annotation referred to that no scope declares. */
    public List<String> getUnresolved() {
        return unresolved;
    }

    /**
     * Rewrites every annotation attached to {@code node}.
     *
     * @param variableMap original name to mangled name, as in scope at that construct
     */
    public void mangle(IrNode node, Map<String, String> variableMap) {
        for (Annotation annotation : node.getAnnotations()) {
            mangle(annotation, variableMap);
        }
    }

    public void mangle(Annotation annotation, Map<String, String> variableMap) {
        if (annotation.isForeignLanguage()) {
            // Passed through unparsed; there is no tree to rewrite.
            return;
        }
        for (AnnDefinition definition : annotation.getDefinitions()) {
            bound.push(new HashSet<>(definition.parameters()));
            rewrite(definition.body(), variableMap);
            bound.pop();
        }
        if (annotation.getBody() != null) {
            rewrite(annotation.getBody(), variableMap);
        }
    }

    // ------------------------------------------------------------------ rewriting

    private void rewrite(AnnExpr expr, Map<String, String> variableMap) {
        if (expr == null) {
            return;
        }
        if (expr instanceof AnnExpr.VarRef ref) {
            rewriteVarRef(ref, variableMap);
        } else if (expr instanceof AnnExpr.Binary binary) {
            rewrite(binary.getLeft(), variableMap);
            rewrite(binary.getRight(), variableMap);
        } else if (expr instanceof AnnExpr.Unary unary) {
            rewrite(unary.getOperand(), variableMap);
        } else if (expr instanceof AnnExpr.Implication implication) {
            rewrite(implication.getLeft(), variableMap);
            rewrite(implication.getRight(), variableMap);
        } else if (expr instanceof AnnExpr.Equivalence equivalence) {
            rewrite(equivalence.getLeft(), variableMap);
            rewrite(equivalence.getRight(), variableMap);
        } else if (expr instanceof AnnExpr.Quantifier quantifier) {
            rewriteQuantifier(quantifier, variableMap);
        } else if (expr instanceof AnnExpr.Call call) {
            // The callee is a definition's name, not a variable; only arguments are rewritten.
            call.getArguments().forEach(argument -> rewrite(argument, variableMap));
        } else if (expr instanceof AnnExpr.Temporal temporal) {
            rewrite(temporal.getFirst(), variableMap);
            rewrite(temporal.getSecond(), variableMap);
            rewrite(temporal.getThird(), variableMap);
        } else if (expr instanceof AnnExpr.Scope scope) {
            rewrite(scope.getBase(), variableMap);
            rewrite(scope.getPhi(), variableMap);
        }
        // Literals, in(...) and time(...) name no variables.
    }

    private void rewriteQuantifier(AnnExpr.Quantifier quantifier, Map<String, String> variableMap) {
        Set<String> names = new HashSet<>();
        for (AnnExpr.BoundVar variable : quantifier.getVariables()) {
            // A domain is read in the enclosing scope, before its own variable is bound.
            rewriteDomain(variable.getDomain(), variableMap);
            names.add(variable.getName());
        }
        bound.push(names);
        rewrite(quantifier.getBody(), variableMap);
        bound.pop();
    }

    private void rewriteDomain(AnnExpr.Domain domain, Map<String, String> variableMap) {
        if (domain instanceof AnnExpr.RangeDomain range) {
            rewrite(range.getFrom(), variableMap);
            rewrite(range.getTo(), variableMap);
        } else if (domain instanceof AnnExpr.SetDomain set) {
            rewrite(set.getFirst(), variableMap);
            rewrite(set.getSecond(), variableMap);
        } else if (domain instanceof AnnExpr.ExprDomain expr) {
            rewrite(expr.getExpr(), variableMap);
        }
        // A type domain names a type, not a variable.
    }

    private void rewriteVarRef(AnnExpr.VarRef ref, Map<String, String> variableMap) {
        ref.getAccesses().forEach(access -> {
            if (access instanceof AnnExpr.IndexAccess index) {
                rewrite(index.getIndex(), variableMap);
            }
        });

        if (isBound(ref.getName()) || ref.isQualified()) {
            return;
        }
        String mangled = variableMap.get(ref.getName());
        if (mangled == null) {
            unresolved.add(ref.getName());
            return;
        }
        ref.setName(mangled);
    }

    private boolean isBound(String name) {
        for (Set<String> scope : bound) {
            if (scope.contains(name)) {
                return true;
            }
        }
        return false;
    }
}
