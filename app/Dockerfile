FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /work
COPY pom.xml .
COPY src src
RUN mvn -B -DskipTests package
FROM eclipse-temurin:21-jre
RUN useradd -r -u 10001 vaultshare
USER vaultshare
WORKDIR /app
COPY --from=build /work/target/vaultshare-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
