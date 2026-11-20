package me.ash.reader.ui.page.settings.tips

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.util.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import me.ash.reader.R
import me.ash.reader.infrastructure.preference.OpenLinkPreference
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.ext.openURL
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun LicenseListPage(
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack
            )
        },
        actions = {
            FeedbackIconButton(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Rounded.Star,
                contentDescription = stringResource(R.string.open_source_licenses),
                tint = MaterialTheme.colorScheme.onSurface
            ) {
                context.openURL(
                    url = context.getString(R.string.github_link) + "/blob/main/LICENSE",
                    openLink = OpenLinkPreference.AutoPreferCustomTabs,
                )
            }
        },
        content = {
            // Brief You: the licence list is read inside a guard: if the data isn't in the app, the page
            // says so and links to the licences online, instead of crashing.
            val libs by produceState<Result<Libs>?>(null) {
                value = withContext(Dispatchers.IO) { runCatching { Libs.Builder().withContext(context).build() } }
            }
            val loaded = libs
            Column {
                if (loaded == null) {
                    // loading
                } else if (loaded.isSuccess && loaded.getOrThrow().libraries.isNotEmpty()) {
                    LibrariesContainer(
                        libraries = loaded.getOrThrow(),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = WindowInsets(0, 0, 0, 24)
                            .add(WindowInsets.navigationBars)
                            .asPaddingValues(),
                    )
                } else {
                    Text(
                        text = "The licence list could not be loaded. The licences are in the project on GitHub.",
                        modifier = Modifier.padding(24.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    )
}
