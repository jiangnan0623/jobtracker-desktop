#!/usr/bin/env bash
set -euo pipefail

FLAVOR="${1:-full}"
if [[ "$FLAVOR" != "lite" && "$FLAVOR" != "full" ]]; then
  echo "Usage: bash desktop/build-desktop-mac.sh [lite|full]" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
FRONTEND="$ROOT/frontend"
BACKEND="$ROOT/backend"
STATIC_DIR="$BACKEND/src/main/resources/static"
DESKTOP_BACKEND="$SCRIPT_DIR/resources/backend"
DESKTOP_RUNTIME="$SCRIPT_DIR/resources/runtime"
MAVEN="${MAVEN:-mvn}"
JLINK="${JLINK:-jlink}"

echo "Building $FLAVOR macOS package..."

echo "Building Vue frontend..."
cd "$FRONTEND"
if [[ -f package-lock.json ]]; then
  npm ci
else
  npm install
fi
npm run build

echo "Copying frontend dist into Spring Boot static resources..."
rm -rf "$STATIC_DIR"
mkdir -p "$STATIC_DIR"
cp -R "$FRONTEND/dist/." "$STATIC_DIR/"

echo "Building Spring Boot jar with desktop profile support..."
cd "$BACKEND"
MAVEN_ARGS=()
if [[ -f settings.xml ]]; then
  MAVEN_ARGS+=("-s" "settings.xml")
fi
MAVEN_ARGS+=("clean" "package" "-DskipTests")
"$MAVEN" "${MAVEN_ARGS[@]}"

echo "Staging backend jar for Electron..."
mkdir -p "$DESKTOP_BACKEND"
cp "$BACKEND/target/job-tracker-0.0.1-SNAPSHOT.jar" "$DESKTOP_BACKEND/app.jar"

if [[ "$FLAVOR" == "full" ]]; then
  echo "Preparing bundled Java runtime..."
  rm -rf "$DESKTOP_RUNTIME"
  RUNTIME_MODULES="java.base,java.compiler,java.datatransfer,java.desktop,java.instrument,java.logging,java.management,java.naming,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.security.sasl,java.sql,java.transaction.xa,java.xml,jdk.charsets,jdk.crypto.ec,jdk.unsupported,jdk.zipfs"
  "$JLINK" --strip-debug --no-header-files --no-man-pages --compress=2 --add-modules "$RUNTIME_MODULES" --output "$DESKTOP_RUNTIME"
else
  echo "Skipping bundled Java runtime for lite package..."
  rm -rf "$DESKTOP_RUNTIME"
fi

echo "Installing Electron dependencies and packaging macOS app..."
cd "$SCRIPT_DIR"
if [[ -f package-lock.json ]]; then
  npm ci
else
  npm install
fi
npm run "dist:mac:$FLAVOR"

echo "Done. $FLAVOR macOS package output is under: $SCRIPT_DIR/release"
