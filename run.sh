#!/usr/bin/env bash
set -e

CP="out/production/KY_PART_1"

echo "[1/4] minimal VFS"
java -cp "$CP" Main --vfs ./vfs/minimal.csv --script ./scripts/test-minimal.txt

echo
echo "[2/4] multiple files"
java -cp "$CP" Main --vfs ./vfs/multiple.csv --script ./scripts/test-multiple.txt

echo
echo "[3/4] deep tree"
java -cp "$CP" Main --vfs ./vfs/deep.csv --script ./scripts/test-all.txt

echo
echo "[4/4] error handling"
java -cp "$CP" Main --vfs ./vfs/multiple.csv --script ./scripts/test-error.txt