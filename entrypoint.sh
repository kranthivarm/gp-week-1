#!/bin/bash
set -e

export TZ=UTC

# Start cron
service cron start

# Start Spring Boot
exec java -jar /app/app.jar --server.port=8080
