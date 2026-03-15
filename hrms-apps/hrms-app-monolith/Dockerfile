FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY build-logic ./build-logic
COPY hrms-platform ./hrms-platform
COPY hrms-modules ./hrms-modules
COPY hrms-apps ./hrms-apps

RUN chmod +x gradlew
RUN ./gradlew :hrms-apps:hrms-app-monolith:bootJar --no-daemon --quiet

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -S hrms && adduser -S hrms -G hrms

COPY --from=builder /build/hrms-apps/hrms-app-monolith/build/libs/hrms-app-monolith.jar app.jar

RUN chown -R hrms:hrms /app

USER hrms:hrms

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
