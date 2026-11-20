package me.ash.reader.brief.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import me.ash.reader.brief.ui.glass.glassActive
import me.ash.reader.brief.ui.style.BriefStyle
import me.ash.reader.brief.ui.style.DesignStyle

/**
 * Brief You: the icon beside a settings row (like Smart RSS's settings). Rows that don't give an
 * icon get one picked from their title; the icon sits in a tile that follows the design style:
 * Expressive a soft rounded tile in the theme colour, Glass a see-through tile, Classic a plain
 * coloured icon with no tile.
 */
private val ICON_WORDS: List<Pair<List<String>, ImageVector>> by lazy {
    listOf(
        listOf("dark", "theme", "amoled") to Icons.Outlined.DarkMode,
        listOf("colour", "color", "palette", "accent") to Icons.Outlined.Palette,
        listOf("font", "typeface", "text size", "text") to Icons.Outlined.TextFields,
        listOf("reading", "article") to Icons.Outlined.MenuBook,
        listOf("image", "picture", "photo", "thumbnail") to Icons.Outlined.Image,
        listOf("video", "youtube") to Icons.Outlined.PlayCircle,
        listOf("podcast", "audio", "listen", "aloud", "speech", "voice") to Icons.Outlined.Headphones,
        listOf("notif", "alert") to Icons.Outlined.Notifications,
        listOf("sync", "refresh", "update", "fetch") to Icons.Outlined.Sync,
        listOf("backup", "restore", "export", "import", "opml") to Icons.Outlined.Backup,
        listOf("offline", "download", "storage", "cache", "clear") to Icons.Outlined.CloudDownload,
        listOf("account", "login", "sign") to Icons.Outlined.AccountCircle,
        listOf("language", "locale") to Icons.Outlined.Language,
        listOf("gesture", "swipe", "tap", "haptic", "vibrat", "interaction") to Icons.Outlined.TouchApp,
        listOf("filter", "mute", "word", "block") to Icons.Outlined.FilterAlt,
        listOf("ai", "gist", "summar", "gemma", "model") to Icons.Outlined.AutoAwesome,
        listOf("triage", "auto-advance", "advance") to Icons.Outlined.Style,
        listOf("bar", "bottom", "navigation") to Icons.Outlined.ViewDay,
        listOf("list", "layout", "view", "density", "card") to Icons.Outlined.ViewAgenda,
        listOf("glass", "blur", "frost", "refraction", "outline") to Icons.Outlined.BlurOn,
        listOf("contrast", "saturation", "brightness") to Icons.Outlined.Contrast,
        listOf("share", "newsletter", "mail") to Icons.Outlined.Share,
        listOf("search") to Icons.Outlined.Search,
        listOf("privacy", "security", "password", "lock") to Icons.Outlined.Lock,
        listOf("log", "debug", "trouble", "stutter", "perf") to Icons.Outlined.BugReport,
        listOf("star", "saved", "later", "bookmark") to Icons.Outlined.StarBorder,
        listOf("time", "sleep", "delay", "schedule", "hour") to Icons.Outlined.Schedule,
        listOf("tip", "support", "donat", "about", "licen", "help") to Icons.Outlined.Info,
    )
}

fun settingIconFor(title: String): ImageVector {
    val t = title.lowercase()
    return ICON_WORDS.firstOrNull { (words, _) -> words.any { it in t } }?.second ?: Icons.Outlined.Tune
}

/** The icon in its tile, in the current design style. */
@Composable
fun SettingIconTile(icon: ImageVector, description: String?, modifier: Modifier = Modifier) {
    val classic = BriefStyle.design == DesignStyle.Classic
    val glass = glassActive()
    when {
        classic -> Icon(icon, description, modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary)
        else -> Box(
            modifier
                .size(40.dp)
                .clip(if (glass) RoundedCornerShape(50) else RoundedCornerShape(14.dp))
                .background(
                    if (glass) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                    else MaterialTheme.colorScheme.secondaryContainer
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon, description, Modifier.size(22.dp),
                tint = if (glass) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}
