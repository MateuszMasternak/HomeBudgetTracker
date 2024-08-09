FROM eclipse-temurin:17-jre

COPY target/HomeBudgetTracker-0.0.1-SNAPSHOT.jar /app/HomeBudgetTracker.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/HomeBudgetTracker.jar"]