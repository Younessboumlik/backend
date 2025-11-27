# Étape 1 : build de l'application
FROM maven:3.9.3-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build du jar
RUN mvn clean package -DskipTests

# Étape 2 : exécuter l'application
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copier le jar depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port de l'application Spring Boot (ex : 8080)
EXPOSE 8080

# Commande pour lancer l'application
ENTRYPOINT ["java", "-jar", "app.jar"]
