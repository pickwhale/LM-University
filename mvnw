#!/bin/sh
set -e

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_JAR="$BASE_DIR/.mvn/wrapper/maven-wrapper.jar"
MAVEN_USER_HOME="$BASE_DIR/.m2"
JAVA_CMD=${JAVA_HOME:+$JAVA_HOME/bin/}java

exec "$JAVA_CMD" \
  -Dmaven.multiModuleProjectDirectory="$BASE_DIR" \
  -Dmaven.user.home="$MAVEN_USER_HOME" \
  -classpath "$WRAPPER_JAR" \
  org.apache.maven.wrapper.MavenWrapperMain "$@"
