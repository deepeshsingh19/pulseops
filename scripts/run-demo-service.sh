#!/usr/bin/env bash

set -euo pipefail

SERVICE_JAR="${1:?Usage: ./scripts/run-demo-service.sh <service-jar> <service-name> <port>}"
SERVICE_NAME="${2:?Usage: ./scripts/run-demo-service.sh <service-jar> <service-name> <port>}"
PORT="${3:?Usage: ./scripts/run-demo-service.sh <service-jar> <service-name> <port>}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

AGENT_PATH="$PROJECT_ROOT/infrastructure/observability/java-agent/opentelemetry-javaagent.jar"

if [[ ! -f "$SERVICE_JAR" ]]; then
    echo "Service JAR not found:"
    echo "$SERVICE_JAR"
    exit 1
fi

if [[ ! -f "$AGENT_PATH" ]]; then
    echo "OpenTelemetry Java agent not found:"
    echo "$AGENT_PATH"
    exit 1
fi

# Use the Homebrew JDK so the runtime matches the JDK used by Maven.
if command -v brew >/dev/null 2>&1; then
    BREW_JAVA_HOME="$(brew --prefix openjdk)/libexec/openjdk.jdk/Contents/Home"

    if [[ -x "$BREW_JAVA_HOME/bin/java" ]]; then
        export JAVA_HOME="$BREW_JAVA_HOME"
        export PATH="$JAVA_HOME/bin:$PATH"
    fi
fi

JAVA_VERSION="$(java -version 2>&1 | head -n 1)"

echo "Starting $SERVICE_NAME on port $PORT"
echo "Java runtime: $JAVA_VERSION"

exec java \
    -javaagent:"$AGENT_PATH" \
    -Dotel.service.name="$SERVICE_NAME" \
    -Dotel.exporter.otlp.endpoint=http://localhost:4319 \
    -Dotel.exporter.otlp.protocol=grpc \
    -Dotel.metrics.exporter=none \
    -Dotel.logs.exporter=none \
    -Dserver.port="$PORT" \
    -jar "$SERVICE_JAR"