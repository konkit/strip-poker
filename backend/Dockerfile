FROM java:8-jdk-alpine
RUN mkdir -p /opt/app
WORKDIR /opt/app
COPY . ./
RUN ./gradlew shadowJar

ENTRYPOINT sh -c 'java -jar /opt/app/build/libs/strippoker-0.0.1-all.jar'