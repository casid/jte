#!/bin/bash

# Run the builds and tests, but only using the local OS and Java versions.
# This is not a substitute for the full Git workflow tests, but makes it easier
# to do testing before commit.

if [[ "$1" == "--clean" ]]; then
    CLEAN="clean"
fi

if [[ -z "$PROJECT_PATH" ]]; then
    PROJECT_PATH=$(git rev-parse --show-toplevel)
fi

(cd $PROJECT_PATH &&
MAVEN_OPTS='-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn' ./mvnw $CLEAN install --file pom.xml --batch-mode)

(cd $PROJECT_PATH/jte-gradle-plugin && ./gradlew $CLEAN build publishToMavenLocal)
(cd $PROJECT_PATH/jte-runtime-test-gradle && ./gradlew $CLEAN check)
(cd $PROJECT_PATH/jte-runtime-cp-test-gradle && ./gradlew $CLEAN check)
(cd $PROJECT_PATH/jte-runtime-cp-test-gradle-kotlin && ./gradlew $CLEAN check)
