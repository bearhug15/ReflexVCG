# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

ReflexVCG generates formal verification conditions (VCs) for programs written in **Reflex**, a small
C-like DSL for programming PLC-style reactive controllers (processes with states, transitions, timeouts).
Given a `.rx` source file it parses the program, lowers it to an IR, rewrites that IR into a canonical
form, builds a control-flow graph, enumerates the paths through one execution cycle, and emits an
Isabelle/HOL lemma per path (one `.thy` file per VC) that a human later proves. `FinalVCGRules.pdf` holds
the formal rules; `Preprocessing.tex` specifies the preprocessing passes.

## Build / test / run

Maven project, Java 17. Three ANTLR grammars are compiled during `generate-sources`, so always build
through Maven — the generated parsers are not in `src/`.

```
mvn compile                                   # generate parsers + compile
mvn test                                      # run all tests
mvn test -Dtest=NewPipelineEndToEndTest       # a single test class
mvn package                                   # builds the jar-with-dependencies
```

```
java -jar target/ReflexVCG-1.0-jar-with-dependencies.jar -s program.rx [-o outDir] [-g] [-a true|false]
```

- `-s` source `.rx` path (required)
- `-o` output directory (defaults to the source's directory)
- `-g` also export the control-flow graph as Graphviz
- `-a` discard conditions for impossible paths (default true)

## Pipeline architecture

Each stage has one input and one output, and no stage knows about the others. Isabelle syntax exists in
exactly one place, at the very end.

```
.rx ──1──▶ parse tree + comment tokens
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
   `NameManglingPass` (globally unique names — this is why no `VariableMapper` exists), then
   `CastInsertionPass` + `TypeEnvironment` (every expression typed, every conversion an explicit cast),
   then `NormalizationPass` (`set next state` resolved, switch fall-through expanded, `wait`/`slice`
   turned into ordinary states). The result is *canonical* IR.
4. **Graph** — `cfg/CfgBuilder` builds one execution cycle: every process runs once in declaration order,
   in whichever state it occupies, then the program yields to the environment. Branching is a node with
   several successors, each starting with a guard. `cfg/ExprLowering` handles short-circuit `&&`/`||`,
   which are branches, not operators. Nodes carry IR, never rendered text.
5. **Enumerate** — `cfg/PathEnumerator` walks paths depth-first; each becomes a `VerificationCondition`
   of symbolic `VcStatement`s. `analysis/StaticAnalysis` (spec: `StaticalAnalysis.tex`) discards a path
   at the node that makes it impossible, so the subtree below is never explored. `for` and inline C
   become `Unsupported` nodes and stop generation.
6. **Render** — `vc/IsabelleRenderer` is the *only* class that knows Isabelle; `vc/VcWriter` writes the
   files. Values live in ReflexBase's `val` datatype: read with `getVarVal` then a projection
   (`theInt`/`theNat`/`theBool`/`theReal`), written through the matching constructor.

`ReflexVcg` wires it together; `Main` is the CLI.

## Key points

- **The static analysis readings are provisional.** `StaticalAnalysis.tex` was never verified and is
  internally inconsistent in about ten places; every reading taken is marked `SPEC` in
  `analysis/`. `StaticAnalysisMeasurementTest` prints the comparison against the old pipeline.
  Over-pruning silently drops proof obligations, so treat the counts as unconfirmed.
- **`ReflexBase.thy` is the semantics** (`src/main/resources/ReflexTheory/`). One `val` datatype with an
  access path, rather than four typed getters. Reflex types map onto HOL as: signed ints → `int`,
  unsigned and `time` → `nat`, `bool` → `bool`, float/double → `real`. Changing codegen means keeping
  step with this file.
- **The IR is mutable on purpose.** The preprocessing passes rewrite the program in place, which is not
  expressible against an ANTLR tree. Use `IrCopier` when duplicating a subtree — sharing one would let a
  later rewrite of one place silently change another.
- **Regression gate.** `src/test/baseline/` holds a structural digest of the *old* pipeline's output;
  `tools/baseline_digest.py` regenerates it. `NewPipelineEndToEndTest` asserts the new pipeline's path
  counts against the old unpruned behaviour, program by program.
- **Generation must stay deterministic.** It was not: maps keyed by graph nodes without `hashCode`
  iterated in identity-hash order, so the same input produced different VC sets on different runs. Prefer
  insertion-ordered collections and explicit sorts anywhere output depends on order.

## Legacy code

The `ProgramGraph/`, `StatementsCreator/`, `expression/`, `formulas/` and `vcg/` packages, `Reflex.g4`,
and `Reflex.thy` are the **previous** pipeline. They are kept only so `BaselineCaptureTest` can regenerate
the comparison baseline, and are due for removal once the static analysis rework lands. Do not extend
them.

## Not done yet

- **The pruned counts for the five realistic programs are unconfirmed**, differing from the old
  pipeline in both directions. See the memory note and `StaticAnalysisMeasurementTest`.
- **Nothing is generated from annotations.** They are parsed, bound and reachable from
  `ExtraInvariantGenerator`, whose hooks all do nothing. The temporal operators of Reflex-AL need an
  execution-history model the theory does not have.
- **The legacy pipeline has not been removed** (see above).
- Division domain conditions, which the old generator emitted as *assumptions*, are not reproduced.
- `exprTest`'s `var++ + var` - the ordering of effects within a single expression is not modelled.
