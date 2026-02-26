# Étape 1 : Build avec Maven et Java 21
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# On installe Maven manuellement car l'image alpine est légère
RUN apk add --no-cache maven

# Copie du pom et téléchargement des dépendances
COPY pom.xml .
RUN mvn dependency:go-offline

# Copie du code source et build
COPY src ./src
RUN mvn clean package -DskipTests

# Étape 2 : Image d'exécution (JRE uniquement)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copie du JAR généré à l'étape précédente
# Note : vérifie bien le nom du JAR dans ton dossier /target
COPY --from=build /app/target/user-service-*.jar app.jar

# Le User-Service tourne sur le port 8081
EXPOSE 8081

# Optimisation de la mémoire pour ton DELL
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]