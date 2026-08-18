# Reflex verification conditions generator

## Quick Overview

Verification conditions generator for Reflex programs. Saves generated conditions as Isabelle/HOL lemmas, one lemma per theory, alongside the theories defining the state model, its lemmas and reusable proof patterns.

Reflex-AL annotations carried in comments are parsed and bound to the constructs they precede.

Static analysis discards conditions for paths the program cannot take.

## Running

To launch the generator use: ``java -jar ReflexVCG.jar [key value]``.

| key | value      | meaning                                                          |
|-----|------------|------------------------------------------------------------------|
| -s  | path       | path to the source .rx file (required)                            |
| -o  | path       | output directory; defaults to the source's folder                 |
| -g  |            | also export the program graph in Graphviz format                  |
| -a  | true/false | discard conditions for impossible paths; true by default          |

## Output

One theory per verification condition, plus:

| file                  | contents                                                        |
|-----------------------|------------------------------------------------------------------|
| `ReflexBase.thy`      | the state datatype, its values and the operations on them        |
| `ReflexLemmas.thy`    | lemmas about those                                               |
| `ReflexPatterns.thy`  | reusable proof patterns                                          |
| `<program>Theory.thy` | the program's clock and the timing function built on it          |
| `Requirements.thy`    | the invariant to be proved                                       |

`<program>_VC0.thy` is the base case of the induction: it starts from `emptyState`, applies the
declared initialisers, starts the first process and assumes no invariant. Every other condition is
an inductive step over one execution cycle, assuming the invariant and showing it is preserved.

## Building

Maven, Java 17. The parsers are generated from the grammars during the build, so build through Maven:

```
mvn package
```
