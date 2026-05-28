allprojects {
    group = "com.edugo.kmp"
    version = project.findProperty("version")?.toString() ?: "0.1.0-DEV"
}
