

FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline

COPY src ./src
RUN mvn -B -DskipTests package

# ============================
# Stage 2: Runtime
# ============================
FROM eclipse-temurin:21-jre

ENV TZ=UTC
WORKDIR /app

# Install cron + Python + curl (curl for health checks)
RUN apt-get update && \
    apt-get install -y cron python3 python3-pip tzdata curl && \
    rm -rf /var/lib/apt/lists/*

# Set timezone to UTC (as per assignment)
RUN ln -fs /usr/share/zoneinfo/UTC /etc/localtime && \
    dpkg-reconfigure -f noninteractive tzdata

# Copy final jar
COPY --from=build /app/target/*.jar /app/app.jar

# Copy keys (assignment requires committing these)
COPY student_private.pem student_public.pem /app/

# Copy scripts + cron files
COPY scripts /app/scripts
COPY cron/2fa-cron /etc/cron.d/totp-cron
COPY entrypoint.sh /app/entrypoint.sh

# Permissions
RUN chmod 644 /etc/cron.d/totp-cron && \
    chmod +x /app/scripts/log_2fa_cron.py && \
    chmod +x /app/entrypoint.sh && \
    crontab /etc/cron.d/totp-cron

# Create mount points
RUN mkdir -p /data /cron

EXPOSE 8080

ENTRYPOINT ["/app/entrypoint.sh"]