package me.ash.reader.brief.ui.style

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt
import me.ash.reader.R
import me.ash.reader.brief.ui.BriefFilterBar
import me.ash.reader.brief.ui.NumberSheet
import me.ash.reader.brief.ui.SettingGroup
import me.ash.reader.domain.model.general.Filter
import me.ash.reader.infrastructure.preference.FeedsFilterBarPaddingPreference
import me.ash.reader.infrastructure.preference.FeedsFilterBarStylePreference
import me.ash.reader.infrastructure.preference.FlowFilterBarPaddingPreference
import me.ash.reader.infrastructure.preference.FlowFilterBarStylePreference
import me.ash.reader.infrastructure.preference.LocalFlowFilterBarPadding
import me.ash.reader.infrastructure.preference.LocalFlowFilterBarStyle
import me.ash.reader.infrastructure.preference.LocalFlowFilterBarTonalElevation
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.RadioDialog
import me.ash.reader.ui.component.base.RadioDialogOption
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

/**
 * Brief You: the bottom bar's settings in one place. They used to be spread over the Feeds page,
 * the Flow page and Colour & style; the style and spacing set here apply to the bar on both the
 * Feeds page and the article list.
 */
@OptIn(ExperimentalFoundationApi::class)
@Suppress("DEPRECATION")
@Composable
fun BottomBarPage(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val style = LocalFlowFilterBarStyle.current
    val padding = LocalFlowFilterBarPadding.current
    val elevation = LocalFlowFilterBarTonalElevation.current
    var styleDialog by remember { mutableStateOf(false) }
    var paddingSheet by remember { mutableStateOf(false) }
    var previewFilter by remember { mutableStateOf(Filter.Unread) }

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
            me.ash.reader.brief.search.SettingsLazyColumn { // Brief You: scrolls to a setting picked in the settings search
                item { DisplayText(text = stringResource(R.string.bottom_bar), desc = "") }
                // The bar itself stays on screen while you scroll, so every change shows at once.
                stickyHeader {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .padding(vertical = 16.dp),
                        ) {
                            BriefFilterBar(
                                modifier = Modifier.fillMaxWidth(),
                                filter = previewFilter,
                                filterBarStyle = style.value,
                                filterBarFilled = true,
                                filterBarPadding = padding.dp,
                                filterBarTonalElevation = elevation.value.dp,
                            ) { previewFilter = it }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
                item {
                    Subtitle(modifier = Modifier.padding(horizontal = 24.dp), text = stringResource(R.string.style))
                    SettingGroup {
                        SettingItem(
                            title = stringResource(R.string.style),
                            desc = style.toDesc(context),
                            onClick = { styleDialog = true },
                        ) {}
                        SettingItem(
                            title = stringResource(R.string.horizontal_padding),
                            desc = "${padding}dp",
                            onClick = { paddingSheet = true },
                        ) {}
                    }
                    Spacer(Modifier.height(8.dp))
                    BottomBarSections()
                }
                item {
                    Spacer(Modifier.height(24.dp))
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        },
    )

    RadioDialog(
        visible = styleDialog,
        title = stringResource(R.string.style),
        options = FlowFilterBarStylePreference.values.map { option ->
            RadioDialogOption(text = option.toDesc(context), selected = option == style) {
                option.put(context, scope)
                // The Feeds page's bar follows, so there is one bottom bar style.
                FeedsFilterBarStylePreference.values.firstOrNull { it.value == option.value }?.put(context, scope)
            }
        },
    ) { styleDialog = false }

    if (paddingSheet) NumberSheet(
        title = stringResource(R.string.horizontal_padding),
        value = padding.toFloat(),
        range = 0f..120f,
        step = 4f,
        default = FlowFilterBarPaddingPreference.default.toFloat(),
        suffix = "dp",
        onSave = {
            FlowFilterBarPaddingPreference.put(context, scope, it.roundToInt())
            FeedsFilterBarPaddingPreference.put(context, scope, it.roundToInt())
        },
        onDismiss = { paddingSheet = false },
    )
}
