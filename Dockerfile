# FROM maven:3.6-slim as builder

# COPY . /build/
# RUN mvn clean install -DskipTests -f /build


# ===== END BUILD STAGE ====

FROM openjdk:11-jre

EXPOSE 8080

COPY slack/target/slack-*-SNAPSHOT.jar /slack-app.jar

ENTRYPOINT ["java", "-jar", "/slack-app.jar"]
# CMD /usr/local/openjdk-11/bin/java -jar -Dserver.port=80 /slack-0.1.0-SNAPSHOT.jar

# USER www-data
# EXPOSE 80 
