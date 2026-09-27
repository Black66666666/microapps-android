package com.microapps.designsystem

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
val ScrollReceiptAccent = Color(0xFFD85A47)
val WorthItAccent = Color(0xFF3C9B7B)
val BorrowBackAccent = Color(0xFF5C7EE6)
val BoxQrAccent = Color(0xFF2C9FA3)
val RefillAccent = Color(0xFF4DAF8C)
val TurnKeeperAccent = Color(0xFFD96C7C)
val FiveMinutesAccent = Color(0xFFE59B3A)
val ReturnClockAccent = Color(0xFF5F7FC9)
val WhereIsItAccent = Color(0xFFA36CCB)
val OpenedOnAccent = Color(0xFFD47352)
val PackTogetherAccent = Color(0xFF4F9AB8)
val PromiseAccent = Color(0xFFE56E8D)
val GiftPocketAccent = Color(0xFFC48A4C)
val BeforeLeaveAccent = Color(0xFF6E8E5E)
val FairPickAccent = Color(0xFF8E6AB8)

fun taviAccentFor(title: String): Color = when (title) {
    "Screenshot Inbox" -> ScreenshotInboxAccent
    "MatchChoice" -> MatchChoiceAccent
    "WhoBringsWhat" -> WhoBringsWhatAccent
    "BuyTomorrow" -> BuyTomorrowAccent
    "MeetingMeter" -> MeetingMeterAccent
    "Scroll Receipt" -> ScrollReceiptAccent
    "WorthIt" -> WorthItAccent
    "BorrowBack" -> BorrowBackAccent
    "BoxQR" -> BoxQrAccent
    "Refill" -> RefillAccent
    "TurnKeeper" -> TurnKeeperAccent
    "FiveMinutes" -> FiveMinutesAccent
    "ReturnClock" -> ReturnClockAccent
    "WhereIsIt" -> WhereIsItAccent
    "OpenedOn" -> OpenedOnAccent
    "PackTogether" -> PackTogetherAccent
    "Promise" -> PromiseAccent
    "GiftPocket" -> GiftPocketAccent
    "BeforeLeave" -> BeforeLeaveAccent
    "FairPick" -> FairPickAccent
    else -> TaviWarmOrange
}

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

private val TaviTypography = Typography(
    displaySmall = TextStyle(
        fontSize = 34.sp,
        lineHeight = 39.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontSize = 27.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp
    ),
    headlineSmall = TextStyle(
        fontSize = 22.sp,
        lineHeight = 27.sp,
        fontWeight = FontWeight.SemiBold
    ),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 26.sp, fontWeight = FontWeight.Medium),
    titleMedium = TextStyle(fontSize = 17.sp, lineHeight = 23.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontSize = 15.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium),
    labelMedium = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
)

val TaviPocketShape = GenericShape { size, _ ->
    val w = size.width
    val h = size.height
    val r = minOf(w, h) * 0.16f
    moveTo(r, 0f)
    lineTo(w - r * 1.8f, 0f)
    quadraticBezierTo(w - r, 0f, w - r, r * 0.8f)
    quadraticBezierTo(w - r, r * 1.45f, w, r * 1.55f)
    lineTo(w, h - r)
    quadraticBezierTo(w, h, w - r, h)
    lineTo(r, h)
    quadraticBezierTo(0f, h, 0f, h - r)
    lineTo(0f, r)
    quadraticBezierTo(0f, 0f, r, 0f)
    close()
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun TaviTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            view.context.findActivity()?.window?.let { window ->
                window.statusBarColor = TaviBackground.toArgb()
                window.navigationBarColor = TaviBackground.toArgb()
                @Suppress("DEPRECATION")
                run {
                    window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                }
            }
        }
    }
    MaterialTheme(colorScheme = TaviScheme, typography = TaviTypography, content = content)
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
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TaviAppMark(accent = accent)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "TAVI",
                    color = TaviTextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    letterSpacing = 1.4.sp
                )
                Text(
                    title,
                    color = TaviGraphite,
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            subtitle,
            color = TaviGraphite,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.widthIn(max = 340.dp)
        )
        Spacer(Modifier.height(20.dp))
        TaviHeroObjectFrame(
            accent = accent,
            modifier = Modifier.fillMaxWidth().height(132.dp)
        ) {
            TaviHeroVisual(title = title, accent = accent)
        }
    }
}

@Composable
fun TaviAppMark(
    modifier: Modifier = Modifier,
    accent: Color = TaviWarmOrange
) {
    Box(modifier = modifier.size(50.dp)) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(46.dp)
                .shadow(2.dp, TaviPocketShape, clip = false)
                .clip(TaviPocketShape)
                .background(TaviSurface)
                .border(1.dp, TaviBorder, TaviPocketShape)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(21.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(accent)
            )
        }
        TaviStatusDot(
            modifier = Modifier.align(Alignment.TopEnd),
            sizeDp = 15
        )
    }
}

@Composable
fun TaviStatusDot(
    modifier: Modifier = Modifier,
    color: Color = TaviLime,
    sizeDp: Int = 10
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, TaviSurface, CircleShape)
    )
}

@Composable
fun TaviCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .shadow(1.dp, shape, clip = false)
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
    val shape = RoundedCornerShape(20.dp)
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
            TaviStatusDot(
                color = if (enabled) TaviLime else TaviTextSecondary.copy(alpha = 0.35f),
                sizeDp = 8
            )
            Spacer(Modifier.width(9.dp))
        }
        Text(
            text,
            color = foreground,
            style = MaterialTheme.typography.labelLarge
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
    val shape = RoundedCornerShape(20.dp)
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
            style = MaterialTheme.typography.labelLarge
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
        shape = RoundedCornerShape(20.dp),
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
    val shape = RoundedCornerShape(999.dp)
    Row(
        modifier = modifier
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(if (selected) accent else TaviSurface)
            .border(1.dp, if (selected) accent else TaviBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selected) {
            TaviStatusDot(sizeDp = 7)
            Spacer(Modifier.width(7.dp))
        }
        Text(
            text,
            color = if (selected) Color.White else TaviGraphite,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun TaviSectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier,
        color = TaviGraphite,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
fun TaviStatusPill(
    text: String,
    accent: Color = TaviWarmOrange,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TaviStatusDot(sizeDp = 7)
        Spacer(Modifier.width(7.dp))
        Text(
            text,
            color = TaviGraphite,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
fun TaviHeroObjectFrame(
    modifier: Modifier = Modifier,
    accent: Color,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(5.dp, TaviPocketShape, clip = false)
            .clip(TaviPocketShape)
            .background(TaviSurface)
            .border(1.dp, TaviBorder, TaviPocketShape)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp)
                .width(48.dp)
                .height(5.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(accent.copy(alpha = 0.58f))
        )
        TaviStatusDot(
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp),
            sizeDp = 18
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
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(Modifier.height(3.dp))
        Text(label, color = TaviTextSecondary, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun BoxScope.TaviHeroVisual(title: String, accent: Color) {
    when (title) {
        "Screenshot Inbox" -> StackObject(accent)
        "MatchChoice" -> MatchObject(accent)
        "WhoBringsWhat" -> TokenObject(accent)
        "BuyTomorrow" -> PauseObject(accent)
        "MeetingMeter" -> DialObject(accent)
        "Scroll Receipt" -> ReceiptObject(accent)
        "WorthIt" -> BalanceObject(accent)
        "BorrowBack" -> LinkObject(accent)
        "BoxQR" -> QrBoxObject(accent)
        "Refill" -> RefillObject(accent)
        "TurnKeeper" -> TurnObject(accent)
        "FiveMinutes" -> FiveObject(accent)
        "ReturnClock" -> ReturnObject(accent)
        "WhereIsIt" -> PlaceObject(accent)
        "OpenedOn" -> OpenedObject(accent)
        "PackTogether" -> PackObject(accent)
        "Promise" -> PromiseObject(accent)
        "GiftPocket" -> GiftObject(accent)
        "BeforeLeave" -> DoorObject(accent)
        "FairPick" -> FairObject(accent)
        else -> PocketObject(accent)
    }
}

@Composable
private fun BoxScope.ObjectPlate(
    modifier: Modifier = Modifier,
    accent: Color,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .shadow(6.dp, shape, clip = false)
            .clip(shape)
            .background(accent),
        content = content
    )
}

@Composable
private fun BoxScope.StackObject(accent: Color) {
    Box(Modifier.align(Alignment.Center).size(116.dp, 78.dp)) {
        ObjectPlate(Modifier.align(Alignment.BottomStart).size(88.dp, 52.dp), accent.copy(alpha = 0.40f))
        ObjectPlate(Modifier.align(Alignment.Center).offset(x = 8.dp).size(88.dp, 52.dp), accent.copy(alpha = 0.72f))
        ObjectPlate(Modifier.align(Alignment.TopEnd).size(88.dp, 52.dp), accent)
    }
}

@Composable
private fun BoxScope.MatchObject(accent: Color) {
    Box(Modifier.align(Alignment.Center).size(126.dp, 70.dp)) {
        ObjectPlate(Modifier.align(Alignment.CenterStart).size(72.dp, 58.dp), accent.copy(alpha = 0.76f), RoundedCornerShape(24.dp))
        ObjectPlate(Modifier.align(Alignment.CenterEnd).size(72.dp, 58.dp), accent, RoundedCornerShape(24.dp))
        TaviStatusDot(Modifier.align(Alignment.Center), sizeDp = 16)
    }
}

@Composable
private fun BoxScope.TokenObject(accent: Color) {
    Row(
        modifier = Modifier.align(Alignment.Center),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(0.45f, 0.72f, 1f).forEach { alpha ->
            Box(Modifier.size(48.dp).shadow(4.dp, CircleShape).clip(CircleShape).background(accent.copy(alpha = alpha)))
        }
    }
}

@Composable
private fun BoxScope.PauseObject(accent: Color) {
    ObjectPlate(
        modifier = Modifier.align(Alignment.Center).size(126.dp, 62.dp),
        accent = accent,
        shape = RoundedCornerShape(31.dp)
    ) {
        Row(Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.width(9.dp).height(28.dp).clip(RoundedCornerShape(5.dp)).background(TaviSurface))
            Box(Modifier.width(9.dp).height(28.dp).clip(RoundedCornerShape(5.dp)).background(TaviSurface))
        }
    }
}

@Composable
private fun BoxScope.DialObject(accent: Color) {
    Box(
        Modifier.align(Alignment.Center).size(82.dp).shadow(5.dp, CircleShape).clip(CircleShape)
            .background(TaviSurfaceMuted).border(10.dp, accent, CircleShape)
    ) {
        Box(Modifier.align(Alignment.TopCenter).offset(y = 9.dp).size(9.dp, 28.dp).clip(RoundedCornerShape(5.dp)).background(TaviGraphite))
        TaviStatusDot(Modifier.align(Alignment.Center), sizeDp = 12)
    }
}

@Composable
private fun BoxScope.ReceiptObject(accent: Color) {
    ObjectPlate(
        modifier = Modifier.align(Alignment.Center).size(84.dp, 92.dp),
        accent = TaviSurfaceMuted,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.align(Alignment.Center).width(52.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { index ->
                Box(Modifier.fillMaxWidth(if (index == 2) 0.65f else 1f).height(7.dp).clip(RoundedCornerShape(4.dp)).background(if (index == 0) accent else TaviGraphite.copy(alpha = 0.36f)))
            }
        }
    }
}

@Composable
private fun BoxScope.BalanceObject(accent: Color) {
    Box(Modifier.align(Alignment.Center).size(128.dp, 76.dp)) {
        Box(Modifier.align(Alignment.Center).width(92.dp).height(7.dp).clip(RoundedCornerShape(4.dp)).background(TaviGraphite))
        Box(Modifier.align(Alignment.CenterStart).size(48.dp).shadow(4.dp, CircleShape).clip(CircleShape).background(accent.copy(alpha = 0.72f)))
        Box(Modifier.align(Alignment.CenterEnd).size(58.dp).shadow(5.dp, CircleShape).clip(CircleShape).background(accent))
        TaviStatusDot(Modifier.align(Alignment.Center), sizeDp = 11)
    }
}

@Composable
private fun BoxScope.LinkObject(accent: Color) {
    Box(Modifier.align(Alignment.Center).size(118.dp, 74.dp)) {
        ObjectPlate(Modifier.align(Alignment.CenterStart).size(68.dp, 54.dp), accent.copy(alpha = 0.66f), RoundedCornerShape(22.dp))
        ObjectPlate(Modifier.align(Alignment.CenterEnd).size(68.dp, 54.dp), accent, RoundedCornerShape(22.dp))
        Box(Modifier.align(Alignment.Center).size(24.dp).clip(CircleShape).background(TaviSurface).border(6.dp, TaviGraphite, CircleShape))
    }
}

@Composable
private fun BoxScope.QrBoxObject(accent: Color) {
    ObjectPlate(Modifier.align(Alignment.Center).size(94.dp), accent.copy(alpha = 0.22f), RoundedCornerShape(24.dp)) {
        Column(Modifier.align(Alignment.Center), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            repeat(3) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    repeat(3) { col ->
                        val dark = (row + col) % 2 == 0 || (row == 2 && col == 1)
                        Box(Modifier.size(15.dp).clip(RoundedCornerShape(4.dp)).background(if (dark) accent else TaviSurface))
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.RefillObject(accent: Color) {
    Box(
        Modifier.align(Alignment.Center).size(72.dp, 92.dp).shadow(5.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp)).background(TaviSurfaceMuted).border(4.dp, TaviGraphite.copy(alpha = 0.22f), RoundedCornerShape(22.dp))
    ) {
        Box(Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(48.dp).background(accent))
        TaviStatusDot(Modifier.align(Alignment.TopCenter).offset(y = 10.dp), sizeDp = 11)
    }
}

@Composable
private fun BoxScope.TurnObject(accent: Color) {
    Row(Modifier.align(Alignment.Center), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(38.dp).clip(CircleShape).background(accent.copy(alpha = 0.30f)))
        Box(Modifier.size(56.dp).shadow(5.dp, CircleShape).clip(CircleShape).background(accent))
        Box(Modifier.size(38.dp).clip(CircleShape).background(accent.copy(alpha = 0.30f)))
    }
}

@Composable
private fun BoxScope.FiveObject(accent: Color) {
    Box(Modifier.align(Alignment.Center).size(82.dp).shadow(5.dp, CircleShape).clip(CircleShape).background(accent)) {
        Text("5", modifier = Modifier.align(Alignment.Center), color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        TaviStatusDot(Modifier.align(Alignment.TopEnd).offset(x = (-4).dp, y = 4.dp), sizeDp = 12)
    }
}

@Composable
private fun BoxScope.ReturnObject(accent: Color) {
    ObjectPlate(Modifier.align(Alignment.Center).size(96.dp, 76.dp), TaviSurfaceMuted, RoundedCornerShape(20.dp)) {
        Box(Modifier.fillMaxWidth().height(18.dp).background(accent))
        Row(Modifier.align(Alignment.Center).padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { Box(Modifier.size(12.dp).clip(CircleShape).background(TaviGraphite.copy(alpha = 0.34f))) }
        }
    }
}

@Composable
private fun BoxScope.PlaceObject(accent: Color) {
    Box(Modifier.align(Alignment.Center).size(86.dp, 94.dp)) {
        Box(Modifier.align(Alignment.TopCenter).size(70.dp).shadow(5.dp, CircleShape).clip(CircleShape).background(accent))
        Box(Modifier.align(Alignment.BottomCenter).offset(y = (-4).dp).size(28.dp, 46.dp).clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)).background(accent))
        Box(Modifier.align(Alignment.TopCenter).offset(y = 20.dp).size(23.dp).clip(CircleShape).background(TaviSurface))
    }
}

@Composable
private fun BoxScope.OpenedObject(accent: Color) {
    Box(Modifier.align(Alignment.Center).size(82.dp, 92.dp)) {
        ObjectPlate(Modifier.align(Alignment.BottomCenter).size(76.dp, 66.dp), accent.copy(alpha = 0.78f), RoundedCornerShape(20.dp))
        Box(Modifier.align(Alignment.TopCenter).size(62.dp, 24.dp).shadow(3.dp, RoundedCornerShape(10.dp)).clip(RoundedCornerShape(10.dp)).background(TaviGraphite))
        TaviStatusDot(Modifier.align(Alignment.Center), sizeDp = 12)
    }
}

@Composable
private fun BoxScope.PackObject(accent: Color) {
    ObjectPlate(Modifier.align(Alignment.Center).size(104.dp, 72.dp), accent, RoundedCornerShape(24.dp)) {
        Box(Modifier.align(Alignment.TopCenter).offset(y = (-14).dp).size(48.dp, 24.dp).border(7.dp, TaviGraphite, RoundedCornerShape(12.dp)))
        Box(Modifier.align(Alignment.Center).width(7.dp).height(72.dp).background(TaviGraphite.copy(alpha = 0.22f)))
        TaviStatusDot(Modifier.align(Alignment.BottomEnd).padding(9.dp), sizeDp = 11)
    }
}

@Composable
private fun BoxScope.PromiseObject(accent: Color) {
    Box(Modifier.align(Alignment.Center).size(112.dp, 72.dp)) {
        Box(Modifier.align(Alignment.CenterStart).size(62.dp).clip(CircleShape).border(14.dp, accent.copy(alpha = 0.76f), CircleShape))
        Box(Modifier.align(Alignment.CenterEnd).size(62.dp).clip(CircleShape).border(14.dp, accent, CircleShape))
        TaviStatusDot(Modifier.align(Alignment.Center), sizeDp = 13)
    }
}

@Composable
private fun BoxScope.GiftObject(accent: Color) {
    ObjectPlate(Modifier.align(Alignment.Center).size(92.dp, 72.dp), accent, RoundedCornerShape(18.dp)) {
        Box(Modifier.align(Alignment.Center).width(12.dp).fillMaxSize().background(TaviSurface.copy(alpha = 0.84f)))
        Box(Modifier.align(Alignment.TopCenter).fillMaxWidth().height(12.dp).background(TaviSurface.copy(alpha = 0.84f)))
        TaviStatusDot(Modifier.align(Alignment.TopEnd).offset(x = 5.dp, y = (-5).dp), sizeDp = 15)
    }
}

@Composable
private fun BoxScope.DoorObject(accent: Color) {
    ObjectPlate(Modifier.align(Alignment.Center).size(70.dp, 96.dp), accent, RoundedCornerShape(22.dp)) {
        TaviStatusDot(Modifier.align(Alignment.CenterEnd).offset(x = (-10).dp), sizeDp = 10)
        Box(Modifier.align(Alignment.BottomCenter).width(46.dp).height(7.dp).clip(RoundedCornerShape(4.dp)).background(TaviGraphite.copy(alpha = 0.25f)))
    }
}

@Composable
private fun BoxScope.FairObject(accent: Color) {
    ObjectPlate(Modifier.align(Alignment.Center).size(82.dp), accent, RoundedCornerShape(24.dp)) {
        val dotColor = TaviSurface
        Box(Modifier.align(Alignment.TopStart).padding(16.dp).size(11.dp).clip(CircleShape).background(dotColor))
        Box(Modifier.align(Alignment.Center).size(11.dp).clip(CircleShape).background(TaviLime))
        Box(Modifier.align(Alignment.BottomEnd).padding(16.dp).size(11.dp).clip(CircleShape).background(dotColor))
    }
}

@Composable
private fun BoxScope.PocketObject(accent: Color) {
    ObjectPlate(Modifier.align(Alignment.Center).size(92.dp), accent, RoundedCornerShape(26.dp)) {
        TaviStatusDot(Modifier.align(Alignment.TopEnd).padding(12.dp), sizeDp = 13)
    }
}
