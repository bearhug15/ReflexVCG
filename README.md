# Reflex verification conditions generator

The `isp-2026` branch is the source release of the generator for the
experiments in A. D. Ishchenko and I. S. Anureev, “Using static analysis to
reduce the number of verification conditions generated for Reflex programs”: https://www.ispras.ru/proceedings/isp_38_2026_6-2/isp_38_2026_6-2_21/.

## Quick Overview

Verification conditions generator for Reflex programs. Saves generated conditions as Isabelle/HOL lemmas. One lemma per theory. Also provides theories containing type definitions, lemmas and proof patterns.

Implements simple static analysis to discard impossible verification conditions.

## Building

Use JDK 17 and Maven. Run `mvn clean package` to generate the ANTLR parser
from `Reflex.g4`, compile the project and build the executable JAR.

## Running

To launch the generator from the repository root, use
`java -jar target/ReflexVCG-1.0-jar-with-dependencies.jar -s path/to/program.rcs -o path/to/output`.
Create the output directory before running the command.

| key   | value          | meaning                                                                               |
|-------|----------------|---------------------------------------------------------------------------------------|
| -s 	  | path           | path to source .rx file                                                               |
| -o 	  | path           | output destination                                                                    |
| -e 	  | simple/regular | choose regular c-like expression semantics or simplified one. Regular by default      |
| -pe 	 | true/false     | split process expression for attributes. Works only with -e=regular. False by default |
| -g 	  | true/false     | generate .gv program graph. False by default                                          |
| -a 	  | true/false     | use static analysis to lower to discard unrelated VC                                  |

The source file path is required. The output directory defaults to the current working directory.
