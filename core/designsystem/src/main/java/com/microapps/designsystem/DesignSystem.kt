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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// TAVI Pocket Objects shared design tokens.
val Night = Color(0xFFF4F0E7)          // compatibility alias: warm neutral background
val NightRaised = Color(0xFFFFFCF5)    // warm elevated surface
val Glass = Color(0xFFF0EBE1)          // soft stone surface
val NeonCyan = Color(0xFF2488F5)       // utility blue
val NeonBlue = Color(0xFF2488F5)
val NeonPurple = Color(0xFF8B4DE8)     // app purple
val NeonPink = Color(0xFFFF6D3A)       // warm orange accent
val NeonGreen = Color(0xFFB7F000)      // signature TAVI lime
val NeonOrange = Color(0xFFFF7A3D)
val TextPrimary = Color(0xFF191A18)    // graphite
val TextSecondary = Color(0xFF666861)
val TaviLime = NeonGreen
val TaviGraphite = TextPrimary
val TaviPaper = Night
val TaviSurface = NightRaised
val TaviBorder = Color(0xFFD9D3C7)

private val UnifiedScheme = lightColorScheme(
    primary = TaviGraphite,
    secondary = TaviLime,
    tertiary = NeonOrange,
    background = TaviPaper,
    surface = TaviSurface,
    surfaceVariant = Glass,
    onPrimary = Color.White,
    onSecondary = TaviGraphite,
    onTertiary = TaviGraphite,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun UnifiedAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = UnifiedScheme, content = content)
}

@Composable
fun NeonBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(TaviPaper)) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 90.dp, y = (-105).dp)
                .size(245.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.58f))
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-120).dp, y = 115.dp)
                .size(270.dp)
                .clip(CircleShape)
                .background(Color(0xFFE6DED0).copy(alpha = 0.52f))
        )
        content()
    }
}

@Composable
fun AppHeader(
    icon: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF0E8DA))
                .border(1.dp, TaviBorder, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 27.sp, color = TaviGraphite)
            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(TaviLime)
                    .border(1.dp, Color(0xFF8DAE00), CircleShape)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, color = TextPrimary, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(TaviSurface.copy(alpha = 0.96f))
            .border(1.dp, TaviBorder, shape)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: String? = null
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(if (enabled) TaviGraphite else Color(0xFFD7D2C8))
            .border(1.dp, if (enabled) TaviGraphite else TaviBorder, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Text(leading, fontSize = 18.sp, color = if (enabled) Color.White else TextSecondary)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, color = if (enabled) Color.White else TextSecondary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        if (enabled) {
            Spacer(Modifier.width(9.dp))
            Box(Modifier.size(8.dp).clip(CircleShape).background(TaviLime))
        }
    }
}

@Composable
fun SecondaryButton(
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
            .background(if (enabled) TaviSurface else Glass.copy(alpha = 0.7f))
            .border(1.dp, if (enabled) TaviGraphite.copy(alpha = 0.22f) else TaviBorder, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.65f), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun NeonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true
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
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            disabledTextColor = TextSecondary,
            focusedBorderColor = TaviGraphite,
            unfocusedBorderColor = TaviBorder,
            disabledBorderColor = TaviBorder.copy(alpha = 0.65f),
            focusedLabelColor = TaviGraphite,
            unfocusedLabelColor = TextSecondary,
            cursorColor = TaviGraphite,
            focusedContainerColor = TaviSurface,
            unfocusedContainerColor = TaviSurface,
            disabledContainerColor = Glass.copy(alpha = 0.65f)
        )
    )
}

@Composable
fun NeonChip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(shape)
            .background(if (selected) TaviLime else TaviSurface)
            .border(1.dp, if (selected) Color(0xFF95BA00) else TaviBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, color = TextPrimary, style = MaterialTheme.typography.labelMedium, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, color = TextPrimary, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
fun StatusPill(text: String, accent: Color = NeonGreen, modifier: Modifier = Modifier) {
    val textColor = if (accent == NeonGreen) TaviGraphite else accent
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = if (accent == NeonGreen) 0.78f else 0.10f))
            .border(1.dp, accent.copy(alpha = 0.75f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(if (accent == NeonGreen) TaviGraphite else accent))
        Spacer(Modifier.width(7.dp))
        Text(text, color = textColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun MetricCard(value: String, label: String, modifier: Modifier = Modifier, accent: Color = NeonCyan) {
    GlassCard(modifier) {
        Box(Modifier.size(11.dp).clip(CircleShape).background(accent))
        Spacer(Modifier.height(8.dp))
        Text(value, color = TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(3.dp))
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}
