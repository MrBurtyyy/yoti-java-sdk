FROM openjdk:8-alpine

ARG ENV

COPY server.keystore.jks ./
COPY target/yoti-sdk-spring-boot-example-2.6.0-SNAPSHOT.jar ./

RUN mkdir /config

COPY config/$ENV/application.yml /config
COPY config/$ENV/*.pem /config

EXPOSE 8443

CMD java -Dspring.config.location=/config/application.yml -jar ./yoti-sdk-spring-boot-example-2.6.0-SNAPSHOT.jar
