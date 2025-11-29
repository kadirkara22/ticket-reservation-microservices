# --------------------------------------------------------
# 1. AŞAMA: BUILD (İnşaat Sahası)
# --------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Adım 1: Sadece pom.xml'i kopyala ve bağımlılıkları indir.
# (Docker Layer Caching sayesinde kod değişse bile bu adım atlanır, hız kazanılır)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Adım 2: Kaynak kodları kopyala ve derle
COPY src ./src
RUN mvn clean package -DskipTests

# --------------------------------------------------------
# 2. AŞAMA: RUNTIME (Sahne)
# --------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Güvenlik: Root kullanıcısı yerine kısıtlı bir kullanıcı oluştur
RUN addgroup -S ticketgroup && adduser -S ticketuser -G ticketgroup
USER ticketuser

# Sadece derlenmiş JAR dosyasını ilk aşamadan (build) al
COPY --from=build /app/target/*.jar app.jar

# Uygulamayı başlat
ENTRYPOINT ["java", "-jar", "app.jar"]