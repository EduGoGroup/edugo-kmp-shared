# edugo-kmp-shared — Makefile
#
# Targets principales:
#   publish-local  Publica todos los modulos a ~/.m2 con la version indicada.
#                  Uso: make publish-local V=0.1.2-LOCAL
#
# Prerequisito: JAVA_HOME apuntando a JDK 21+.

.PHONY: help publish-local

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
