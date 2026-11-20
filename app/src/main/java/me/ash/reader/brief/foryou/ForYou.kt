package me.ash.reader.brief.foryou

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Xml
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.ash.reader.R
import me.ash.reader.brief.ui.BriefDropdownMenu
import me.ash.reader.brief.ui.BriefLoading
import me.ash.reader.brief.ui.Corner
import me.ash.reader.brief.ui.TextButton
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.theme.palette.onLight
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Brief You: "For you", a Discover-like page. Google has no public Discover feed, so this is built
 * from what Google does publish: its News search feeds, one per topic you pick. Nothing about you
 * is sent anywhere except the topic words. Not interested / Hide source are remembered on the phone.
 */
data class ForYouItem(
    val title: String,
    val link: String,
    val source: String,
    val publishedMs: Long,
    val topic: String,
)

object ForYouStore {
    private const val FILE = "brief_foryou"
    private val DEFAULT_TOPICS = listOf("Psychiatry", "Mental health", "Medical education", "Health policy India")

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun topics(context: Context): List<String> =
        prefs(context).getString("topics", null)?.split('\n')?.filter { it.isNotBlank() } ?: DEFAULT_TOPICS

    fun setTopics(context: Context, topics: List<String>) =
        prefs(context).edit().putString("topics", topics.joinToString("\n")).apply()

    fun hiddenLinks(context: Context): Set<String> = prefs(context).getStringSet("hiddenLinks", emptySet()) ?: emptySet()
    fun hiddenSources(context: Context): Set<String> = prefs(context).getStringSet("hiddenSources", emptySet()) ?: emptySet()

    fun hideLink(context: Context, link: String) =
        prefs(context).edit().putStringSet("hiddenLinks", (hiddenLinks(context) + link).toList().takeLast(500).toSet()).apply()

    fun hideSource(context: Context, source: String) =
        prefs(context).edit().putStringSet("hiddenSources", hiddenSources(context) + source).apply()
}

object ForYouFetcher {
    private val dateFormat get() = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)

    /** Google News' search feed for one topic, last week, English (India). */
    suspend fun fetch(topic: String): List<ForYouItem> = withContext(Dispatchers.IO) {
        runCatching {
            val q = URLEncoder.encode("$topic when:7d", "UTF-8")
            val url = URL("https://news.google.com/rss/search?q=$q&hl=en-IN&gl=IN&ceid=IN:en")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 10_000
                readTimeout = 15_000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android) Lede")
            }
            try {
                conn.inputStream.use { parse(it, topic) }
            } finally {
                conn.disconnect()
            }
        }.getOrDefault(emptyList())
    }

    /**
     * Google News links are redirects (news.google.com/rss/articles/...). This asks Google for the
     * real address the same way its own page does, so the story can be opened in the app. Null when
     * it can't be worked out (Google changed something, or no connection).
     */
    suspend fun resolve(link: String): String? = withContext(Dispatchers.IO) {
        runCatching {
            val id = Regex("articles/([^?/]+)").find(link)?.groupValues?.get(1) ?: return@runCatching null
            if (!link.contains("news.google.com")) return@runCatching link
            // 1. The article page carries a signature and timestamp for this story.
            val page = (URL("https://news.google.com/rss/articles/$id").openConnection() as HttpURLConnection).run {
                connectTimeout = 10_000; readTimeout = 15_000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android) Lede")
                try { inputStream.bufferedReader().readText() } finally { disconnect() }
            }
            val sg = Regex("data-n-a-sg=\"([^\"]+)\"").find(page)?.groupValues?.get(1) ?: return@runCatching null
            val ts = Regex("data-n-a-ts=\"([^\"]+)\"").find(page)?.groupValues?.get(1) ?: return@runCatching null
            // 2. The same call Google's page makes to turn it into the publisher's address.
            val inner = "[\"garturlreq\",[[\"X\",\"X\",[\"X\",\"X\"],null,null,1,1,\"US:en\",null,1,null,null,null,null,null,0,1],\"X\",\"X\",1,[1,1,1],1,1,null,0,0,null,0],\"$id\",$ts,\"$sg\"]"
            val outer = "[[[\"Fbv4je\"," + org.json.JSONObject.quote(inner) + ",null,\"generic\"]]]"
            val body = "f.req=" + URLEncoder.encode(outer, "UTF-8")
            val conn = (URL("https://news.google.com/_/DotsSplashUi/data/batchexecute").openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 10_000; readTimeout = 15_000
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                setRequestProperty("User-Agent", "Mozilla/5.0 (Android) Lede")
            }
            val response = try {
                conn.outputStream.use { it.write(body.toByteArray()) }
                conn.inputStream.bufferedReader().readText()
            } finally { conn.disconnect() }
            // The reply is ")]}'" then a JSON array whose first row holds, as text, [..., "<address>", ...].
            val json = org.json.JSONArray(response.substringAfter("\n\n"))
            val text = json.getJSONArray(0).getString(2)
            org.json.JSONArray(text).getString(1).takeIf { it.startsWith("http") }
        }.getOrNull()
    }

    private fun parse(stream: java.io.InputStream, topic: String): List<ForYouItem> {
        val parser = Xml.newPullParser()
        parser.setInput(stream, null)
        val out = mutableListOf<ForYouItem>()
        var title = ""; var link = ""; var date = 0L; var source = ""
        var inItem = false
        var event = parser.eventType
        while (event != org.xmlpull.v1.XmlPullParser.END_DOCUMENT) {
            when (event) {
                org.xmlpull.v1.XmlPullParser.START_TAG -> when (parser.name) {
                    "item" -> { inItem = true; title = ""; link = ""; date = 0L; source = "" }
                    "title" -> if (inItem) title = parser.nextText()
                    "link" -> if (inItem) link = parser.nextText()
                    "pubDate" -> if (inItem) date = runCatching { dateFormat.parse(parser.nextText())?.time }.getOrNull() ?: 0L
                    "source" -> if (inItem) source = parser.nextText()
                }
                org.xmlpull.v1.XmlPullParser.END_TAG -> if (parser.name == "item") {
                    inItem = false
                    val clean = if (source.isNotBlank() && title.endsWith(" - $source")) title.removeSuffix(" - $source") else title
                    if (clean.isNotBlank() && link.isNotBlank()) out += ForYouItem(clean.trim(), link.trim(), source.trim(), date, topic)
                }
            }
            event = parser.next()
        }
        return out
    }

    /** All topics at once, interleaved (one story from each in turn, newest first) so no topic floods the page. */
    suspend fun forYou(topics: List<String>): List<ForYouItem> = coroutineScope {
        val lists = topics.map { t -> async { fetch(t).sortedByDescending { it.publishedMs }.take(12) } }.awaitAll()
        val seen = HashSet<String>()
        val result = mutableListOf<ForYouItem>()
        var i = 0
        while (lists.any { i < it.size }) {
            lists.forEach { l ->
                l.getOrNull(i)?.let {
                    if (seen.add(it.title.lowercase().take(60))) result += it
                }
            }
            i++
        }
        result
    }
}

private fun ago(ms: Long): String {
    if (ms <= 0) return ""
    val min = ((System.currentTimeMillis() - ms) / 60_000).coerceAtLeast(0)
    return when {
        min < 60 -> "${min}m"
        min < 60 * 24 -> "${min / 60}h"
        else -> "${min / 60 / 24}d"
    }
}

@dagger.hilt.android.lifecycle.HiltViewModel
class ForYouViewModel @javax.inject.Inject constructor(
    private val readLater: me.ash.reader.brief.readlater.ReadLater,
) : androidx.lifecycle.ViewModel() {
    /** Brings the story into the app (as an unstarred article in "Saved Articles") and returns where to find it; null if that wasn't possible. */
    suspend fun bringIn(item: ForYouItem): me.ash.reader.brief.readlater.ReadLater.Saved? {
        if (!readLater.canSave()) return null
        val real = ForYouFetcher.resolve(item.link) ?: return null
        return runCatching { readLater.save(real, item.title, star = false) }.getOrNull()?.takeIf { it.fetched }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Suppress("DEPRECATION")
@Composable
fun ForYouPage(onBack: () -> Unit, viewModel: ForYouViewModel = androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var topics by remember { mutableStateOf(ForYouStore.topics(context)) }
    var items by remember { mutableStateOf<List<ForYouItem>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var reload by remember { mutableIntStateOf(0) }
    var hiddenLinks by remember { mutableStateOf(ForYouStore.hiddenLinks(context)) }
    var hiddenSources by remember { mutableStateOf(ForYouStore.hiddenSources(context)) }
    var addVisible by remember { mutableStateOf(false) }
    var opening by remember { mutableStateOf<String?>(null) }
    val openStory: (ForYouItem) -> Unit = { item ->
        if (opening == null) {
            opening = item.link
            scope.launch {
                val saved = viewModel.bringIn(item)
                opening = null
                if (saved != null) {
                    context.startActivity(
                        Intent(context, me.ash.reader.infrastructure.android.MainActivity::class.java)
                            .putExtra(me.ash.reader.ui.page.common.ExtraName.ARTICLE_ID, saved.articleId)
                            .putExtra(me.ash.reader.ui.page.common.ExtraName.FEED_ID, saved.feedId)
                    )
                } else {
                    // Couldn't bring it in: the browser still has it.
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.link)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                }
            }
        }
    }

    LaunchedEffect(topics, reload) {
        loading = true
        items = ForYouFetcher.forYou(topics)
        loading = false
    }
    val shown = items.filter { it.link !in hiddenLinks && it.source !in hiddenSources }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack,
            )
        },
        content = {
            LazyColumn {
                item { DisplayText(text = stringResource(R.string.for_you), desc = "") }
                item {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        topics.forEach { t ->
                            InputChip(
                                selected = false,
                                onClick = {},
                                label = { Text(t) },
                                trailingIcon = {
                                    Icon(
                                        Icons.Outlined.Close,
                                        contentDescription = stringResource(R.string.for_you_remove_topic),
                                        modifier = Modifier.size(18.dp).clickable {
                                            topics = topics - t
                                            ForYouStore.setTopics(context, topics)
                                        },
                                    )
                                },
                            )
                        }
                        AssistChip(
                            onClick = { addVisible = true },
                            label = { Text(stringResource(R.string.for_you_add_topic)) },
                            leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.for_you_source_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f).padding(start = 8.dp),
                        )
                        IconButton(onClick = { reload++ }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = stringResource(R.string.for_you_refresh))
                        }
                    }
                }
                if (loading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { BriefLoading() }
                    }
                } else if (shown.isEmpty()) {
                    item {
                        Text(
                            stringResource(if (topics.isEmpty()) R.string.for_you_no_topics else R.string.for_you_empty),
                            modifier = Modifier.padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    item {
                        me.ash.reader.ui.component.base.Subtitle(
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                            text = stringResource(R.string.for_you), // Brief You
                        )
                    }
                    items(shown, key = { it.link }) { item ->
                        var menu by remember { mutableStateOf(false) }
                        // Brief You: follows the design style: plain rows in Classic, cards in Expressive,
                        // a see-through tinted card with Glass.
                        val classicStyle = me.ash.reader.brief.ui.style.BriefStyle.design == me.ash.reader.brief.ui.style.DesignStyle.Classic
                        val glassStyle = me.ash.reader.brief.ui.glass.glassActive()
                        Surface(
                            shape = RoundedCornerShape(if (classicStyle) 0.dp else if (glassStyle) 28.dp else Corner.Small),
                            color = when {
                                classicStyle -> androidx.compose.ui.graphics.Color.Transparent
                                glassStyle -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                else -> MaterialTheme.colorScheme.surfaceContainerHigh
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = if (classicStyle) 0.dp else 16.dp, vertical = if (classicStyle) 0.dp else 4.dp)
                                .clickable { openStory(item) },
                        ) {
                            Row(modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.Top) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        listOf(item.source, ago(item.publishedMs)).filter { it.isNotBlank() }.joinToString(" · "),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                    if (opening == item.link) {
                                        Text(
                                            stringResource(R.string.for_you_opening),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.padding(top = 6.dp),
                                        )
                                    }
                                    Text(
                                        item.topic,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 6.dp),
                                    )
                                }
                                Box {
                                    IconButton(onClick = { menu = true }) {
                                        Icon(Icons.Outlined.MoreVert, contentDescription = null)
                                    }
                                    BriefDropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                        DropdownMenuItem(
                                            text = { Text(stringResource(R.string.for_you_not_interested)) },
                                            onClick = {
                                                menu = false
                                                ForYouStore.hideLink(context, item.link)
                                                hiddenLinks = ForYouStore.hiddenLinks(context)
                                            },
                                        )
                                        if (item.source.isNotBlank()) {
                                            DropdownMenuItem(
                                                text = { Text(stringResource(R.string.for_you_hide_source, item.source)) },
                                                onClick = {
                                                    menu = false
                                                    ForYouStore.hideSource(context, item.source)
                                                    hiddenSources = ForYouStore.hiddenSources(context)
                                                },
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(48.dp)) }
            }
        },
    )

    if (addVisible) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { addVisible = false },
            title = { Text(stringResource(R.string.for_you_add_topic)) },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.for_you_topic_hint)) },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val t = text.trim()
                        if (t.isNotEmpty() && t !in topics) {
                            topics = topics + t
                            ForYouStore.setTopics(context, topics)
                        }
                        addVisible = false
                    },
                ) { Text(stringResource(R.string.for_you_add)) }
            },
            dismissButton = { TextButton(onClick = { addVisible = false }) { Text(stringResource(R.string.cancel)) } },
        )
    }
}
