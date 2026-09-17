#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if ! command -v javac >/dev/null 2>&1; then
  echo "Java JDK not found. Install a JDK and ensure javac is on PATH." >&2
  exit 1
fi

mkdir -p build

echo "Compiling PC Part Picker Tool..."
javac -cp "lib/*" -d build PCPartPicker/src/*.java

echo "Starting demo..."
java -cp "build:lib/*" Main
