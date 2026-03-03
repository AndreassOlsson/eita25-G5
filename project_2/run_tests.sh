#!/bin/bash
set -euo pipefail

JUNIT_VERSION="1.10.2"
JUNIT_JAR="libs/junit-platform-console-standalone-${JUNIT_VERSION}.jar"
BUILD_DIR="build/test-classes"
SRC_LIST=".java-sources.txt"

echo "Ensuring JUnit console runner is available..."
if [ ! -f "$JUNIT_JAR" ]; then
	mkdir -p "$(dirname "$JUNIT_JAR")"
	echo "Downloading JUnit Platform Console ${JUNIT_VERSION}..."
	curl -sSL "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/junit-platform-console-standalone-${JUNIT_VERSION}.jar" -o "$JUNIT_JAR"
fi

echo "Compiling sources..."
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR"
find src -name "*.java" > "$SRC_LIST"
javac -d "$BUILD_DIR" -cp "$JUNIT_JAR" @"$SRC_LIST"
rm "$SRC_LIST"

echo "Running JUnit test suite..."
java -jar "$JUNIT_JAR" --class-path "$BUILD_DIR" --scan-classpath
