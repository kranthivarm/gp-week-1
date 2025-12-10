#FROM ubuntu:latest
#LABEL authors="KRANTHI VARMA"
#
#ENTRYPOINT ["top", "-ab"]

# builder
FROM maven:3.8.7-eclipse-temurin-17 as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests package

# runtime
FROM eclipse-temurin:17-jre
ENV TZ=UTC
WORKDIR /app
COPY --from=build /app/target/pki-2fa-0.0.1-SNAPSHOT.jar ./app.jar

# copy keys (the assignment requires committing student_private.pem)
COPY student_private.pem ./student_private.pem
COPY student_public.pem ./student_public.pem
COPY instructor_public.pem ./instructor_public.pem

# create volumes
VOLUME ["/data","/cron"]
EXPOSE 8080

# add cron script
COPY cron/2fa-cron /etc/cron.d/2fa-cron
RUN chmod 0644 /etc/cron.d/2fa-cron && crontab /etc/cron.d/2fa-cron

CMD service cron start && java -jar app.jar
