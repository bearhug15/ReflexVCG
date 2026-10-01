# Extra invariants in ReflexVCG

This document describes every way the generator finds invariants beyond the ones the engineer
writes: what each claims, how it is found, which priority it has and why, why it is provable, and
what was measured. It ends with the approaches not implemented yet.

The four structural kinds of `mainOverview.tex`, section "Extra Invariants", are marked **(M)**.
Everything else was added on top of them.

Code: `src/main/java/su/nsk/iae/reflex/inv/` (the analysis) and `vc/ExtraInvariantGenerator` (how the
results reach the conditions). Tests: `src/test/java/su/nsk/iae/reflex/inv/` and
`vc/ExtraInvariantGeneratorTest`.

---

## 1. How it works

### 1.1 What an extra invariant is

A claim that holds at every **cycle boundary** a run can reach: after initialisation, and after every
cycle. It is found by the generator and trusted by nobody: generation emits the conditions that prove
it, and it is only assumed where those conditions prove it.

Every derived invariant is written in **the same wrap as an annotation invariant**
(`AnnTranslator.invariantWrapper`, used directly by `ExtraInvariant.wrapped`). It has a *scale* and a
*body* `B(s)`:

```
∀ s1. ((toEnvP s1 ∧ substate s1 s) ∧ scope(s1)) ⟶ B(s1)
scope: program – none;  process P – P neither stopped nor in error;  state (P, q) – getPstate s1 P = q
```

So an invariant claims `scope ⟶ B` at every boundary at or below `s`. That per-boundary statement is
the invariant's *claim* (`ExtraInvariant.claim`). The checker establishes the claim boundary by
boundary; the concrete runs of the tests evaluate both the claim and the wrapped formula. This is also
the paper's `∀s. toEnvP s ⟶ …`, restricted to the history of `s`.

**Boundaries inside loops.** In the model a loop's iterations each end in `toEnv`, so their ends are
boundaries too, and the wrap speaks about them. The checker therefore checks every candidate there
as well, where what the loop body writes is unknown (1.2). An invariant that holds at cycle ends but
not mid-loop is dropped: in loopSum, `total` has no fixed value at iteration ends.

**Provability.** A wrapped goal at a cycle's end follows from the hypothesis at the boundary before
it and the claim at the end: lemmas `wrapped_step`/`wrapped_step_unscoped` in `ReflexLemmas.thy`, with
`wrapped_here` reading the hypothesis at `st0`, and `wrapped_empty` giving the base case's (nothing
below `emptyState` is a boundary). On a path that passes a loop, the boundary before the
end is the loop's opaque state, and nothing is known about the iteration ends between the loop's
entry and it. A wrapped goal is not provable on such a path, exactly as for annotation invariants
(the missing `toLoop` constructor, CLAUDE.md). Section 4 has the measurements.

`predEnv s` (stabilized values) and `prevProcState s P` (transitions, values since entry) are
functions of the state, so the bodies that look back stay simple.

### 1.2 Finding them: guess and check

Every guessed kind is found the same way (`InvariantChecker`, a Houdini scheme):

1. **Guess** a set of candidates (a *candidate source* does this).
2. Keep those that hold at the **first boundary**, the state `InitialCondition` builds.
3. Walk **every path of one cycle** over the same control-flow graph the conditions are generated
   from. Assume every standing candidate held at the cycle's first state, and drop every candidate
   that may fail at its last. Repeat until nothing is dropped.

What survives holds at the start and is carried through every cycle by itself: an inductive
invariant. The walk is abstract (`AbstractCycle`), and a value is one of three things:

- a known constant;
- unknown;
- "as it was when the cycle began", which is what lets a hypothesis be used once the walk learns
  which state each process began the cycle in.

The walk also tracks:

- a strict upper bound on each process's `ltime`;
- the order of every write and every state change, which is what "written since the process
  entered its state" needs;
- for transitions, which guards still hold.

A path whose guard evaluates to false under what is known is dropped, which is where the hypotheses
pay off.

Two details keep it precise:

- **A process moved before its own body runs**, e.g. by `start` from an earlier process: the walk
  would never see which state it began the cycle in. So the walk splits over each state it could
  have begun in, and applies the hypotheses to each.
- **A loop** is seen from outside, as the conditions see it. What its body writes becomes unknown,
  the processes it moves are in unknown states, and every timer is unknown. Every candidate is
  checked there, since the loop's iteration ends are boundaries, with the boundary before each
  unknown. The state past the loop is a boundary for `predEnv`. A process moved inside a loop
  loses its `prevProcState` there.

### 1.3 The stages

`StructuralInvariants` runs three stages:

1. **High priority, alone.** Process states join the global invariant, so they must be inductive
   resting on nothing else. They are settled first with no other hypothesis. The states they leave
   reachable are what every other source guesses about.
2. **Every other guessed kind, together,** taking stage 1 as given. The survivors are inductive
   relative to stage 1 and themselves, which is exactly what one obligation per cycle proves.
3. **Collected kinds** (transitions, copied-on-entry): one more walk that assumes everything settled
   so far, and reports every state change on every possible path, loop bodies included, together
   with what still held just before it.

### 1.4 Reaching the conditions

| Priority | Where | Assumed by | Proved by |
|---|---|---|---|
| high | conjoined into `inv` (`Requirements.thy`) | every condition assuming `inv st0` | every cycle's main condition and the base case, as `inv st_final` |
| mid, low | conjoined into `extraInv` (`ExtraInvariants.thy`) | each condition gets those tagged with a state its path passes through, or not tied to a state, as named assumptions about `st0` | an `EXTRA<n>` condition per cycle (assuming `inv st0` and all of `extraInv st0`) and one for the base case |

Derived invariants are `definition`s in `ExtraInvariants.thy`, each with a comment giving its
description and tags. `Requirements.thy` imports that theory, so conditions keep their imports.
`EXTRA` conditions are numbered on their own, so asking for mid or low renames no other condition,
and a proof recorded against one keeps working.

A loop's frame (the state past a cut) now also says that a process the loop never moves keeps its
`prevProcState`. That is true in the model, since such a loop contains no `setPstate` for it, and it
is what carries the since-entry kinds past a loop.

### 1.5 The container and the key

`ExtraInvariants` holds every invariant, annotation ones included, each with a set of tags:

- `kind=…`, `priority=…` (attached automatically);
- `process=P`, `state=P.q`, `variable=v`;
- `transition=INITIAL|CONDITIONAL|TIMED`, `loop=loopInv0`;
- anything a caller attaches.

`find(tags…)` returns those carrying every tag and `findAny` those carrying some, always in insertion
order. Tags can be attached and detached at any time. The per-condition selection in 1.4 is one tag
search.

The key is `-x` on the command line (`ReflexVcg.setExtraInvariantSelection`, `Selection.parse`):

```
-x high                 the default: process states (always in, unless none)
-x mid                  + pairs, static-analysis claims, defined/stabilized values,
                          values unchanged since entry, timer bounds
-x low                  + transitions, values copied on entry        (also: -x all)
-x high,timer_bounds    the default plus named kinds, comma-separated
-x none                 nothing derived - the output as it was before extra invariants
```

### 1.6 Adding a way to find invariants

Implement `CandidateSource<C>`: `guess` returns candidates, and `build` turns the survivors into
`ExtraInvariant`s. A candidate implements `Candidate`:

- `holdsAt(Boundary)`, checked at every cycle's end;
- optionally `concerns()` and `assume(CycleStart)`, which make it a hypothesis once the walk learns
  where a process began the cycle.

Implement `EntryCollector` as well to see every state change. Register the source with
`ExtraInvariantGenerator.addSource` (or `StructuralInvariants.addSource`). It joins stage 2, and its
invariants go into `extraInv` with their obligations. Nothing a source guesses is trusted, so it may
guess freely. `StructuralInvariantsTest.aPluggedInSourceIsCheckedLikeAnyOther` is a worked example.

---

## 2. Priorities

A kind's priority weighs **how hard its proofs are** against **how often a condition needs it**.

| Kind | Priority | Proof complexity | Necessity |
|---|---|---|---|
| Annotation invariants | **high** | whatever the engineer wrote | they are the requirements |
| Process states | **high** | minimal: reads only `getPstate` off the path | every condition branches on process states; paths through impossible states become vacuous |
| Process pairs | mid | same as process states, but there are more of them | multi-process programs; states that never co-occur |
| Static-analysis claims | mid | same, pure `getPstate` | justify the pruning static analysis does |
| Defined values | mid | simple: constants, plus the hypothesis at `st0` | very often: what outputs are in each state |
| Stabilized values | mid | simple, plus `predEnv` | often: steady-state outputs |
| Unchanged since entry | mid | moderate: `prevProcState` through the path | frame reasoning across cycles |
| Timer bounds | mid | moderate: `ltime` arithmetic on `nat` | timed requirements ("at most 5 s") |
| Transitions | low | high: disjunctions quoting guards, under `let`/`prevProcState` | rarely needed directly; large |
| Copied on entry | low | high: relational, under `prevProcState` | occasionally: latched setpoints |

High is kept minimal on purpose: it enlarges every condition's goal. Anything else can be promoted by
naming it in the key.

---

## 3. The approaches

Each entry gives the invariant's **claim** (Isabelle, `s` one boundary), how it is found, why it is
provable, and how it is tested. The formula actually written is the claim's body in the annotation
wrap (1.1), at **program** scale for process states and static-analysis claims, and at **state**
scale `(P, q)` for everything else, the `getPstate s P = q ⟶` at the front of each claim being that
scope. For example, 3.3's defined value is written

```
∀ s1. ((toEnvP s1 ∧ substate s1 s) ∧ getPstate s1 ''Switch'' = ''lit'') ⟶ theInt (getVarVal s1 ''#level'' []) = 5
```

Examples are from `src/test/resources/programs-extra/lamp.rcs` and `crew.rcs`, two programs small
enough to work out by hand. `extra-invariants.tex` states every kind formally, with its algorithm.

### 3.1 Annotation invariants: high

`[invariant: …]` on the program, a process or a state. It is translated by `AnnTranslator` and
conjoined into `inv` as before, and is now also registered in the container (`kind=ANNOTATION`, with
`process`/`state` tags for its scope). Loop invariants are registered as `kind=LOOP_INVARIANT` and
written to `LoopInvariants.thy` as before. Neither is derived; both are the requirements themselves.

### 3.2 Process states (M): high

```
(getPstate s ''Switch'') = ''dark'' ∨ (getPstate s ''Switch'') = ''lit''
```

- **Found:** candidates "P is never in x" for every process and every state, including `stop` and
  `error`. What survives is removed from the list. Lamp: `never`, `stop` and `error` are unreachable.
- **Provable because:** `getPstate` changes only by `setPstate` with a literal state. At the cycle's
  last state it is read straight off the path; for a process not moved, `inv st0` gives it. A path
  through a state excluded here becomes vacuous.
- **Tests:** `aProcessIsOnlyFoundInTheStatesSomethingMovesItTo`,
  `processStatesAreTheSameWhateverElseIsAskedFor`.

### 3.3 Defined values (M, "defined non-stabilized"): mid

```
(getPstate s ''Switch'') = ''lit'' ⟶ theInt (getVarVal s ''#level'' []) = 5
```

- **Found:** candidates for every reachable state, every tracked scalar and every constant the
  variable is ever given (its initial value and each constant assigned). A defined fact is also a
  hypothesis: once a process is known to have begun the cycle in the state, the variable's value then
  is known.
- **Provable because:** if the process entered the state this cycle, the value is computed along the
  path. If it was there already, the hypothesis at `st0` gives it and the path does not write it.
- **Tests:** `aConstantSetOnTheWayInAndLeftAloneIsDefined`.

### 3.4 Stabilized values (M): mid

```
(getPstate s ''Switch'') = ''lit'' ∧ (getPstate (predEnv s) ''Switch'') = ''lit''
    ⟶ theBool (getVarVal s ''outp_0'' [])
```

- **Found:** as for defined values, but a boundary only has to satisfy it if the process was in the
  state at the boundary before too (`predEnv`). It is never a hypothesis. If the surviving facts
  contradict each other or the defined ones, the process never stays; the invariant would be vacuous,
  and is dropped (thermopot's `Init`).
- **Provable because:** `predEnv st_final` computes to `st0` (or to a loop's opaque state), whose
  process state the path or frame gives. The value is then computed along the path.
- **Tests:** `aConstantWrittenOnEveryPassIsStabilized`, `aStateNeverStayedInGetsNoStabilizedValues`.

### 3.5 Transition conditions (M): low

```
(getPstate s ''Switch'') = ''dark'' ⟶ (let s2 = prevProcState s ''Switch'' in
    (ltime s2 ''Switch'' ≥ 2000 ∧ getPstate s2 ''Switch'' = ''lit'')    -- timed
  ∨ toEnvNum emptyState s2 = 0)                                          -- initial
```

- **Found:** collected, not guessed. Each `set state`/`start` that really changes a process's state
  becomes one disjunct, stating at `s2`, the state just before the move:
  - the guards of the running process's body that still hold there (a guard is dropped once
    anything it reads is written);
  - the timeout if the move came from one (tagged `TIMED`);
  - the known states of the moving and the moved process.

  The start state of the first process also gets **initial**: nothing before `s2` was a boundary. A
  way in that asks for nothing makes the invariant trivial, and then it is left out.
- **`prevProcState`** is added to `ReflexBase.thy`: walking back, the first `setPstate` of the
  process that changed its state, and the state it was applied to. It gives the paper's
  `prevProcState` a definition.
- **The paper's subtypes:** "initial" is `toEnvNum emptyState s2 = 0` rather than `s2 = emptyState`,
  because initialisation writes variables before it moves the first process. "Conditional" and
  "timed" are as in the paper.
- **Provable because:** if the move happened this cycle, `prevProcState st_final` computes to the
  state before it, where the path's conditions hold (the guards read nothing written in between). If
  not, `prevProcState st_final = prevProcState st0` (past a loop, via the new frame fact) and the
  hypothesis gives it.
- **Tests:** `aTransitionIsTheGuardsThatHeldJustBeforeIt`,
  `theStartStateIsEnteredInitiallyAndATimeoutIsATimedTransition`,
  `aStartByAnotherProcessSaysWhereTheTargetCameFrom`.

### 3.6 Values unchanged since entry: mid

```
(getPstate s ''Switch'') = ''lit'' ⟶ (let s2 = prevProcState s ''Switch'' in
    getVarVal s ''#setpoint'' [] = getVarVal s2 ''#setpoint'' [])
```

- **Found:** candidates for every reachable state and every written variable, whatever its type:
  whole values, so arrays and structs count. It holds at a boundary if the variable was not written
  since the process's last known state change, or, with no change this cycle, anywhere in the cycle.
  It is the frame of a process state, and needs no constant. Values that a defined invariant already
  pins are not restated.
- **Provable because:** the same case split as transitions, with the path writing nothing to the
  variable in the window.
- **Tests:** `aVariableNothingWritesWhileInAStateIsUnchangedSinceEntry`,
  `aVariableWrittenOnTheWayInIsStillUnchangedSinceEntry`, `aVariableWrittenInsideTheStateIsNotUnchanged`.

### 3.7 Values copied on entry: low

```
(getPstate s ''Switch'') = ''lit'' ⟶ (let s2 = prevProcState s ''Switch'' in
    theInt (getVarVal s ''#setpoint'' []) = theInt (getVarVal s2 ''dial_0'' []))
```

- **Found:** two halves. The value is unchanged since entry (checked as in 3.6). Every way in assigns
  `v := e` before the move, with nothing `e` reads written in between (collected, like transitions,
  compared as rendered). This is the relational generalisation of a defined value: a setpoint
  latched from an input. A state entered at program start is left out.
- **Provable because:** the value at `s2` equals `e(s2)` by the path, and nothing writes `v` after.
- **Tests:** `aValueLatchedOnTheWayInIsCopiedOnEntry`, `aValueTheWaysInDisagreeOnIsNotCopied`.

### 3.8 Timer bounds: mid

```
(getPstate s ''Switch'') = ''lit'' ⟶ ltime s ''Switch'' < 2100      -- timeout 2 s, clock 100
```

- **Found:** for each state with a timeout of fixed duration `T`, the candidate `ltime < T + clock`.
  The timeout is checked at the end of every cycle spent in the state; if it moves the process on or
  resets its timer, the process is still there only if the check found `ltime < T`, and one tick has
  passed since. The walk tracks an upper bound on `ltime`:
  - reset to 0 by `setPstate`/`reset`;
  - capped by a not-reached timeout;
  - grown by `clock` at the environment step;
  - unknown after a loop, whose iterations each add a step.

  A reached timeout below the bound makes the path impossible (the static analysis's rule 7, derived
  here). A timeout body that neither leaves nor resets makes the candidate fail.
- **Provable because:** `ltime` is a `fun` in the program theory, so it computes along the path;
  `ltime st0` comes from the path's timeout condition, and the rest is linear `nat` arithmetic.
- **Tests:** `aTimeoutThatMovesTheProcessOnBoundsItsTime`, `aTimeoutThatLeavesTheProcessWhereItIsBoundsNothing`.

### 3.9 Process pairs: mid

```
(getPstate s ''Starter'') = ''begin'' ⟶ getPstate s ''Worker'' = ''stop'' ∧ getPstate s ''Helper'' = ''stop''
```

- **Found:** candidates "P in a and Q in b never together" for every pair of processes and their
  reachable states. Survivors are written from the earlier process's side, one invariant per state.
  As hypotheses they exclude combinations of starting states.
- **Provable because:** like process states, it reads only `getPstate`.
- **Tests:** `statesOfTwoProcessesNeverFoundTogetherAreExcluded`, `aSingleProcessHasNoPairs`.

### 3.10 Static-analysis claims: mid

```
(getPstate s ''Worker'' = ''stop'') = (getPstate s ''Helper'' = ''stop'')
  ∧ (getPstate s ''Worker'' = ''error'') = (getPstate s ''Helper'' = ''error'')
```

- **Found:** `StaticAnalysis` discards paths on per-process facts (StaticalAnalysis.tex rules 1–2 and
  the group rules). Those are claims about every boundary, and CLAUDE.md marks them provisional,
  because a wrong one silently drops proof obligations. Each claim is put through the same check.
  Survivors become invariants and so something Isabelle proves. Claims that do not survive are
  reported (`-x mid` prints them; `ReflexVcg.getExtraInvariantDiagnostics`).
- **Result:** every claim for newThermopot, newTurnstile, newSmartLighting and newBarrier is
  confirmed (`theMultiProcessProgramsClaimsAreAllConfirmed`). The facts the pruning of the four
  multi-process programs rests on are therefore inductive, though whether the pruning *uses* them
  correctly is a separate question.
- **Provable because:** pure `getPstate`, as above.
- **Tests:** `theStaticAnalysisGroupsAreConfirmedAndExported`, `anUnconfirmedClaimIsReported`,
  `aWrongGroupClaimIsReported`.

#### The `group` attribute does not mean what it should

The static analysis paper (IVMEM 2026, Ishchenko and Anureev, section 3) defines a group as
processes that "are always started, stopped or failed within one iteration of the control cycle";
different groups mean nothing. Rules 1 and 2 of its section 4.7 discard a path on which two
processes of a group disagree, at their turns in one cycle, on being in `error` or in `stop`. (The
paper prints those rules as `¬(p.s = stop ⊕ p'.s' = stop)`; the prose, and every implementation,
discard on disagreement, i.e. without the `¬`.) **Neither the paper's algorithm nor the port
guarantees what the rules assume.** The group rule is therefore **not** added as an invariant of
its own: only the group claims the check confirms are exported, as above.

**The paper's algorithm** (Algorithm 5 `ResolveStartStates`, read with `p'.startS = false` as its
authors confirm, and Algorithm 6 `BuildGroups`/`SetsDiv`/`SetsInter`) starts from a split by
`startS`. It then walks the whole program - each process, each state, each statement, branches
included - and at each construct splits every group by six classes of the changes in force there
(`nhPC = st.procChange ∪ hPC`): the processes {started, stopped, failed}, declared {before, after}
the current one. A change the current process makes **to itself** is in none of them, except a
restart in its first state: `stopPpred`/`stopPsucc` and `errorPpred`/`errorPsucc` test `p'.id <
pcur.id` and `p'.id > pcur.id`. So a process that stops or fails itself is never separated from
the processes it was grouped with.

*Example*, `programs-extra/groupRule.rcs`:

```
process Starter { state begin { start A; start B; stop; } }
process A       { state run   { stop; } }          -- groups.rcs: if (x) { stop; }
process B       { state open  { ; } }
```

`startS` is false for all three (`Starter` starts `A` and `B` in its first state, and `A`'s first
state cannot stop `B`). `Starter`'s `start A; start B` puts `A` and `B` in `startPsucc`, which splits
`Starter` off. `A`'s `stop` is a change to itself: no class, no split. Result: groups `[A, B]`,
`[Starter]` - the same as the port's, and the same with the printed `p'.startS = true`. But `A`
stops at its first turn and `B` never does: from the second cycle on `A` is stopped and `B` is
running. Rule 2 then discards every path on which one of them is asserted stopped and the other
not. Of the 27 paths without pruning (36 for the `if (x)` variant), the 3 with `A` stopped and `B`
running are all feasible, and all pruned; their proof obligations are lost. Among them is
`Starter=stop, A=stop, B=open`, the cycle the program performs forever after the first, so its
steady state is never verified. The check reports "group [A, B]: A and B are in stop together – not
confirmed". `group-rule-counterexample.tex` (local, not committed) traces it attribute by attribute
through Algorithms 1-6.

**The repair** files every change a process makes to itself - start, stop and error - with the
processes declared before it (`p'.id ≤ pcur.id` in the three `Ppred` classes), and drops the
first-state case of `startPsucc`: the process has had its turn this cycle, so it shows the change
at its next turn, like a predecessor. Filing only stops and errors that way is not enough:
in `programs-extra/groupSelfRestart.rcs` a process stops a predecessor and itself and then
restarts itself, which as printed is filed nowhere outside the first state, so the two stay grouped
while one is stopped and the other running. With the full repair, rules 1 and 2 discard no feasible
path; `group-rule-counterexample.tex` proves it (an activation that changes one process of a group
changes the other last to the same value, on the same side of the actor).

`inv/simulation/GroupRuleSimulationTest` implements Algorithms 5 and 6 as printed and checks every
grouping on random runs of every test program, comparing processes at their turns:

| grouping | programs a run refutes |
|---|---|
| as printed (either `startS` reading) | `groupRule`, `groups` |
| only stops and errors of itself filed with predecessors | `groupSelfRestart` |
| every change to itself filed with predecessors (the repair) | none |
| the port | `groupRule`, `groups`, `groupSelfRestart`, `groupStress` |

`programs-extra/groupStress.rcs` exercises the rest: pairs split by a process declared between
them, a stop undone by a restart in the same activation, a process failing a predecessor and itself.

**The port** (`ProcessFacts`) has the self-change defect and three differences from the paper:
`computeStartS` uses the misprinted `p'.startS = true` and checks the processes in between for a
`stop` in any state rather than a `stop` or `error` in the first; `refine` reads only the definite
changes of each *process* (`par` over its states), where the paper visits every statement, so a
change made under a condition, or in some states only, splits nothing - on `groupStress` it leaves
seven processes in one group; and it files any self-restart as a successor, not only one in the
first state. On every program in `programs-new` every grouping above agrees with
the runs.

The check tests boundaries, and the static analysis compares process states *at each process's
turn* within a cycle, so a reported claim is not always wrong by the analysis's own reading. In a
chain where `Starter` starts `A` and `A` starts `B`, rule 2 ("B is never stopped") is false at the
first boundary but true at every turn of `B`. The self-change defect produces claims false by
either reading.

---

## 4. Are they provable? Measured

### 4.1 Isabelle

`tools/check-with-isabelle.sh <dir> <Theory> @structural [filter] [timeout]` puts every condition
through one structured proof, listing every `extra_*_def` itself:

- **a cycle:** unfold `inv`, `constants` and every invariant; carry each wrapped goal across the
  cycle with `wrapped_step[OF pred]`, where `pred: predEnv st_final = st0`, and the hypothesis at
  `st0`; read the hypotheses at `st0` with `wrapped_here`; `auto` the rest along the path;
- **the base case:** the same, with `wrapped_empty` as the hypothesis.

Results at `-x low` (every kind), wrapped as annotations are, Isabelle2025-2, 600 s per condition,
2026-09-30:

| Program | Conditions checked | Proved |
|---|---|---|
| lamp | all (6 VC + 6 EXTRA) | 12 / 12 |
| crew | all | 16 / 16 |
| newThermopot | all | 84 / 84 |
| newTurnstile | all | 194 / 194 |
| newEscalator | all | 54 / 54 |
| newBarrier | all | 64 / 64 |
| ifTest1 | all | 6 / 6 |
| switchTest1 | all | 8 / 8 |
| loopSum | all | 6 / 10 |
| annotatedTank | `EXTRA` | 4 / 6 |
| palletStation | `EXTRA` (two loops in a row, one nested) | 5 / 15 |

Every program without a loop is proved in full. On the loop programs, every condition that fails
is either on a path through a loop - `VC1`/`EXTRA1` of loopSum, two of annotatedTank, ten of
palletStation - or loopSum's `LOOPENTRY`/`LOOPSTEP`, which need `substate_refl loopInv0_def`, as
they always have, and prove with them. The first group is the wrap's cost (1.1): the iteration
ends between a loop's entry and its opaque state are boundaries the wrap speaks about, and the
condition knows nothing of them. Before the wrap, the state-local form of the same invariants proved
on all of those paths too.

The main conditions of annotatedTank and palletStation were not put through `@structural`, because
their `inv` also carries the engineer's requirements, which is a different question.
`tools/palletStation.proofs` still replays 36 / 36 with process states in `inv` and the extended
loop frame, so nothing recorded broke.

### 4.2 Concrete runs

`inv/simulation/SimulationTest` runs every test program (all 17) for 20 runs of 120 cycles with random
inputs. It uses an interpreter over the same graph with a concrete ReflexBase state (`History`), and
evaluates every derived invariant's claim, as rendered, at every boundary: cycle ends and loop
iteration ends. `theWrappedInvariantsHoldAsWritten` evaluates the wrapped formulas themselves,
quantifier and all, on shorter runs. A deliberately false invariant is caught
(`aFalseInvariantIsCaught`). It runs in `mvn test`, so a wrong invariant is caught long before a
proof fails on it. It is what showed that, once wrapped, invariants must also be checked at loop
iteration ends.

---

## 5. Deviations from `mainOverview.tex`

- **The wrap** is the annotation invariants' (`∀ s1. toEnvP s1 ∧ substate s1 s ∧ scope ⟶ body`),
  the paper's `∀s. toEnvP s ⟶ …` restricted to the history of `s`.
- **`prevProcState`** is defined in `ReflexBase.thy`, and the paper's `let s2 = prevProcState s P in …`
  is kept literally.
- **Initial transitions** use `toEnvNum emptyState s2 = 0`, not `s2 = emptyState`: initialisation
  writes variables before the first `setPstate`.
- **"Not changed by processes executing after"** (paper, both variable kinds) is not a separate rule.
  The walk covers every process in the cycle, before and after.
- **The paper's groups** ("advanced", "optional") become priorities. Process states move from
  advanced to high, as part of `inv`; the variable kinds stay in the middle.

---

## 6. Not implemented yet

Ordered roughly by value against cost. The priority is where each would sit.

### Loops

- **Invariants for loops written without one** (would-be high for that loop). Loops of the shape
  `for (i = a; i < n; i++)` give `a ≤ i ≤ n`. Accumulators stepped by a constant give
  `x = x₀ + c·(i − a)`, which is palletStation's `placed = r*COLS`. Today such a loop's `loopInvN` is
  uninterpreted.
- **Variants for canonical loops** (would-be mid). `n − i`, so `LOOPBOUND`/`LOOPDECREASE` need no
  annotation.
- **Loop summaries instead of unknowns** (infrastructure). For example `i = n` on exit, or "the body
  always writes `v = c` and runs at least once". These would keep defined values and timer bounds
  alive through loops.
- **The `toLoop` boundary constructor** (a prerequisite). Needed before any history-closed claim,
  including the annotation invariants, can be proved on a path through a loop.

### States

- **Ranges and relations** (mid). Intervals or octagons per state (`0 ≤ counter ≤ 10`, `x ≤ y`),
  from widening the abstract domain.
- **Monotonicity** (mid). `v(s) ≥ v(predEnv s)` while in a state, for counters.
- **Minimum dwell time** (low). A lower bound on `ltime` at exit, from guards such as
  `ltime ≥ T` on every way out.

### Processes

- **Lifecycle** (mid). "Once stopped, never restarted", "P active ⟹ Q active", from `start`/`stop`
  targets across the program.
- **Single-writer facts** (mid). "Only P writes v, and only in states S": frame reasoning across
  processes becomes a lookup.
- **Order and communication** (low). Processes run in declaration order, so a value a later process
  reads is the one an earlier process wrote this cycle; relations such as `x_consumer = x_producer`.

### Whole program

- **Property-directed search (IC3/PDR)** (low, on demand). Start from a main condition that fails and
  derive the lemma it lacks, from interpolants or from the predicates the failed goal mentions. This
  is the only approach aimed at the engineer's own `inv`.
- **Constrained Horn clauses** (low, on demand). Encode one cycle for Spacer (Z3) or Eldarica, which
  fits the SMT-LIB translation `mainOverview.tex` plans. Import the solutions as a `CandidateSource`;
  the `EXTRA` obligations keep that sound.
- **Invariants spotted on runs, Daikon-style** (mid). The simulation interpreter already exists:
  record ranges, linear relations and implications keyed on process state over random runs, and feed
  them as candidates to the checker.
- **k-induction** (low). Facts inductive only over two cycles, generalising the stabilized kind:
  "set at most k cycles ago".
- **LLM-proposed candidates** (low). Untrusted by construction, which is exactly what the checker and
  the obligations are for.

### Infrastructure

- **Checking candidates with an SMT solver per path** instead of the abstract walk. It would accept
  exactly what the conditions can prove, and relational candidates would stop needing hand-written
  abstract transfer functions.
- **Choosing lemmas per condition** beyond "states on the path". Per the paper's "Auxiliary lemmas",
  fewer assumptions per condition matter once an SMT solver is in the loop.

---

## 7. Limits

- Only scalar bool, integer and time variables have tracked values (defined, stabilized, copied);
  reals, arrays and structs do not. Unchanged-since-entry works on whole values of any type.
- A process moved inside a loop body loses its `prevProcState` at the loop's opaque state, so
  since-entry and transition invariants about it drop out there. The frame covers only processes the
  loop does not move.
- The abstract domain is constants. Any fact that depends on a range, not a value, is found only via
  the timer bound.
- **Loops, because of the wrap.** A wrapped invariant speaks about loop iteration ends, so anything a
  loop body writes has no fixed value there, and invariants about it drop out. On a path through a
  loop, a wrapped goal is not provable at all (the iteration ends between the loop's entry and its
  opaque state are unknown), as for annotation invariants. The `toLoop` constructor (CLAUDE.md) is
  what would fix both.
- Every limit above costs invariants or provability, never soundness. Soundness rests on the obligations generation
  emits, not on the analysis.
