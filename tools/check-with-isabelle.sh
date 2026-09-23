#!/bin/bash
# Puts a generated directory of verification conditions through Isabelle.
#
#   tools/check-with-isabelle.sh <generated dir> <program theory> [proof] [name filter] [timeout]
#   tools/check-with-isabelle.sh <generated dir> <program theory> --proofs <file> [filter] [timeout]
#
# The generator writes the Reflex theories beside the conditions, so <generated dir> is
# self-contained. This builds a base session holding those, the program theory, the loop
# invariants and the requirements, then one session per condition on top of it - a session
# each, so one hard goal cannot stall the rest, and `timeout` bounds it.
#
# Default proof is `sorry`, which only type-checks. The two that close most conditions:
#   "using assms by (simp add: setVarVal_def constants_def inv_def)"
#   "using assms by (auto simp add: setVarVal_def constants_def inv_def substate_refl \
#                                   loopInv0_def loopInv1_def ...)"
#
# With --proofs, each condition gets the proof a file records for it, and the run says which
# recorded proofs no longer work - which is what makes a measurement a regression gate. The
# file holds "<condition>\t<proof>" lines, "@alias = <proof>" definitions to name a proof
# used more than once, and # comments; tools/palletStation.proofs is one. Conditions the
# file says nothing about are skipped.
#
# mvn test pins the text of the output, not whether a prover accepts it, so a change to what
# a condition assumes can make it unprovable without failing a single Java test. Run this
# after any such change.
set -e

GEN="${1:?usage: check-with-isabelle.sh <generated dir> <program theory name> [proof|--proofs file] [filter] [timeout]}"
THEORY="${2:?the theory named after the program, e.g. PalletStationTheory}"
PROOF="${3:-sorry}"
PROOF_FILE=""
if [ "$PROOF" = "--proofs" ]; then
  PROOF_FILE="${4:?--proofs needs a file}"
  shift
  PROOF="(recorded)"
fi
ONLY="${4:-.}"
TIMEOUT="${5:-180}"
ISABELLE_HOME="${ISABELLE_HOME:-/d/Isabelle2025-2}"

export LC_ALL=C
GEN=$(cd "$GEN" && pwd)
PREFIX=$(basename "$(ls "$GEN"/*_VC*.thy "$GEN"/*_LOOP*.thy 2>/dev/null | head -1)" | sed 's/_[A-Z]*[0-9]*\.thy//')
WORK="$GEN/isabelle-check"
QUICK=""
[ "$PROOF" = "sorry" ] && QUICK=", quick_and_dirty"

cygpath_of() { echo "/cygdrive/${1:1:1}$(echo "${1:2}")"; }

# The proof a file records for one condition: its own, or the @alias it names. Empty when
# the file says nothing about it, which means skip it.
recorded_proof() {
  awk -F'\t' -v want="$1" '
    /^[[:space:]]*#/ || /^[[:space:]]*$/ { next }
    /^@/ { split($0, a, /[[:space:]]*=[[:space:]]*/); name = a[1]; sub(/^@/, "", name);
           alias[name] = substr($0, index($0, "=") + 1); sub(/^[[:space:]]+/, "", alias[name]); next }
    $1 == want { p = $2; if (p ~ /^@/) { sub(/^@/, "", p); p = alias[p] } print p; exit }
  ' "$PROOF_FILE"
}

cat > "$GEN/ROOT" <<EOF
session CheckBase = HOL +
  options [document = false]
  theories
    ReflexBase ReflexLemmas ReflexPatterns
    $THEORY LoopInvariants Requirements
EOF

rm -rf "$WORK"; mkdir -p "$WORK"
: > "$WORK/ROOT"
sessions=()
for f in "$GEN"/${PREFIX}_*.thy; do
  name=$(basename "${f%.thy}")
  short="${name#${PREFIX}_}"
  echo "$name" | grep -qE "$ONLY" || continue
  if [ -n "$PROOF_FILE" ]; then
    PROOF=$(recorded_proof "$short")
    [ -n "$PROOF" ] || continue
  fi
  mkdir -p "$WORK/$short"
  sed -e '/^  sorry$/d' -e '/^end$/d' \
      -e "s/^\timports $THEORY LoopInvariants Requirements/\timports \"CheckBase.$THEORY\" \"CheckBase.LoopInvariants\" \"CheckBase.Requirements\"/" \
      "$f" > "$WORK/$short/$name.thy"
  printf '  %s\nend\n' "$PROOF" >> "$WORK/$short/$name.thy"
  { echo "session Check_$short in \"$short\" = CheckBase +"
    echo "  options [document = false, timeout = $TIMEOUT$QUICK]"
    echo "  theories $name"; echo; } >> "$WORK/ROOT"
  sessions+=("Check_$short")
done
[ ${#sessions[@]} -eq 0 ] && { echo "no conditions matched $ONLY"; exit 1; }

cd "$ISABELLE_HOME"
set +e
./contrib/cygwin/bin/bash.exe -l -c \
  "export LANG=C.UTF-8; cd $(cygpath_of "$ISABELLE_HOME") && ./bin/isabelle build -j 4 -o threads=2 -d $(cygpath_of "$GEN") -d $(cygpath_of "$WORK") ${sessions[*]}" \
  > "$WORK/build.out" 2>&1
BUILD_STATUS=$?
set -e

# The build's own status is what says whether every session went through: it is 0 only when
# all of them did. The log names the ones that did not - but it says nothing at all about a
# session it did not need to rebuild, since Isabelle keeps heaps keyed by content and skips
# a condition whose theory and proof have not changed. Counting those as failures is what
# an earlier version of this script got wrong.
failed() { grep -qE "^$1 FAILED|Unknown session \"?$1" "$WORK/build.out"; }

proved=0
if [ -n "$PROOF_FILE" ]; then
  echo "recorded proof no longer works for:"
else
  echo "unproved:"
fi
for s in "${sessions[@]}"; do
  if failed "$s"; then
    echo -n "  ${s#Check_}"
  else
    proved=$((proved + 1))
  fi
done
if [ "$BUILD_STATUS" -ne 0 ] && [ "$proved" -eq "${#sessions[@]}" ]; then
  # The build failed without naming a session: something went wrong before the proofs did.
  echo "  (none named, but the build failed - read the log)"
  proved=0
fi
echo
if [ -n "$PROOF_FILE" ]; then
  echo "proved $proved of ${#sessions[@]} recorded in $PROOF_FILE"
else
  echo "proved $proved of ${#sessions[@]} with: $PROOF"
fi
echo "log: $WORK/build.out"
# A recorded proof that stopped working is a regression, so say so in the exit code.
[ -n "$PROOF_FILE" ] && [ "$proved" -lt "${#sessions[@]}" ] && exit 1
exit 0
