# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Copy BOM parent + commons modules first (layer-cache friendly)
COPY financial-app-parent financial-app-parent
RUN mvn -f financial-app-parent/pom.xml -B -q -DskipTests install

COPY ms-banks/pom.xml ms-banks/pom.xml

# Pre-fetch dependencies
RUN mvn -f ms-banks/pom.xml dependency:resolve -q

# Copy source and build
COPY ms-banks/src ms-banks/src
RUN mvn -f ms-banks/pom.xml -B -q -DskipTests package

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/ms-banks/target/ms-banks-*.jar /app/app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
