package me.ash.reader.brief.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import me.ash.reader.brief.ui.glass.glassActive
import me.ash.reader.brief.ui.style.BriefStyle
import me.ash.reader.brief.ui.style.DesignStyle

/**
 * Brief You: the app's buttons. They look like Material's, but follow the chosen design: in
 * Classic they are flat, square-cornered and lose their shadow (like Smart RSS); in Expressive
 * they are exactly Material's; with glass on, tonal buttons turn translucent. Used in place of
 * androidx.compose.material3's Button, FilledTonalButton, OutlinedButton and TextButton (same
 * parameters, so only the import differs).
 */
private val ClassicButtonShape = RoundedCornerShape(4.dp)

@Composable private fun classicButtons(): Boolean = BriefStyle.design == DesignStyle.Classic

@Composable
private fun tonalColors(colors: ButtonColors?): ButtonColors {
    if (colors != null) return colors
    val base = ButtonDefaults.filledTonalButtonColors()
    return if (glassActive()) base.copy(containerColor = base.containerColor.copy(alpha = 0.62f)) else base
}

@Composable
fun Button(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val classic = classicButtons()
    androidx.compose.material3.Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape ?: if (classic) ClassicButtonShape else ButtonDefaults.shape,
        colors = colors,
        elevation = if (classic) null else elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun Button(
    onClick: () -> Unit,
    shapes: ButtonShapes,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    if (classicButtons()) {
        Button(onClick, modifier, enabled, ClassicButtonShape, colors, null, border, contentPadding, interactionSource, content)
    } else {
        androidx.compose.material3.Button(
            onClick = onClick,
            shapes = shapes,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content,
        )
    }
}

@Composable
fun FilledTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    colors: ButtonColors? = null,
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    val classic = classicButtons()
    androidx.compose.material3.FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape ?: if (classic) ClassicButtonShape else ButtonDefaults.shape,
        colors = tonalColors(colors),
        elevation = if (classic) null else elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FilledTonalButton(
    onClick: () -> Unit,
    shapes: ButtonShapes,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors? = null,
    elevation: ButtonElevation? = ButtonDefaults.filledTonalButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    if (classicButtons()) {
        FilledTonalButton(onClick, modifier, enabled, ClassicButtonShape, colors, null, border, contentPadding, interactionSource, content)
    } else {
        androidx.compose.material3.FilledTonalButton(
            onClick = onClick,
            shapes = shapes,
            modifier = modifier,
            enabled = enabled,
            colors = tonalColors(colors),
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content,
        )
    }
}

@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    androidx.compose.material3.OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape ?: if (classicButtons()) ClassicButtonShape else ButtonDefaults.shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OutlinedButton(
    onClick: () -> Unit,
    shapes: ButtonShapes,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ButtonDefaults.outlinedButtonBorder(enabled),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    if (classicButtons()) {
        OutlinedButton(onClick, modifier, enabled, ClassicButtonShape, colors, elevation, border, contentPadding, interactionSource, content)
    } else {
        androidx.compose.material3.OutlinedButton(
            onClick = onClick,
            shapes = shapes,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content,
        )
    }
}

@Composable
fun TextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape? = null,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape ?: if (classicButtons()) ClassicButtonShape else ButtonDefaults.textShape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TextButton(
    onClick: () -> Unit,
    shapes: ButtonShapes,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.textButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit,
) {
    if (classicButtons()) {
        TextButton(onClick, modifier, enabled, ClassicButtonShape, colors, elevation, border, contentPadding, interactionSource, content)
    } else {
        androidx.compose.material3.TextButton(
            onClick = onClick,
            shapes = shapes,
            modifier = modifier,
            enabled = enabled,
            colors = colors,
            elevation = elevation,
            border = border,
            contentPadding = contentPadding,
            interactionSource = interactionSource,
            content = content,
        )
    }
}

/** Brief You: Material's drop-down menu, following the design (flat and outlined in Classic, see-through with glass). */
@Composable
fun BriefDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    val classic = classicButtons()
    val glass = glassActive()
    val base = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
    androidx.compose.material3.DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        shape = RoundedCornerShape(if (classic) 4.dp else 16.dp),
        containerColor = if (glass) base.copy(alpha = 0.78f) else base,
        border = if (classic || glass) BorderStroke(0.5.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant) else null,
        shadowElevation = if (classic) 0.dp else 3.dp,
        content = content,
    )
}
