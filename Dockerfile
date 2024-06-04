FROM maven:3.8.7-eclipse-temurin-17
WORKDIR /app
COPY pom.xml .
COPY src/ src/
RUN mvn -f pom.xml clean package

COPY /target/*.jar battleship-be.jar

EXPOSE 8080

ENV SPRING_DATASOURCE_URL ${SPRING_DATASOURCE_URL}
ENV SPRING_DATASOURCE_USERNAME ${SPRING_DATASOURCE_USERNAME}
ENV SPRING_DATASOURCE_PASSWORD ${SPRING_DATASOURCE_PASSWORD}

ENTRYPOINT ["java", "-Dspring.datasource.url=${SPRING_DATASOURCE_URL}", "-Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME}", "-Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD}", "-jar", "battleship-be.jar"]