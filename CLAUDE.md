# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

ReflexVCG generates formal verification conditions (VCs) for programs written in **Reflex**, a small
C-like DSL for programming PLC-style reactive controllers (processes with states, transitions, timeouts).
Given a `.rcs` source file it parses the program, lowers it to an IR, rewrites that IR into a canonical
form, builds a control-flow graph, enumerates the paths through one execution cycle, and emits an
Isabelle/HOL lemma per path (one `.thy` file per VC) that a human later proves. `FinalVCGRules.pdf` holds
the formal rules; `Preprocessing.tex` specifies the preprocessing passes.

## Build / test / run

Maven project, Java 17. `NewReflex.g4` and `ReflexAL.g4` are compiled during `generate-sources`, so
always build through Maven — the generated parsers are not in `src/`.

```
mvn compile                                   # generate parsers + compile
mvn test                                      # run all tests
mvn test -Dtest=NewPipelineEndToEndTest       # a single test class
mvn package                                   # builds the jar-with-dependencies
```

```
java -jar target/ReflexVCG-1.0-jar-with-dependencies.jar -s program.rcs [-o outDir] [-g] [-a true|false]
```

- `-s` source `.rcs` path (required)
- `-o` output directory (defaults to the source's directory)
- `-g` also export the control-flow graph as Graphviz
- `-a` discard conditions for impossible paths (default true)

## Pipeline architecture

Each stage has one input and one output, and no stage knows about the others. Isabelle syntax exists in
exactly one place, at the very end.

```
.rcs ──1──▶ parse tree + comment tokens
      2──▶ Reflex IR, annotations bound to constructs
      3──▶ canonical IR   [mangle → cast → normalize]
      4──▶ control-flow graph
      5──▶ VerificationCondition objects (symbolic)
      6──▶ .thy files
```

1. **Parse** — `NewReflex.g4` produces the tree; `ReflexAL.g4` parses annotations. Comments are on the
   **hidden channel**, so they can appear anywhere.
2. **Lower + bind** — `frontend/AstBuilder` translates the tree to IR (`ir/`) without changing meaning.
   `frontend/AnnotationBinder` pulls Reflex-AL annotations from comments and binds each to the construct
   whose first token it precedes, claiming each exactly once.
3. **Preprocess** (`preprocess/`, spec: `Preprocessing.tex`) — `Preprocessor.run` in dependency order:
   `NameManglingPass` (globally unique `node#process#state#variable` names — this is why there is no
   variable lookup table), then
   `CastInsertionPass` + `TypeEnvironment` (every expression typed, every conversion an explicit cast),
   then `NormalizationPass` (`set next state` resolved, switch fall-through expanded, `wait`/`slice`
   turned into ordinary states). The result is *canonical* IR.
4. **Graph** — `cfg/CfgBuilder` builds one execution cycle: every process runs once in declaration order,
   in whichever state it occupies, then the program yields to the environment. Branching is a node with
   several successors, each starting with a guard. `cfg/ExprLowering` evaluates an expression into the
   ways it can run (see **C expression semantics** below). Nodes carry IR, never rendered text.
5. **Enumerate** — `cfg/PathEnumerator` walks paths depth-first; each becomes a `VerificationCondition`
   of symbolic `VcStatement`s. `analysis/StaticAnalysis` (spec: `StaticalAnalysis.tex`) discards a path
   at the node that makes it impossible, so the subtree below is never explored. A path may produce more
   than one condition: an annotation on it contributes the obligation discharging it, and a loop
   contributes its entry and preservation conditions. Inline C, and a `for` with no invariant, become
   `Unsupported` nodes and stop generation.
6. **Render** — `vc/IsabelleRenderer` is the *only* class that knows Isabelle; `vc/VcWriter` writes the
   files. Values live in ReflexBase's `val` datatype: read with `getVarVal` then a projection
   (`theInt`/`theNat`/`theBool`/`theReal`), written through the matching constructor.

`ReflexVcg` wires it together; `Main` is the CLI.

## Key points

- **The static analysis readings are provisional.** `StaticalAnalysis.tex` was never verified and is
  internally inconsistent in about ten places; every reading taken is marked `SPEC` in `analysis/`.
  `IvReadings2026.pdf` states the rules precisely and is the better reference. Every single-process
  program now reproduces the old pruned counts exactly; the four multi-process ones differ, in the
  grouping rules. Over-pruning silently drops proof obligations, so treat those four as unconfirmed.
  `StaticAnalysisMeasurementTest` prints the table; `StaticAnalysisRulesTest` covers rules one by one.
- **C expression semantics** (`cfg/ExprLowering`). Reflex takes them from C, so a write can sit
  anywhere inside an expression and a statement's expression need not be an assignment. An expression
  therefore lowers to a *sequence* of `CfgNode`s — the writes it performs, in order — plus a value.
  - **Floating reads.** A read carries no state until something needs its value, and is *fixed* at
    whatever state is current then; once fixed it never moves. That single rule gives `v++` the old
    value (fixed before its own write) and `++v` the new one (left floating past it).
  - **The incoming stage.** Evaluation is told how many writes are already behind it. Without this a
    cast or a negation wrapping an operand fixes it too early — the bug that made `v = v++ + v` read
    the pre-increment value on *both* sides. This is what the old `ExpressionVisitor2` got from taking
    the current state as a constructor argument, the right operand being visited by a visitor already
    positioned after the left.
  - **Several outcomes.** Short-circuiting yields one per way (`&&` either stops at a false left or
    goes on, and the right operand's writes belong only to the way that runs it); operands that each
    evaluate several ways multiply out. This is the old `ExprGenRes` list.
  - **`IrExpr.At`** is the pin: a read stated some number of states back. `ExprLowering` emits it as a
    distance, `PathEnumerator` resolves the distance to a state name — a graph node is shared by every
    path through it and the paths number their states differently, so resolving copies. Expressions
    that write nothing carry no pins at all, so nothing here costs anything for ordinary code.
- **`ReflexBase.thy` is the semantics** (`src/main/resources/ReflexTheory/`). One `val` datatype with an
  access path, rather than four typed getters. Reflex types map onto HOL as: signed ints → `int`,
  unsigned and `time` → `nat`, `bool` → `bool`, float/double → `real`. Changing codegen means keeping
  step with this file.
- **Annotations reach the output four ways** (spec: `Annotations.tex`, language: `Reflex-AL.pdf`).
  `ann/AnnTranslator` turns a bound annotation into a `term/Term`; nothing else renders one.
  - `assume` — an obligation proving it (`ASSUME<n>.thy`), *and* an intermediate assumption in every
    main condition that passes it.
  - `assert` — the same obligation (`ASSERT<n>.thy`), and nothing else. Both kinds are stated where
    they are written, in front of the statement they annotate; the only difference is that assumption.
  - `invariant` on the program, a process or a state — conjoined into `inv` in `Requirements.thy`, so
    every condition carries it without restating it. The scope decides the guard: none for a program,
    "not stopped and not in error" for a process, "in that state" for a state.
  - `invariant` on a `for` — the loop is cut out of the path. It becomes `LOOPENTRY<n>` (holds on
    entry), `LOOPSTEP<n>` (one iteration preserves it), and an opaque state on the main path knowing
    only the invariant and the negated loop condition.

  Mangling and typing run over annotations along with the code, so an unqualified name resolves in the
  scope the annotation sits in. `AnnotatedGenerationTest` covers all of this against
  `programs-new/annotatedTank.rx`, which carries one of every kind.
- **An obligation is written once, not once per path.** It depends only on the path up to where it is
  stated, so every path continuing past it restates it word for word. `VcWriter` drops the repeats,
  comparing lemmas with their bound state names renumbered, since those come from a program-wide
  counter and differ between otherwise identical statements.
- **The IR is mutable on purpose.** The preprocessing passes rewrite the program in place, which is not
  expressible against an ANTLR tree. Use `IrCopier` when duplicating a subtree — sharing one would let a
  later rewrite of one place silently change another.
- **Regression gate.** `NewPipelineEndToEndTest` asserts path counts per program against the old
  pipeline's unpruned behaviour, and `StaticAnalysisMeasurementTest` against its pruned behaviour.
  Those numbers are recorded in the tests; the old pipeline that produced them has been removed, so
  they cannot be regenerated - see the history if the detail is ever needed.
- **Generation must stay deterministic.** It was not: maps keyed by graph nodes without `hashCode`
  iterated in identity-hash order, so the same input produced different VC sets on different runs. Prefer
  insertion-ordered collections and explicit sorts anywhere output depends on order.

## Not done yet

- **The pruned counts for the four multi-process programs are unconfirmed.** They differ from the old
  pipeline, whose grouping was the non-deterministic part, so matching it is not evidence either way.
- **`ExtraInvariantGenerator` still does nothing.** Its hooks are wired in and reach the annotations,
  the graph and each generated condition, but every one returns its input unchanged.
- **Two gaps in `ReflexAL.g4`.** Its `variable` rule writes a qualified prefix with exactly two
  separators, so a program-scope variable has to be written `##x` to mean the mangled `#x`
  (`AnnMangling` normalises that away), and a state-scoped name — four parts,
  `node#process#state#x` — cannot be written at all. Unqualified names resolve by scope, which is
  what the test programs use.
- **No annotation output has been proved in Isabelle.** Every temporal operator translates and is
  covered by `AnnTranslatorTest` — they become quantifiers over substates, with `timer`, `within`,
  `stable` and `cooldown` leaning on `ltime` — but no generated lemma has been put to a prover, so
  the shapes are only as good as `Annotations.tex` and `ReflexBase.thy`.
- Division domain conditions, which the old generator emitted as *assumptions*, are not reproduced.
  The old `ExprGenRes` carried a `domain` field alongside the value, accumulating `divisor ≠ 0` per
  `/` and `%`; `ExprLowering.Outcome` has the same shape to hang it on if it comes back, but as an
  obligation rather than an assumption.
- **A write to a read-only physical input is not rejected.** `light = LOW` where `light` is bound to
  a `read =` address now generates a setter rather than being reported. Nothing checks the direction
  of a bound address.
- **A loop condition that writes is still rejected.** The cut states the condition twice — negated
  past the loop, asserted inside it — which only means anything if evaluating it changes nothing.
