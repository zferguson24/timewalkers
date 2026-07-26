# Builds the Spring Boot app and packages it for AWS Lambda (container image) via the
# Lambda Web Adapter — the app itself is unmodified, unaware it's running on Lambda.
# Verify these base image tags exist before building; Java 25 images are recent.

FROM public.ecr.aws/docker/library/maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn clean package

FROM public.ecr.aws/docker/library/eclipse-temurin:25-jre
COPY --from=public.ecr.aws/awsguru/aws-lambda-adapter:0.8.4 /lambda-adapter /opt/extensions/lambda-adapter
COPY --from=build /build/target/timewalkers-*.jar /app.jar
ENV PORT=8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
