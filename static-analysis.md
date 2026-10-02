# Static analysis of Reflex programs

The static analysis discards paths that no run of the program can take, so that no verification
condition is generated for them. This file is the reference for what it does: the attributes it
computes, the rules it applies, why each rule is sound, and where each lives in the code.

It replaces `StaticalAnalysis.tex` and is based on the IVMEM 2026 paper by Ishchenko and Anureev
("Using static analysis to reduce the number of correctness conditions generated for Reflex
programs"), sections 3 and 4. It follows the paper's numbering (Algorithms 1-6, rules 4.1-4.7), with
the corrections in section 7 applied. Where it says "the paper", it means that one.

Everything here is implemented in `su.nsk.iae.reflex.analysis` and checked by the tests in section 8.

---

## 1. The model

**Program.** A list of processes `p₁ … pₙ`; `p.id` is the declaration order. A process is
*running* in one of its own states, *stopped* (state `stop`) or *failed* (state `error`); these
are its **status**: `active`, `stop`, `error`. Initially the first process is running in its first
state and every other process is stopped.

**Cycle.** One execution cycle dispatches every process once, in id order, in whichever state it
occupies at its turn. A running process executes one path through the body of its current state,
to its end. A stopped or failed process executes nothing. Then the program yields to the
environment (`toEnv`).

**Process statements**, in canonical form (the preprocessing makes the target explicit):
`start p'` (to the first state of `p'`, whatever it was doing; `restart` is `start` of oneself),
`stop p'`, `error p'`, `set state s'` (only of oneself; `set next state` is resolved), and
`reset timer`. The expression `process p' in state k`, `k ∈ {active, inactive, stop, error}`,
reads a status; negated it also gives `nonstop` and `nonerror`.

**Local time.** `ltime p` counts the time since `p` was last moved to a state - by anyone, to any
state, the same one included - or since it reset its timer. It grows by the clock at every `toEnv`.
`timeout t {…}`, the last statement of a state, runs its body when `ltime p ≥ t`. This is
`ReflexBase.thy` with the `ltime` of the generated program theory.

**Loops.** The verification conditions cut a `for` out of the path (CLAUDE.md, "Annotations"):
the path continues past it from an opaque state known through the loop invariant and the frame.
In that model every iteration ends in a `toEnv`, so local time may grow across a loop.

**Path.** The conditions of one cycle are the paths through its control-flow graph. A path is a
sequence of dispatches (`InState`: process `p` found in state `s`), guards, timeout branches,
assignments and process statements, with a loop represented by its cut. The analysis walks the
path and keeps `passed`, the sequence of facts and changes so far (section 4).

**Feasible.** A path is feasible if some run of the program, in the model the conditions are
stated in, performs it as a cycle. The analysis must never discard a feasible path: that would
silently drop a proof obligation. Discarding an infeasible path is the point.

---

## 2. Basic attributes (Algorithms 1-3)

Every construct `st` - statement, block, `if`, `switch` case, loop, timeout, state, process -
gets these attributes:

| attribute | meaning |
|---|---|
| `procChange : P ⇀ {start, stop, error}` | every path through `st` changes `p'`, and its last change to `p'` in `st` is this |
| `potProcChange ⊆ P × {start, stop, error}` | some path through `st` may make this change |
| `reset : bool` | every path resets the current process's timer |
| `stateChanged : bool` | every path changes the current process's state |
| `reachTo` (`changesTo`) ⊆ states ∪ {stop, error}, `mayStay : bool` | where a path through `st` leaves the current process; `mayStay` if some path leaves it where it was |

**Algorithm 1 - leaves.** For statement `st` in state `s` of process `p`:

| statement | `procChange` | `potProcChange` | `reset` | `stateChanged` | `reachTo` |
|---|---|---|---|---|---|
| `start p'`, `p' ≠ p` | `{p': start}` | `{(p', start)}` | | | |
| `restart` (`start p`) | `{p: start}` | `{(p, start)}` | ✓ | iff `s ≠ first(p)` | `{first(p)}` |
| `stop p'`, `p' ≠ p` | `{p': stop}` | `{(p', stop)}` | | | |
| `stop` (`stop p`) | `{p: stop}` | `{(p, stop)}` | ✓ | ✓ | `{stop}` |
| `error p'`, `p' ≠ p` | `{p': error}` | `{(p', error)}` | | | |
| `error` (`error p`) | `{p: error}` | `{(p, error)}` | ✓ | ✓ | `{error}` |
| `set state s'` | | | ✓ | iff `s' ≠ s` | `{s'}` |
| `reset timer` | | | ✓ | | |
| anything else | | | | | |

**Algorithm 2 - a sequence `st₁; …; stₖ`.** Fold from the empty attributes. For the next `st'`:

- a definite change survives `st'` unless `st'` may change the process differently without
  definitely changing it: for `p' ∈ defined(pc) ∩ (pot(st') \ defined(st'.procChange))`, drop
  `pc(p')` if it is `start` and `st'` may stop or fail `p'`, if it is `stop` and `st'` may start or
  fail it, if it is `error` and `st'` may start or stop it;
- `pc := pc ⊕ st'.procChange` (the later value wins);
- potential changes of a process `st'` definitely changes are dropped, then `st'`'s are added;
- `reset`, `stateChanged`: or; `reachTo`: where the sequence ends - `st'`'s targets if `st'`
  always moves the process, else both.

**Algorithm 3 - alternatives** (the branches of an `if`, the cases of a `switch`, the states of a
process). A definite change survives only if every branch makes it; potential changes are united,
with each definite one written as such; `reset` and `stateChanged`: and; `reachTo`: united, and
`mayStay` if some branch may stay. An `if` without `else`, a `switch` without `default`, a timeout
body and a loop body are alternatives with a branch that does nothing.

**Property used by every proof below.** If `st.procChange(p') = X`, then every path through `st`
changes `p'`, and its last change to `p'` inside `st` is `X`. (A potential change that disagrees
removes the definite one; a later definite one overrides it; alternatives keep only what all
branches agree on.)

---

## 3. Derived attributes (Algorithms 4-6)

### 3.1 `reachS`, `reachE` - Algorithm 4

`p.reachS` iff some state of `p` has `stop ∈ reachTo`, or some process has `(p, stop)` in its
`potProcChange`. `p.reachE` likewise for `error`. If `p.reachE` is false, `p` is never failed; if
`p.reachS` is false, nothing ever stops `p` (it may still be stopped because it was never started).

### 3.2 `startS` - Algorithm 5 (`ResolveStartStates`)

`p.startS`: whether `p` may be found stopped at its turn **in the first cycle**. Default: `false`
for the first process, `true` for every other. Then, in id order, `p.startS := false` if

```
∃ p' . p'.id < p.id ∧ p'.startS = false ∧ first(p').procChange(p) = start
     ∧ ∄ p'' . p'.id < p''.id < p.id ∧ ((p, stop) ∈ first(p'').potProcChange
                                        ∨ (p, error) ∈ first(p'').potProcChange)
```

*Why it is sound.* In the first cycle every running process is in its first state: the first
process starts there, every other runs only if started this cycle, which puts it there. `p'` is
running in its first state, which definitely ends by starting `p`; afterwards only processes
between `p'` and `p` run before `p`'s turn, each in its first state or not at all, and none of
those may stop or fail `p`.

The paper prints `p'.startS = true`, which would derive "never stopped" from a starter that is
itself stopped. Corrected with the authors.

### 3.3 `reachFrom`, `alwaysLeaves`

`first(p).reachFrom` is the set of states with a `set state first(p)` in them. `alwaysLeaves(s)`
holds when every path through `s` ends in another state: `¬s.mayStay ∧ s.reachTo ≠ ∅ ∧ s ∉
s.reachTo`. A restart counts as reaching the first state (Algorithm 1), so a first state ending in
`restart` does not always leave.

**Just started.** `justStarted(p, s)` iff `s = first(p)`, `first(p).reachFrom = ∅` and
`alwaysLeaves(first(p))`. Then a process found in `s` was started since its previous turn: had it
been there at its previous turn and run, it would have left, and no `set state` leads back.

### 3.4 `group` - Algorithm 6 (`BuildGroups`), repaired

**What it promises.** If `p.group = p'.group`, then at every point of every cycle where both are
looked at, they agree on being stopped and on being failed, and each start reaches both alike.
(The paper: "they are always started, stopped and put into the error state simultaneously.")

**Algorithm.**

```
BuildGroups(r):
    s := { {p | p.startS = false}, {p | p.startS = true} }
    for p in processes(r):  actor := p;  s := SetsDiv(s, ⊥, p)
    p.group := index of the set containing p

SetsDiv(s, hPC, st):
    nhPC := st.procChange ∪ hPC                      -- the changes in force at st
    for X in {start, stop, error}:
        before := { p' | nhPC(p') = X ∧ p'.id ≤ actor.id }
        after  := { p' | nhPC(p') = X ∧ p'.id > actor.id }
        s := SetsInter(SetsInter(s, before), after)
    for line in lines(st):  s := SetsDiv(s, nhPC, line)
    return s

SetsInter(s, c) := { x ∩ c | x ∈ s } ∪ { x \ c | x ∈ s }   (empty sets dropped)
```

`lines(st)`: the states of a process; the statements and the timeout of a state; the body of a
timeout or a loop; the statements of a block; both branches of an `if`; the cases of a `switch`
and the statements of a case. The walk visits every statement, so a change made in one branch, one
state or one loop body splits what it should.

**The sides** say when a process shows a change made by `actor`. One declared after it shows the
change at its turn in this cycle. One declared before it has already had its turn, and shows it
in the next cycle. The actor itself has also had its turn, since that turn is what it is executing,
so a change it makes **to itself** belongs on the same side as the processes before it: `≤`.

**The repair.** The paper writes `p'.id < actor.id` for all three `…Ppred` classes and adds
`p'.id = actor.id ∧ s_cur = first(actor)` to `startPsucc` only. So a process stopping or failing
itself is filed in no class and never splits a group. `programs-extra/groupRule.rcs` is the
smallest program where that discards feasible paths: `Starter{start A; start B; stop;}`,
`A{stop;}`, `B{;}`. A and B are grouped, and from the second cycle on A is stopped while B runs.
Filing only stops and errors with `≤` is not enough. In `programs-extra/groupSelfRestart.rcs` a
process stops a predecessor and itself, then restarts itself in a non-first state, which as
printed is filed nowhere. Every change of the actor to itself, restarts included, must go on the
`≤` side. The first-state case of `startPsucc` then goes.

**The union** in `nhPC` is not specified when both sides define a process. The implementation
keeps the enclosing value. The grouping is sound either way, because the proof uses only the value
at the statement making a last change.

**Why it is sound** (full proof: `group-rule-counterexample.tex`, local).
1. *Windows.* The changes to `q` made between its turns in cycles `c-1` and `c` are those of
   activations `(a, c-1)` with `a.id ≥ q.id` and `(a, c)` with `a.id < q.id`.
2. *Same changes.* If `p` and `p'` share a group, every activation changes both or neither, last
   to the same value, and `a.id ≥ p.id ⇔ a.id ≥ p'.id`. Take the statement making the last change to
   `p`. There `nhPC(p)` is that change, so `p'` is in the same class through an enclosing construct
   that definitely changes it. The symmetric argument from `p'`'s last change, together with
   contiguity of constructs, makes the two last changes coincide.
3. *Induction over cycles.* Both windows hold the same last change or none. The first cycle is
   handled by the initial split: `startS = false` processes are running at their first turn,
   `startS = true` ones begin stopped.

### 3.5 Measured

| program | paths | kept | kept by the old pipeline |
|---|---|---|---|
| ifTest1 / ifTest2 / ifTest3 | 4 / 4 / 5 | 2 / 2 / 3 | 2 / 4* / 3 |
| switchTest1 / switchTest2 | 5 / 6 | 3 / 4 | 3 / 4 |
| newEscalator | 42 | 26 | 26 |
| newBarrier | 72 | 32 | 32 |
| newThermopot | 180 | 40 | 40 |
| newSmartLighting | 486 | 119 | 126 |
| newTurnstile | 1008 | 122 | 133 |

\* the old generator also emitted two division domain conditions. `StaticAnalysisMeasurementTest`
prints this table.

---

## 4. The rules (section 4 of the paper)

### 4.0 What a path knows

Walking a path, the analysis keeps `passed` as a sequence of events (`Event`):

| event | from | |
|---|---|---|
| `StateAsserted(p, s)` | a dispatch `InState` | `p` is in `s` |
| `StatusAsserted(p, k)` | a guard asserting `process p in state k` | |
| `Changed(p, X)` | a `start`/`stop`/`error` statement on the path | |
| `MayChange(p, X)` | a loop cut whose body may make that change | |
| `TimerReset(p)` | any statement moving `p` to a state (`SetState`, by anyone) or resetting its timer | `ltime p = 0` |
| `TimePassed` | a loop cut | local time may have grown |

A guard asserts only what holds on every way through it: the conjuncts of a condition, and the
negations of the disjuncts of a negated one (`Term.assertedBy`).

From `passed` three things are derived for a process `p`:

- **Possible statuses** `Poss(p) ⊆ {active, stop, error}`. Start from `Init(p)` (4.1) and fold:
  `StatusAsserted(p,k)` and `StateAsserted(p,s)` intersect with the statuses they allow;
  `Changed(p,X)` replaces the set with `{status(X)}`; `MayChange(p,X)` adds `status(X)`.
  Statuses allowed: `active` {active}, `inactive` {stop, error}, `stop` {stop}, `error` {error},
  `nonstop` {active, error}, `nonerror` {active, stop}; a state: its status.
- **Changed** `Changed(p)`: the path definitely started, stopped or failed `p`. Then `p`'s state
  at its turn is the result of the last change that actually happened, a definite one or a
  possible later one.
- **Timer zero** `Zero(p)`: some `TimerReset(p)` with no `TimePassed` after it.

This is the "nothing in between allows the difference" condition every rule needs, stated once.
A fact constrains a later one exactly when no change of the process lies between them. A change
replaces what was known by its result, and a change that may have happened weakens it.

### 4.1 Redundant states

1. `p.reachE = false ∧ p.s = error` - discard.
2. `p.reachS = false ∧ p.startS = false ∧ p.s = stop` - discard.

The same facts seed `Poss`: `Init(p) = {active} ∪ {stop | p.reachS ∨ p.startS} ∪ {error | p.reachE}`.
So a status check can be discarded on them too (`process p in state inactive` for a process
that can never be stopped or failed).

*Sound:* 3.1 and 3.2.

### 4.2 Timer reset

At a timeout `timeout t` of process `p`, the branch where it elapsed is discarded if
`Zero(p)` and `t` is a known number `> 0` (a literal, or a constant built from literals with
`+ - *`).

*Sound:* `ltime p = 0` from the reset until the next `toEnv`, and `0 < t`.

*Corrections:*
- The paper's rule looks at resets in `p.s` only. The implementation used to look at resets made
  by any process, which discarded feasible paths. `newTurnstile` lost conditions to it.
- In the model any move of `p` zeroes its time. So a process started this cycle by one dispatched
  before it cannot reach a timeout in that cycle either. That case is added, as agreed.
- `t > 0` is required: `timeout 0` elapses at local time zero.
- A loop on the path ends the knowledge, because each iteration ends in a `toEnv` in the model.

### 4.3 Status against status

At a guard asserting `process p in state k`: discard if `Poss(p) ∩ allowed(k) = ∅`.

This covers the paper's six rules, each with the condition that no change of `p` lies between the
two checks (the paper prints that condition garbled, as "no start followed by a stop" and the
like). It also covers the combinations the six pairwise rules miss, e.g. `!(p in state error)`,
then `!(p in state stop)`, then `p in state inactive`: each pair is compatible, all three are not.
That one was chosen over pairwise comparison, with the authors.

### 4.4 Status after a change

The same check: after `Changed(p, X)` `Poss(p) = {status(X)}`, so a later check incompatible with
`X` and with no change in between is discarded. This is the paper's six rules of 4.4.

### 4.5 State after a change

At the dispatch of `p` in state `s`:

1. `Changed(p) ∧ status(s) = active ∧ s ≠ first(p)` - discard.
2. the last change was a stop, `s ≠ stop` - discard;
3. the last change was an error, `s ≠ error` - discard.

2 and 3 are the `Poss` check of 4.6: after `Changed(p, stop)`, `Poss(p) = {stop}` until something
else changes `p`. 1 says: a change was made this cycle and `p` is running, so the last change that
actually happened was a start, the only one that leaves a process running, and a start puts `p`
in its first state. It covers the paper's "started, nothing in between" (4.5.1). It also covers
"started, then perhaps stopped by a loop" and "stopped, then perhaps restarted".

The condition "nothing in between allows the difference" is built into `Poss` and `Changed`: a
later change replaces an earlier one, and a possible one weakens what is known. As printed, the
paper's between-condition reads "no stop or error that is followed by a start", which would
discard a process started, then stopped, then dispatched in `stop`.

*Sound:* the changes on the path are the ones made, in order, with loops as possible changes. No
process runs between them and `p`'s dispatch except those on the path, and `p` cannot change its
own state before its turn.

### 4.6 State against status

At the dispatch of `p` in `s`: discard if `status(s) ∉ Poss(p)`.

The paper prints this rule with no "nothing in between" condition. Read literally, it discards
`if (process W in state active) { stop W; }` followed by W dispatched in `stop`, a feasible path.
With `Poss`, the change in between replaces the earlier fact.

### 4.7 Groups

At the dispatch of `p` in `s`, for every process `p'` dispatched earlier in the cycle in `s'`
with `p'.group = p.group`:

1. `(s = error) ⊕ (s' = error)` - discard.
2. `(s = stop) ⊕ (s' = stop)` - discard.
3. `justStarted(p, s) ∧ s' ≠ first(p')` - discard.
4. `justStarted(p', s') ∧ s ≠ first(p)` - discard.

The paper prints 1 and 2 as `¬(… ⊕ …)`, which would discard the paths where the two *agree*. Its
own prose, and the implementation, discard on disagreement. 3 and 4 are printed with
`p.s.procChange = true`, which is not a boolean attribute. They are read as `justStarted` (3.3),
as agreed.

*Sound:* 1 and 2 are the promise of 3.4. For 3 and 4: `p` in `first(p)` with `justStarted` was
started since its previous turn, by a start that reached `p'` alike (3.4, item 2, with restarts
filed). `p'` has not run since, so it is in `first(p')`.

### 4.8 Combining the rules

Rules 4.1-4.6 are the *simple* rules and 4.7 the *group* rules. `Combination.EITHER`, the default,
discards a path when either set objects. `Combination.BOTH` discards only when both object, the old
specification's `||` read literally; status checks and timeouts are then never discarded.

---

## 5. Soundness, and how it is checked

Each rule above comes with its argument. Together they rest on three things: the attribute property
of section 2, the window argument of 3.4, and the fact that a path lists every change it makes, in
order, with loops as possible changes.

**On runs.** `StaticAnalysisSoundnessTest` runs every test program 30 times for 150 cycles with
random inputs, over the graph the conditions come from. It replays each cycle's path through
`StaticAnalysis.step`, the same call the enumerator makes, and fails if a cycle a run performed is
rejected. `programs-extra/analysisStress.rcs` is written to reach every rule, and
`groupFirstStates.rcs` to reach 4.7.3/4.7.4. The test was checked against the bugs it is meant to
catch, each put back in turn: global timer resets, ignored loop bodies, unfiled self-changes, and
the lost "may stay". Each makes it fail.

**Groups.** `GroupRuleSimulationTest` implements Algorithm 6 as printed and repaired, and shows the
printed one refuted on runs while the repaired one, the implemented one, is not.

**Rule by rule.** `StaticAnalysisRulesTest`: for every rule, a program where it discards a path and
a look-alike where it must not.

---

## 6. Where it lives

| what | code |
|---|---|
| Algorithm 1 (leaves) | `AttributePreparation.compute`, `processControl`, `restartSelf` |
| Algorithms 2, 3 | `AttributeCalculus.cons`, `par`, `concludePar`, `optional` |
| attributes on the graph | `CfgBuilder.carrying` (process statements); loop cut: `CfgBuilder.buildFor` |
| Algorithm 4 | `ProcessFacts.compute` |
| Algorithm 5 | `ProcessFacts.computeStartS` |
| Algorithm 6 | `ProcessFacts.Grouping`, `lines`, `intersect` |
| `reachFrom`, `alwaysLeaves`, `justStarted` | `StaticAnalysis` |
| events, `Poss`, `Changed`, `Zero` | `Event`, `PathState.possibleStatuses`, `changed`, `timerReset`; `Status` |
| walking a path | `StaticAnalysis.step`, called by `PathEnumerator.walk` |
| 4.1, 4.5, 4.6 | `StaticAnalysis.simpleAllowsState`, `atCycleStart` |
| 4.2 | `StaticAnalysis.allowsTimeout`, `durationValue` |
| 4.3, 4.4 | `StaticAnalysis.allowsActivities`; guards: `Term.assertedBy` |
| 4.7 | `StaticAnalysis.groupAllowsState` |
| claims exported as invariants | `inv/StaticAnalysisClaims` (rules 4.1 and 4.7 at boundaries, see extra-invariants.md 3.10) |

Rule 4.1.2 holds at a process's turn, not at boundaries. A process started in the first cycle
before its turn is stopped when the program begins. So `StaticAnalysisClaims` exports it only for
the first process.

---

## 7. Differences from the paper

| where | the paper | here | why |
|---|---|---|---|
| Algorithm 1, restart / `set state s'` | `reachTo = s` | first state / `s'` | misprint |
| Algorithm 1, `set state s'` | `stateChanged` iff `s' ≠ s` | same; the port had it always | fixed in the port |
| Algorithm 2 `stateChanged` | reset when the sequence returns to `s` | not reset (or); rules use `alwaysLeaves` from `reachTo` instead | `reachTo` is "where it ends", which is what 4.7.3 needs |
| Algorithm 5 | `p'.startS = true` | `false` | misprint, confirmed |
| Algorithm 6 | self-changes filed only for a first-state restart | every self-change on the `≤` side | counterexamples 3.4 |
| Algorithm 6 `nhPC` union | unspecified | enclosing value kept | either is sound |
| 4.1 | dispatch only | also seeds `Poss` for checks | same facts |
| 4.2 | resets in `p.s` | any zeroing of `ltime p` this cycle, start by an earlier process included; `t > 0`; cleared by a loop | model semantics, confirmed |
| 4.3-4.6 | pairwise, between-conditions garbled or (4.6) missing | one possible-status set per process | confirmed; strictly stronger, still sound |
| 4.5 | `p ∈ p'.s'.st'.stop` notation, wrong between-condition | `Poss(p)`; 4.5.1 for any definite change, not only a start | as asked; sound, see 4.5 |
| Algorithm 2 `reachTo` port | a construct that never moves followed by one that may lost "may stay" | kept | port bug: made 4.7.3 discard feasible paths |
| 4.7.1-2 | `¬(… ⊕ …)` | `… ⊕ …` | misprint; prose agrees |
| 4.7.3-4 | `p.s.procChange = true` | `justStarted` | confirmed |
| loops | not discussed | a loop cut contributes `MayChange` and `TimePassed` | the main path does not walk the body |

`StaticalAnalysis.tex`, the earlier description, differs further. Its `setStartS` reads an undefined
`proc.active`, `setReachE` assigns `reachS`, rule 7 (the timer) is inverted, and its six group
classes are the paper's. The implementation followed it, with readings marked `SPEC`, until it was
moved to this description.

---

## 8. Tests

| test | covers |
|---|---|
| `analysis/StaticAnalysisRulesTest` | every rule on small programs: one path it discards, one look-alike it keeps |
| `analysis/AttributeTest` | Algorithms 1-3 on single constructs |
| `analysis/ProcessFactsTest` | Algorithms 4-6: `reachS`/`reachE`, `startS` and what blocks it, groups and the repair |
| `analysis/PathStateTest` | `Poss`, `Changed`, `Zero` event by event |
| `analysis/StaticAnalysisMeasurementTest` | the table of 3.5; single-process programs match the old pipeline |
| `inv/simulation/StaticAnalysisSoundnessTest` | no cycle a random run performs is discarded, on every test program |
| `inv/simulation/GroupRuleSimulationTest` | Algorithm 6 as printed is refuted on runs; the repair, which is the implementation, is not |
| `inv/StructuralInvariantsTest` | the analysis's claims exported as invariants are confirmed |

---

## 9. Not covered

- No path is discarded inside a loop body. Bodies are enumerated separately and the analysis
  reasons about whole cycles.
- Only facts asserted on every way through a guard count. `a || process p in state stop` says
  nothing about `p`.
- A timeout duration is "known" only if it is a literal or a constant built from literals with
  `+ - *`.
- `if`, `switch` and expression guards are not evaluated, which the paper also leaves out. A guard
  is a fact only for its process-status checks.
