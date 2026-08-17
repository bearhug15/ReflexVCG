"""Build a structural digest of generated verification conditions.

The digest deliberately abstracts away Isabelle *rendering* (which changes when the
theory moves to ReflexBase.thy: getVarBool -> getVarVal, setVarBool -> setVarVal, ...)
and away from variable naming (which changes when name mangling is introduced).

What it keeps is the part the pipeline rework must preserve:

  * how many VCs each program produces (with and without static analysis), and
  * the shape of each VC: the ordered sequence of assumption kinds, plus the
    process/state identity of each state assumption.

Usage:
    python tools/baseline_digest.py target/baseline > src/test/baseline/structure.txt
"""
import re
import sys
import pathlib
from collections import Counter

ASSUME_RE = re.compile(r'^\s*(?:and\s+)?([A-Za-z_0-9]+)\s*:\s*"(.*)"\s*$')
STATE_RE = re.compile(r"getPstate\s+\w+\s+''([^']*)''\s*=\s*''([^']*)''")
PSTATE_RE = re.compile(r"setPstate\s+\w+\s+''([^']*)''\s*''([^']*)''")
RESET_RE = re.compile(r"reset\s+\w+\s+''([^']*)''")


def classify(label: str, body: str) -> str:
    """Map one lemma assumption to a rendering-independent kind."""
    if label == "base_inv":
        return "BASE_INV"
    if label == "st_final":
        return "FINAL"
    if label.endswith("_state"):
        m = STATE_RE.search(body)
        return f"STATE({m.group(1)},{m.group(2)})" if m else "STATE(?)"
    if "_condition_" in label:
        return "COND"
    # Plain stN assignment: classify by the state constructor applied.
    if "setPstate" in body:
        m = PSTATE_RE.search(body)
        return f"SETPSTATE({m.group(1)},{m.group(2)})" if m else "SETPSTATE(?)"
    if re.search(r"=\s*reset\b", body):
        m = RESET_RE.search(body)
        return f"RESET({m.group(1)})" if m else "RESET(?)"
    if "toEnv" in body:
        return "ENV"
    if re.search(r"setVar\w*", body):
        return "SETVAR"
    return "OTHER"


def vc_shape(path: pathlib.Path) -> str:
    kinds = []
    for line in path.read_text(encoding="utf-8").splitlines():
        m = ASSUME_RE.match(line)
        if not m:
            continue
        label, body = m.group(1), m.group(2)
        if label == "assumes":
            continue
        kinds.append(classify(label, body))
    return " ".join(kinds)


def digest_dir(program_dir: pathlib.Path) -> tuple[int, Counter]:
    vcs = sorted(program_dir.glob("*_VC*.thy"))
    shapes = Counter(vc_shape(p) for p in vcs)
    return len(vcs), shapes


def main() -> None:
    root = pathlib.Path(sys.argv[1])
    for mode in sorted(d.name for d in root.iterdir() if d.is_dir()):
        for program_dir in sorted((root / mode).iterdir()):
            if not program_dir.is_dir():
                continue
            count, shapes = digest_dir(program_dir)
            print(f"## {mode}/{program_dir.name}  vcs={count}  distinct_shapes={len(shapes)}")
            for shape, n in sorted(shapes.items()):
                print(f"{n:5d}  {shape}")
            print()


if __name__ == "__main__":
    main()
