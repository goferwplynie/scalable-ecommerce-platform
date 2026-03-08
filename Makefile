.PHONY: build clean up down restart logs

GRADLE = ./gradlew
DOCKER_COMPOSE = podman-compose

build:
	$(GRADLE) :product-catalog-service:bootJar

up: build
	$(DOCKER_COMPOSE) up --build

down:
	$(DOCKER_COMPOSE) down

restart: down up

logs:
	$(DOCKER_COMPOSE) logs

clean:
	$(GRADLE) clean
	$(DOCKER_COMPOSE) down -v
