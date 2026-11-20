package me.ash.reader.infrastructure.preference

import me.ash.reader.brief.ui.emphasizeHeadings // Brief You
import android.content.Context
import androidx.compose.material3.Typography
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.R
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.basicFonts
import me.ash.reader.ui.ext.ExternalFonts
import me.ash.reader.ui.ext.dataStore
import me.ash.reader.ui.ext.put
import me.ash.reader.ui.ext.restart
import me.ash.reader.brief.ui.GoogleSansFontFamily // Brief You
import me.ash.reader.brief.ui.SystemTypography // Brief You
import me.ash.reader.brief.ui.applyFontFamily // Brief You
val LocalBasicFonts = compositionLocalOf<BasicFontsPreference> { BasicFontsPreference.default }

sealed class BasicFontsPreference(val value: Int) : Preference() {
    object System : BasicFontsPreference(0)

    object GoogleSans : BasicFontsPreference(1)

    object External : BasicFontsPreference(5)

    // Brief You: the same extra fonts as for reading, for the whole app.
    object Atkinson : BasicFontsPreference(8)
    object Inter : BasicFontsPreference(9)
    object Jost : BasicFontsPreference(10)
    object Poppins : BasicFontsPreference(11)
    object Vollkorn : BasicFontsPreference(12)

    override fun put(context: Context, scope: CoroutineScope) {
        scope.launch {
            context.dataStore.put(DataStoreKey.basicFonts, value)
            if (this@BasicFontsPreference == External) {
                context.restart()
            }
        }
    }

    fun toDesc(context: Context): String =
        when (this) {
            System -> context.getString(R.string.system_default)
            GoogleSans -> context.getString(R.string.google_sans)
            External -> context.getString(R.string.external_fonts)
            Atkinson -> "Atkinson Hyperlegible" // Brief You
            Inter -> "Inter" // Brief You
            Jost -> "Jost" // Brief You
            Poppins -> "Poppins" // Brief You
            Vollkorn -> "Vollkorn" // Brief You
        }

    fun asFontFamily(context: Context): FontFamily =
        when (this) {
            System -> FontFamily.Default
            GoogleSans -> GoogleSansFontFamily
            External ->
                ExternalFonts.loadBasicTypography(context).displayLarge.fontFamily
                    ?: FontFamily.Default
            Atkinson -> me.ash.reader.brief.ui.AtkinsonFontFamily // Brief You
            Inter -> me.ash.reader.brief.ui.InterFontFamily // Brief You
            Jost -> me.ash.reader.brief.ui.JostFontFamily // Brief You
            Poppins -> me.ash.reader.brief.ui.PoppinsFontFamily // Brief You
            Vollkorn -> me.ash.reader.brief.ui.VollkornFontFamily // Brief You
        }

    // Brief You: titles and headings a step heavier, as in Material 3 Expressive.
    fun asTypography(context: Context): Typography =
        when (this) {
            System -> SystemTypography
            GoogleSans -> SystemTypography.applyFontFamily(GoogleSansFontFamily)
            External -> ExternalFonts.loadBasicTypography(context)
            else -> SystemTypography.applyFontFamily(asFontFamily(context)) // Brief You: the extra fonts
        }.emphasizeHeadings() // Brief You

    companion object {

        val default = GoogleSans
        val values = listOf(GoogleSans, Atkinson, Inter, Jost, Poppins, Vollkorn, System, External) // Brief You

        fun fromPreferences(preferences: Preferences): BasicFontsPreference =
            when (preferences[DataStoreKey.keys[basicFonts]?.key as Preferences.Key<Int>]) {
                0 -> System
                1 -> GoogleSans
                5 -> External
                8 -> Atkinson // Brief You
                9 -> Inter // Brief You
                10 -> Jost // Brief You
                11 -> Poppins // Brief You
                12 -> Vollkorn // Brief You
                else -> default
            }
    }
}
