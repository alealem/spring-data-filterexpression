FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml .
COPY app/pom.xml app/pom.xml
COPY search-jpa/pom.xml search-jpa/pom.xml
COPY .mvn .mvn
COPY mvnw mvnw
COPY search-jpa/src search-jpa/src
COPY app/src app/src

RUN chmod +x mvnw && ./mvnw -q -pl app -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /workspace/app/target/demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
