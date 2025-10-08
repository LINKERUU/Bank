
FROM eclipse-temurin:21-jdk-jammy AS build

WORKDIR /app


RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*


COPY pom.xml .
RUN mvn -B dependency:go-offline


COPY src ./src
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx450m","-Xms128m" ,"-XX:+UseSerialGC", "-XX:MaxMetaspaceSize=100m" ,"-jar" ,"app.jar"]

