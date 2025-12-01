#!/usr/bin/env bash

JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
GS_HOME="/home/dixson/gigaspaces-smart-cache-enterprise-17.1.2"
PROJ_DIR="/home/dixson/export-import"
LOOKUPLOCATORS="localhost"

CLASSPATH="$GS_HOME/lib/required/*"
CLASSPATH="$CLASSPATH:$PROJ_DIR/lib/*"
CLASSPATH="$CLASSPATH:$PROJ_DIR/target/export-import-1.0-SNAPSHOT.jar"

$JAVA_HOME/bin/java -cp "$CLASSPATH" \
  com.gigaspaces.tools.importexport.Program -o import -l "$LOOKUPLOCATORS" -s demo -d /home/dixson/exporter/output
