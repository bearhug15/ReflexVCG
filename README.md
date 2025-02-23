# Reflex verification conditions generator

## Quick Overview

Verification conditions generator for Reflex programs. Saves generated conditions as Isabelle/HOL lemmas. One lemma per theory. Also provides theories containing type definitions, lemmas and proof patterns.

Implements simple static analysis to discard impossible verification conditions.

## Running

To launch generator use: ``java -jar ReflexVCG.jar [key/value]``.

| key  | value          | meaning                                                                          |
|------|----------------|----------------------------------------------------------------------------------|
| -s 	 | path           | path to source .rx file                                                          |
| -o 	 | path           | output destination                                                               |
| -e 	 | simple/regular | choose regular c-like expression semantics or simplified one. Regular by default |
| -g 	 | true/false     | generate .gv program graph. False by default                                     |

Specification of source file path  is required. Output path is optional and set to source folder if not defined.
