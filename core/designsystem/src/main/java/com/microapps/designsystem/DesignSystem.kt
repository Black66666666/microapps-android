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
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Night = Color(0xFF070B1F)
val NightRaised = Color(0xFF10162F)
val Glass = Color(0xFF172044)
val NeonCyan = Color(0xFF31D9FF)
val NeonBlue = Color(0xFF4B7BFF)
val NeonPurple = Color(0xFF8E5CFF)
val NeonPink = Color(0xFFFF4FD8)
val NeonGreen = Color(0xFF43F4B2)
val NeonOrange = Color(0xFFFFA64D)
val TextPrimary = Color(0xFFF7F8FF)
val TextSecondary = Color(0xFFAEB7D4)

private val UnifiedScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = NeonPurple,
    tertiary = NeonPink,
    background = Night,
    surface = NightRaised,
    surfaceVariant = Glass,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0B1030), Night, Color(0xFF080A1A))
                )
            )
    ) {
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = 90.dp, y = (-90).dp)
                .size(250.dp)
                .clip(CircleShape)
                .background(NeonPurple.copy(alpha = 0.18f))
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-110).dp, y = 100.dp)
                .size(280.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.10f))
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
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Brush.linearGradient(listOf(NeonCyan, NeonPurple, NeonPink)))
                .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 26.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(
                title,
                color = TextPrimary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.10f),
                        Glass.copy(alpha = 0.82f),
                        NightRaised.copy(alpha = 0.92f)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.13f), shape)
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
    val colors = if (enabled) {
        listOf(NeonCyan, NeonBlue, NeonPurple, NeonPink)
    } else {
        listOf(Color(0xFF303857), Color(0xFF272E49))
    }
    Row(
        modifier = modifier
            .heightIn(min = 52.dp)
            .clip(shape)
            .background(Brush.horizontalGradient(colors))
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.30f else 0.10f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Text(leading, fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text,
            color = if (enabled) Color.White else TextSecondary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
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
            .background(Color.White.copy(alpha = if (enabled) 0.06f else 0.03f))
            .border(1.dp, Color.White.copy(alpha = if (enabled) 0.16f else 0.07f), shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (enabled) TextPrimary else TextSecondary.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
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
            focusedBorderColor = NeonCyan,
            unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
            disabledBorderColor = Color.White.copy(alpha = 0.08f),
            focusedLabelColor = NeonCyan,
            unfocusedLabelColor = TextSecondary,
            cursorColor = NeonCyan,
            focusedContainerColor = NightRaised.copy(alpha = 0.78f),
            unfocusedContainerColor = NightRaised.copy(alpha = 0.68f),
            disabledContainerColor = NightRaised.copy(alpha = 0.40f)
        )
    )
}

@Composable
fun NeonChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                if (selected) Brush.horizontalGradient(listOf(NeonCyan, NeonPurple))
                else Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.06f)))
            )
            .border(1.dp, Color.White.copy(alpha = if (selected) 0.25f else 0.10f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = TextPrimary,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        color = TextPrimary,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
fun StatusPill(
    text: String,
    accent: Color = NeonGreen,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(accent))
        Spacer(Modifier.width(7.dp))
        Text(text, color = accent, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun MetricCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Color = NeonCyan
) {
    GlassCard(modifier) {
        Text(
            value,
            color = accent,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(3.dp))
        Text(label, color = TextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}
