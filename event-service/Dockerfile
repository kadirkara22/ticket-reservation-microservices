# 1. Aşama: Maven ile Build (Derleme)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Testleri atlayarak paketliyoruz (Hız kazanmak için)
RUN mvn clean package -DskipTests

# 2. Aşama: Çalıştırma (Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Build aşamasından çıkan JAR dosyasını alıyoruz
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]