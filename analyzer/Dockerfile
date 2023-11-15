FROM openjdk:17-jdk-alpine3.14 as build
WORKDIR /analyzer
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY ./src ./src
RUN ./mvnw clean package

FROM openjdk:17-jdk-alpine3.14
WORKDIR /home/application/java
COPY --from=build "/analyzer/target/analyzer.jar" .
ENTRYPOINT ["java", "-jar", "/home/application/java/analyzer.jar"]