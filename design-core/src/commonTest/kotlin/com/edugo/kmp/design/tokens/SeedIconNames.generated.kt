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
            "archive",
            "arrow-left-right",
            "assign",
            "award",
            "ban",
            "bar-chart",
            "bar_chart",
            "bell",
            "book",
            "book-open",
            "bullhorn",
            "calendar-range",
            "check-circle",
            "check-square",
            "check_circle",
            "checklist",
            "clipboard",
            "copy",
            "dashboard",
            "download",
            "file-search",
            "file-text",
            "folder",
            "google",
            "graduation-cap",
            "help_outline",
            "history",
            "layers",
            "list",
            "menu",
            "message-circle",
            "pencil",
            "people",
            "person_add",
            "pie-chart",
            "pie_chart",
            "play-circle",
            "plus",
            "quiz",
            "save",
            "school",
            "settings",
            "smartphone",
            "star",
            "tag",
            "ticket",
            "trash",
            "trash-2",
            "trending_up",
            "upload",
            "user-check",
            "user-plus",
            "users",
            "visibility",
            "visibility_off",
        )
}
