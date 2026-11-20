package me.ash.reader.brief.ui.style

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CropSquare
import androidx.compose.material.icons.outlined.PhotoSizeSelectLarge
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import me.ash.reader.brief.ui.TextButton // Brief You
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import me.ash.reader.R
import me.ash.reader.brief.ui.BriefSlider
import me.ash.reader.brief.ui.Corner
import me.ash.reader.brief.ui.SettingGroup
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.page.settings.SettingItem

/**
 * Brief You: the list-look sections of the Flow page: view mode, density, and two sliders (the
 * picture size and the corner roundness) with a reset. The article list behind it changes as a
 * slider moves.
 */
@Composable
fun ListLookSections() {
    val context = LocalContext.current
    BriefLook.init(context)

    Subtitle(modifier = Modifier.padding(horizontal = 24.dp), text = stringResource(R.string.list_look_view))
    SettingGroup {
        // Brief You: Classic Material 3 has no cards look, so the choice isn't offered there.
        ListMode.entries.filter { it != ListMode.Cards || BriefStyle.design != DesignStyle.Classic }.forEach { option ->
            SettingItem(
                autoIcon = false, // Brief You: a tick-mark choice
                title = stringResource(option.title),
                desc = stringResource(option.desc),
                onClick = { BriefLook.setMode(context, option) },
            ) {
                if (BriefLook.mode == option) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    Subtitle(modifier = Modifier.padding(horizontal = 24.dp), text = stringResource(R.string.list_look_density))
    SettingGroup {
        ListDensity.entries.forEach { option ->
            SettingItem(autoIcon = false, title = stringResource(option.title), onClick = { BriefLook.setDensity(context, option) }) {
                if (BriefLook.density == option) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    Subtitle(modifier = Modifier.padding(horizontal = 24.dp), text = stringResource(R.string.list_look_pictures))
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        LookSlider(
            icon = Icons.Outlined.PhotoSizeSelectLarge,
            label = stringResource(R.string.list_look_image_size),
            value = BriefLook.imageSize,
            range = 48f..140f,
            steps = 22,
            onChange = { BriefLook.setImageSize(context, it) },
        )
        Spacer(Modifier.size(2.dp))
        LookSlider(
            icon = Icons.Outlined.CropSquare,
            label = stringResource(R.string.list_look_corners),
            value = BriefLook.corner,
            range = 0f..32f,
            steps = 15,
            onChange = { BriefLook.setCorner(context, it) },
        )
        Spacer(Modifier.size(2.dp))
        LookSlider(
            icon = Icons.Outlined.TextFields,
            label = stringResource(R.string.list_look_text_size),
            value = BriefLook.listFontPct,
            range = 80f..140f,
            steps = 11,
            onChange = { BriefLook.setListFontPct(context, it) },
            unit = "%",
        )
        TextButton(
            onClick = {
                BriefLook.setImageSize(context, BriefLook.IMAGE_DEFAULT)
                BriefLook.setCorner(context, BriefLook.CORNER_DEFAULT)
                BriefLook.setListFontPct(context, 100f)
                BriefLook.setShortenTitles(context, false)
            }
        ) {
            Text(stringResource(R.string.list_look_reset))
        }
    }
    SettingGroup {
        SettingItem(
            title = stringResource(R.string.list_look_shorten),
            desc = stringResource(R.string.list_look_shorten_desc),
            onClick = { BriefLook.setShortenTitles(context, !BriefLook.shortenTitles) },
        ) { me.ash.reader.ui.component.base.RYSwitch(activated = BriefLook.shortenTitles) { BriefLook.setShortenTitles(context, !BriefLook.shortenTitles) } }
    }
}

/** One slider row in the joined-card design: icon, name, the value in a pill, then the slider. */
@Composable
private fun LookSlider(
    icon: ImageVector,
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onChange: (Float) -> Unit,
    unit: String = "dp",
) {
    var current by remember(value) { mutableStateOf(value) }
    Surface(shape = androidx.compose.foundation.shape.RoundedCornerShape(Corner.Card), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(34.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))
                Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                    Text(
                        "${current.roundToInt()} $unit",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    )
                }
            }
            BriefSlider(
                value = current.coerceIn(range.start, range.endInclusive),
                onValueChange = {
                    current = it
                    onChange(it)
                },
                valueRange = range,
                steps = steps,
            )
        }
    }
}

/** Brief You: the bottom bar's thickness, its distance from the sides, and its distance from the bottom. */
@Composable
fun BottomBarSections() {
    val context = LocalContext.current
    BriefLook.init(context)
    val labels =
        me.ash.reader.infrastructure.preference.LocalFlowFilterBarStyle.current.value !=
            me.ash.reader.infrastructure.preference.FlowFilterBarStylePreference.Icon.value
    Subtitle(modifier = Modifier.padding(horizontal = 24.dp), text = stringResource(R.string.bottom_bar))
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        LookSlider(
            icon = Icons.Outlined.CropSquare,
            label = stringResource(R.string.bottom_bar_thickness),
            value = BriefLook.barHeightDp(labels),
            range = (if (labels) BriefLook.MIN_LABEL_BAR else 48f)..100f,
            steps = 25,
            onChange = { BriefLook.setBarHeight(context, it) },
        )
        Spacer(Modifier.size(2.dp))
        // Classic's bar always runs edge to edge, so it has no side distance to set.
        if (BriefStyle.design != DesignStyle.Classic) {
            LookSlider(
                icon = Icons.Outlined.CropSquare,
                label = stringResource(R.string.bottom_bar_sides),
                value = BriefLook.barSideDp(),
                range = 0f..32f,
                steps = 15,
                onChange = { BriefLook.setBarSide(context, it) },
            )
            Spacer(Modifier.size(2.dp))
        }
        LookSlider(
            icon = Icons.Outlined.CropSquare,
            label = stringResource(R.string.bottom_bar_bottom),
            value = BriefLook.barBottom,
            range = 0f..32f,
            steps = 15,
            onChange = { BriefLook.setBarBottom(context, it) },
        )
        TextButton(onClick = { BriefLook.resetBar(context) }) { Text(stringResource(R.string.list_look_reset)) }
    }
    SettingGroup {
        SettingItem(
            title = stringResource(R.string.bottom_bar_compact),
            desc = stringResource(R.string.bottom_bar_compact_desc),
            onClick = { BriefLook.setCompactBar(context, !BriefLook.compactBar) },
        ) { me.ash.reader.ui.component.base.RYSwitch(activated = BriefLook.compactBar) { BriefLook.setCompactBar(context, !BriefLook.compactBar) } }
    }
}

/** Brief You: how the glass looks, with every part adjustable. Shown under the glass switch. */
@Composable
fun GlassCustomSections() {
    val context = LocalContext.current
    me.ash.reader.brief.ui.glass.BriefGlass.init(context)
    val g = me.ash.reader.brief.ui.glass.BriefGlass
    Subtitle(modifier = Modifier.padding(horizontal = 24.dp), text = stringResource(R.string.glass_customise))
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        LookSlider(Icons.Outlined.CropSquare, stringResource(R.string.glass_blur), g.blurDp(), 0f..30f, 29, { g.setBlur(context, it) })
        Spacer(Modifier.size(2.dp))
        LookSlider(Icons.Outlined.CropSquare, stringResource(R.string.glass_frost), g.frostPct(), 0f..80f, 15, { g.setFrost(context, it) }, unit = "%")
        Spacer(Modifier.size(2.dp))
        LookSlider(Icons.Outlined.CropSquare, stringResource(R.string.glass_tint), g.tintPct(), 0f..80f, 15, { g.setTint(context, it) }, unit = "%")
        Spacer(Modifier.size(2.dp))
        LookSlider(Icons.Outlined.CropSquare, stringResource(R.string.glass_saturation), g.saturationPct(), 50f..250f, 19, { g.setSaturation(context, it) }, unit = "%")
    }
    SettingGroup {
        SettingItem(
            title = stringResource(R.string.glass_refraction),
            desc = stringResource(R.string.glass_refraction_desc),
            onClick = { g.setRefraction(context, !g.refractionOn()) },
        ) { me.ash.reader.ui.component.base.RYSwitch(activated = g.refractionOn()) { g.setRefraction(context, !g.refractionOn()) } }
        SettingItem(
            title = stringResource(R.string.glass_reading),
            desc = stringResource(R.string.glass_reading_desc),
            onClick = { g.setReadingGlass(context, !g.readingGlass) },
        ) { me.ash.reader.ui.component.base.RYSwitch(activated = g.readingGlass) { g.setReadingGlass(context, !g.readingGlass) } }
        SettingItem(
            title = stringResource(R.string.glass_outline),
            desc = stringResource(R.string.glass_outline_desc),
            onClick = { g.setOutline(context, !g.outlineOn()) },
        ) { me.ash.reader.ui.component.base.RYSwitch(activated = g.outlineOn()) { g.setOutline(context, !g.outlineOn()) } }
    }
    TextButton(modifier = Modifier.padding(horizontal = 16.dp), onClick = { g.reset(context) }) { Text(stringResource(R.string.list_look_reset)) }
}


