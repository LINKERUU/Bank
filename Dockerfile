# Стадия сборки с JDK 21
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app

# Установка Maven (совместимого с JDK 21)
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Копируем POM и скачиваем зависимости
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Копируем исходники и собираем
COPY src ./src
RUN mvn -B package -DskipTests

# Финальный образ с JRE 21
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx350m", "-Xms128m", "-jar", "app.jar"]
