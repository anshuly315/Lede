package me.ash.reader.ui.page.home.flow

// Brief You
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.graphics.Color
import me.ash.reader.brief.feeds.rememberFeedAccent
import me.ash.reader.brief.reading.FeedContent
import me.ash.reader.brief.reading.estimateReadingMinutes
import me.ash.reader.ui.component.swipe.SwipeableActionsState
import me.ash.reader.ui.component.swipe.rememberSwipeableActionsState

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height // Brief You: the card picture
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.FiberManualRecord
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CheckCircleOutline
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics // Brief You
import androidx.compose.ui.semantics.contentDescription // Brief You
import androidx.compose.ui.semantics.onClick // Brief You
import androidx.compose.ui.semantics.onLongClick // Brief You
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import coil.size.Precision
import coil.size.Scale
import me.ash.reader.R
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.infrastructure.preference.FlowArticleListDescPreference
import me.ash.reader.infrastructure.preference.FlowArticleReadIndicatorPreference
import me.ash.reader.infrastructure.preference.LocalArticleListSwipeEndAction
import me.ash.reader.infrastructure.preference.LocalArticleListSwipeStartAction
import me.ash.reader.infrastructure.preference.LocalFlowArticleListDesc
import me.ash.reader.infrastructure.preference.LocalFlowArticleListFeedIcon
import me.ash.reader.infrastructure.preference.LocalFlowArticleListFeedName
import me.ash.reader.infrastructure.preference.LocalFlowArticleListImage
import me.ash.reader.infrastructure.preference.LocalFlowArticleListReadIndicator
import me.ash.reader.infrastructure.preference.LocalFlowArticleListTime
import me.ash.reader.infrastructure.preference.SwipeEndActionPreference
import me.ash.reader.infrastructure.preference.SwipeStartActionPreference
import me.ash.reader.ui.component.FeedIcon
import me.ash.reader.ui.component.base.RYAsyncImage
import me.ash.reader.ui.component.base.SIZE_1000
import me.ash.reader.ui.component.menu.AnimatedDropdownMenu
import me.ash.reader.ui.component.swipe.SwipeAction
import me.ash.reader.ui.component.swipe.SwipeableActionsBox
import me.ash.reader.ui.ext.requiresBidi
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.page.settings.color.flow.generateArticleWithFeedPreview
import me.ash.reader.ui.theme.Shape20
import me.ash.reader.brief.ui.applyTextDirection // Brief You
import me.ash.reader.ui.theme.palette.onDark

private const val TAG = "ArticleItem"

@Composable
fun ArticleItem(
    modifier: Modifier = Modifier,
    articleWithFeed: ArticleWithFeed,
    isUnread: Boolean = articleWithFeed.article.isUnread,
    onClick: (ArticleWithFeed) -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    val feed = articleWithFeed.feed
    val article = articleWithFeed.article

    ArticleItem(
        modifier = modifier,
        feedName = feed.name,
        feedIconUrl = feed.icon,
        title = article.title,
        shortDescription = article.shortDescription,
        timeString = article.dateString,
        imgData = FeedContent.listImage(article.img, article.link), // Brief You: better pictures
        isStarred = article.isStarred,
        isUnread = isUnread,
        readingMinutes = remember(article.id) { me.ash.reader.brief.reading.ListReadingTime.of(article.id) }, // Brief You: worked out in the background
        onClick = { onClick(articleWithFeed) },
        onLongClick = onLongClick,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ArticleItem(
    modifier: Modifier = Modifier,
    feedName: String = "",
    feedIconUrl: String? = null,
    title: String = "",
    shortDescription: String = "",
    timeString: String? = null,
    imgData: Any? = null,
    isStarred: Boolean = false,
    isUnread: Boolean = false,
    readingMinutes: Int? = null, // Brief You
    onClick: () -> Unit = {},
    onLongClick: (() -> Unit)? = null,
) {
    if (me.ash.reader.brief.perf.PerfLog.enabled) androidx.compose.runtime.SideEffect { me.ash.reader.brief.perf.PerfLog.note("row built") } // Brief You: Nightly stutter log
    val articleListFeedIcon = LocalFlowArticleListFeedIcon.current
    val articleListFeedName = LocalFlowArticleListFeedName.current
    val articleListImage = LocalFlowArticleListImage.current
    val articleListDesc = LocalFlowArticleListDesc.current
    val articleListDate = LocalFlowArticleListTime.current
    val articleListReadIndicator = LocalFlowArticleListReadIndicator.current
    // Brief You: view mode, density, picture size and corners (Flow page settings).
    me.ash.reader.brief.ui.style.BriefLook.init(androidx.compose.ui.platform.LocalContext.current) // Brief You
    val lookCards = me.ash.reader.brief.ui.style.BriefLook.mode == me.ash.reader.brief.ui.style.ListMode.Cards // Brief You
    val lookMagazine = me.ash.reader.brief.ui.style.BriefLook.mode == me.ash.reader.brief.ui.style.ListMode.Magazine // Brief You
    val lookCorner = me.ash.reader.brief.ui.style.BriefLook.corner.dp // Brief You
    val lookImage = me.ash.reader.brief.ui.style.BriefLook.imageSize.dp // Brief You
    val lookCompact = me.ash.reader.brief.ui.style.BriefLook.density == me.ash.reader.brief.ui.style.ListDensity.Compact // Brief You
    // Brief You: reading time next to the time ("4 min · 2h").
    val readingLabel = readingMinutes?.let { stringResource(R.string.reading_time_min, it) }
    val timeLabel = listOfNotNull(readingLabel, timeString?.takeIf { it.isNotBlank() })
        .joinToString(" · ")
    // Brief You: the row is one element for accessibility (read as one item, like Android's own
    // lists), so accessibility services don't walk its every text and icon after each change.
    val unreadLabel = stringResource(R.string.unread) // Brief You
    val starredLabel = stringResource(R.string.starred) // Brief You
    val rowDescription = // Brief You
        listOfNotNull( // Brief You
            feedName.takeIf { it.isNotBlank() }, title, shortDescription.takeIf { it.isNotBlank() }, // Brief You
            timeLabel.takeIf { it.isNotBlank() }, unreadLabel.takeIf { isUnread }, starredLabel.takeIf { isStarred }, // Brief You
        ).joinToString(", ") // Brief You
    val rowClick = onClick // Brief You
    val rowLongClick = onLongClick // Brief You

    Column(
        modifier =
            modifier
                .padding(horizontal = 12.dp)
                .clip(if (lookCards) androidx.compose.foundation.shape.RoundedCornerShape(lookCorner) else Shape20) // Brief You: cards use the corner setting
                .then(if (lookCards) Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow) else Modifier) // Brief You
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 12.dp, vertical = if (lookCompact) 6.dp else 12.dp) // Brief You: density
                .alpha(
                    when (articleListReadIndicator) {
                        FlowArticleReadIndicatorPreference.None -> 1f

                        FlowArticleReadIndicatorPreference.AllRead -> {
                            if (isUnread) 1f else 0.5f
                        }

                        FlowArticleReadIndicatorPreference.ExcludingStarred -> {
                            if (isUnread || isStarred) 1f else 0.5f
                        }
                    }
                )
                .clearAndSetSemantics { // Brief You
                    contentDescription = rowDescription // Brief You
                    this.onClick(label = null) { rowClick(); true } // Brief You
                    if (rowLongClick != null) this.onLongClick(label = null) { rowLongClick(); true } // Brief You
                } // Brief You
    ) {
        // Top
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Feed name, with the feed icon inline at its start (Brief You)
            if (articleListFeedName.value) {
                if (articleListFeedIcon.value) {
                    FeedIcon(feedName = feedName, iconUrl = feedIconUrl, size = 16.dp)
                    Spacer(modifier = Modifier.width(6.dp))
                }
                // Brief You: the feed name, then a watched word's chip right after it.
                Row(modifier = Modifier.weight(1f).padding(end = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.weight(1f, fill = false),
                    text = feedName,
                    // Brief You: a muted accent taken from the feed's icon.
                    color = rememberFeedAccent(feedIconUrl) ?: MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                me.ash.reader.brief.filters.WatchedChip(title, isUnread) // Brief You: watched words
                } // Brief You

                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier) {
                    // Starred
                    if (isStarred) {
                        StarredIcon()
                    }

                    if (articleListDate.value) {
                        // Time
                        Text(
                            modifier = Modifier,
                            text = timeLabel, // Brief You
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (articleListFeedIcon.value) { // Brief You: the icon inline
                        FeedIcon(feedName = feedName, iconUrl = feedIconUrl, size = 16.dp)
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (articleListDate.value) {
                        // Time
                        Text(
                            modifier = Modifier.weight(1f),
                            text = timeLabel, // Brief You
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        // Starred
                        if (isStarred) {
                            StarredIcon()
                        }
                    }
                    me.ash.reader.brief.filters.WatchedChip(title, isUnread) // Brief You: watched words
                }
            }

            // Right

        }

        // Brief You: a card's picture, above the title (its height follows the picture size setting)
        if (lookCards && imgData != null && articleListImage.value) {
            RYAsyncImage(
                modifier = Modifier.padding(top = 8.dp).fillMaxWidth().height(lookImage * 2).clip(androidx.compose.foundation.shape.RoundedCornerShape(lookCorner)),
                data = imgData,
                scale = Scale.FILL,
                precision = Precision.INEXACT,
                size = with(androidx.compose.ui.platform.LocalDensity.current) { coil.size.Size(720.dp.roundToPx(), (lookImage * 2).roundToPx()) },
                contentScale = ContentScale.Crop,
            )
        }

        // Bottom
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            // (Brief You: the feed icon moved up, next to the feed name.)
            // Article
            Column(modifier = Modifier.weight(1f)) {

                // Title
                Row {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style =
                            MaterialTheme.typography.titleMedium
                                .applyTextDirection(title.requiresBidi())
                                .merge(lineHeight = 22.sp)
                                .let { it.copy(fontSize = it.fontSize * (me.ash.reader.brief.ui.style.BriefLook.listFontPct / 100f), lineHeight = it.lineHeight * (me.ash.reader.brief.ui.style.BriefLook.listFontPct / 100f)) }, // Brief You: list text size
                        maxLines =
                            if (me.ash.reader.brief.ui.style.BriefLook.shortenTitles || articleListDesc != FlowArticleListDescPreference.NONE) 2 else 4, // Brief You: shorten titles
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (!articleListFeedName.value && !articleListDate.value) {
                        if (isStarred) {
                            StarredIcon()
                        } else {
                            Spacer(modifier = Modifier.width(16.dp))
                        }
                    }
                }

                // Description
                if (
                    articleListDesc != FlowArticleListDescPreference.NONE &&
                        shortDescription.isNotBlank()
                ) {
                    Text(
                        modifier = Modifier.padding(top = 4.dp),
                        text = shortDescription,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style =
                            MaterialTheme.typography.bodySmall.applyTextDirection(
                                shortDescription.requiresBidi()
                            ).let { it.copy(fontSize = it.fontSize * (me.ash.reader.brief.ui.style.BriefLook.listFontPct / 100f), lineHeight = it.lineHeight * (me.ash.reader.brief.ui.style.BriefLook.listFontPct / 100f)) }, // Brief You: list text size
                        maxLines =
                            when (articleListDesc) {
                                FlowArticleListDescPreference.LONG -> 4
                                FlowArticleListDescPreference.SHORT -> 2
                                else -> throw IllegalStateException()
                            },
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            // Image
            if (imgData != null && articleListImage.value && lookMagazine) { // Brief You: only in the Magazine view
                RYAsyncImage(
                    modifier = Modifier.padding(start = 10.dp).size(lookImage).clip(androidx.compose.foundation.shape.RoundedCornerShape(lookCorner)), // Brief You: size and corners from the settings (80 dp, 16 dp by default)
                    data = imgData,
                    scale = Scale.FILL,
                    precision = Precision.INEXACT,
                    // Brief You: decoded at the size shown (80 dp), not 1000 px, so scrolling keeps
                    // more thumbnails in memory and decodes less
                    size = with(androidx.compose.ui.platform.LocalDensity.current) { lookImage.roundToPx() }.let { coil.size.Size(it, it) },
                    contentScale = ContentScale.Crop,
                )
            }
        }
    }
}

@Composable
fun StarredIcon(modifier: Modifier = Modifier) {
    val fontSize = LocalTextStyle.current.fontSize
    val iconSize = with(LocalDensity.current) { fontSize.toDp() }

    Icon(
        modifier = modifier
            .size(iconSize)
            .padding(end = 2.dp),
        imageVector = Icons.Rounded.Star,
        contentDescription = stringResource(R.string.starred),
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
    )
}

private const val PositionalThresholdFraction = 0.4f
private const val SwipeActionDelay = 300L

@Composable
fun SwipeableArticleItem(
    articleWithFeed: ArticleWithFeed,
    isUnread: Boolean = articleWithFeed.article.isUnread,
    articleListTonalElevation: Int = 0,
    onClick: (ArticleWithFeed) -> Unit = {},
    isSwipeEnabled: () -> Boolean = { false },
    isMenuEnabled: Boolean = true,
    onToggleStarred: (ArticleWithFeed) -> Unit = {},
    onToggleRead: (ArticleWithFeed) -> Unit = {},
    onMarkAboveAsRead: ((ArticleWithFeed) -> Unit)? = null,
    onMarkBelowAsRead: ((ArticleWithFeed) -> Unit)? = null,
    onShare: ((ArticleWithFeed) -> Unit)? = null,
) {
    var isMenuExpanded by remember { mutableStateOf(false) }
    val pressContext = androidx.compose.ui.platform.LocalContext.current // Brief You
    val pressedArticle by androidx.compose.runtime.rememberUpdatedState(articleWithFeed) // Brief You
    // Brief You: the menu is only built once it is first opened (every row built it, closed, as the
    // list appeared), and kept after that so it can animate closed.
    var menuUsed by remember { mutableStateOf(false) } // Brief You

    val onLongClick =
        if (isMenuEnabled) {
            {
                menuUsed = true // Brief You
                isMenuExpanded = true
            }
        } else {
            null
        }
    var menuOffset by remember { mutableStateOf(IntOffset.Zero) }

    SwipeActionBox(
        articleWithFeed = articleWithFeed,
        isRead = !isUnread,
        isStarred = articleWithFeed.article.isStarred,
        onToggleStarred = onToggleStarred,
        onToggleRead = onToggleRead,
    ) {
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .pointerInput(isMenuExpanded) {
                        awaitEachGesture {
                            while (true) {
                                awaitFirstDown(requireUnconsumed = false).let {
                                    menuOffset = it.position.round()
                                    me.ash.reader.brief.reading.ArticlePress.pressed(pressContext, pressedArticle) // Brief You: the article is prepared on the press
                                }
                            }
                        }
                    }
                    .background(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(
                            articleListTonalElevation.dp
                        ) onDark MaterialTheme.colorScheme.surface
                    )
                    .wrapContentSize()
        ) {
            ArticleItem(
                articleWithFeed = articleWithFeed,
                isUnread = isUnread,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            with(articleWithFeed.article) {
                if (isMenuEnabled && menuUsed) { // Brief You
                    AnimatedDropdownMenu(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false },
                        offset = menuOffset,
                    ) {
                        ArticleItemMenuContent(
                            articleWithFeed = articleWithFeed,
                            isStarred = isStarred,
                            isRead = !isUnread,
                            onToggleStarred = onToggleStarred,
                            onToggleRead = onToggleRead,
                            onMarkAboveAsRead = onMarkAboveAsRead,
                            onMarkBelowAsRead = onMarkBelowAsRead,
                            onShare = onShare,
                        ) {
                            isMenuExpanded = false
                        }
                    }
                }
            }
        }
    }
}

private enum class SwipeDirection {
    StartToEnd,
    EndToStart,
}

@Composable
private fun SwipeActionBox(
    modifier: Modifier = Modifier,
    articleWithFeed: ArticleWithFeed,
    isStarred: Boolean,
    isRead: Boolean,
    onToggleStarred: (ArticleWithFeed) -> Unit,
    onToggleRead: (ArticleWithFeed) -> Unit,
    content: @Composable () -> Unit,
) {
    val containerColor = MaterialTheme.colorScheme.tertiaryContainer
    // Brief You: the icon sits on the page color until the swipe passes the threshold, then on the accent
    // container: it uses the accent there, and the container's own "on" color (which Material You
    // keeps readable on it, whatever the theme) after.
    // The color is worked out inside each icon, so only the icon redraws while it changes: if the
    // whole box redrew, the swipe would see "new" actions every frame and buzz again and again.
    val swipeState = rememberSwipeableActionsState()

    val swipeToStartAction = LocalArticleListSwipeStartAction.current
    val swipeToEndAction = LocalArticleListSwipeEndAction.current

    val onSwipeEndToStart =
        when (swipeToStartAction) {
            SwipeStartActionPreference.None -> null
            SwipeStartActionPreference.ToggleRead -> onToggleRead
            SwipeStartActionPreference.ToggleStarred -> onToggleStarred
        }

    val onSwipeStartToEnd =
        when (swipeToEndAction) {
            SwipeEndActionPreference.None -> null
            SwipeEndActionPreference.ToggleRead -> onToggleRead
            SwipeEndActionPreference.ToggleStarred -> onToggleStarred
        }

    if (onSwipeStartToEnd == null && onSwipeEndToStart == null) {
        content()
        return
    }

    val startAction =
        onSwipeStartToEnd?.let {
            SwipeAction(
                icon = {
                    swipeActionIcon(
                            direction = SwipeDirection.StartToEnd,
                            isStarred = isStarred,
                            isRead = isRead,
                        )
                        ?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = swipeIconTint(swipeState), // Brief You
                                modifier = Modifier.padding(horizontal = 24.dp),
                            )
                        }
                },
                background = containerColor,
                isUndo = false,
                onSwipe = { onSwipeStartToEnd.invoke(articleWithFeed) },
            )
        }

    val endAction =
        onSwipeEndToStart?.let {
            SwipeAction(
                icon = {
                    swipeActionIcon(
                            direction = SwipeDirection.EndToStart,
                            isStarred = isStarred,
                            isRead = isRead,
                        )
                        ?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = null,
                                tint = swipeIconTint(swipeState), // Brief You
                                modifier = Modifier.padding(horizontal = 24.dp),
                            )
                        }
                },
                background = containerColor,
                isUndo = false,
                onSwipe = { onSwipeEndToStart.invoke(articleWithFeed) },
            )
        }

    SwipeableActionsBox(
        modifier = modifier,
        state = swipeState, // Brief You
        startActions = listOfNotNull(startAction),
        endActions = listOfNotNull(endAction),
        backgroundUntilSwipeThreshold = MaterialTheme.colorScheme.surface,
    ) {
        content.invoke()
    }
}

@Composable
private fun swipeActionIcon(
    direction: SwipeDirection,
    isStarred: Boolean,
    isRead: Boolean,
): ImageVector? {
    val swipeToStartAction = LocalArticleListSwipeStartAction.current
    val swipeToEndAction = LocalArticleListSwipeEndAction.current

    val starImageVector =
        remember(isStarred) { if (isStarred) Icons.Outlined.StarOutline else Icons.Rounded.Star }

    val readImageVector =
        remember(isRead) { if (isRead) Icons.Outlined.Circle else Icons.Rounded.CheckCircleOutline }

    return remember(direction) {
        when (direction) {
            SwipeDirection.StartToEnd -> {

                when (swipeToEndAction) {
                    SwipeEndActionPreference.None -> null
                    SwipeEndActionPreference.ToggleRead -> readImageVector
                    SwipeEndActionPreference.ToggleStarred -> starImageVector
                }
            }

            SwipeDirection.EndToStart -> {
                when (swipeToStartAction) {
                    SwipeStartActionPreference.None -> null
                    SwipeStartActionPreference.ToggleRead -> readImageVector
                    SwipeStartActionPreference.ToggleStarred -> starImageVector
                }
            }
        }
    }
}

@Composable
private fun swipeActionText(
    direction: SwipeDirection,
    isStarred: Boolean,
    isRead: Boolean,
): String {
    val swipeToStartAction = LocalArticleListSwipeStartAction.current
    val swipeToEndAction = LocalArticleListSwipeEndAction.current

    val starText =
        stringResource(if (isStarred) R.string.mark_as_unstar else R.string.mark_as_starred)

    val readText = stringResource(if (isRead) R.string.mark_as_unread else R.string.mark_as_read)

    return remember(direction) {
        when (direction) {
            SwipeDirection.StartToEnd -> {
                when (swipeToEndAction) {
                    SwipeEndActionPreference.None -> "null"
                    SwipeEndActionPreference.ToggleRead -> readText
                    SwipeEndActionPreference.ToggleStarred -> starText
                }
            }

            SwipeDirection.EndToStart -> {
                when (swipeToStartAction) {
                    SwipeStartActionPreference.None -> "null"
                    SwipeStartActionPreference.ToggleRead -> readText
                    SwipeStartActionPreference.ToggleStarred -> starText
                }
            }
        }
    }
}

@Composable
fun ArticleItemMenuContent(
    articleWithFeed: ArticleWithFeed,
    iconSize: DpSize = DpSize(width = 20.dp, height = 20.dp),
    isStarred: Boolean = false,
    isRead: Boolean = false,
    onToggleStarred: (ArticleWithFeed) -> Unit = {},
    onToggleRead: (ArticleWithFeed) -> Unit = {},
    onMarkAboveAsRead: ((ArticleWithFeed) -> Unit)? = null,
    onMarkBelowAsRead: ((ArticleWithFeed) -> Unit)? = null,
    onShare: ((ArticleWithFeed) -> Unit)? = null,
    onItemClick: (() -> Unit)? = null,
) {
    val starImageVector =
        remember(isStarred) { if (isStarred) Icons.Outlined.StarOutline else Icons.Rounded.Star }

    val readImageVector =
        remember(isRead) {
            if (isRead) Icons.Outlined.FiberManualRecord else Icons.Rounded.FiberManualRecord
        }

    val starText =
        stringResource(if (isStarred) R.string.mark_as_unstar else R.string.mark_as_starred)

    val readText = stringResource(if (isRead) R.string.mark_as_unread else R.string.mark_as_read)

    DropdownMenuItem(
        text = { Text(text = readText) },
        onClick = {
            onToggleRead(articleWithFeed)
            onItemClick?.invoke()
        },
        leadingIcon = {
            Icon(
                imageVector = readImageVector,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
            )
        },
    )
    DropdownMenuItem(
        text = { Text(text = starText) },
        onClick = {
            onToggleStarred(articleWithFeed)
            onItemClick?.invoke()
        },
        leadingIcon = {
            Icon(
                imageVector = starImageVector,
                contentDescription = null,
                modifier = Modifier.size(iconSize),
            )
        },
    )

    if (onMarkAboveAsRead != null || onMarkBelowAsRead != null) {
        HorizontalDivider()
    }
    onMarkAboveAsRead?.let {
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.mark_above_as_read)) },
            onClick = {
                onMarkAboveAsRead(articleWithFeed)
                onItemClick?.invoke()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                )
            },
        )
    }
    onMarkBelowAsRead?.let {
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.mark_below_as_read)) },
            onClick = {
                onMarkBelowAsRead(articleWithFeed)
                onItemClick?.invoke()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.ArrowDownward,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                )
            },
        )
    }
    onShare?.let {
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(text = stringResource(id = R.string.share)) },
            onClick = {
                onShare(articleWithFeed)
                onItemClick?.invoke()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize),
                )
            },
        )
        me.ash.reader.brief.share.NewspaperMenuItem(articleWithFeed, iconSize, onItemClick) // Brief You
    }
}

@Preview
@Composable
fun MenuContentPreview() {
    MaterialTheme {
        Surface() {
            Column(modifier = Modifier.padding()) {
                ArticleItemMenuContent(
                    articleWithFeed = generateArticleWithFeedPreview(),
                    onMarkBelowAsRead = {},
                    onMarkAboveAsRead = {},
                    onShare = {},
                )
            }
        }
    }
}

/** Brief You: the swipe icon's color: the accent on the page color, then the container's "on" color. */
@Composable
private fun swipeIconTint(state: SwipeableActionsState): Color {
    val tint by
        animateColorAsState(
            if (state.hasCrossedSwipeThreshold() || state.isResettingOnRelease) {
                MaterialTheme.colorScheme.onTertiaryContainer
            } else {
                MaterialTheme.colorScheme.tertiary
            },
            label = "swipeIconTint",
        )
    return tint
}
