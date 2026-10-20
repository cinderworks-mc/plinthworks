#!/usr/bin/env python3
# fails when a built jar carries any part of thaumaturge, and with --expect-compat when the essentia compat is missing.
# thaumaturge is all rights reserved: we compile against it and never ship it.
#   jar_guard.py [--thaumaturge libs/thaumaturge-...jar] [--expect-compat] build/libs/*.jar
# with --thaumaturge it also compares contents, so a renamed copy of one of its files is caught too
import argparse
import io
import sys
import zipfile

OURS = "dev/cinderworks/plinthworks/compat/thaumaturge/"
COMPAT_CLASS = OURS + "ThaumaturgeHooks.class"
# written by every build tool, never a copy of anything
GENERIC = {"META-INF/MANIFEST.MF"}
# sprite metadata is vanilla boilerplate: our 12px nine slice buttons match theirs byte for byte
SAME_BY_CHANCE = (".mcmeta",)


def walk(zf, outer=""):
    for info in zf.infolist():
        if info.is_dir():
            continue
        name = outer + info.filename
        yield name, info
        if info.filename.endswith(".jar"):
            with zipfile.ZipFile(io.BytesIO(zf.read(info))) as nested:
                yield from walk(nested, name + "!/")


def foreign(name):
    inner = name.rsplit("!/", 1)[-1].lower()
    if inner.startswith(OURS):
        return False
    base = inner.rsplit("/", 1)[-1]
    return ("leclowndu93150" in inner or inner.startswith(("assets/thaumaturge/", "data/thaumaturge/"))
            or base.startswith("thaumaturge"))


def fingerprints(path):
    prints = {}
    with zipfile.ZipFile(path) as zf:
        for name, info in walk(zf):
            if info.file_size > 0 and name not in GENERIC:
                prints.setdefault((info.CRC, info.file_size), name)
    return prints


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--thaumaturge")
    parser.add_argument("--expect-compat", action="store_true")
    parser.add_argument("jars", nargs="+")
    args = parser.parse_args()

    theirs = fingerprints(args.thaumaturge) if args.thaumaturge else {}
    problems = []
    compat_in = []
    for jar in args.jars:
        with zipfile.ZipFile(jar) as zf:
            names = 0
            for name, info in walk(zf):
                names += 1
                if foreign(name):
                    problems.append(f"{jar}: {name} is part of thaumaturge")
                elif theirs and name not in GENERIC and not name.endswith(SAME_BY_CHANCE) \
                        and (info.CRC, info.file_size) in theirs:
                    problems.append(f"{jar}: {name} is a copy of thaumaturge's {theirs[(info.CRC, info.file_size)]}")
                if name == COMPAT_CLASS:
                    compat_in.append(jar)
        print(f"{jar}: {names} files checked")

    if args.expect_compat and not compat_in:
        problems.append("the essentia compat was expected in the jar but is not there (" + COMPAT_CLASS + ")")
    for problem in problems:
        print(f"::error::{problem}")
    if problems:
        return 1
    print("no thaumaturge files in the jars" + (", essentia compat present" if compat_in else ", core only"))
    return 0


if __name__ == "__main__":
    sys.exit(main())
