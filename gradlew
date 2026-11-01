#!/bin/sh
set -e
DIR=$(dirname "$0")
JAR="$DIR/gradle/wrapper/gradle-wrapper.jar"
exec java -jar "$JAR" "$@"
