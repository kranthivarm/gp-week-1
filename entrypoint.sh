#!/usr/bin/env bash
set -e

echo "=== Starting W1AuthTotp Container ==="
export TZ=UTC

# Start cron daemon
echo "Starting cron daemon..."
service cron start

if [ $? -eq 0 ]; then
    echo "✓ Cron started successfully"
else
    echo "✗ Cron failed to start"
fi

# Give cron a moment to initialize
sleep 2

# Verify cron is running
if pgrep cron > /dev/null; then
    echo "✓ Cron is running (PID: $(pgrep cron))"
else
    echo "✗ WARNING: Cron is not running!"
fi

# Start Spring Boot application
echo "Starting Spring Boot application on port 8080..."
exec java -jar /app/app.jar --server.port=8080