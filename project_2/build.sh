#!/bin/bash
set -euo pipefail

JUNIT_VERSION="1.10.2"
JUNIT_JAR="libs/junit-platform-console-standalone-${JUNIT_VERSION}.jar"
MAIN_BUILD_DIR="build/classes"
TEST_BUILD_DIR="build/test-classes"
MAIN_SOURCES_FILE=".main-sources.txt"
TEST_SOURCES_FILE=".test-sources.txt"

echo "Ensuring JUnit console runner is available..."
if [ ! -f "$JUNIT_JAR" ]; then
	mkdir -p "$(dirname "$JUNIT_JAR")"
	echo "Downloading JUnit Platform Console ${JUNIT_VERSION}..."
	curl -sSL "https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/${JUNIT_VERSION}/junit-platform-console-standalone-${JUNIT_VERSION}.jar" -o "$JUNIT_JAR"
fi

echo "Cleaning build output directories..."
rm -rf "$MAIN_BUILD_DIR" "$TEST_BUILD_DIR"
mkdir -p "$MAIN_BUILD_DIR" "$TEST_BUILD_DIR"

echo "Collecting source files..."
if [ -d src ]; then
	find src -path "src/tests" -prune -o -name "*.java" -print > "$MAIN_SOURCES_FILE"
else
	echo "Error: src directory not found"
	exit 1
fi

if [ -d src/tests ]; then
	find src/tests -name "*.java" > "$TEST_SOURCES_FILE"
else
	: > "$TEST_SOURCES_FILE"
fi

echo "Compiling application sources..."
if [ -s "$MAIN_SOURCES_FILE" ]; then
	javac -d "$MAIN_BUILD_DIR" -cp "libs/*" @"$MAIN_SOURCES_FILE"
fi

echo "Compiling test sources..."
if [ -s "$TEST_SOURCES_FILE" ]; then
	javac -d "$TEST_BUILD_DIR" -cp "$MAIN_BUILD_DIR:$JUNIT_JAR" @"$TEST_SOURCES_FILE"
fi

rm "$MAIN_SOURCES_FILE" "$TEST_SOURCES_FILE"

echo "Build complete."
