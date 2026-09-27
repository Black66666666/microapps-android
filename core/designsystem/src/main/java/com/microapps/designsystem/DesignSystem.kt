package com.microapps.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val TaviBackground = Color(0xFFF7F3EA)
val TaviSurface = Color(0xFFFFFDF8)
val TaviSurfaceMuted = Color(0xFFF1ECE3)
val TaviGraphite = Color(0xFF20211F)
val TaviTextSecondary = Color(0xFF73746F)
val TaviBorder = Color(0xFFE7E1D7)
val TaviLime = Color(0xFFB9F500)
val TaviSuccess = Color(0xFF2F8D62)
val TaviWarmOrange = Color(0xFFE98545)

val ScreenshotInboxAccent = Color(0xFFFF8252)
val MatchChoiceAccent = Color(0xFFEF6F91)
val WhoBringsWhatAccent = Color(0xFFE2A934)
val BuyTomorrowAccent = Color(0xFF3E87F7)
val MeetingMeterAccent = Color(0xFF8A5CE6)

private val TaviScheme = lightColorScheme(
    primary = TaviGraphite,
    secondary = TaviLime,
    tertiary = BuyTomorrowAccent,
    background = TaviBackground,
    surface = TaviSurface,
    surfaceVariant = TaviSurfaceMuted,
    onPrimary = Color.White,
    onSecondary = TaviGraphite,
    onTertiary = Color.White,
    onBackground = TaviGraphite,
    onSurface = TaviGraphite,
    onSurfaceVariant = TaviTextSecondary,
    outline = TaviBorder
)

@Composable
fun TaviTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = TaviScheme, content = content)
}

@Composable
fun TaviScreen(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TaviBackground),
        content = content
    )
}

@Composable
fun TaviTopBar(
    title: String,
    subtitle: String,
    accent: Color = TaviLime,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaviAppMark(accent = accent)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = TaviGraphite,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                color = TaviTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun TaviAppMark(
    modifier: Modifier = Modifier,
    accent: Color = TaviLime
) {
    Box(modifier = modifier.size(48.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(42.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(TaviGraphite)
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(15.dp)
                .clip(CircleShape)
                .background(accent)
                .border(2.dp, TaviBackground, CircleShape)
        )
    }
}

@Composable
fun TaviCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(TaviSurface)
            .border(1.dp, TaviBorder, shape)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun TaviPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: String? = null
) {
    val shape = RoundedCornerShape(18.dp)
    val background = if (enabled) TaviGraphite else TaviSurfaceMuted
    val foreground = if (enabled) Color.White else TaviTextSecondary.copy(alpha = 0.65f)

    Row(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(background)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Box(
                Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (enabled) TaviLime else TaviTextSecondary.copy(alpha = 0.35f))
            )
            Spacer(Modifier.width(9.dp))
        }
        Text(
            text,
            color = foreground,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun TaviSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .heightIn(min = 50.dp)
            .clip(shape)
            .background(if (enabled) TaviSurface else TaviSurfaceMuted.copy(alpha = 0.7f))
            .border(1.dp, if (enabled) TaviBorder else TaviBorder.copy(alpha = 0.6f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) TaviGraphite else TaviTextSecondary.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TaviTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    accent: Color = TaviGraphite
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier,
        enabled = enabled,
        singleLine = singleLine,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TaviGraphite,
            unfocusedTextColor = TaviGraphite,
            disabledTextColor = TaviTextSecondary,
            focusedBorderColor = accent,
            unfocusedBorderColor = TaviBorder,
            disabledBorderColor = TaviBorder.copy(alpha = 0.6f),
            focusedLabelColor = accent,
            unfocusedLabelColor = TaviTextSecondary,
            disabledLabelColor = TaviTextSecondary.copy(alpha = 0.7f),
            cursorColor = TaviLime,
            focusedContainerColor = TaviSurface,
            unfocusedContainerColor = TaviSurface,
            disabledContainerColor = TaviSurfaceMuted.copy(alpha = 0.72f)
        )
    )
}

@Composable
fun TaviChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accent: Color = TaviGraphite
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(if (selected) accent else TaviSurface)
            .border(1.dp, if (selected) accent else TaviBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            Box(Modifier.size(7.dp).clip(CircleShape).background(TaviLime))
            Spacer(Modifier.width(7.dp))
        }
        Text(
            text,
            color = if (selected) Color.White else TaviGraphite,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun TaviSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        color = TaviGraphite,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun TaviStatusPill(
    text: String,
    accent: Color = TaviLime,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.14f))
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
        Spacer(Modifier.width(7.dp))
        Text(
            text,
            color = TaviGraphite,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TaviHeroObjectFrame(
    modifier: Modifier = Modifier,
    accent: Color,
    content: @Composable BoxScope.() -> Unit
) {
    val shape = RoundedCornerShape(64.dp)
    Box(
        modifier = modifier
            .shadow(8.dp, shape, clip = false)
            .clip(shape)
            .background(TaviSurface)
            .border(1.dp, TaviBorder, shape)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(18.dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(TaviLime)
                .border(2.dp, TaviSurface, CircleShape)
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
                .width(46.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(accent.copy(alpha = 0.75f))
        )
        content()
    }
}

@Composable
fun TaviMetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = TaviGraphite
) {
    TaviCard(modifier) {
        Text(
            value,
            color = accent,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(3.dp))
        Text(label, color = TaviTextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}
