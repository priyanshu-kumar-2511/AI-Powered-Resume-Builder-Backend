FROM maven:3.9.9-eclipse-temurin-17 AS build

ARG SERVICE_DIR

WORKDIR /workspace
COPY . .
RUN [ -f /workspace/.env ] || touch /workspace/.env

WORKDIR /workspace/${SERVICE_DIR}
RUN sh -c 'attempt=0; \
  until [ "$attempt" -ge 5 ]; do \
    mvn -B -DskipTests -Dmaven.wagon.http.retryHandler.count=5 package && exit 0; \
    attempt=$((attempt + 1)); \
    echo "Maven build failed, retrying ($attempt/5)..." >&2; \
    sleep 10; \
  done; \
  exit 1'

FROM eclipse-temurin:17-jre

WORKDIR /app
ARG SERVICE_DIR
COPY --from=build /workspace/${SERVICE_DIR}/target/*.jar /app/app.jar

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
