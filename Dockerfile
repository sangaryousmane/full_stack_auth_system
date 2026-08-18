# Stage 1: Build

FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw clean package -DskipTests


# Stage 2: Runtime

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

LABEL authors="Ousmane Sangary"

ENTRYPOINT ["java", "-jar", "app.jar"]
