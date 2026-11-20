package me.ash.reader.brief.search

import android.content.Context
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import me.ash.reader.R
import me.ash.reader.brief.ui.SettingGroup
import me.ash.reader.ui.page.nav3.key.Route

/**
 * Searching the settings from the Settings page. Picking a result opens the page it is on and
 * [target] holds the setting's name: the page scrolls down ([SettingsLazyColumn]) until the row
 * that has that name shows up, brings it into view and makes it blink ([searchHighlight]).
 */
object SettingsSearch {
    /** The name of the setting to show, or null. */
    var target by mutableStateOf<String?>(null)

    /** Whether a row with that name has appeared on the page. */
    var found by mutableStateOf(false)
}

/** A page's scrolling list that can scroll to the setting picked in the search. */
@Composable
fun SettingsLazyColumn(content: LazyListScope.() -> Unit) {
    val state = rememberLazyListState()
    val target = SettingsSearch.target
    LaunchedEffect(target) {
        if (target == null) return@LaunchedEffect
        delay(450) // the page is still sliding in
        var steps = 0
        while (SettingsSearch.target == target && !SettingsSearch.found && state.canScrollForward && steps < 40) {
            state.animateScrollBy(650f)
            delay(80)
            steps++
        }
        // Not on this page (a setting inside a dialog, say): back to the top, nothing to show.
        if (SettingsSearch.target == target && !SettingsSearch.found) {
            state.scrollToItem(0)
            SettingsSearch.target = null
        }
    }
    DisposableEffect(Unit) { onDispose { SettingsSearch.found = false } }
    LazyColumn(state = state, content = content)
}

/** For a setting's row: when the search picked it, it scrolls into view and blinks three times. */
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.searchHighlight(title: String): Modifier = composed {
    val picked = SettingsSearch.target == title
    val flash = remember { Animatable(0f) }
    val bring = remember { BringIntoViewRequester() }
    val color = MaterialTheme.colorScheme.tertiaryContainer
    LaunchedEffect(picked) {
        if (!picked) return@LaunchedEffect
        SettingsSearch.found = true
        delay(200) // the page has laid itself out
        bring.bringIntoView()
        repeat(3) {
            flash.animateTo(1f, tween(260))
            flash.animateTo(0f, tween(360))
        }
        SettingsSearch.target = null
        SettingsSearch.found = false
    }
    this.bringIntoViewRequester(bring).drawBehind {
        if (flash.value > 0f) {
            drawRoundRect(
                color = color.copy(alpha = flash.value),
                cornerRadius = CornerRadius(16.dp.toPx()),
            )
        }
    }
}

private class Resolved(val entry: SettingsEntry, val title: String, val desc: String, val path: String) {
    val haystack = "$title $desc $path".lowercase()
    val isPage get() = entry.title == entry.page
}

private fun resolve(context: Context): List<Resolved> =
    SettingsIndex.entries.map { e ->
        Resolved(
            e,
            context.getString(e.title),
            e.desc?.let { context.getString(it) } ?: "",
            // A page's own row shows where it is, a setting shows the pages leading to it.
            e.path.let { p -> (if (e.title == e.page) p.dropLast(1) else p).joinToString(" › ") { context.getString(it) } },
        )
    }

private fun search(all: List<Resolved>, query: String): List<Resolved> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return emptyList()
    val words = q.split(Regex("\\s+"))
    return all.mapNotNull { r ->
        val title = r.title.lowercase()
        val rank =
            when {
                title.startsWith(q) -> 0
                title.contains(q) -> 1
                words.all { r.haystack.contains(it) } -> 2
                else -> return@mapNotNull null
            }
        rank to r
    }.sortedWith(compareBy({ it.first }, { it.second.isPage.not() })).map { it.second }.take(40)
}

/** The search bar at the top of the Settings page. */
@Composable
fun SettingsSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        singleLine = true,
        shape = CircleShape,
        placeholder = { Text(stringResource(R.string.settings_search_hint)) },
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close))
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        colors =
            TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
            ),
    )
}

/** The matching settings, each with the page it lives on; picking one opens that page. */
@Composable
fun SettingsSearchResults(query: String, onOpen: (Route) -> Unit) {
    val context = LocalContext.current
    val all = remember { resolve(context) }
    val matches = remember(query, all) { search(all, query) }
    if (matches.isEmpty()) {
        Text(
            text = stringResource(R.string.settings_search_empty),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        )
        return
    }
    SettingGroup {
        matches.forEach { r ->
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .clickable {
                            SettingsSearch.found = false
                            SettingsSearch.target = if (r.isPage) null else r.title
                            onOpen(r.entry.route)
                        }
                        .padding(20.dp, 14.dp, 16.dp, 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = r.title,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                    )
                    if (r.path.isNotEmpty()) {
                        Text(
                            text = r.path,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
