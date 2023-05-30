#!/bin/bash

# Run the builds and tests, but only using the local OS and Java versions.
# This is not a substitute for the full Git workflow tests, but makes it easier
# to do testing before commit.
function usage() {
    echo "Usage: build-local.sh [--clean] [--help] [--no-maven] [--no-gradle] [--native]"
}

MAVEN=true
GRADLE=true
NATIVE=

while (( "$#" )); do
    case "$1" in
        --help)
            usage
            exit 0
            ;;
        --clean)
            CLEAN="clean"
            shift
            ;;
        --no-maven)
            MAVEN=""
            shift
            ;;
        --no-gradle)
            GRADLE=""
            shift
            ;;
        --native)
            NATIVE=nativeTest
            shift
            ;;
        *)
            echo "Unrecognized option $1"
            usage
            exit 1
            ;;
    esac
done

if [[ -z "$PROJECT_PATH" ]]; then
    PROJECT_PATH=$(git rev-parse --show-toplevel)
fi

if [[ "$MAVEN" == "true" ]]; then
(cd "$PROJECT_PATH" &&
MAVEN_OPTS='-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn' ./mvnw $CLEAN install --file pom.xml --batch-mode)
fi
 
if [[ "$GRADLE" == "true" ]]; then
  (cd "$PROJECT_PATH"/jte-gradle-plugin && ./gradlew $CLEAN build publishToMavenLocal)
  (cd "$PROJECT_PATH"/test/gradle-test-wrapper && ./gradlew $CLEAN check)
  if [[ -n "$NATIVE" ]]; then
    (cd "$PROJECT_PATH"/test/jte-runtime-cp-test-gradle-convention && echo "Building in $PWD" && ./gradlew $CLEAN $NATIVE)
  fi
fi
