package me.ash.reader.brief.ui.style

import android.os.Build
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.ash.reader.R
import me.ash.reader.brief.ui.SettingGroup
import me.ash.reader.ui.component.base.RYSwitch
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.page.settings.SettingItem

/**
 * Brief You: the extra Color & Style sections, in the same joined-card design as the page's own:
 * colour-scheme style, contrast, and the glass switch.
 */
@Composable
fun StyleOptionsSections() {
    val context = LocalContext.current
    BriefStyle.init(context)
    val glassSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

    Subtitle(modifier = Modifier.padding(horizontal = 24.dp), text = stringResource(R.string.color_scheme_style))
    SettingGroup {
        SchemeStyle.entries.forEach { option ->
            SettingItem(
                autoIcon = false, // Brief You: a tick-mark choice
                title = stringResource(option.title),
                desc = stringResource(option.desc),
                onClick = { BriefStyle.setScheme(context, option) },
            ) {
                if (BriefStyle.scheme == option) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    Subtitle(modifier = Modifier.padding(horizontal = 24.dp), text = stringResource(R.string.contrast_level))
    SettingGroup {
        ContrastLevel.entries.forEach { option ->
            SettingItem(
                autoIcon = false, // Brief You: a tick-mark choice
                title = stringResource(option.title),
                onClick = { BriefStyle.setContrast(context, option) },
            ) {
                if (BriefStyle.contrast == option) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    Subtitle(modifier = Modifier.padding(horizontal = 24.dp), text = stringResource(R.string.design_style))
    SettingGroup {
        DesignStyle.entries.forEach { option ->
            SettingItem(
                autoIcon = false, // Brief You: a tick-mark choice
                title = stringResource(option.title),
                desc = stringResource(option.desc),
                onClick = { BriefStyle.setDesign(context, option) },
            ) {
                if (BriefStyle.design == option) {
                    Icon(Icons.Outlined.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }

    Subtitle(modifier = Modifier.padding(horizontal = 24.dp), text = stringResource(R.string.glass_effect))
    SettingGroup {
        SettingItem(
            title = stringResource(R.string.glass_effect_title),
            desc =
                stringResource(
                    if (glassSupported) R.string.glass_effect_desc else R.string.glass_effect_unsupported
                ),
            onClick = { if (glassSupported) BriefStyle.setGlass(context, !BriefStyle.glass) },
        ) {
            RYSwitch(activated = BriefStyle.glass && glassSupported) {
                if (glassSupported) BriefStyle.setGlass(context, !BriefStyle.glass)
            }
        }
    }

    if (BriefStyle.glass && glassSupported) GlassCustomSections()
}
