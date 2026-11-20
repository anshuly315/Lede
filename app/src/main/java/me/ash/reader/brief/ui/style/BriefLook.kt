package me.ash.reader.brief.ui.style

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import me.ash.reader.R

/** How an article looks in the list: text only, a small picture beside the text (Read You's), or a card. */
enum class ListMode(val title: Int, val desc: Int) {
    Text(R.string.list_mode_text, R.string.list_mode_text_desc),
    Magazine(R.string.list_mode_magazine, R.string.list_mode_magazine_desc),
    Cards(R.string.list_mode_cards, R.string.list_mode_cards_desc),
}

enum class ListDensity(val title: Int) {
    Comfortable(R.string.density_comfortable),
    Compact(R.string.density_compact),
}

/**
 * Brief You: how the article list looks (view mode, density, picture size, corner roundness). Own
 * preferences file (`brief_look`); the values are Compose state, so the list redraws as a slider moves.
 */
object BriefLook {
    private const val FILE = "brief_look"
    private var ready = false

    const val IMAGE_DEFAULT = 80f
    const val CORNER_DEFAULT = 16f

    var mode by mutableStateOf(ListMode.Magazine)
        private set
    var density by mutableStateOf(ListDensity.Comfortable)
        private set
    /** The picture's size in dp: the side picture's width and height, or half the card picture's height. */
    var imageSize by mutableFloatStateOf(IMAGE_DEFAULT)
        private set
    /** Corner radius in dp of the list pictures and the cards. */
    var corner by mutableFloatStateOf(CORNER_DEFAULT)
        private set

    /** The bottom bar's own measures in dp; -1 means "automatic" (it depends on the design style). */
    var barHeight by mutableFloatStateOf(-1f)
        private set
    var barSide by mutableFloatStateOf(-1f)
        private set
    var barBottom by mutableFloatStateOf(0f)
        private set

    private val classic get() = BriefStyle.design == DesignStyle.Classic
    /** What "automatic" means for each measure, in this design style. */
    fun autoBarHeight(labels: Boolean) = if (classic) 72f else if (labels) 88f else 76f
    fun autoBarSide() = if (classic) 0f else 20f
    fun barHeightDp(labels: Boolean) = if (barHeight >= 0f) barHeight else autoBarHeight(labels)
    fun barSideDp() = if (barSide >= 0f) barSide else autoBarSide()
    /** Classic bar corners: square at the screen edge, rounding off as the bar moves in from the sides (a pill from 20 dp). */
    fun classicBarCorner(): Float = (barSideDp() * 1.6f).coerceIn(0f, 32f)

    fun setBarHeight(context: Context, value: Float) {
        init(context); barHeight = value.coerceIn(48f, 100f)
        prefs(context).edit { putFloat("barHeight", barHeight) }
    }

    fun setBarSide(context: Context, value: Float) {
        init(context); barSide = value.coerceIn(0f, 32f)
        prefs(context).edit { putFloat("barSide", barSide) }
    }

    fun setBarBottom(context: Context, value: Float) {
        init(context); barBottom = value.coerceIn(0f, 32f)
        prefs(context).edit { putFloat("barBottom", barBottom) }
    }

    fun resetBar(context: Context) {
        init(context); barHeight = -1f; barSide = -1f; barBottom = 0f
        prefs(context).edit { remove("barHeight"); remove("barSide"); remove("barBottom") }
    }

    fun init(context: Context) {
        if (ready) return
        val prefs = prefs(context)
        mode = ListMode.entries.firstOrNull { it.name == prefs.getString("mode", null) } ?: ListMode.Magazine
        density = ListDensity.entries.firstOrNull { it.name == prefs.getString("density", null) } ?: ListDensity.Comfortable
        imageSize = prefs.getFloat("imageSize", IMAGE_DEFAULT).coerceIn(48f, 140f)
        corner = prefs.getFloat("corner", CORNER_DEFAULT).coerceIn(0f, 32f)
        barHeight = prefs.getFloat("barHeight", -1f)
        barSide = prefs.getFloat("barSide", -1f)
        barBottom = prefs.getFloat("barBottom", 0f)
        ready = true
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun setMode(context: Context, value: ListMode) {
        init(context); mode = value
        prefs(context).edit { putString("mode", value.name) }
    }

    fun setDensity(context: Context, value: ListDensity) {
        init(context); density = value
        prefs(context).edit { putString("density", value.name) }
    }

    fun setImageSize(context: Context, value: Float) {
        init(context); imageSize = value.coerceIn(48f, 140f)
        prefs(context).edit { putFloat("imageSize", imageSize) }
    }

    fun setCorner(context: Context, value: Float) {
        init(context); corner = value.coerceIn(0f, 32f)
        prefs(context).edit { putFloat("corner", corner) }
    }
}
