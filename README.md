# timewalkers

Spring Boot + PostgreSQL API for a personal WoW Timewalking gear planner. Pairs with a separate React frontend that consumes this API.

## What it does

Serves WoW gear reference data (armor and weapons from Classic through Shadowlands) plus character creation and equipment management.

## Auth

Every endpoint needs an `Authorization: Bearer <key>` header, checked by a custom filter. The API key is only known by me, but of course you can run this project locally and seed it with the data here if you want to play around with these endpoints yourself.

## Hosting

Runs on AWS Lambda as a container image, behind a Function URL, backed by a shared Aurora Serverless v2 database. Both scale to zero when nothing's using it. Used to run on an always-on EC2 box; migrated over since this is only used by me (and a couple friends), and paying for idle compute made no sense.

## Deploying

Push to `main` and GitHub Actions runs the tests (as part of the container build), pushes the image to ECR, and updates the Lambda function to use it.

## Running locally

```bash
docker-compose up -d   # Postgres

API_KEY=local ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
# Windows PowerShell:
# $env:API_KEY='local'; mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local
```

Runs on `localhost:8080`.

## Tests

```bash
./mvnw test
```

## API examples

`gear-api.http` has example requests for every endpoint, including the error cases. Works straight out of IntelliJ's HTTP client.
