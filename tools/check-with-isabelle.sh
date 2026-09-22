#!/bin/bash
# Puts a generated directory of verification conditions through Isabelle.
#
#   tools/check-with-isabelle.sh <generated dir> <program theory> [proof] [name filter] [timeout]
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
# mvn test pins the text of the output, not whether a prover accepts it, so a change to what
# a condition assumes can make it unprovable without failing a single Java test. Run this
# after any such change.
set -e

GEN="${1:?usage: check-with-isabelle.sh <generated dir> <program theory name> [proof] [filter] [timeout]}"
THEORY="${2:?the theory named after the program, e.g. PalletStationTheory}"
PROOF="${3:-sorry}"
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
./contrib/cygwin/bin/bash.exe -l -c \
  "export LANG=C.UTF-8; cd $(cygpath_of "$ISABELLE_HOME") && ./bin/isabelle build -j 4 -o threads=2 -d $(cygpath_of "$GEN") -d $(cygpath_of "$WORK") ${sessions[*]}" \
  > "$WORK/build.out" 2>&1 || true

proved=0
echo "unproved:"
for s in "${sessions[@]}"; do
  if grep -q "^Finished $s " "$WORK/build.out"; then
    proved=$((proved + 1))
  else
    echo -n "  ${s#Check_}"
  fi
done
echo
echo "proved $proved of ${#sessions[@]} with: $PROOF"
echo "log: $WORK/build.out"
