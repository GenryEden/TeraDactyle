FROM gradle:jdk17 as build
COPY src /home/gradle/src
COPY build.gradle /home/gradle/
COPY settings.gradle /home/gradle/
RUN gradle bootJar

FROM openjdk:17
COPY --from=build /home/gradle/build/libs/antiplagiat-0.0.1-SNAPSHOT.jar /usr/local/lib/demo.jar
EXPOSE 8080
ENTRYPOINT java -jar /usr/local/lib/demo.jar