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
 *   <li>{@code state} - the point the formula speaks about, the {@code ref} of the
 *       specification. Temporal operators move it and restore it afterwards.</li>
 *   <li>{@code windowStart} - the {@code win} of the specification, where {@code timer}
 *       counts from. Null until {@code during} or {@code on} opens a window, which is why
 *       the operators needing one can say so rather than silently measuring nothing.</li>
 *   <li>{@code floor} - how far back history reaches. Null for the program, whose history
 *       has no lower bound; the entry state for a loop invariant, so that the operators
 *       reading history see this run of the loop and not what preceded it.</li>
 *   <li>{@code preOpState} - the state before the annotated statement, used only by
 *       {@code .scope(pre)}, and deliberately unaffected by the moves above.</li>
 *   <li>{@code scale} - which invariant wrapper applies, and whether {@code timer} is
 *       measured in cycles or in iterations.</li>
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
    private Term floor;
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
        floor = null;
        preOpState = preOp;
        Template template = buildTemplate(annotation.getBody());
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
        floor = null;
        process = owningProcess;
        pstate = owningState;
        preOpState = at;
        rejectBareNext(annotation);

        Template template = buildTemplate(annotation.getBody());
        return wrapInvariant(template, at);
    }

    /**
     * The body of a loop invariant's definition: what {@code loopInv0 t0 t} means.
     *
     * <p>Two states, not one. {@code t} is where the invariant is stated; {@code t0} is the
     * state the loop was entered at, which bounds how far back the operators reading
     * history may look. A loop that runs again in a later cycle is a different run, and
     * {@code t0} is what tells the two apart - without it, {@code once} inside the
     * invariant could be satisfied by an iteration of a previous run.
     */
    public Term translateLoopInvariant(Annotation annotation, Term entry, Term at) {
        if (annotation.isForeignLanguage()) {
            return new Term.Raw(annotation.getText());
        }
        scale = Scale.FOR;
        floor = entry;
        preOpState = at;
        rejectBareNext(annotation);

        Template template = buildTemplate(annotation.getBody());
        floor = null;
        return Term.substitute(template.body(), template.hole(), at);
    }

    /**
     * A loop invariant referred to by name, stated at one state: {@code loopInv0 t0 t}.
     *
     * <p>Every loop is cut this way, whether or not an invariant was written for it, so a
     * condition mentions the name rather than carrying the formula. What the name means is
     * settled once, in the theory declaring it - either by the annotation the loop carries
     * or, when it carries none, not at all.
     */
    public Term loopInvariantAt(String name, Term entry, Term at) {
        return new Term.App(name, List.of(entry, at));
    }

    /**
     * A loop invariant stated at every iteration boundary this run has reached:
     * {@code \<forall> t1. t0 \<le> t1 \<and> t1 \<le> t \<and> toEnvP t1 \<longrightarrow>
     * loopInv0 t0 t1} - the {@code Inv_loop} of the specification.
     *
     * <p>The lower bound is what makes an iteration of an earlier run of the same loop
     * inadmissible, so assuming the invariant before a body and showing it afterwards speak
     * about the same run.
     */
    public Term loopInvariantUpTo(String name, Term entry, Term upTo) {
        Term bound = freshState();
        return Terms.forall(bound, Terms.implication(
                Terms.conjunction(List.of(Terms.substate(entry, bound),
                        Terms.substate(bound, upTo), boundaryOf(bound, entry))),
                loopInvariantAt(name, entry, bound)));
    }

    /**
     * That a state is a boundary of a loop's run: reached from the entry, and either the
     * entry itself or the end of an iteration - the {@code toLoopP} hypothesis of the
     * specification. A condition about an iteration states this of the state the body
     * starts from; without it the invariant, quantified over the run's boundaries, could
     * not be applied to that state at all.
     */
    public Term loopBoundary(Term entry, Term at) {
        return Terms.conjunction(List.of(Terms.substate(entry, at), boundaryOf(at, entry)));
    }

    /** A formula built against a placeholder state, ready to be stated at any state. */
    private record Template(Term body, Term hole) {
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

    /**
     * Translates a body against a placeholder, so it can be stated at several states.
     *
     * <p>The window starts undefined: an annotation is not inside one until {@code during}
     * or {@code on} opens it, which is what makes a bare {@code timer} an error rather than
     * a measurement from wherever the formula happens to be stated.
     */
    private Template buildTemplate(AnnExpr body) {
        Term hole = freshState();
        Term translated = body == null ? Terms.TRUE : at(hole, null, body);
        return new Template(translated, hole);
    }

    /** Translates a body at another point, with another window, and restores both. */
    private Term at(Term reference, Term window, AnnExpr body) {
        Term savedState = state;
        Term savedWindow = windowStart;
        state = reference;
        windowStart = window;
        Term result = translate(body);
        state = savedState;
        windowStart = savedWindow;
        return result;
    }

    /**
     * {@code floor \<le> r \<and> r \<le> upTo \<and> boundary r}: where a bound state may
     * range over the history this scale reaches.
     *
     * @param lower the earliest admissible state, or null when history has no lower bound
     */
    private List<Term> historyBounds(Term bound, Term upTo, Term lower) {
        List<Term> conditions = new ArrayList<>();
        if (lower != null) {
            conditions.add(Terms.substate(lower, bound));
        }
        conditions.add(Terms.substate(bound, upTo));
        conditions.add(boundary(bound));
        return conditions;
    }

    /** Whether a state is a boundary of the scale being compiled against. */
    private Term boundary(Term state) {
        return boundaryOf(state, scale == Scale.FOR ? floor : null);
    }

    /**
     * The boundaries of a loop's run are the state it was entered at and the end of each
     * iteration; everywhere else they are the boundaries between cycles.
     *
     * <p>An iteration ends in a {@code toEnv}, so within a run those are what
     * {@code toEnvP} picks out - but the state the loop was entered at is mid-cycle and
     * carries no marker, and a run has to count from somewhere. Naming it is what makes
     * {@code once} at the entry of a loop mean "here", rather than a claim about an
     * iteration that has not happened.
     *
     * @param entry the state a loop run began at, or null outside a loop
     */
    private static Term boundaryOf(Term state, Term entry) {
        if (entry == null) {
            return Terms.toEnvP(state);
        }
        return Terms.disjunction(List.of(new Term.Infix("=", state, entry), Terms.toEnvP(state)));
    }

    /**
     * {@code Adj(a,b)}: a is the nearest boundary strictly before b - nothing between them
     * is one.
     *
     * <p>The operators reading one step of history are written against this rather than
     * against a function returning the previous boundary, because it says nothing when
     * there is none: at the start of a scale the quantifier it sits under is simply
     * unsatisfiable, where such a function would hand back a state from before the scale
     * began.
     */
    private Term adjacent(Term earlier, Term later) {
        Term between = freshState();
        return Terms.conjunction(List.of(
                Terms.strictlyBefore(earlier, later),
                boundary(earlier),
                Terms.forall(between, Terms.implication(
                        Terms.conjunction(List.of(Terms.strictlyBefore(earlier, between),
                                Terms.strictlyBefore(between, later))),
                        Terms.not(boundary(between))))));
    }

    /** Where the operators opening no window of their own start looking. */
    private Term windowOrFloor() {
        return windowStart != null ? windowStart : floor;
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
            case PREVIOUSLY -> previouslyTerm(temporal.getFirst());
            case NEXT -> nextTerm(temporal.getFirst());
            case ONCE -> onceTerm(temporal.getFirst());
            case DURING -> duringTerm(temporal.getFirst(), temporal.getSecond(), temporal.getThird());
            case TIMER -> timerTerm(temporal.getFirst());
            case WITHIN -> withinTerm(temporal.getFirst(), temporal.getSecond());
            case STABLE -> stableTerm(temporal.getFirst(), temporal.getSecond());
            case COOLDOWN -> cooldownTerm(temporal.getFirst(), temporal.getSecond());
            case ON -> onTerm(temporal.getFirst(), temporal.getSecond());
            case WITHIN_SINCE -> withinSinceTerm(
                    temporal.getFirst(), temporal.getSecond(), temporal.getThird());
            case STABLE_SINCE -> stableSinceTerm(
                    temporal.getFirst(), temporal.getSecond(), temporal.getThird());
        };
    }

    /**
     * At the nearest boundary before this one. False where there is none: the quantifier
     * simply has no witness, which is what makes the operator safe at the start of a scale.
     */
    private Term previouslyTerm(AnnExpr phi) {
        Term earlier = freshState();
        List<Term> condition = new ArrayList<>();
        if (floor != null) {
            condition.add(Terms.substate(floor, earlier));
        }
        condition.add(adjacent(earlier, state));
        condition.add(at(earlier, windowStart, phi));
        return Terms.exists(earlier, Terms.conjunction(condition));
    }

    /** At the next boundary: the same question the other way round the order. */
    private Term nextTerm(AnnExpr phi) {
        Term later = freshState();
        Term condition = adjacent(state, later);
        return Terms.exists(later,
                Terms.conjunction(List.of(condition, at(later, windowStart, phi))));
    }

    /** At some boundary this scale has reached, this one included. */
    private Term onceTerm(AnnExpr phi) {
        Term earlier = freshState();
        List<Term> condition = new ArrayList<>(historyBounds(earlier, state, floor));
        condition.add(at(earlier, windowStart, phi));
        return Terms.exists(earlier, Terms.conjunction(condition));
    }

    /**
     * From every boundary where the trigger held, if nothing has interrupted since, the
     * body has held at every boundary through to now.
     *
     * <p>The interrupt and the body are both asked under the window the trigger opens, so
     * {@code timer} inside either counts from that trigger rather than from an enclosing
     * one. The trigger itself keeps the window it was written under: it defines a window
     * rather than sitting inside one.
     */
    private Term duringTerm(AnnExpr trigger, AnnExpr interrupt, AnnExpr body) {
        Term reference = state;
        Term triggered = freshState();
        List<Term> condition =
                new ArrayList<>(historyBounds(triggered, reference, windowOrFloor()));
        condition.add(at(triggered, windowStart, trigger));

        Term interrupted = freshState();
        Term uninterrupted = Terms.forall(interrupted, Terms.implication(
                Terms.conjunction(List.of(Terms.strictlyBefore(triggered, interrupted),
                        Terms.substate(interrupted, reference), boundary(interrupted))),
                Terms.not(at(interrupted, triggered, interrupt))));

        Term through = freshState();
        Term maintained = Terms.forall(through, Terms.implication(
                Terms.conjunction(historyBounds(through, reference, triggered)),
                at(through, triggered, body)));

        return Terms.forall(triggered, Terms.implication(Terms.conjunction(condition),
                Terms.implication(uninterrupted, maintained)));
    }

    /**
     * Wherever the trigger has held, the property holds now.
     *
     * <p>The property is asked at the state the annotation speaks about, not at the
     * trigger; what the trigger contributes is the window, so that a {@code timer} inside
     * the property measures from it.
     */
    private Term onTerm(AnnExpr trigger, AnnExpr property) {
        Term reference = state;
        Term triggered = freshState();
        List<Term> condition =
                new ArrayList<>(historyBounds(triggered, reference, windowOrFloor()));
        condition.add(at(triggered, windowStart, trigger));
        return Terms.forall(triggered, Terms.implication(Terms.conjunction(condition),
                at(reference, triggered, property)));
    }

    /** The condition held recently enough: at some boundary less than t ago. */
    private Term cooldownTerm(AnnExpr phi, AnnExpr threshold) {
        Term reference = state;
        Term when = freshState();
        List<Term> condition = new ArrayList<>(historyBounds(when, reference, windowOrFloor()));
        condition.add(at(when, windowStart, phi));
        condition.add(elapsedBelow(when, reference, threshold));
        return Terms.exists(when, Terms.conjunction(condition));
    }

    /** The window has lasted at least this long. */
    private Term timerTerm(AnnExpr threshold) {
        return elapsedAtLeast(requireWindow("timer"), state, threshold);
    }

    /** Either the condition has held somewhere in the window, or there is still time. */
    private Term withinTerm(AnnExpr threshold, AnnExpr phi) {
        Term window = requireWindow("within");
        Term later = freshState();
        List<Term> condition = new ArrayList<>(historyBounds(later, state, window));
        condition.add(at(later, window, phi));
        return Terms.disjunction(List.of(Terms.exists(later, Terms.conjunction(condition)),
                elapsedBelow(window, state, threshold)));
    }

    /** While the window is still young, the condition holds. */
    private Term stableTerm(AnnExpr threshold, AnnExpr phi) {
        Term window = requireWindow("stable");
        return Terms.implication(elapsedBelow(window, state, threshold), translate(phi));
    }

    /**
     * {@code within(psi, t, phi)}: since psi, phi arrives before t is up. The window is its
     * own, so unlike {@code within(t, phi)} it needs no enclosing one - it is
     * {@code during(psi, phi, !timer(t))}, phi being what ends the wait.
     */
    private Term withinSinceTerm(AnnExpr trigger, AnnExpr threshold, AnnExpr phi) {
        AnnExpr notElapsed = new AnnExpr.Unary(AnnExpr.UnaryOp.NOT,
                new AnnExpr.Temporal(AnnExpr.Temporal.Kind.TIMER, threshold, null, null));
        return duringTerm(trigger, phi, notElapsed);
    }

    /**
     * {@code stable(psi, t, phi)}: while psi is less than t old, phi holds.
     *
     * <p>Through {@code cooldown} rather than through {@code during}: the question is what
     * holds now, not what has held throughout, and those differ once psi has held more than
     * once.
     */
    private Term stableSinceTerm(AnnExpr trigger, AnnExpr threshold, AnnExpr phi) {
        return Terms.implication(cooldownTerm(trigger, threshold), translate(phi));
    }

    /**
     * How far the window has run. Outside a loop a boundary is a cycle, so the count is
     * scaled to a duration; inside one it is an iteration, which the threshold is written
     * in directly.
     */
    private Term elapsed(Term from, Term to) {
        Term count = Terms.toEnvNum(from, to);
        return scale == Scale.FOR ? count : new Term.Infix("*", count, clock);
    }

    private Term elapsedAtLeast(Term from, Term to, AnnExpr threshold) {
        return new Term.Infix("\\<ge>", elapsed(from, to), translate(threshold));
    }

    private Term elapsedBelow(Term from, Term to, AnnExpr threshold) {
        return new Term.Infix("<", elapsed(from, to), translate(threshold));
    }

    /**
     * The window the measuring operators need. They measure from where a window was opened,
     * so outside {@code during} and {@code on} there is nothing for them to measure from
     * and the annotation is rejected rather than quietly measured from its own state.
     */
    private Term requireWindow(String operator) {
        if (windowStart == null) {
            throw new IllegalStateException(operator + " has no window to measure from:"
                    + " it is only meaningful inside during or on");
        }
        return windowStart;
    }

    /**
     * {@code next} of a constant as the whole of an invariant asks for a boundary beyond
     * the last one, which nothing supplies. Rejected here rather than left to fail at the
     * prover, where it would look like a property that merely did not go through.
     */
    private static void rejectBareNext(Annotation annotation) {
        if (annotation.getBody() instanceof AnnExpr.Temporal temporal
                && temporal.getKind() == AnnExpr.Temporal.Kind.NEXT
                && temporal.getFirst() instanceof AnnExpr.Literal literal
                && literal.getKind() == AnnExpr.Literal.Kind.BOOL) {
            throw new IllegalStateException("invariant at line " + annotation.getLine()
                    + " is next(" + literal.getText() + "), which claims a boundary after the"
                    + " last one and so can never hold");
        }
    }

    // ------------------------------------------------------------------ scope

    private Term translateScope(AnnExpr.Scope scope) {
        return switch (scope.getKind()) {
            // The state before the annotated statement, unaffected by temporal moves.
            case PRE -> at(preOpState == null ? state : preOpState, windowStart, scope.getBase());
            case PREV -> at(previousState(), windowStart, scope.getBase());
            case PAST -> at(pastState(scope.getPhi()), windowStart, scope.getBase());
        };
    }

    /**
     * The nearest earlier boundary, as a term rather than as a bound variable: a scope
     * reads an expression there, and an expression needs a state to read from.
     *
     * <p>Where there is no such boundary the choice is unconstrained, so the expression
     * reads an arbitrary state. That is the reading wanted at the start of a scale - the
     * value is unknown, rather than the formula around it being vacuously true.
     */
    private Term previousState() {
        Term earlier = freshState();
        List<Term> condition = new ArrayList<>();
        if (floor != null) {
            condition.add(Terms.substate(floor, earlier));
        }
        condition.add(adjacent(earlier, state));
        return Terms.choice(earlier, Terms.conjunction(condition));
    }

    /** The most recent earlier state where the condition held. */
    private Term pastState(AnnExpr phi) {
        Term when = freshState();
        List<Term> condition = new ArrayList<>(historyBounds(when, state, floor));
        condition.add(at(when, windowStart, phi));

        Term later = freshState();
        condition.add(Terms.forall(later, Terms.implication(
                Terms.conjunction(List.of(Terms.strictlyBefore(when, later),
                        Terms.substate(later, state), boundary(later))),
                Terms.not(at(later, windowStart, phi)))));
        return Terms.choice(when, Terms.conjunction(condition));
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
