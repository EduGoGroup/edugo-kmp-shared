allprojects {
    group = "com.edugo.kmp"
    // En Fases 0-6 NO hay versión publicable (composite-build local). Cuando un módulo
    // necesite una versión nominal durante el composite-build, declara `version = "0.1.0-DEV"`.
    // La publicación real (versión 0.1.0) ocurre solamente en Fase 7 (P43/P44).
}
