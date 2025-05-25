# Используем образ с Maven и JDK 21 для сборки
FROM maven:3.9.6-eclipse-temurin-21-alpine as builder

WORKDIR /app
COPY pom.xml .
# Сначала кэшируем зависимости
RUN mvn -B dependency:go-offline

COPY src ./src
# Сборка с увеличением памяти Maven
RUN mvn -B package -DskipTests -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

# Финальный образ с JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]