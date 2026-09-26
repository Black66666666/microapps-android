package com.microapps.screenshotinbox

import android.Manifest
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { ScreenshotInboxScreen() } }
    }
}

data class ScreenshotItem(val id: Long, val name: String, val dateAdded: Long)

@Composable
private fun ScreenshotInboxScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
    var hasPermission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) }
    var screenshots by remember { mutableStateOf(emptyList<ScreenshotItem>()) }
    var refreshToken by remember { mutableStateOf(0) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
        if (granted) refreshToken++
    }

    val deleteLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        refreshToken++
    }

    LaunchedEffect(hasPermission, refreshToken) {
        if (hasPermission) screenshots = loadScreenshots(context)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Screenshot Inbox", style = MaterialTheme.typography.headlineMedium)
        Text("Разбирай скриншоты как входящие: по одному, без бесконечной галереи.")

        if (!hasPermission) {
            Button(onClick = { permissionLauncher.launch(permission) }) { Text("Разрешить доступ к скриншотам") }
            Text("Приложение читает изображения только для поиска файлов со скриншотами.")
        } else {
            Text("Найдено: ${screenshots.size}", style = MaterialTheme.typography.titleLarge)
            if (screenshots.isEmpty()) {
                Text("Входящие пусты 🎉")
            } else {
                Button(onClick = { refreshToken++ }) { Text("Обновить") }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(screenshots.take(50), key = { it.id }) { item ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.titleMedium)
                                    Text("Добавлен: ${item.dateAdded}")
                                }
                                Button(onClick = {
                                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, item.id)
                                    if (Build.VERSION.SDK_INT >= 30) {
                                        val pendingIntent: PendingIntent = MediaStore.createDeleteRequest(context.contentResolver, listOf(uri))
                                        deleteLauncher.launch(IntentSenderRequest.Builder(pendingIntent.intentSender).build())
                                    } else {
                                        @Suppress("DEPRECATION")
                                        context.contentResolver.delete(uri, null, null)
                                        refreshToken++
                                    }
                                }) { Text("Удалить") }
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun loadScreenshots(context: Context): List<ScreenshotItem> = withContext(Dispatchers.IO) {
    val projection = mutableListOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.DATE_ADDED
    ).apply {
        if (Build.VERSION.SDK_INT >= 29) add(MediaStore.Images.Media.RELATIVE_PATH)
        else add(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
    }.toTypedArray()

    val result = mutableListOf<ScreenshotItem>()
    context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        "${MediaStore.Images.Media.DATE_ADDED} DESC"
    )?.use { cursor ->
        val idIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        val nameIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
        val dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
        val folderIndex = if (Build.VERSION.SDK_INT >= 29) cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH) else cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
        while (cursor.moveToNext()) {
            val name = cursor.getString(nameIndex) ?: "Screenshot"
            val folder = if (folderIndex >= 0) cursor.getString(folderIndex).orEmpty() else ""
            if (isScreenshot(name, folder)) {
                result += ScreenshotItem(cursor.getLong(idIndex), name, cursor.getLong(dateIndex))
            }
        }
    }
    result
}
