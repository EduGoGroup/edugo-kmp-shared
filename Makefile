# edugo-kmp-shared — Makefile
#
# Targets principales:
#   publish-local  Publica todos los modulos a ~/.m2 con la version indicada.
#                  Uso: make publish-local V=0.1.2-LOCAL
#   ci-local       ktlintCheck + detekt + check (JVM, sin Android/Web/iOS).
#   ci-docker      Igual pero en contenedor Docker JDK 21 (réplica de GH Actions Linux).
#
# Prerequisito: JAVA_HOME apuntando a JDK 21+.

JAVA_VERSION  := 21
KMP_CI_IMAGE  := edugo-kmp-ci:21
DOCKER_DIR    := $(dir $(abspath $(lastword $(MAKEFILE_LIST))))../docker

.PHONY: help publish-local ci-image ci-local ci-docker

help: ## Muestra esta ayuda
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
	    awk 'BEGIN {FS = ":.*?## "}; {printf "  %-22s %s\n", $$1, $$2}'

publish-local: ## Publica a Maven Local (~/.m2). Uso: make publish-local V=0.1.2-LOCAL
	@if [ -z "$(V)" ]; then \
	    echo "ERROR: falta version. Uso: make publish-local V=0.1.2-LOCAL"; \
	    exit 1; \
	fi
	./gradlew publishToMavenLocal -Pversion=$(V)
	@echo ""
	@echo "Publicado com.edugo.kmp:*:$(V) en ~/.m2"
	@echo ""
	@echo "Proximos pasos en kmp-ui:"
	@echo "  1. gradle.properties:   includeSharedLocally=false"
	@echo "  2. libs.versions.toml:  edugo-kmp-bom = \"$(V)\""

ci-image: ## Construye la imagen Docker de CI para proyectos KMP (una sola vez)
	@printf "\033[1;33mConstruyendo $(KMP_CI_IMAGE)...\033[0m\n"
	@docker build -t $(KMP_CI_IMAGE) -f $(DOCKER_DIR)/kmp-ci.Dockerfile $(DOCKER_DIR)
	@printf "\033[0;32mImagen $(KMP_CI_IMAGE) lista\033[0m\n"

ci-local: ## CI local: ktlintCheck + detekt + check JVM (sin Android/Web/iOS)
	@printf "\033[1;33mEjecutando CI local (kmp-shared)...\033[0m\n"
	./gradlew ktlintCheck detekt check \
		-PenableAndroid=false \
		-PenableWeb=false \
		-PenableIos=false \
		--console=plain
	@printf "\033[0;32mCI local OK\033[0m\n"

ci-docker: ## CI en Docker ($(KMP_CI_IMAGE)) — réplica exacta de GH Actions Linux
	@if ! docker image inspect $(KMP_CI_IMAGE) > /dev/null 2>&1; then \
		echo "Imagen $(KMP_CI_IMAGE) no encontrada. Ejecuta: make ci-image"; \
		exit 1; \
	fi
	@printf "\033[1;33mEjecutando CI en Docker ($(KMP_CI_IMAGE))...\033[0m\n"
	@docker run --rm \
		-v "$(CURDIR)":/workspace \
		-v "$(HOME)/.gradle":/root/.gradle \
		-v "$(HOME)/.konan":/root/.konan \
		-w /workspace \
		$(KMP_CI_IMAGE) \
		bash -c " \
			chmod +x ./gradlew && \
			./gradlew ktlintCheck detekt check \
				-PenableAndroid=false \
				-PenableWeb=false \
				-PenableIos=false \
				--console=plain --no-daemon \
		"
	@printf "\033[0;32mCI Docker OK\033[0m\n"
