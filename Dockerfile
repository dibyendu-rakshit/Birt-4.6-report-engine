FROM openjdk:8-jre
ARG APP_FILE

WORKDIR /home
ENV JAVA_XMS=512M \
    JAVA_XMX=2048M \
    spring.profiles.active=production


COPY target/$APP_FILE.jar /home/app.jar

ENTRYPOINT ["java", "-jar", "app.jar", "-Xms", "$JAVA_XMS", "-Xmx", "$JAVA_XMX"]

