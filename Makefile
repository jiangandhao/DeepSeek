COMPOSE = docker compose --env-file .env -p deploy -f deploy/docker-compose.yml
SERVICE =

.PHONY: deploy up down ps logs ai-service backend frontend mysql redis

deploy:
	$(COMPOSE) up -d --build

up:
	$(COMPOSE) up -d

down:
	$(COMPOSE) down

ps:
	$(COMPOSE) ps

logs:
	$(COMPOSE) logs -f --tail=200 $(SERVICE)

ai-service backend frontend mysql redis:
	$(COMPOSE) up -d --build $@
