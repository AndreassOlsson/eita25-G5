#!/bin/bash
set -euo pipefail

JUNIT_VERSION="1.10.2"
JUNIT_JAR="libs/junit-platform-console-standalone-${JUNIT_VERSION}.jar"
TEST_BUILD_DIR="build/test-classes"
MAIN_BUILD_DIR="build/classes"

if [ ! -f "$JUNIT_JAR" ]; then
	echo "Error: $JUNIT_JAR not found. Run ./build.sh to fetch dependencies and compile tests."
	exit 1
fi

if [ ! -d "$TEST_BUILD_DIR" ] || [ -z "$(find "$TEST_BUILD_DIR" -name '*.class' -print -quit)" ]; then
	echo "Error: Compiled test classes missing. Run ./build.sh before executing tests."
	exit 1
fi

if [ ! -d "$MAIN_BUILD_DIR" ] || [ -z "$(find "$MAIN_BUILD_DIR" -name '*.class' -print -quit)" ]; then
	echo "Error: Compiled application classes missing. Run ./build.sh before executing tests."
	exit 1
fi

echo "Running JUnit test suite..."
CLASSPATH="$TEST_BUILD_DIR:$MAIN_BUILD_DIR"
java -jar "$JUNIT_JAR" --class-path "$CLASSPATH" --scan-classpath
