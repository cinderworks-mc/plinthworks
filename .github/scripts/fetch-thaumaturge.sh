#!/bin/bash
# puts the thaumaturge jar the essentia compat compiles against into libs/, from the private link in the
# THAUMATURGE_1211_URL secret. the jar is only compiled against: it is never committed, cached, uploaded or released.
#   THAUMATURGE_URL  the secret, empty on forks and on pull requests from forks
#   EXPECT_COMPAT    true when this build has to ship the compat (upstream tags): a missing secret fails it
# writes available=true|false to $GITHUB_OUTPUT
set -euo pipefail
JAR=libs/thaumaturge-1.21.1-NeoForge-1.0.3.jar
# thaumaturge-1.21.1-NeoForge-1.0.3.jar as released. a newer thaumaturge means a new name and hash here
SHA256=161da023e3bf852ccbfdc5d097ef53e7b18806862ac0c6ffaa8bf0070df13da7

if [ -z "${THAUMATURGE_URL:-}" ]; then
  if [ "${EXPECT_COMPAT:-false}" = "true" ]; then
    echo "::error::THAUMATURGE_1211_URL is not set and this build has to ship the essentia compat. add the secret, or build the release by hand"
    exit 1
  fi
  echo "::notice::no THAUMATURGE_1211_URL secret here, building plinthworks without the essentia compat"
  echo "available=false" >> "${GITHUB_OUTPUT:-/dev/null}"
  exit 0
fi

mkdir -p libs
curl --fail --silent --show-error --location --retry 3 --max-time 300 -o "$JAR" "$THAUMATURGE_URL"
if ! unzip -tq "$JAR" > /dev/null; then
  echo "::error::the file behind THAUMATURGE_1211_URL is not a readable jar"
  rm -f "$JAR"
  exit 1
fi
if ! echo "$SHA256  $JAR" | sha256sum --check --status; then
  echo "::error::the jar behind THAUMATURGE_1211_URL is not thaumaturge-1.21.1-NeoForge-1.0.3.jar (sha256 differs)"
  rm -f "$JAR"
  exit 1
fi
echo "thaumaturge jar in place and checked"
echo "available=true" >> "${GITHUB_OUTPUT:-/dev/null}"
