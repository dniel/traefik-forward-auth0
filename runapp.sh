#!/bin/sh
set -e

# The module to start.
APP_JAR="application/application.jar"

echo " --- RUNNING $(basename "$0") $(date -u "+%Y-%m-%d %H:%M:%S Z") --- "
set -x

# Print some debug info about the JVM and Heap
exec "$JAVA_HOME/bin/java" \
  -XX:MaxRAMPercentage=80 \
  -XX:+PrintFlagsFinal \
  -version | grep Heap

# Start the application.
exec "$JAVA_HOME/bin/java" \
  -XX:MaxRAMPercentage=80 \
  -Dlogback.configurationFile=logback-cloud.xml \
  -jar "$APP_JAR"