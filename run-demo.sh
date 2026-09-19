#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"

if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven 3.9+ is required and must be available on PATH." >&2
  exit 1
fi

mvn -q -DskipTests package
java -jar target/pc-part-picker-demo.jar
