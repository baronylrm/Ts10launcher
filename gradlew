#!/bin/sh
#
# Gradle wrapper script
#

# Attempt to set APP_HOME
APP_HOME="$(cd "$(dirname "$0")" && pwd)"

exec java -jar "$APP_HOME/gradle/wrapper/gradle-wrapper.jar" "$@"
