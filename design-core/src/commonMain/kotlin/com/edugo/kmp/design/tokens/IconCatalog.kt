package com.edugo.kmp.design.tokens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.outlined.ManageSearch
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.DisplaySettings
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.GroupAdd
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Catálogo declarativo de iconos soportados por este frontend.
 *
 * El seed (backend) declara `"icon": "<name>"` como dato; este catálogo
 * es la decisión local del cliente Android/KMP sobre cómo materializar
 * cada nombre en un `ImageVector` Material. Otros clientes (web,
 * desktop, terceros) son libres de mapear los mismos nombres a otros
 * recursos visuales — no hay contrato visual cross-cliente.
 *
 * Reglas para mantener el catálogo:
 * - Cada entrada se registra con sus aliases (snake/kebab/sinónimos)
 *   que ya hayan estado en uso en el seed o en pantallas existentes.
 *   La normalización se hace en [normalize] — buscar siempre en
 *   lowercase.
 * - Si un icono no tiene variante outlined disponible en
 *   `material-icons-extended`, se reutiliza el filled para ambas
 *   variantes.
 * - Si el seed introduce un icon-name nuevo no registrado aquí, el
 *   resolver loguea warning en runtime y aplica el fallback.
 */
object IconCatalog {
    private data class Entry(
        val filled: ImageVector,
        val outlined: ImageVector,
    )

    private val table: Map<String, Entry> =
        buildMap {
            // --- Navigation & layout -------------------------------------------------
            register("home", Icons.Filled.Home, Icons.Outlined.Home)
            register("dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard)
            register("menu", Icons.Filled.Menu)
            register("settings", "gear", filled = Icons.Filled.Settings, outlined = Icons.Outlined.Settings)

            // --- Content & files -----------------------------------------------------
            register("folder", "materials", filled = Icons.Filled.Folder, outlined = Icons.Outlined.FolderOpen)
            register("book", "book-open", "book_open", filled = Icons.Filled.Book, outlined = Icons.Outlined.Book)
            register(
                "file-text",
                "file_text",
                filled = Icons.Filled.Description,
                outlined = Icons.Outlined.Description,
            )
            register(
                "clipboard",
                "assessment",
                filled = Icons.Filled.Assessment,
                outlined = Icons.Outlined.Assessment,
            )
            register("layers", Icons.Filled.Layers, Icons.Outlined.Layers)
            register("list", filled = Icons.AutoMirrored.Filled.FormatListBulleted)

            // --- People & users ------------------------------------------------------
            register(
                "person",
                "profile",
                "user",
                filled = Icons.Filled.Person,
                outlined = Icons.Outlined.Person,
            )
            register(
                "users",
                "people",
                filled = Icons.Filled.People,
                outlined = Icons.Outlined.People,
            )
            register("group", Icons.Filled.Group, Icons.Outlined.Group)
            register(
                "user-plus",
                "user_plus",
                filled = Icons.Filled.PersonAdd,
                outlined = Icons.Outlined.PersonAdd,
            )
            register("group_add", Icons.Filled.GroupAdd, Icons.Outlined.GroupAdd)
            register("school", Icons.Filled.School, Icons.Outlined.School)
            register(
                "graduation-cap",
                "graduation_cap",
                filled = Icons.Filled.School,
                outlined = Icons.Outlined.School,
            )

            // --- Actions -------------------------------------------------------------
            register("add", "plus", "create", filled = Icons.Filled.Add)
            register("edit", "pencil", filled = Icons.Filled.Edit)
            register("delete", "trash", "trash-2", "trash_2", "remove", filled = Icons.Filled.Delete)
            register("minus", filled = Icons.Filled.Remove)
            register("revoke", filled = Icons.Filled.PersonRemove)
            register("save", filled = Icons.Filled.Save)
            register("search", filled = Icons.Filled.Search)
            register("refresh", "reload", filled = Icons.Filled.Refresh)
            register("share", filled = Icons.Filled.Share)
            register("check", "done", filled = Icons.Filled.Check)
            register("close", "cancel", "x", filled = Icons.Filled.Close)
            register("block", filled = Icons.Filled.Block)
            register(
                "filter",
                "filter-list",
                "filter_list",
                filled = Icons.Filled.FilterList,
            )
            register("more", "more-vert", "more_vert", filled = Icons.Filled.MoreVert)
            register("link", filled = Icons.Filled.Link)
            register("upload", filled = Icons.Filled.Upload)
            register("cloud_upload", "cloud-upload", filled = Icons.Filled.CloudUpload)
            register("download", filled = Icons.Filled.Download)
            register(
                "archive",
                filled = Icons.Filled.Archive,
                outlined = Icons.Outlined.Archive,
            )
            register(
                "assign",
                filled = Icons.Filled.Assignment,
                outlined = Icons.Outlined.Assignment,
            )

            // --- Favorites & ratings -------------------------------------------------
            register("favorite", "heart", filled = Icons.Filled.Favorite)
            register(
                "favorite-border",
                "favorite_border",
                "heart-outline",
                "heart_outline",
                filled = Icons.Filled.FavoriteBorder,
            )
            register("star", filled = Icons.Filled.Star)

            // --- Communication -------------------------------------------------------
            register("email", "mail", filled = Icons.Filled.Email)
            register("phone", "call", filled = Icons.Filled.Phone)
            register("notifications", "bell", filled = Icons.Filled.Notifications)

            // --- Security & access ---------------------------------------------------
            register("shield", Icons.Filled.Shield, Icons.Outlined.Shield)
            register("key", Icons.Filled.Key, Icons.Outlined.Key)
            register("lock", filled = Icons.Filled.Lock)
            register("visibility", "eye", "show", filled = Icons.Filled.Visibility)
            register(
                "visibility-off",
                "visibility_off",
                "eye-off",
                "eye_off",
                "hide",
                filled = Icons.Filled.VisibilityOff,
            )

            // --- Charts & analytics --------------------------------------------------
            register(
                "bar-chart",
                "bar_chart",
                filled = Icons.Filled.BarChart,
                outlined = Icons.Outlined.BarChart,
            )
            register(
                "trending-up",
                "trending_up",
                filled = Icons.AutoMirrored.Filled.TrendingUp,
                outlined = Icons.AutoMirrored.Outlined.TrendingUp,
            )
            register(
                "pie-chart",
                "pie_chart",
                filled = Icons.Filled.PieChart,
                outlined = Icons.Outlined.PieChart,
            )

            // --- System & device -----------------------------------------------------
            register(
                "settings_applications",
                filled = Icons.Filled.DisplaySettings,
                outlined = Icons.Outlined.DisplaySettings,
            )
            register("devices", Icons.Filled.Devices, Icons.Outlined.Devices)
            register(
                "file-search",
                "file_search",
                "audit",
                "history",
                filled = Icons.AutoMirrored.Filled.ManageSearch,
                outlined = Icons.AutoMirrored.Outlined.ManageSearch,
            )

            // --- Status & info -------------------------------------------------------
            register("info", Icons.Filled.Info, Icons.Outlined.Info)
            register("warning", "alert", filled = Icons.Filled.Warning)
            register(
                "check_circle",
                "check-circle",
                filled = Icons.Filled.CheckCircle,
                outlined = Icons.Outlined.CheckCircle,
            )
            register("help_outline", "help-outline", "help", filled = Icons.Filled.HelpOutline)

            // --- Arrows & navigation -------------------------------------------------
            register("arrow-back", "arrow_back", "back", filled = Icons.AutoMirrored.Filled.ArrowBack)
            register(
                "arrow-forward",
                "arrow_forward",
                "forward",
                filled = Icons.AutoMirrored.Filled.ArrowForward,
            )

            // --- Domain specific -----------------------------------------------------
            // `zap` aparece en seed para "Quiz"; mapeamos al icono Bolt (rayo)
            // que es la traducción visual habitual de Material para zap.
            register("zap", filled = Icons.Filled.Bolt)
            register("quiz", filled = Icons.Filled.Quiz)
            // `google` no tiene icono propio en material-icons-extended; usamos
            // Language como placeholder neutro hasta que el DS aporte una marca.
            register("google", filled = Icons.Filled.Language)

            // --- Iconos del seed SDUI que antes caían a fallback ---------------------
            register("tag", filled = Icons.Filled.Tag)
            register("bullhorn", filled = Icons.Filled.Campaign)
            register("calendar-range", "calendar_range", filled = Icons.Filled.DateRange)
            register("award", filled = Icons.Filled.EmojiEvents)
            register("check-square", "check_square", filled = Icons.Filled.CheckBox)
            register("checklist", filled = Icons.Filled.Checklist)
            register("ticket", filled = Icons.Filled.ConfirmationNumber)
            register("play-circle", "play_circle", filled = Icons.Filled.PlayCircle)
        }

    /**
     * Lista canónica de aliases registrados (en lowercase). Útil para
     * tests de contrato y para exportar el JSON consumido por el seed.
     */
    val canonicalNames: List<String> by lazy { table.keys.sorted() }

    /**
     * Devuelve el [ImageVector] asociado a [iconName], o `null` si el
     * nombre no está registrado. La búsqueda es case-insensitive.
     *
     * @param iconName Nombre del icono (alias permitidos).
     * @param filled `true` (default) para variante filled; `false` para
     *   outlined. Si la entrada no declara outlined explícito, retorna
     *   el filled.
     */
    fun lookup(
        iconName: String?,
        filled: Boolean = true,
    ): ImageVector? {
        if (iconName == null) return null
        val entry = table[normalize(iconName)] ?: return null
        return if (filled) entry.filled else entry.outlined
    }

    private fun normalize(raw: String): String = raw.trim().lowercase()

    // MutableMap<String, Entry>.register helpers ---------------------------------

    private fun MutableMap<String, Entry>.register(
        vararg names: String,
        filled: ImageVector,
        outlined: ImageVector = filled,
    ) {
        require(names.isNotEmpty()) { "register requiere al menos un nombre" }
        val entry = Entry(filled = filled, outlined = outlined)
        for (name in names) {
            val key = name.lowercase()
            require(this[key] == null) { "Icono duplicado en IconCatalog: $key" }
            this[key] = entry
        }
    }

    private fun MutableMap<String, Entry>.register(
        name: String,
        filled: ImageVector,
        outlined: ImageVector,
    ) {
        register(names = arrayOf(name), filled = filled, outlined = outlined)
    }

    private fun MutableMap<String, Entry>.register(
        name: String,
        filled: ImageVector,
    ) {
        register(names = arrayOf(name), filled = filled, outlined = filled)
    }
}
