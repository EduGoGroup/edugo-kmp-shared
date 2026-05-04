# resources-core

Módulo central de **recursos genéricos** (strings y traducciones) para la familia `edugo-kmp-shared` (Compose Resources). Contiene únicamente cadenas reutilizables sin marca EduGo: acciones, errores, mensajes, validación de formularios, selectores y UX de borrado.

## Propósito

Las cadenas EduGo-específicas (login, escuela, dashboard, splash) viven en la capa producto `:kmp-resources` del repo principal. `resources-core` provee solo lo que cualquier app KMP de cualquier dominio puede usar tal cual. La fusión de keys la hace el plugin de Compose Resources en el módulo agregador.

## Set de strings (R6.5)

Lista cerrada — 33 keys multi-idioma (`es` por defecto, `en` traducido):

- **Acciones (`action_*`, 11):** `accept`, `cancel`, `save`, `edit`, `delete`, `back`, `retry`, `dismiss`, `understood`, `undo`, `new`.
- **Errores (`error_*`, 8):** `loading_details`, `no_permission`, `network_offline`, `session_expired`, `resource_not_found`, `server_unavailable`, `generic`, `unknown`.
- **Mensajes (`message_*`, 5):** `loading`, `error_title`, `success_title`, `warning_title`, `info_title`.
- **Form (`form_*`, 2):** `field_required`, `fix_errors`.
- **Selectores (`select_*`, 2):** `loading`, `load_error`.
- **Borrado (`delete_*`, 3):** `confirmation_title`, `confirmation_message`, `cancelled_message`.
- **Permisos / persistencia (2):** `permission_denied`, `save_default_message`.

## Soporte i18n

Locales presentes en `src/commonMain/composeResources/`:

- `values/strings.xml` — español (default).
- `values-en/strings.xml` — inglés.

Para añadir un idioma, crear `values-<locale>/strings.xml` con las mismas 33 keys traducidas. El plugin Compose Resources resuelve el locale en runtime según la configuración del sistema.

## Instalación

`resources-core` se publica como parte del BOM `edugo-kmp-bom` (publicación efectiva en Fase 7). Para consumirlo:

```kotlin
// build.gradle.kts
dependencies {
    implementation(project.dependencies.platform(libs.edugo.kmp.bom))
    api(libs.edugo.kmp.resources.core)
}
```

## Uso

```kotlin
import com.edugo.kmp.resources.generated.Res
import com.edugo.kmp.resources.generated.string
import org.jetbrains.compose.resources.stringResource

@Composable
fun MyButton() {
    Text(stringResource(Res.string.action_accept))
}
```

Cuando el agregador `:kmp-resources-edugo` declare `api(libs.edugo.kmp.resources.core)`, los call-sites pueden usar tanto las keys EduGo como las `action_*`/`error_*`/etc. mediante el mismo `Res` reexportado.

## Política de evolución

- **No** se añaden keys con marca, dominio EduGo, o copys de negocio.
- Cambios en las traducciones existentes son aceptables sin bump de major.
- Renombrar o eliminar una key es un cambio breaking — diferir a una mayor del BOM.
