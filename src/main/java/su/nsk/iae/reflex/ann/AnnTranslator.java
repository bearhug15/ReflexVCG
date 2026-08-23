package su.nsk.iae.reflex.ann;

import su.nsk.iae.reflex.ir.Annotation;
import su.nsk.iae.reflex.ir.IrType;
import su.nsk.iae.reflex.term.Term;
import su.nsk.iae.reflex.term.Terms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Translates an annotation into an Isabelle formula - the {@code parseA} of
 * Annotations.tex.
 *
 * <p>The context the specification threads through the recursion is held here:
 * <ul>
 *   <li>{@code state} - the state variables are read in. Temporal operators shift it and
 *       restore it afterwards.</li>
 *   <li>{@code windowStart} - where {@code timer} counts from. {@code during} moves it to
 *       the start of its window.</li>
 *   <li>{@code preOpState} - the state before the annotated statement, used only by
 *       {@code .scope(pre)}, and deliberately unaffected by the shifts above.</li>
 *   <li>{@code scale} - which invariant wrapper applies, and whether {@code timer} is
 *       measured in cycles or in ticks.</li>
 * </ul>
 *
 * <p>Names and types are expected to be settled already: {@link AnnMangling} and
 * {@link AnnTyping} run first, so a variable is read under its final name and an operand's
 * type is known where a conversion has to be chosen.
 */
public final class AnnTranslator {

    /** Which cyclicity wrapper an annotation gets, and how timer is measured. */
    public enum Scale {
        /** assume and assert: no wrapper, the formula holds at one state. */
        NONE,
        /** An invariant over the whole program. */
        PROGRAM,
        /** An invariant while a process is active. */
        PROCESS,
        /** An invariant while a process is in one state. */
        PSTATE,
        /** A loop invariant: timer counts iterations rather than ticks. */
        FOR
    }

    private final Term clock;

    private Term state;
    private Term windowStart;
    private Term preOpState;
    private Scale scale = Scale.NONE;
    private String process;
    private String pstate;

    private final Map<String, AnnDefinition> definitions = new HashMap<>();
    private final Map<String, Term> locals = new HashMap<>();
    private int nextBoundState;

    /**
     * @param clock the program's cycle duration, which timer multiplies by outside a loop
     */
    public AnnTranslator(long clock) {
        this.clock = new Term.Var(Long.toString(clock));
    }

    /** Definitions in scope, which a {@code define} annotation adds to. */
    public void register(AnnDefinition definition) {
        definitions.put(definition.name(), definition);
    }

    public void register(Annotation annotation) {
        annotation.getDefinitions().forEach(this::register);
    }

    // ------------------------------------------------------------------ entry points

    /**
     * Translates an annotation whose formula holds at one state - assume and assert.
     *
     * @param at    the state it is stated about: before the statement for assume, after
     *              for assert
     * @param preOp the state before the statement, which {@code .scope(pre)} reaches for
     */
    public Term translateAt(Annotation annotation, Term at, Term preOp) {
        if (annotation.isForeignLanguage()) {
            // Not Reflex-AL: carried through as written, names already rewritten.
            return new Term.Raw(annotation.getText());
        }
        scale = Scale.NONE;
        preOpState = preOp;
        Template template = buildTemplate(annotation.getBody(), at);
        return Term.substitute(template.body(), template.hole(), at);
    }

    /**
     * Translates an invariant, wrapping it so it is stated at every reachable state
     * rather than one - {@code invariantWrapper} of the specification.
     */
    public Term translateInvariant(Annotation annotation, Term at, Scale invariantScale,
                                   String owningProcess, String owningState) {
        if (annotation.isForeignLanguage()) {
            return new Term.Raw(annotation.getText());
        }
        scale = invariantScale;
        process = owningProcess;
        pstate = owningState;
        preOpState = at;

        Template template = buildTemplate(annotation.getBody(), at);
        return wrapInvariant(template, at);
    }

    /**
     * Builds the template of a loop invariant. The states it has to be stated at are only
     * known once the loop body has been translated, so this stops at the template and
     * {@link #instantiateLoopInvariant} finishes the job.
     */
    public Template translateLoopInvariant(Annotation annotation, Term at) {
        scale = Scale.FOR;
        preOpState = at;
        return buildTemplate(annotation.getBody(), at);
    }

    /**
     * The template of a loop nobody wrote an invariant for: an uninterpreted predicate
     * applied to the state, standing in for whatever the loop preserves.
     *
     * <p>The three conditions a loop produces have the same shape either way, so a loop
     * without an invariant still says what one would have to satisfy - it holds on entry,
     * survives an iteration, and is all the path past the loop may rely on. What they are
     * worth depends on the definition {@code name} is eventually given.
     */
    public Template placeholderLoopInvariant(String name) {
        Term hole = freshState();
        return new Template(new Term.App(name, List.of(hole)), hole);
    }

    /**
     * The invariant stated at each of the states a loop's conditions need it.
     *
     * <p>Deliberately reads no context: the loop body has been translated by the time this
     * is called, so the context now describes some other construct.
     */
    public LoopInvariant instantiateLoopInvariant(Template template, Term preLoop,
                                                  Term afterBody, Term afterLoop) {
        Term afterIteration = Terms.toEnv(afterBody);

        Term entry = Term.substitute(template.body(), template.hole(), preLoop);

        Term before = freshState();
        Term assumption = Terms.forall(before, Terms.implication(
                Terms.conjunction(List.of(Terms.toEnvP(before), Terms.substate(before, preLoop))),
                Term.substitute(template.body(), template.hole(), before)));

        Term after = freshState();
        Term conclusion = Terms.forall(after, Terms.implication(
                Terms.conjunction(List.of(Terms.toEnvP(after), Terms.substate(after, afterIteration))),
                Term.substitute(template.body(), template.hole(), after)));

        Term exit = Term.substitute(template.body(), template.hole(), afterLoop);
        return new LoopInvariant(entry, assumption, conclusion, exit);
    }

    /** A formula built against a placeholder state, ready to be stated at any state. */
    public record Template(Term body, Term hole) {
    }

    /** The three shapes a loop invariant contributes to the loop's conditions. */
    public record LoopInvariant(Term onEntry, Term assumedBeforeBody, Term shownAfterBody,
                                Term onExit) {
    }

    // ------------------------------------------------------------------ wrappers

    /**
     * {@code forall s1. toEnvP s1 & substate s1 s & ... --> F s1}. The extra conjunct
     * narrows the invariant to when the process is running, or in one state.
     */
    private Term wrapInvariant(Template template, Term reference) {
        Term bound = freshState();
        List<Term> conditions = new ArrayList<>();
        conditions.add(Terms.toEnvP(bound));
        conditions.add(Terms.substate(bound, reference));

        if (scale == Scale.PROCESS) {
            conditions.add(Terms.processActivity(bound, process, "active"));
        } else if (scale == Scale.PSTATE) {
            conditions.add(Terms.pstateCompare(bound, process, pstate));
        }

        Term body = Term.substitute(template.body(), template.hole(), bound);
        return Terms.forall(bound, Terms.implication(Terms.conjunction(conditions), body));
    }

    /** Translates a body against a placeholder, so it can be stated at several states. */
    private Template buildTemplate(AnnExpr body, Term at) {
        Term savedState = state;
        Term savedWindow = windowStart;
        Term hole = freshState();

        state = hole;
        windowStart = hole;
        Term translated = body == null ? Terms.TRUE : translate(body);

        state = savedState;
        windowStart = savedWindow;
        return new Template(translated, hole);
    }

    private Term freshState() {
        return new Term.Var("sa" + nextBoundState++);
    }

    // ------------------------------------------------------------------ expressions

    /** The {@code parseA} of the specification. */
    public Term translate(AnnExpr expr) {
        if (expr instanceof AnnExpr.Literal literal) {
            return translateLiteral(literal);
        }
        if (expr instanceof AnnExpr.VarRef ref) {
            return translateVarRef(ref);
        }
        if (expr instanceof AnnExpr.Binary binary) {
            return translateBinary(binary);
        }
        if (expr instanceof AnnExpr.Unary unary) {
            return translateUnary(unary);
        }
        if (expr instanceof AnnExpr.Implication implication) {
            return Terms.implication(translate(implication.getLeft()), translate(implication.getRight()));
        }
        if (expr instanceof AnnExpr.Equivalence equivalence) {
            return Terms.equivalence(translate(equivalence.getLeft()), translate(equivalence.getRight()));
        }
        if (expr instanceof AnnExpr.Quantifier quantifier) {
            return translateQuantifier(quantifier);
        }
        if (expr instanceof AnnExpr.Call call) {
            return expandDefinition(call.getName(), call.getArguments());
        }
        if (expr instanceof AnnExpr.InState inState) {
            return Terms.pstateCompare(state, inState.getProcess(), inState.getPstate());
        }
        if (expr instanceof AnnExpr.LocalTime localTime) {
            return Terms.localTime(state, localTime.getProcess());
        }
        if (expr instanceof AnnExpr.Temporal temporal) {
            return translateTemporal(temporal);
        }
        if (expr instanceof AnnExpr.Scope scope) {
            return translateScope(scope);
        }
        throw new IllegalStateException("Cannot translate: " + expr.getClass().getSimpleName());
    }

    private Term translateLiteral(AnnExpr.Literal literal) {
        return switch (literal.getKind()) {
            case BOOL -> literal.getText().equals("true") ? Terms.TRUE : Terms.FALSE;
            // A time literal denotes milliseconds, as it does in the program.
            case TIME -> new Term.Var(Long.toString(parseTimeMillis(literal.getText())));
            default -> new Term.Var(literal.getText());
        };
    }

    /** A bound variable, a definition, or a program variable read in the current state. */
    private Term translateVarRef(AnnExpr.VarRef ref) {
        Term local = locals.get(ref.getName());
        if (local != null) {
            return local;
        }
        if (definitions.containsKey(ref.getName())) {
            return expandDefinition(ref.getName(), List.of());
        }
        List<Term> path = new ArrayList<>();
        for (AnnExpr.Access access : ref.getAccesses()) {
            if (access instanceof AnnExpr.FieldAccess field) {
                path.add(Terms.accessField(field.getField()));
            } else {
                AnnExpr index = ((AnnExpr.IndexAccess) access).getIndex();
                path.add(Terms.accessIndex(asNat(translate(index), index.getType())));
            }
        }
        return Terms.valueGetter(state, ref.getType(), ref.getName(), path);
    }

    private Term asNat(Term rendered, IrType type) {
        return Terms.sortOf(type) == Terms.Sort.NAT
                ? rendered
                : Terms.cast(rendered, type, IrType.of(IrType.BuiltinKind.UINT32));
    }

    /** Operands meet at one type, as they do in the program. */
    private Term translateBinary(AnnExpr.Binary binary) {
        Term left = translate(binary.getLeft());
        Term right = translate(binary.getRight());

        if (binary.getOp() == AnnExpr.BinaryOp.AND) {
            return Terms.conjunction(List.of(left, right));
        }
        if (binary.getOp() == AnnExpr.BinaryOp.OR) {
            return Terms.disjunction(List.of(left, right));
        }

        IrType leftType = binary.getLeft().getType();
        IrType rightType = binary.getRight().getType();
        IrType at = su.nsk.iae.reflex.preprocess.CastInsertionPass.defType(
                binary.getOp().symbol(), leftType, rightType);
        left = Terms.cast(left, leftType, at);
        right = Terms.cast(right, rightType, at);
        return new Term.Infix(operatorOf(binary.getOp()), left, right);
    }

    private static String operatorOf(AnnExpr.BinaryOp op) {
        return switch (op) {
            case ADD -> "+";
            case SUB -> "-";
            case MUL -> "*";
            case DIV -> "div";
            case MOD -> "mod";
            case SHL -> "<<";
            case SHR -> ">>";
            case LT -> "<";
            case GT -> ">";
            case LE -> "\\<le>";
            case GE -> "\\<ge>";
            case EQ -> "=";
            case NE -> "\\<noteq>";
            case BIT_AND -> "AND";
            case BIT_OR -> "OR";
            case BIT_XOR -> "XOR";
            case AND -> "\\<and>";
            case OR -> "\\<or>";
        };
    }

    private Term translateUnary(AnnExpr.Unary unary) {
        Term operand = translate(unary.getOperand());
        return switch (unary.getOp()) {
            case NOT -> Terms.not(operand);
            case NEG -> new Term.Prefix("-", operand);
            case PLUS -> operand;
            case BIT_NOT -> new Term.Prefix("NOT", operand);
        };
    }

    /**
     * A quantifier binds one variable per name, gathers the conditions saying where each
     * ranges, and joins them: implication for forall, conjunction for exists.
     */
    private Term translateQuantifier(AnnExpr.Quantifier quantifier) {
        List<Term> bound = new ArrayList<>();
        List<Term> conditions = new ArrayList<>();
        Map<String, Term> saved = new HashMap<>(locals);

        for (AnnExpr.BoundVar variable : quantifier.getVariables()) {
            Term name = new Term.Var(variable.getName());
            locals.put(variable.getName(), name);
            bound.add(name);
            Term membership = domainMembership(name, variable.getDomain());
            if (membership != null) {
                conditions.add(membership);
            }
        }

        Term body = translate(quantifier.getBody());
        locals.clear();
        locals.putAll(saved);

        Term condition = Terms.conjunction(conditions);
        return quantifier.getKind() == AnnExpr.Quantifier.Kind.FORALL
                ? Terms.forall(bound, Terms.implication(condition, body))
                : Terms.exists(bound, Terms.conjunction(List.of(condition, body)));
    }

    private Term domainMembership(Term variable, AnnExpr.Domain domain) {
        if (domain == null) {
            return null;
        }
        if (domain instanceof AnnExpr.RangeDomain range) {
            return Terms.conjunction(List.of(
                    new Term.Infix("\\<le>", translate(range.getFrom()), variable),
                    new Term.Infix("<", variable, translate(range.getTo()))));
        }
        if (domain instanceof AnnExpr.SetDomain set) {
            Term first = new Term.Infix("=", variable, translate(set.getFirst()));
            if (set.getSecond() == null) {
                return first;
            }
            return Terms.disjunction(List.of(first,
                    new Term.Infix("=", variable, translate(set.getSecond()))));
        }
        if (domain instanceof AnnExpr.TypeDomain) {
            // Membership of a type is carried by the bound variable's HOL type.
            return null;
        }
        // An array or set named by an expression: the variable is one of its elements.
        return null;
    }

    /**
     * A definition is a typed macro: the arguments are translated in the caller's context
     * before the parameters are bound, so an argument sharing a parameter's name is not
     * read from the bindings being made for it.
     */
    private Term expandDefinition(String name, List<AnnExpr> arguments) {
        AnnDefinition definition = definitions.get(name);
        if (definition == null) {
            throw new IllegalStateException("annotation uses an undefined name: " + name);
        }
        List<Term> values = new ArrayList<>();
        arguments.forEach(argument -> values.add(translate(argument)));

        Map<String, Term> saved = new HashMap<>(locals);
        for (int i = 0; i < definition.parameters().size() && i < values.size(); i++) {
            locals.put(definition.parameters().get(i), values.get(i));
        }
        Term result = translate(definition.body());
        locals.clear();
        locals.putAll(saved);
        return result;
    }

    // ------------------------------------------------------------------ temporal

    private Term translateTemporal(AnnExpr.Temporal temporal) {
        return switch (temporal.getKind()) {
            case PREVIOUSLY -> atState(Terms.predEnv(state), temporal.getFirst());
            case NEXT -> translateNext(temporal);
            case ONCE -> onceTerm(temporal.getFirst());
            case DURING -> duringTerm(temporal.getFirst(), temporal.getSecond(), temporal.getThird());
            case TIMER -> timerTerm(temporal.getFirst());
            case WITHIN -> windowTerm(temporal, true);
            case STABLE -> windowTerm(temporal, false);
            case COOLDOWN -> cooldownTerm(temporal);
            case ON -> onTerm(temporal);
        };
    }

    /** Translates {@code body} with the reference state temporarily moved. */
    private Term atState(Term newState, AnnExpr body) {
        Term saved = state;
        state = newState;
        Term result = translate(body);
        state = saved;
        return result;
    }

    /**
     * The successor state, expressed by searching for the state whose predecessor is the
     * current one - there is no forward constructor. False when none exists.
     */
    private Term translateNext(AnnExpr.Temporal temporal) {
        Term saved = state;
        Term successor = freshState();
        state = successor;
        Term body = translate(temporal.getFirst());
        state = saved;

        Term condition = Terms.conjunction(List.of(Terms.toEnvP(saved),
                new Term.Infix("=", saved, Terms.predEnv(successor))));
        return Terms.exists(successor, Terms.conjunction(List.of(condition, body)));
    }

    private Term onceTerm(AnnExpr phi) {
        Term saved = state;
        Term earlier = freshState();
        state = earlier;
        Term body = translate(phi);
        state = saved;

        return Terms.exists(earlier, Terms.conjunction(List.of(
                Terms.toEnvP(earlier), Terms.substate(earlier, saved), body)));
    }

    /**
     * From the last state where the trigger held and nothing has interrupted since, the
     * body has held at every state through to now.
     */
    private Term duringTerm(AnnExpr trigger, AnnExpr interrupt, AnnExpr body) {
        Term saved = state;
        Term savedWindow = windowStart;
        Term triggered = freshState();

        Term triggerTerm = atState(triggered, trigger);

        Term middle = freshState();
        state = middle;
        windowStart = triggered;
        Term notInterrupted = Terms.forall(middle, Terms.implication(
                Terms.conjunction(List.of(Terms.toEnvP(middle), Terms.substate(triggered, middle),
                        Terms.substate(middle, saved))),
                Terms.not(translate(interrupt))));

        Term through = freshState();
        state = through;
        windowStart = triggered;
        Term holds = Terms.forall(through, Terms.implication(
                Terms.conjunction(List.of(Terms.toEnvP(through), Terms.substate(triggered, through),
                        Terms.substate(through, saved))),
                translate(body)));

        state = saved;
        windowStart = savedWindow;
        Term condition = Terms.conjunction(List.of(Terms.toEnvP(triggered),
                Terms.substate(triggered, saved), triggerTerm, notInterrupted));
        return Terms.forall(triggered, Terms.implication(condition, holds));
    }

    /**
     * How long the window has lasted. Outside a loop that is a number of cycles, so it is
     * multiplied by the cycle duration; inside one it counts iterations.
     */
    private Term timerTerm(AnnExpr threshold) {
        Term bound = translate(threshold);
        Term elapsed = Terms.toEnvNum(windowStart, state);
        if (scale == Scale.FOR) {
            return new Term.Infix(">", elapsed, bound);
        }
        return new Term.Infix(">", new Term.Infix("*", elapsed, clock), bound);
    }

    /** within: somewhere in the window. stable: everywhere in it. */
    private Term windowTerm(AnnExpr.Temporal temporal, boolean existential) {
        Term saved = state;
        Term savedWindow = windowStart;
        Term later = freshState();

        state = later;
        windowStart = saved;
        Term condition = Terms.conjunction(List.of(Terms.toEnvP(later),
                Terms.substate(saved, later), Terms.not(timerTerm(temporal.getSecond()))));
        Term body = translate(temporal.getFirst());

        state = saved;
        windowStart = savedWindow;
        return existential
                ? Terms.exists(later, Terms.conjunction(List.of(condition, body)))
                : Terms.forall(later, Terms.implication(condition, body));
    }

    /** cooldown(phi, t) is once(phi) and during(phi, false, !timer(t)). */
    private Term cooldownTerm(AnnExpr.Temporal temporal) {
        AnnExpr phi = temporal.getFirst();
        AnnExpr never = new AnnExpr.Literal(AnnExpr.Literal.Kind.BOOL, "false");
        AnnExpr notElapsed = new AnnExpr.Unary(AnnExpr.UnaryOp.NOT,
                new AnnExpr.Temporal(AnnExpr.Temporal.Kind.TIMER, temporal.getSecond(), null, null));
        return Terms.conjunction(List.of(onceTerm(phi), duringTerm(phi, never, notElapsed)));
    }

    /** Wherever the trigger holds at a reachable state, so does the property. */
    private Term onTerm(AnnExpr.Temporal temporal) {
        Term saved = state;
        Term reachable = freshState();
        state = reachable;
        Term trigger = translate(temporal.getFirst());
        Term property = translate(temporal.getSecond());
        state = saved;

        Term condition = Terms.conjunction(List.of(Terms.toEnvP(reachable),
                Terms.substate(reachable, saved), trigger));
        return Terms.forall(reachable, Terms.implication(condition, property));
    }

    // ------------------------------------------------------------------ scope

    private Term translateScope(AnnExpr.Scope scope) {
        return switch (scope.getKind()) {
            // The state before the annotated statement, unaffected by temporal shifts.
            case PRE -> atState(preOpState == null ? state : preOpState, scope.getBase());
            case PREV -> atState(Terms.predEnv(state), scope.getBase());
            case PAST -> pastTerm(scope);
        };
    }

    /** The most recent earlier state where the condition held. */
    private Term pastTerm(AnnExpr.Scope scope) {
        Term saved = state;
        Term when = freshState();

        Term held = atState(when, scope.getPhi());

        Term between = freshState();
        state = between;
        Term notHeld = Terms.not(translate(scope.getPhi()));
        state = saved;

        Term maximal = Terms.forall(between, Terms.implication(
                Terms.conjunction(List.of(Terms.toEnvP(between), Terms.substate(when, between),
                        Terms.substate(between, saved),
                        Terms.not(new Term.Infix("=", between, when)),
                        Terms.not(new Term.Infix("=", between, saved)))),
                notHeld));

        Term condition = Terms.conjunction(List.of(Terms.toEnvP(when), Terms.substate(when, saved),
                Terms.not(new Term.Infix("=", when, saved)), held, maximal));

        Term body = atState(when, scope.getBase());
        return Terms.forall(when, Terms.implication(condition, body));
    }

    // ------------------------------------------------------------------ helpers

    /** Time literals in annotations are written {@code T#1h30m}. */
    static long parseTimeMillis(String text) {
        String value = text.trim();
        if (value.length() < 2 || (value.charAt(0) != 't' && value.charAt(0) != 'T')
                || value.charAt(1) != '#') {
            return 0;
        }
        value = value.substring(2);

        long total = 0;
        int i = 0;
        while (i < value.length()) {
            int start = i;
            while (i < value.length() && Character.isDigit(value.charAt(i))) {
                i++;
            }
            if (start == i) {
                break;
            }
            long amount = Long.parseLong(value.substring(start, i));
            String unit = value.substring(i).toLowerCase();
            if (unit.startsWith("ms")) {
                total += amount;
                i += 2;
            } else if (unit.startsWith("d")) {
                total += amount * 24 * 60 * 60 * 1000L;
                i += 1;
            } else if (unit.startsWith("h")) {
                total += amount * 60 * 60 * 1000L;
                i += 1;
            } else if (unit.startsWith("m")) {
                total += amount * 60 * 1000L;
                i += 1;
            } else if (unit.startsWith("s")) {
                total += amount * 1000L;
                i += 1;
            } else {
                break;
            }
        }
        return total;
    }
}
