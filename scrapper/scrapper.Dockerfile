FROM openjdk:21
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} scrapper.jar
ENTRYPOINT ["java","-jar","scrapper.jar"]
