.PHONY: help build test run clean docker-up docker-down install

help:
	@echo "Available commands:"
	@echo "  make install      - Install dependencies"
	@echo "  make build        - Build the project"
	@echo "  make test         - Run all tests"
	@echo "  make run          - Run the application (dev profile)"
	@echo "  make clean        - Clean build artifacts"
	@echo "  make docker-up    - Start Docker services (MySQL, Redis, Elasticsearch)"
	@echo "  make docker-down  - Stop Docker services"
	@echo "  make package      - Package the application"

install:
	mvn clean install -DskipTests

build:
	mvn clean compile

test:
	mvn test

run:
	mvn spring-boot:run -Dspring-boot.run.profiles=dev

clean:
	mvn clean

docker-up:
	docker-compose up -d

docker-down:
	docker-compose down

package:
	mvn clean package -DskipTests

docker-build:
	docker build -t kiro-web-backend:latest .

docker-run:
	docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod kiro-web-backend:latest
