# Imagen de la API para Render u otro servicio con Docker.
# Etapa 1: compila el jar con Maven.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -B -DskipTests package

# Etapa 2: imagen liviana solo con el JRE.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
# Render define PORT; la aplicación lo lee en server.port (por defecto 8080).
EXPOSE 8080
# Limita la memoria a un porcentaje del contenedor (útil en planes de 512 MB).
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
