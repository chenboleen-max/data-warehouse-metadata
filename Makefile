# Makefile for kiro_web project

.PHONY: help up down restart logs clean build test

help:
	@echo "Available commands:"
	@echo "  make up          - Start all services"
	@echo "  make down        - Stop all services"
	@echo "  make restart     - Restart all services"
	@echo "  make logs        - View logs"
	@echo "  make clean       - Clean up containers and volumes"
	@echo "  make build       - Build Docker images"
	@echo "  make test        - Run tests"
	@echo "  make backend-shell - Open backend container shell"
	@echo "  make frontend-shell - Open frontend container shell"
	@echo "  make db-shell    - Open MySQL shell"

up:
	docker-compose up -d

down:
	docker-compose down

restart:
	docker-compose restart

logs:
	docker-compose logs -f

clean:
	docker-compose down -v
	rm -rf backend/uploads/* backend/exports/* backend/logs/*

build:
	docker-compose build

test:
	cd backend && pytest tests/
	cd frontend && npm run test:unit

backend-shell:
	docker-compose exec backend /bin/bash

frontend-shell:
	docker-compose exec frontend /bin/sh

db-shell:
	docker-compose exec mysql mysql -u root -p

backend-logs:
	docker-compose logs -f backend

frontend-logs:
	docker-compose logs -f frontend

db-logs:
	docker-compose logs -f mysql
