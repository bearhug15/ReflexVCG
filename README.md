# ReflexCVG

## Quick Overview

Verification conditions generator for Reflex programs. Saves generated conditions as Isabelle/HOL lemmas. One lemma per theory. Also provides theories containing type definitions, lemmas and proof patterns.

Implements simple static analysis to discard impossible verification conditions.

## Running

To launch generator use: ``java -jar ReflexVCG.jar [key/value]``.
Specification of path to source file is required. Output path is optional and set to source folder if not defined.

| key    | value | meaning                   |
|--------|-------|---------------------------|
| -s 	 | path  | path to source .post file |
| -o 	 | path  | output destination        |
