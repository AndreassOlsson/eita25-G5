#!/bin/bash
echo "Compiling tests..."
# Include all source files to ensure dependencies are met
javac src/tests/mocks/*.java src/tests/*.java src/access/*.java src/models/*.java src/networking/*.java src/repositories/*.java src/exceptions/*.java

echo "Running tests..."
# Run the TestRunner
java -cp . src.tests.TestRunner
