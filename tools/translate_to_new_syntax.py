"""Mechanically translate the legacy-syntax Reflex test programs to NewReflex syntax.

Three transformations, all local:

  1. Physical variable bindings.
         bool x = inp[1];        ->  bool x as (read = inp, bit = 1);
         bool x = inp[];         ->  bool x as (read = inp);
     The name mangling of the new pipeline builds a physical variable's name from
     its read port and bit, newIndirectName(port, bit), which reproduces the names
     the old VariableMapper produced (inp_1), so generated conditions keep naming
     the same things.

  2. Processes must name a node. A single node is introduced carrying the program's
     clock, and every process is bound to it. Nodes are namespaces only, so this
     does not change behaviour.

  3. `process P {` -> `process P :: node N {`.

Everything else - states, statements, expressions, ports - is unchanged between the
two grammars.

Usage:
    python tools/translate_to_new_syntax.py <src-dir> <dest-dir>
"""
import pathlib
import re
import sys

# `bool name = port[3];` or `bool name = port[];`
BINDING = re.compile(
    r"^(?P<indent>\s*)(?P<type>[A-Za-z_][A-Za-z_0-9]*)\s+(?P<name>[A-Za-z_][A-Za-z_0-9]*)"
    r"\s*=\s*(?P<port>[A-Za-z_][A-Za-z_0-9]*)\s*\[\s*(?P<bit>[0-9]*)\s*\]\s*;\s*$")

PROCESS = re.compile(r"^(?P<indent>\s*)process\s+(?P<name>[A-Za-z_][A-Za-z_0-9]*)\s*\{\s*$")

CLOCK = re.compile(r"^(?P<indent>\s*)clock\s+(?P<value>[^;]+);\s*$")

NODE_NAME = "MainNode"


def translate(source: str) -> str:
    lines = source.splitlines()
    out = []
    node_emitted = False

    for line in lines:
        clock = CLOCK.match(line)
        if clock and not node_emitted:
            # Keep the program clock, then declare the node every process binds to.
            out.append(line)
            out.append(f"{clock.group('indent')}node {NODE_NAME} {{ clock {clock.group('value').strip()}; }}")
            node_emitted = True
            continue

        binding = BINDING.match(line)
        if binding:
            bit = binding.group("bit")
            suffix = f", bit = {bit}" if bit else ""
            out.append(
                f"{binding.group('indent')}{binding.group('type')} {binding.group('name')}"
                f" as (read = {binding.group('port')}{suffix});")
            continue

        process = PROCESS.match(line)
        if process:
            out.append(f"{process.group('indent')}process {process.group('name')} :: node {NODE_NAME} {{")
            continue

        out.append(line)

    return "\n".join(out) + "\n"


def main() -> None:
    src = pathlib.Path(sys.argv[1])
    dest = pathlib.Path(sys.argv[2])
    dest.mkdir(parents=True, exist_ok=True)

    for path in sorted(src.glob("*.rx")):
        translated = translate(path.read_text(encoding="utf-8"))
        (dest / path.name).write_text(translated, encoding="utf-8")
        print(f"{path.name}: {len(translated.splitlines())} lines")


if __name__ == "__main__":
    main()
