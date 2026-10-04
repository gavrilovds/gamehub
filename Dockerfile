FROM eclipse-temurin:25-jdk-alpine AS build

WORKDIR /workspace
COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew && \
    ./gradlew dependencies --configuration runtimeClasspath --no-daemon >/dev/null

COPY src src
RUN ./gradlew bootJar --no-daemon && \
    cp "$(find build/libs -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' | head -n 1)" build/app.jar

FROM eclipse-temurin:25-jre-alpine

RUN addgroup -S gamehub && adduser -S gamehub -G gamehub
WORKDIR /app
COPY --from=build --chown=gamehub:gamehub /workspace/build/app.jar /app/app.jar
USER gamehub

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
