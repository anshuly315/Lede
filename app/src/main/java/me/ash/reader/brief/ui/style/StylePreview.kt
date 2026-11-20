package me.ash.reader.brief.ui.style

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/*
 * Brief You: a small picture of the app in the colours you have chosen. It is drawn from the live
 * theme, so it changes the moment you pick a colour, a scheme style, a contrast level or dark mode.
 */
@Composable
fun StylePreview(modifier: Modifier = Modifier) {
    val c = MaterialTheme.colorScheme
    val glassOn = me.ash.reader.brief.ui.glass.glassActive()
    val dark = c.surface.luminance() < 0.5f
    val classic = BriefStyle.design == DesignStyle.Classic
    val compact = BriefLook.compactBar
    val barHeight = ((if (compact) 56f else BriefLook.barHeightDp(true)) * 0.4f).dp
    val side = (BriefLook.barSideDp() * 0.4f).dp
    val bottom = (BriefLook.barBottom * 0.4f).dp
    val barShape = if (classic) RoundedCornerShape((BriefLook.classicBarCorner() * 12f / 32f).dp) else if (compact) CircleShape else RoundedCornerShape(12.dp)
    Box(modifier = modifier.fillMaxSize()) {
        // The page behind the bar. With glass on it gets colourful stripes, so the glass has something to show.
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .then(
                        if (glassOn) Modifier.drawBehind {
                            // A soft colourful wallpaper, so the glass has something to show.
                            fun blob(color: Color, cx: Float, cy: Float, r: Float) =
                                drawCircle(
                                    Brush.radialGradient(listOf(color.copy(alpha = 0.55f), Color.Transparent), center = Offset(cx * size.width, cy * size.height), radius = r * size.width),
                                    radius = r * size.width,
                                    center = Offset(cx * size.width, cy * size.height),
                                )
                            blob(c.primary, 0.15f, 0.25f, 0.55f)
                            blob(c.tertiary, 0.85f, 0.55f, 0.5f)
                            blob(c.secondary, 0.4f, 0.95f, 0.5f)
                        } else Modifier
                    ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // A top bar: back circle, title, accent circle.
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Dot(18.dp, c.onSurface)
                Line(80.dp, c.onSurface)
                Spacer(Modifier.weight(1f))
                Dot(18.dp, c.primary)
            }
            // An article card in the primary container.
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(c.primaryContainer)
                        .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Line(120.dp, c.onPrimaryContainer)
                Line(170.dp, c.onPrimaryContainer.copy(alpha = 0.6f))
                Line(90.dp, c.onPrimaryContainer.copy(alpha = 0.6f))
            }
            // Chips in the other accent roles.
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(c.secondaryContainer)
                Chip(c.tertiaryContainer)
                Chip(c.surfaceContainerHigh)
            }
            Spacer(Modifier.height(barHeight))
        }
        // The bottom bar, drawn from the real bar settings: thickness, distance from the sides and from the bottom.
        Box(
            Modifier.align(Alignment.BottomCenter)
                .then(if (compact) Modifier.padding(bottom = bottom).height(barHeight) else Modifier.fillMaxWidth().padding(horizontal = side).padding(bottom = bottom).height(barHeight))
        ) {
            val barContent: @Composable () -> Unit = {
                if (compact) {
                    // The small bar: icons, with the name beside the selected one.
                    Row(
                        modifier = Modifier.fillMaxHeight().padding(horizontal = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Dot(10.dp, c.onSecondaryContainer)
                        Row(
                            modifier = Modifier.clip(if (classic) RoundedCornerShape(5.dp) else CircleShape).background(c.primary).padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Dot(8.dp, c.onPrimary)
                            Line(26.dp, c.onPrimary)
                        }
                        Dot(10.dp, c.onSecondaryContainer)
                    }
                } else Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Dot(10.dp, c.onSecondaryContainer)
                    Box(Modifier.width(30.dp).height(16.dp).clip(CircleShape).background(c.primary))
                    Dot(10.dp, c.onSecondaryContainer)
                }
            }
            if (glassOn) {
                // An approximation of the glass (the real glass needs a real page behind it): the frost
                // and the theme tint are drawn over the stripes. Blur and refraction show on the real screens.
                val g = me.ash.reader.brief.ui.glass.BriefGlass
                val veil = (if (dark) Color.Black else Color.White).copy(alpha = (g.frostPct() / 100f * 1.6f).coerceIn(0f, 0.9f))
                Box(
                    (if (compact) Modifier else Modifier.fillMaxSize()).clip(barShape)
                        .background(veil)
                        .background(c.primaryContainer.copy(alpha = (g.tintPct() / 100f).coerceIn(0f, 0.9f)))
                ) { barContent() }
            } else {
                Box((if (compact) Modifier else Modifier.fillMaxSize()).clip(barShape).background(c.secondaryContainer)) { barContent() }
            }
        }
    }
}

@Composable private fun Dot(size: Dp, color: Color) = Box(Modifier.size(size).clip(CircleShape).background(color))

@Composable
private fun Line(width: Dp, color: Color) =
    Box(Modifier.width(width).height(8.dp).clip(RoundedCornerShape(4.dp)).background(color))

@Composable
private fun Chip(color: Color) =
    Box(Modifier.width(48.dp).height(22.dp).clip(RoundedCornerShape(8.dp)).background(color))
