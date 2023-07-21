FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM openjdk:17-jdk-slim AS production
WORKDIR /app
COPY --from=build /app/target/Maxbuyer_shop_bot-0.0.1-SNAPSHOT.jar .
CMD ["java", "-jar", "Maxbuyer_shop_bot-0.0.1-SNAPSHOT.jar"]
# Используйте базовый образ с подходящей версией OpenJDK для вашего проекта Java

