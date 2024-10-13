# BUILD
FROM eclipse-temurin:17-jdk-jammy AS build
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME
ADD . $APP_HOME
RUN sed -i 's/\r$//' ./mvnw
RUN --mount=type=cache,target=/root/.m2 ./mvnw -f $APP_HOME/pom.xml clean package -DskipTests

# PACKAGE
FROM eclipse-temurin:17-jre-jammy
ARG JAR_FILE=/usr/app/target/*.jar
COPY --from=build ${JAR_FILE} /app/HomeBudgetTracker.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/HomeBudgetTracker.jar"]