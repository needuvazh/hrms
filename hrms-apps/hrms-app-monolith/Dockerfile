FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

RUN apk add --no-cache curl unzip sed

RUN curl -sL https://services.gradle.org/distributions/gradle-8.10.2-bin.zip -o /tmp/gradle.zip && \
    mkdir -p /root/.gradle/wrapper/dists/gradle-8.10.2-bin/zip2 && \
    unzip -q /tmp/gradle.zip -d /root/.gradle/wrapper/dists/gradle-8.10.2-bin/zip2 && \
    mv /root/.gradle/wrapper/dists/gradle-8.10.2-bin/zip2/gradle-8.10.2 /root/.gradle/wrapper/dists/gradle-8.10.2-bin/zip2/unknown

COPY gradle/wrapper/gradle-wrapper.properties ./gradle/wrapper/gradle-wrapper.properties
COPY gradlew ./gradlew
COPY gradle/wrapper/gradle-wrapper.jar ./gradle/wrapper/gradle-wrapper.jar
COPY settings.gradle.kts build.gradle.kts ./
COPY build-logic ./build-logic
COPY hrms-platform ./hrms-platform
COPY hrms-modules ./hrms-modules
COPY hrms-apps ./hrms-apps

RUN chmod +x gradlew && sed -i 's/\r$//' gradlew
RUN ./gradlew :hrms-apps:hrms-app-monolith:bootJar --no-daemon --quiet
RUN JAR_PATH=$(ls /build/hrms-apps/hrms-app-monolith/build/libs/hrms-app-monolith-*.jar | grep -v -- '-plain\.jar' | head -n 1) && \
    cp "$JAR_PATH" /build/hrms-apps/hrms-app-monolith/build/libs/app.jar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S hrms && adduser -S hrms -G hrms

COPY --from=builder /build/hrms-apps/hrms-app-monolith/build/libs/app.jar /app/app.jar

RUN chown -R hrms:hrms /app

USER hrms:hrms

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD sh -c 'wget --no-verbose --tries=1 --spider "http://localhost:${PORT:-8080}/actuator/health" || exit 1'

ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
