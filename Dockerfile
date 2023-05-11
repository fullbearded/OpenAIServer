# build backend
FROM maven:3.8.3-openjdk-17 as backend

WORKDIR /app

COPY /server /app

RUN mvn clean package -DskipTests=true

# service
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=backend /app/target/server-0.0.1-SNAPSHOT.jar /app

EXPOSE 8080

CMD java -jar server-0.0.1-SNAPSHOT.jar
