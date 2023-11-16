FROM openjdk:17-jdk-alpine3.14 as build
WORKDIR /payment
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
COPY ./src ./src
RUN ./mvnw clean package

FROM openjdk:17-jdk-alpine3.14
WORKDIR /home/application/java
COPY --from=build "/payment/target/payment-db-service.jar" .
ENTRYPOINT ["java", "-jar", "/home/application/java/payment-db-service.jar"]