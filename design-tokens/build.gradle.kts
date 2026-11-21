// Tokens del design system SIN Compose (deuda 131): enums y políticas puras que la lógica
// (p. ej. el motor SDUI) necesita sin arrastrar Compose ni skiko. `design-core` lo expone con
// `api(project(":design-tokens"))`, así que sus consumidores no notan el corte. Mismos paquetes
// que antes (`com.edugo.kmp.design.tokens`, `com.edugo.kmp.design.platform`).
plugins {
    id("kmp.logic.core")
    id("kmp.quality")
    id("kmp.publish")
}
