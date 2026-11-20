package me.ash.reader.brief.ui

// Brief You's copy of Read You's filter bar (ui/component/FilterBar.kt), which it rewrote: a
// taller, floating bar with rounded corners and a fade behind it. Read You's original stays
// untouched; see .github/fork/copies.txt.

import android.os.Build
import android.view.SoundEffectConstants
import androidx.compose.foundation.background
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.infrastructure.preference.FlowFilterBarStylePreference
import me.ash.reader.infrastructure.preference.LocalThemeIndex
import me.ash.reader.ui.ext.surfaceColorAtElevation
import me.ash.reader.ui.theme.palette.onDark

/** A tall bar, like a music player's. Taller than the stock navigation bar. */
fun filterBarContainerHeight(filterBarStyle: Int): Dp =
    // Brief You: the height can be set (Color & Style > Bottom bar); automatic is 88/76 dp, or 72 in Classic.
    if (me.ash.reader.brief.ui.style.BriefLook.compactBar) 56.dp else me.ash.reader.brief.ui.style.BriefLook.barHeightDp(filterBarStyle != FlowFilterBarStylePreference.Icon.value).dp

/**
 * The gap under everything that floats at the bottom of the screen: the reading page's pill (above
 * the system gesture bar), and the article list's AI gist / Triage button and pull-up hint (above
 * the bottom bar).
 */
val FloatingBarGap = 12.dp

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun BriefFilterBar(
    modifier: Modifier = Modifier,
    filter: Filter,
    filterBarStyle: Int,
    filterBarFilled: Boolean,
    filterBarPadding: Dp,
    filterBarTonalElevation: Dp,
    filterOnClick: (Filter) -> Unit = {},
) {
    val view = LocalView.current
    val themeIndex = LocalThemeIndex.current
    val glassBar = me.ash.reader.brief.ui.glass.glassActive() && me.ash.reader.brief.ui.glass.GlassHost.backdrop != null
    val indicatorColor = if (themeIndex == 5 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    } onDark MaterialTheme.colorScheme.secondaryContainer

    val containerHeight = filterBarContainerHeight(filterBarStyle)
    // Brief You: Classic Material 3 is a flat full-width bar; Expressive is the floating rounded bar.
    me.ash.reader.brief.ui.style.BriefStyle.init(LocalView.current.context)
    me.ash.reader.brief.ui.style.BriefLook.init(LocalView.current.context)
    val classic = me.ash.reader.brief.ui.style.BriefStyle.design == me.ash.reader.brief.ui.style.DesignStyle.Classic

    val compactBar = me.ash.reader.brief.ui.style.BriefLook.compactBar
    // A compact bar floats (rounded, a little above the bottom), in Classic too.
    val insideInsets = classic && !compactBar && me.ash.reader.brief.ui.style.BriefLook.barBottom == 0f
    // A fade to black behind the bar: clear at the top, fully black from the bottom of the bar down
    // to the screen edge, so nothing shows below the bar. On a light theme it fades into the page
    // color instead, since black would look harsh there.
    val scrim =
        if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color.Black
        else MaterialTheme.colorScheme.background

    // Brief You: a floating bar with rounded corners (not a full-width strip, and not a pill). It
    // sits right on top of the system gesture area, with side margins and a little room above.
    val compact = me.ash.reader.brief.ui.style.BriefLook.compactBar
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            modifier
                .then(if (classic) Modifier else Modifier.drawBehind {
                    // Where the bar ends: everything from there down is solid.
                    val barBottom = (FILTER_BAR_TOP_PADDING + containerHeight).toPx()
                    val solidFrom = (barBottom / size.height).coerceIn(0.3f, 1f)
                    drawRect(
                        Brush.verticalGradient(
                            0f to scrim.copy(alpha = 0f),
                            solidFrom to scrim,
                            1f to scrim,
                        )
                    )
                })
                // Brief You: a flat Classic bar with no gap below it draws itself down to the screen edge (insets inside it).
                .then(if (insideInsets) Modifier else Modifier.windowInsetsPadding(NavigationBarDefaults.windowInsets))
                .padding(start = me.ash.reader.brief.ui.style.BriefLook.barSideDp().dp, end = me.ash.reader.brief.ui.style.BriefLook.barSideDp().dp, top = if (classic && !compactBar) 0.dp else FILTER_BAR_TOP_PADDING, bottom = if (classic && compactBar) maxOf(me.ash.reader.brief.ui.style.BriefLook.barBottom, 8f).dp else me.ash.reader.brief.ui.style.BriefLook.barBottom.dp)
    ) {
    val barColor = if (classic) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surfaceColorAtElevation(filterBarTonalElevation)
    // Brief You: glass when the switch is on (needs a glass source on the page behind it).
    me.ash.reader.brief.ui.glass.GlassOrSurface(
        color = barColor,
        contentColor = MaterialTheme.colorScheme.contentColorFor(barColor),
        shape = RoundedCornerShape(if (classic && !compactBar) me.ash.reader.brief.ui.style.BriefLook.classicBarCorner().dp else if (classic) 22.dp else 32.dp), // Brief You: same shape type for both designs
        shadowElevation = if (classic && !compactBar) 0.dp else if (classic) 2.dp else 6.dp,
    ) {
        Row(
            modifier =
            Modifier
                .then(if (insideInsets) Modifier.windowInsetsPadding(NavigationBarDefaults.windowInsets) else Modifier)
                .then(if (compact) Modifier.padding(horizontal = 6.dp) else Modifier.fillMaxWidth())
                .defaultMinSize(minHeight = if (compact) 56.dp else containerHeight)
                .selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            if (compact) {
                // Brief You: the small bar (like Capy Reader's): icons, and the name beside the selected one.
                Filter.values.forEach { item ->
                    val selected = filter == item
                    val chip by androidx.compose.animation.animateColorAsState(
                        targetValue = if (selected) (if (glassBar) MaterialTheme.colorScheme.primary.copy(alpha = 0.30f) else indicatorColor) else Color.Transparent,
                        label = "compactChip",
                    )
                    val chipInteraction = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                    val chipPressed by chipInteraction.collectIsPressedAsState()
                    // Diagnostic (Nightly log): did the touch reach this chip?
                    androidx.compose.runtime.LaunchedEffect(chipPressed) {
                        if (chipPressed) me.ash.reader.brief.perf.PerfLog.action("Compact bar: touch on ${item.toName()}")
                    }
                    val wiggle = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
                    androidx.compose.runtime.LaunchedEffect(selected) {
                        if (selected) {
                            wiggle.snapTo(1f)
                            wiggle.animateTo(0f, androidx.compose.animation.core.spring(dampingRatio = 0.28f, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow))
                        }
                    }
                    val swell by androidx.compose.animation.core.animateFloatAsState(
                        targetValue = if (chipPressed) 1.12f else 1f,
                        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.5f, stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow),
                        label = "compactSwell",
                    )
                    Row(
                        modifier = Modifier
                            .graphicsLayer {
                                val w = wiggle.value
                                rotationZ = 4f * w
                                scaleX = (1f + 0.08f * w) * swell
                                scaleY = (1f + 0.08f * w) * swell
                            }
                            .clip(RoundedCornerShape(if (classic) 16.dp else 50.dp))
                            .background(if (!selected && chipPressed) (if (glassBar) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f) else indicatorColor.copy(alpha = 0.5f)) else chip)
                            .selectable(selected = selected, interactionSource = chipInteraction, indication = null, role = androidx.compose.ui.semantics.Role.Tab) {
                                view.playSoundEffect(SoundEffectConstants.CLICK)
                                me.ash.reader.brief.perf.PerfLog.action("Compact bar: tap on ${item.toName()}")
                                filterOnClick(item)
                            }
                            .defaultMinSize(minWidth = 48.dp, minHeight = 40.dp)
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (selected && filterBarFilled) item.iconFilled else item.iconOutline,
                            contentDescription = item.toName(),
                            tint = if (selected) {
                                if (glassBar) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.contentColorFor(indicatorColor)
                            } else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        androidx.compose.animation.AnimatedVisibility(visible = selected) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = item.toName().uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    letterSpacing = 0.8.sp,
                                    maxLines = 1,
                                    color = if (glassBar) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.contentColorFor(indicatorColor),
                                )
                            }
                        }
                    }
                }
            } else {
            Spacer(modifier = Modifier.width(filterBarPadding))
            // Brief You: Classic and glass have no pill-shaped press highlight around a tab (its own square marks the selected one).
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalRippleConfiguration provides (if (classic || glassBar) null else androidx.compose.material3.LocalRippleConfiguration.current)
            ) {
            Filter.values.forEach { item ->
                val interaction = androidx.compose.runtime.remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                val pressed by interaction.collectIsPressedAsState()
                NavigationBarItem(
                    interactionSource = interaction,
                    modifier = Modifier.height(containerHeight),
                    alwaysShowLabel = when (filterBarStyle) {
                        FlowFilterBarStylePreference.Icon.value -> false
                        FlowFilterBarStylePreference.IconLabel.value -> true
                        FlowFilterBarStylePreference.IconLabelOnlySelected.value -> false
                        else -> false
                    },
                    icon = {
                        val glyph: @Composable () -> Unit = {
                            Icon(
                                imageVector = if (filter == item && filterBarFilled) {
                                    item.iconFilled
                                } else {
                                    item.iconOutline
                                },
                                contentDescription = item.toName()
                            )
                        }
                        // Brief You: the bar draws its own indicator, which wiggles a little when its tab is picked.
                        // Classic: a small rounded square, Expressive: a pill. With glass on it is a translucent tint.
                        BarIndicator(
                            selected = filter == item,
                            pressed = pressed,
                            shape = if (classic) RoundedCornerShape(8.dp) else RoundedCornerShape(50),
                            color = if (glassBar) MaterialTheme.colorScheme.primary.copy(alpha = 0.30f) else indicatorColor,
                        ) { glyph() }
                    },
                    label = if (filterBarStyle == FlowFilterBarStylePreference.Icon.value) {
                        null
                    } else {
                        {
                            Text(
                                text = item.toName(),
//                            style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    selected = filter == item,
                    onClick = {
//                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                        filterOnClick(item)
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = Color.Transparent,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedIconColor = if (glassBar) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.contentColorFor(indicatorColor),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
            }
            Spacer(modifier = Modifier.width(filterBarPadding))
            } // Brief You: not compact
        }
    }
    } // Brief You
}

/** A tab's highlight: it wiggles a little (a small turn and swell, settling like a spring) each time its tab is picked. */
@Composable
private fun BarIndicator(
    selected: Boolean,
    pressed: Boolean,
    shape: androidx.compose.ui.graphics.Shape,
    color: Color,
    content: @Composable () -> Unit,
) {
    val wiggle = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0f) }
    androidx.compose.runtime.LaunchedEffect(selected) {
        if (selected) {
            wiggle.snapTo(1f)
            wiggle.animateTo(
                0f,
                androidx.compose.animation.core.spring(
                    dampingRatio = 0.28f,
                    stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
                ),
            )
        }
    }
    // Held down: the highlight swells under the finger, like a drop of glass, and springs back when let go.
    val swell by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (pressed) 1.22f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = 0.5f,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow,
        ),
        label = "swell",
    )
    Box(
        modifier = Modifier
            .graphicsLayer {
                val w = wiggle.value
                rotationZ = 5f * w
                scaleX = (1f + 0.10f * w) * swell
                scaleY = (1f + 0.10f * w) * swell
            }
            .clip(shape)
            .background(if (selected) color else if (pressed) color.copy(alpha = color.alpha * 0.7f) else Color.Transparent)
            .padding(horizontal = 20.dp, vertical = 4.dp)
    ) { content() }
}
