#!/bin/bash

# Run the builds and tests, but only using the local OS and Java versions.
# This is not a substitute for the full Git workflow tests, but makes it easier
# to do testing before commit.

if [[ -z "$PROJECT_PATH" ]]; then
    PROJECT_PATH=$(git rev-parse --show-toplevel)
fi

(cd $PROJECT_PATH &&
MAVEN_OPTS='-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn' ./mvnw install --file pom.xml --batch-mode)

(cd $PROJECT_PATH/jte-gradle-plugin && ./gradlew publishToMavenLocal)
(cd $PROJECT_PATH/jte-runtime-test-gradle && ./gradlew check)
(cd $PROJECT_PATH/jte-runtime-cp-test-gradle && ./gradlew check)
(cd $PROJECT_PATH/jte-runtime-cp-test-gradle-kotlin && ./gradlew check)
