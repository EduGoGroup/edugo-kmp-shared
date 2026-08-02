#!/usr/bin/env bash
#
# regen-seed-icon-names.sh
#
# Regenera la lista de icon-names que el seed SDUI declara, para que el test de
# contrato de IconCatalog (design-core) deje de mantenerse a mano.
#
# POR QUE ESTO ES MANUAL (limitacion conocida y aceptada):
#   La verdad vive en OTRO repositorio — `EduBack/edugo-infrastructure` — que el
#   CI de kmp-shared no clona. El test no puede leer el seed en CI, asi que este
#   script se corre EN LOCAL cuando el seed cambia y el archivo generado se
#   commitea. El gate compara el catalogo contra esa ultima foto commiteada: si
#   el seed suma un icono y nadie regenera, el gate no lo ve. Regenerar es parte
#   de "tocar el seed", igual que correr las migraciones.
#
# Uso:
#   make regen-seed-icons                        # rutas por defecto
#   tools/regen-seed-icon-names.sh [SEEDS_DIR]   # ruta explicita
#   EDUGO_SEEDS_DIR=/otra/ruta tools/regen-seed-icon-names.sh
#
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
REPO_ROOT=$(cd "$SCRIPT_DIR/.." && pwd)

# edugo-kmp-shared vive en EduGo/EduUI/, el seed en EduGo/EduBack/.
DEFAULT_SEEDS="$REPO_ROOT/../../EduBack/edugo-infrastructure/postgres/seeds"
SEEDS_DIR="${1:-${EDUGO_SEEDS_DIR:-$DEFAULT_SEEDS}}"

OUT_FILE="$REPO_ROOT/design-core/src/commonTest/kotlin/com/edugo/kmp/design/tokens/SeedIconNames.generated.kt"

# Piso de cordura: si la extraccion devuelve menos que esto, algo se rompio
# (ruta equivocada, seed movido, patron obsoleto). Preferimos fallar aqui antes
# que escribir una lista raquitica que deje el gate verde por vacio.
MIN_NAMES=20

if [ ! -d "$SEEDS_DIR" ]; then
	echo "ERROR: no encuentro los seeds de edugo-infrastructure en:" >&2
	echo "  $SEEDS_DIR" >&2
	echo "" >&2
	echo "Clona el ecosistema completo o indica la ruta:" >&2
	echo "  tools/regen-seed-icon-names.sh /ruta/a/edugo-infrastructure/postgres/seeds" >&2
	exit 1
fi

SEEDS_DIR=$(cd "$SEEDS_DIR" && pwd)

TMP_SRC=$(mktemp)
TMP_NAMES=$(mktemp)
trap 'rm -f "$TMP_SRC" "$TMP_NAMES"' EXIT

# Fuente: todos los .go del seed menos los tests (sus fixtures inventan datos).
# Se descartan las lineas de comentario para no capturar iconos citados en la
# bitacora de cambios del propio seed (p.ej. `// normalizado de "swap_horiz"`).
find "$SEEDS_DIR" -name '*.go' ! -name '*_test.go' -print0 |
	while IFS= read -r -d '' file; do
		grep -v -E '^[[:space:]]*//' "$file" || true
	done >"$TMP_SRC"

# Las tres formas en que el seed declara un icon-name:
#   1. JSON embebido en raw strings (actions/slots):  "icon": "trash"
#   2. Campo de struct Go (recursos L4):              Icon: "message-circle"
#   3. Variable local (capas L0/L3):                  icon := "bullhorn"
# NO se capturan los valores de slot que un template pinta con
# controlType "icon" (p.ej. `"app_logo": "edugo_logo"`): son assets de marca
# del producto, no nombres del catalogo neutral de design-core.
{
	grep -ohE '"icon"[[:space:]]*:[[:space:]]*"[^"]+"' "$TMP_SRC" || true
	grep -ohE '(^|[^[:alnum:]_])Icon:[[:space:]]*"[^"]+"' "$TMP_SRC" || true
	grep -ohE '(^|[^[:alnum:]_])icon[[:space:]]*:=[[:space:]]*"[^"]+"' "$TMP_SRC" || true
# LC_ALL=C fija el orden byte a byte: sin eso el archivo generado cambia segun
# el locale de quien lo corre (`bar-chart` vs `bar_chart`) y ensucia los diffs.
} | sed -E 's/.*"([^"]+)"$/\1/' | tr '[:upper:]' '[:lower:]' | LC_ALL=C sort -u >"$TMP_NAMES"

COUNT=$(wc -l <"$TMP_NAMES" | tr -d ' ')
if [ "$COUNT" -lt "$MIN_NAMES" ]; then
	echo "ERROR: solo extraje $COUNT icon-names de $SEEDS_DIR (minimo esperado: $MIN_NAMES)." >&2
	echo "Revisa que la ruta sea el seed real y que los patrones de extraccion sigan vigentes." >&2
	exit 1
fi

{
	cat <<'KOTLIN_HEADER'
package com.edugo.kmp.design.tokens

/**
 * ARCHIVO GENERADO — NO EDITAR A MANO.
 *
 * Regenerar con `make regen-seed-icons` (script `tools/regen-seed-icon-names.sh`)
 * y commitear el resultado.
 *
 * Contenido: los icon-names que el seed SDUI declara hoy, extraidos de los
 * archivos `.go` de `EduBack/edugo-infrastructure/postgres/seeds` (sin tests y
 * sin lineas de comentario) en sus tres formas: `"icon": "x"` dentro del JSON
 * de acciones, el campo `Icon:` de los recursos L4 y la variable `icon :=` de
 * las capas L0/L3.
 *
 * LIMITACION: la fuente de verdad vive en otro repositorio, que el CI de
 * kmp-shared no clona; por eso la regeneracion es manual y local. Este archivo
 * es la ultima foto commiteada del seed, no un espejo automatico: si alguien
 * agrega un icono al seed y no regenera, [IconCatalogContractTest] no lo vera.
 */
internal object SeedIconNames {
    val ALL: List<String> =
        listOf(
KOTLIN_HEADER

	while IFS= read -r name; do
		printf '            "%s",\n' "$name"
	done <"$TMP_NAMES"

	cat <<'KOTLIN_FOOTER'
        )
}
KOTLIN_FOOTER
} >"$OUT_FILE"

echo "Generados $COUNT icon-names en:"
echo "  $OUT_FILE"
echo "Fuente: $SEEDS_DIR"
if command -v git >/dev/null 2>&1; then
	SEED_REV=$(git -C "$SEEDS_DIR" rev-parse --short HEAD 2>/dev/null || echo "desconocida")
	echo "Revision del seed: $SEED_REV"
fi
echo ""
echo "Siguiente paso: ./gradlew :design-core:desktopTest --tests '*IconCatalogContractTest*'"
