#!/bin/bash

# Run the builds and tests, but only using the local OS and Java versions.
# Copied from the Github workflow

if [[ -z "$PROJECT_PATH" ]]; then
    PROJECT_PATH=$(git rev-parse --show-toplevel)
fi

cd $PROJECT_PATH &&
MAVEN_OPTS='-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn' ./mvnw install --file pom.xml --batch-mode

cd $PROJECT_PATH/jte-gradle-plugin && ./gradlew publishToMavenLocal
cd $PROJECT_PATH/jte-runtime-test-gradle && ./gradlew test
cd $PROJECT_PATH/jte-runtime-cp-test-gradle && ./gradlew test
cd $PROJECT_PATH/jte-runtime-cp-test-gradle-kotlin && ./gradlew test
