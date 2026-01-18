# ============================
# Stage 1: Builder
# ============================
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

# Install system dependencies including python3-requests via apt
# This is more reliable than using pip
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        cron \
        python3 \
        python3-requests \
        tzdata \
        curl \
        bash && \
    rm -rf /var/lib/apt/lists/*

# Set timezone to UTC (as per assignment)
RUN ln -fs /usr/share/zoneinfo/UTC /etc/localtime && \
    dpkg-reconfigure -f noninteractive tzdata

# Copy final jar
COPY --from=build /app/target/*.jar /app/app.jar

# Copy keys (assignment requires committing these)
COPY student_private.pem student_public.pem instructor_public.pem /app/

# Copy scripts + entrypoint
COPY scripts /app/scripts
COPY entrypoint.sh /app/entrypoint.sh

# Copy cron configuration
COPY cron/2fa-cron /etc/cron.d/totp-cron

# Set permissions and install cron job
# Add newline to cron file before installing (required by cron)
RUN chmod 0644 /etc/cron.d/totp-cron && \
    echo "" >> /etc/cron.d/totp-cron && \
    chmod +x /app/scripts/log_2fa_cron.py && \
    chmod +x /app/entrypoint.sh && \
    crontab /etc/cron.d/totp-cron

# Create mount points for volumes
RUN mkdir -p /data /cron && \
    chmod 755 /data /cron

EXPOSE 8080

ENTRYPOINT ["/app/entrypoint.sh"]