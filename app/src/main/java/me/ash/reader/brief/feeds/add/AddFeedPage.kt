package me.ash.reader.brief.feeds.add

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Newspaper
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AssistChip
import me.ash.reader.brief.ui.Button // Brief You
import me.ash.reader.brief.ui.BriefDropdownMenu // Brief You
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import me.ash.reader.brief.ui.OutlinedButton // Brief You
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.domain.model.group.Group
import me.ash.reader.ui.component.base.TextFieldDialog
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeState
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel

/**
 * Add Feed as one full page (Follow sources and Add Feed in one): the country and language the
 * sources come from, one box that searches the ready-made list as you type and also takes a pasted
 * link, `r/subreddit` or `@youtubechannel` (with a paste button), the category grid, the group the
 * feed goes into, and Add Feed / Import OPML. Add Feed hands the address to the same found-feed
 * step as before (SubscribeViewModel.searchFeed), which shows the feed's settings. Brief You.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddFeedPage(
    viewModel: SubscribeViewModel,
    state: SubscribeState.Input,
    openFile: (Array<String>) -> Unit,
    close: () -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val uiState = viewModel.subscribeUiState.collectAsStateValue()
    val groups by viewModel.groupsFlow.collectAsState()
    val linkState = state.linkState
    val query = linkState.text.toString()
    val fetching = state is SubscribeState.Fetching

    var categoryId by rememberSaveable { mutableStateOf<String?>(null) }
    // Browsing the list (a category, or "All") instead of the category grid.
    var browsing by rememberSaveable { mutableStateOf(false) }
    val category = categoryId?.let { FeedCatalogue.Category.of(it) }
    var country by remember { mutableStateOf(AddFeedPrefs.country(context)) }
    // Brief You: no language filter here any more (articles in other languages are read with the AI translation).
    val language = ""
    var picked by rememberSaveable { mutableStateOf<String?>(null) }
    var resolving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val groupId = picked ?: groups.firstOrNull()?.id
    val groupName = groups.firstOrNull { it.id == groupId }?.name ?: ""

    // A group made from the dropdown is selected as soon as it appears in the list.
    var knownGroups by remember { mutableStateOf(groups.map { it.id }.toSet()) }
    var waitingForNewGroup by remember { mutableStateOf(false) }
    LaunchedEffect(groups) {
        if (waitingForNewGroup) {
            groups.firstOrNull { it.id !in knownGroups }?.let {
                picked = it.id
                waitingForNewGroup = false
            }
        }
        knownGroups = groups.map { it.id }.toSet()
    }

    fun submit(text: String) {
        if (fetching || resolving || text.isBlank()) return
        error = null
        focusManager.clearFocus()
        scope.launch {
            resolving = true
            val link = runCatching { FeedSourceResolver.resolve(text) }
            resolving = false
            link.onFailure { error = it.message }
            link.onSuccess {
                AddFeedPrefs.pendingGroupId = groupId
                linkState.edit { replace(0, length, it) }
                viewModel.searchFeed()
            }
        }
    }

    Dialog(
        onDismissRequest = close,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = close) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                    Text(
                        stringResource(R.string.add_page_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }

                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
                ) {
                    // Sources from
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.add_page_sources_from),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.width(8.dp))
                        PickerChip(
                            value = country.ifEmpty { stringResource(R.string.add_page_all_countries) },
                            options = listOf(FeedCatalogue.ALL) + FeedCatalogue.countries(context),
                            label = { it.ifEmpty { stringResource(R.string.add_page_all_countries) } },
                            selected = country,
                            onPick = {
                                country = it
                                AddFeedPrefs.setCountry(context, it)
                            },
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    // The one box: search the list, or paste a link / r/name / @channel.
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 18.dp, end = 6.dp),
                        ) {
                            Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(12.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                BasicTextField(
                                    state = linkState,
                                    readOnly = fetching || resolving,
                                    lineLimits = androidx.compose.foundation.text.input.TextFieldLineLimits.SingleLine,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    onKeyboardAction = KeyboardActionHandler { submit(query) },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                if (query.isEmpty()) {
                                    Text(
                                        stringResource(R.string.add_page_search_hint),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    pasteFromClipboard(context)?.let { text ->
                                        categoryId = null
                                        browsing = false
                                        error = null
                                        linkState.edit { replace(0, length, text) }
                                    }
                                }
                            ) {
                                Icon(Icons.Rounded.ContentPaste, contentDescription = stringResource(R.string.add_page_paste))
                            }
                        }
                    }
                    if (fetching || resolving) {
                        LinearWavyProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
                    }
                    val message = error ?: (state as? SubscribeState.Idle)?.errorMessage
                    if (!message.isNullOrBlank()) {
                        Text(
                            message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                        )
                    }

                    if (query.isBlank() && !browsing) {
                        Text(
                            stringResource(R.string.add_page_browse),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                        )
                        CategoryGrid(
                            onPick = {
                                categoryId = it?.id
                                browsing = true
                            }
                        )
                    } else {
                        Spacer(Modifier.height(8.dp))
                        if (browsing) {
                            AssistChip(
                                onClick = {
                                    categoryId = null
                                    browsing = false
                                },
                                label = {
                                    Text(
                                        stringResource(R.string.add_page_back_to_categories) +
                                            (category?.let { "  ·  " + stringResource(it.title) } ?: "")
                                    )
                                },
                            )
                        }
                        Results(
                            query = query,
                            category = category,
                            country = country,
                            language = language,
                            listAll = browsing,
                            onAdd = { submit(it) },
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // Group and buttons stay at the bottom, in reach of the thumb.
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    GroupPicker(
                        groups = groups,
                        selectedId = groupId,
                        selectedName = groupName,
                        onPick = { picked = it },
                        onCreate = {
                            knownGroups = groups.map { it.id }.toSet()
                            waitingForNewGroup = true
                            viewModel.showNewGroupDialog()
                        },
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { submit(query) },
                            enabled = query.isNotBlank() && !fetching && !resolving,
                            modifier = Modifier.weight(1f).height(52.dp),
                        ) {
                            Text(stringResource(R.string.add_page_add))
                        }
                        if (state is SubscribeState.Idle && state.importFromOpmlEnabled) {
                            OutlinedButton(
                                onClick = {
                                    openFile(arrayOf(me.ash.reader.ui.ext.MimeType.ANY))
                                    close()
                                },
                                modifier = Modifier.height(52.dp),
                            ) {
                                Icon(Icons.Rounded.UploadFile, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.import_from_opml))
                            }
                        }
                    }
                }
            }
        }
    }

    // The name box for "Create new group" (the dropdown's last entry).
    val newGroupName = rememberTextFieldState()
    LaunchedEffect(Unit) {
        androidx.compose.runtime.snapshotFlow { newGroupName.text.toString() }.collect { viewModel.inputNewGroup(it) }
    }
    LaunchedEffect(uiState.newGroupDialogVisible) {
        if (!uiState.newGroupDialogVisible) newGroupName.edit { replace(0, length, "") }
    }
    TextFieldDialog(
        textFieldState = newGroupName,
        visible = uiState.newGroupDialogVisible,
        title = stringResource(R.string.create_new_group),
        icon = Icons.Outlined.CreateNewFolder,
        placeholder = stringResource(R.string.name),
        onDismissRequest = {
            waitingForNewGroup = false
            viewModel.hideNewGroupDialog()
        },
        onConfirm = { viewModel.addNewGroup() },
    )
}

private fun pasteFromClipboard(context: Context): String? {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
    val clip = clipboard.primaryClip ?: return null
    if (clip.itemCount == 0) return null
    return clip.getItemAt(0).coerceToText(context)?.toString()?.trim()?.takeIf { it.isNotEmpty() }
}

@Composable
private fun PickerChip(
    value: String,
    options: List<String>,
    label: @Composable (String) -> String,
    selected: String,
    onPick: (String) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        AssistChip(onClick = { open = true }, label = { Text(value, maxLines = 1) }, trailingIcon = {
            Icon(Icons.Rounded.ArrowDropDown, contentDescription = null, modifier = Modifier.size(18.dp))
        })
        BriefDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(label(option)) },
                    trailingIcon = { if (option == selected) Icon(Icons.Rounded.Check, contentDescription = null) },
                    onClick = {
                        open = false
                        onPick(option)
                    },
                )
            }
        }
    }
}

private val categoryIcons: Map<FeedCatalogue.Category, ImageVector> =
    mapOf(
        FeedCatalogue.Category.World to Icons.Rounded.Public,
        FeedCatalogue.Category.Business to Icons.Rounded.BusinessCenter,
        FeedCatalogue.Category.Politics to Icons.Rounded.AccountBalance,
        FeedCatalogue.Category.Lifestyle to Icons.Rounded.Palette,
        FeedCatalogue.Category.SciTech to Icons.Rounded.Devices,
        FeedCatalogue.Category.Sports to Icons.Rounded.SportsSoccer,
        FeedCatalogue.Category.Opinion to Icons.Rounded.Forum,
        FeedCatalogue.Category.Local to Icons.Outlined.Newspaper,
        FeedCatalogue.Category.Other to Icons.Rounded.MoreHoriz,
    )

/** "All" then the nine categories, four to a row. [onPick] gets null for "All" (the whole list). */
@Composable
private fun CategoryGrid(onPick: (FeedCatalogue.Category?) -> Unit) {
    val items: List<Pair<FeedCatalogue.Category?, ImageVector>> =
        listOf<Pair<FeedCatalogue.Category?, ImageVector>>(null to Icons.Rounded.RssFeed) +
            FeedCatalogue.Category.entries.map { it to categoryIcons.getValue(it) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(4).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                row.forEach { (cat, icon) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable { onPick(cat) }.padding(vertical = 8.dp, horizontal = 2.dp),
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            stringResource(cat?.title ?: R.string.cat_all),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun Results(
    query: String,
    category: FeedCatalogue.Category?,
    country: String,
    language: String,
    listAll: Boolean,
    onAdd: (String) -> Unit,
) {
    val context = LocalContext.current
    val detected = remember(query) { FeedSourceResolver.detect(query) }
    val found = remember(query, category, country, language, listAll) { FeedCatalogue.find(context, query, category, country, language, listAll) }
    val looksLikeAddress = remember(query) { query.trim().let { it.startsWith("http", true) || ('.' in it && ' ' !in it) } }

    Column {
        if (detected != null) {
            val titleRes =
                when (detected.kind) {
                    FeedSourceResolver.Kind.Reddit -> R.string.add_page_hint_reddit
                    FeedSourceResolver.Kind.YouTube -> R.string.add_page_hint_youtube
                    FeedSourceResolver.Kind.YouTubePlaylist -> R.string.add_page_hint_playlist
                }
            val icon = if (detected.kind == FeedSourceResolver.Kind.Reddit) Icons.Rounded.Forum else Icons.Rounded.PlayCircle
            ResultRow(icon, stringResource(titleRes), detected.title) { onAdd(query) }
        } else if (looksLikeAddress) {
            ResultRow(Icons.Rounded.RssFeed, stringResource(R.string.add_page_hint_site), query.trim()) { onAdd(query) }
        }
        found.forEach { e ->
            ResultRow(Icons.Outlined.Newspaper, e.title, "${e.shortUrl}  ·  ${e.country}") { onAdd(e.url) }
        }
        if (found.isEmpty() && detected == null && !looksLikeAddress) {
            Text(
                stringResource(if (category != null && query.isBlank()) R.string.add_page_empty_category else R.string.add_page_no_results),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun ResultRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 10.dp, horizontal = 4.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(30.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
            }
        }
    }
}

@Composable
private fun GroupPicker(
    groups: List<Group>,
    selectedId: String?,
    selectedName: String,
    onPick: (String) -> Unit,
    onCreate: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    var anchorWidth by remember { mutableStateOf(0.dp) }
    val density = androidx.compose.ui.platform.LocalDensity.current
    Box(modifier = Modifier.onSizeChanged { anchorWidth = with(density) { it.width.toDp() } }) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.background,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(14.dp)).clickable { open = true },
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                Icon(Icons.Rounded.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.add_page_group), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(selectedName, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
            }
        }
        BriefDropdownMenu(expanded = open, onDismissRequest = { open = false }, modifier = Modifier.width(anchorWidth)) {
            groups.forEach { g ->
                DropdownMenuItem(
                    text = { Text(g.name) },
                    leadingIcon = { Icon(Icons.Rounded.Folder, contentDescription = null) },
                    trailingIcon = { if (g.id == selectedId) Icon(Icons.Rounded.Check, contentDescription = null) },
                    onClick = {
                        open = false
                        onPick(g.id)
                    },
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(stringResource(R.string.create_new_group), color = MaterialTheme.colorScheme.primary) },
                leadingIcon = { Icon(Icons.Outlined.CreateNewFolder, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                onClick = {
                    open = false
                    onCreate()
                },
            )
        }
    }
}
