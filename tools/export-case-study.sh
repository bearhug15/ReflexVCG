#!/bin/bash
# Assembles a case-study folder: the program, everything generation writes for it, and a
# proof on every condition.
#
#   tools/export-case-study.sh <program.rcs> <proofs file> <destination>
#
# Each condition gets the proof the proofs file records for it. A condition the file says
# nothing about gets `sorry` with a comment saying so, because a case study should show
# what is open as plainly as what is closed - never a proof that was not actually run.
#
# A ROOT is written alongside, so the folder builds as one session.
set -e

SOURCE="${1:?usage: export-case-study.sh <program.rcs> <proofs file> <destination>}"
PROOF_FILE="${2:?the proofs file, e.g. tools/palletStation.proofs}"
DEST="${3:?where to write the case study}"
JAR="${JAR:-target/ReflexVCG-1.0-jar-with-dependencies.jar}"

export LC_ALL=C
[ -f "$JAR" ] || { echo "no $JAR - run mvn package -DskipTests"; exit 1; }
PROOF_FILE=$(cd "$(dirname "$PROOF_FILE")" && pwd)/$(basename "$PROOF_FILE")

mkdir -p "$DEST"
find "$DEST" -maxdepth 1 -name '*.thy' -delete
find "$DEST" -maxdepth 1 -name 'ROOT' -delete
java -jar "$JAR" -s "$SOURCE" -o "$DEST" | tail -1
cp "$SOURCE" "$DEST/"
# The open conditions point at it for the reason they are open, so it travels with them.
cp "$PROOF_FILE" "$DEST/"

PREFIX=$(basename "$(ls "$DEST"/*_VC*.thy "$DEST"/*_LOOP*.thy 2>/dev/null | head -1)" | sed 's/_[A-Z]*[0-9]*\.thy//')
THEORY="${PREFIX}Theory"

recorded_proof() {
  awk -F'\t' -v want="$1" '
    /^[[:space:]]*#/ || /^[[:space:]]*$/ { next }
    /^@/ { split($0, a, /[[:space:]]*=[[:space:]]*/); name = a[1]; sub(/^@/, "", name);
           alias[name] = substr($0, index($0, "=") + 1); sub(/^[[:space:]]+/, "", alias[name]); next }
    $1 == want { p = $2; if (p ~ /^@/) { sub(/^@/, "", p); p = alias[p] } print p; exit }
  ' "$PROOF_FILE"
}

proved=0
open=0
for f in "$DEST"/${PREFIX}_*.thy; do
  short=$(basename "${f%.thy}" | sed "s/^${PREFIX}_//")
  proof=$(recorded_proof "$short")
  if [ -n "$proof" ]; then
    proved=$((proved + 1))
  else
    proof="sorry"
    open=$((open + 1))
  fi
  body=$(sed -e '/^end$/d' "$f")
  if [ "$proof" = "sorry" ]; then
    printf '%s\n  (* Open: no proof is recorded for this condition in %s,\n     which says why. *)\n  sorry\nend\n' \
      "$body" "$(basename "$PROOF_FILE")" > "$f"
  else
    printf '%s\n  %s\nend\n' "$body" "$proof" > "$f"
  fi
done

{ echo "session $PREFIX = HOL +"
  echo "  options [document = false, quick_and_dirty]"
  echo "  theories"
  echo "    ReflexBase ReflexLemmas ReflexPatterns"
  echo "    $THEORY LoopInvariants Requirements"
  [ -f "$DEST/ExtraInvariants.thy" ] && echo "    ExtraInvariants"
  for f in "$DEST"/${PREFIX}_*.thy; do echo "    $(basename "${f%.thy}")"; done
} > "$DEST/ROOT"

echo "$DEST: $proved conditions with a recorded proof, $open left open"
