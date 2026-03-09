.PHONY: build clean up down restart logs

GRADLE = ./gradlew
DOCKER_COMPOSE = podman-compose
PROTO = protoc

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
grpc-go:
	$(PROTO) -I=./shared-grpc/src/main/proto \
                                                 --go_out=./api-gateway/pb --go_opt=paths=source_relative \
                                                 --go-grpc_out=./api-gateway/pb --go-grpc_opt=paths=source_relative \
                                                 ./shared-grpc/src/main/proto/*.proto
