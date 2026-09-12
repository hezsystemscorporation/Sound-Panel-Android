package com.example.robotflow.viewmodel

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.usb.UsbManager
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.Environment
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.OpenableColumns
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.robotflow.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import java.util.zip.ZipInputStream
import kotlin.math.log10
import kotlin.math.pow
import kotlin.random.Random

data class CustomTheme(val id: String, val primary: Color, val light: Color, val dark: Color)

enum class UILanguage { JA, EN, ZH_TW, FR }

data class SequenceInfo(val dir: File, val name: String, val addDate: Long)

data class LoopRegion(val id: String = UUID.randomUUID().toString(), val startIndex: Int, val endIndex: Int, val targetLoops: Int, var currentLoops: Int = 0)

enum class BgmMode { LOOP_ALL, SHUFFLE, SINGLE_LOOP, SINGLE_ONCE }

data class BroadcastState(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isCustom: Boolean = false,
    val customFile: File? = null,
    val originalBroadcast: OtherBroadcast? = null
)

data class PersistedBroadcast(val name: String, val fileName: String)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences = application.getSharedPreferences("robot_settings", Context.MODE_PRIVATE)

    val defaultThemes = listOf(
        CustomTheme("PINK", Color(0xFFFF6699), Color(0xFFFFE6EE), Color(0xFFFF3366)),
        CustomTheme("BLUE", Color(0xFF00AEEC), Color(0xFFE5F7FD), Color(0xFF008CBE)),
        CustomTheme("GREEN", Color(0xFF2AC864), Color(0xFFEAF9F0), Color(0xFF22A050)),
        CustomTheme("HIGH_CONTRAST", Color(0xFF000000), Color(0xFFE0E0E0), Color(0xFF404040))
    )

    var customThemes = mutableStateListOf<CustomTheme>().apply { addAll(getSavedCustomThemes()) }
    var currentTheme by mutableStateOf<CustomTheme>(getSavedTheme())

    var uiLanguage by mutableStateOf(getSavedLanguage())
    var showTopNavBar by mutableStateOf(prefs.getBoolean("show_top_nav", false))

    var isSwipeToAction by mutableStateOf(prefs.getBoolean("swipe_to_action", true))

    fun toggleSwipeMode() {
        isSwipeToAction = !isSwipeToAction
        prefs.edit().putBoolean("swipe_to_action", isSwipeToAction).apply()
    }

    var importedSequences by mutableStateOf<List<SequenceInfo>>(emptyList())
    var currentSequence by mutableStateOf<SequenceData?>(null)
    var currentDir by mutableStateOf<File?>(null)

    var currentStepIndex by mutableStateOf(0)
    var currentRound by mutableStateOf(1)
    var currentLanguage by mutableStateOf("English")

    var skippedSteps by mutableStateOf<Set<Int>>(emptySet())
    var disabledActions by mutableStateOf<Set<String>>(emptySet())
    var loopRegions = mutableStateListOf<LoopRegion>()

    var mainVolume by mutableFloatStateOf(1.0f)
    var subVolume by mutableFloatStateOf(1.0f)
    var bgmVolume by mutableFloatStateOf(0.5f)
    var currentDb by mutableFloatStateOf(-60f)

    var playingActionName by mutableStateOf<String?>(null)
    private var playingActionEnDes: String? = null
    var isSubAudioPlaying by mutableStateOf(false)
    var playingGlobalBroadcastName by mutableStateOf<String?>(null)

    var activeBroadcasts = mutableStateListOf<BroadcastState>()

    var bgmPlaylist = mutableStateListOf<File>()
    var currentBgmIndex by mutableIntStateOf(0)
    var isBgmLoaded by mutableStateOf(false)
    var isBgmPlaying by mutableStateOf(false)
    var bgmMode by mutableStateOf(BgmMode.LOOP_ALL)

    var showCollisionDialog by mutableStateOf(false)
    var collisionNames by mutableStateOf<List<String>>(emptyList())
    private var pendingToAdd = mutableListOf<Pair<Uri, String>>()
    private var pendingCollisions = mutableListOf<Pair<Uri, String>>()

    private var audioPlayer: MediaPlayer? = null
    private var subAudioPlayer: MediaPlayer? = null
    private var globalAudioPlayer: MediaPlayer? = null
    private var bgmPlayer: MediaPlayer? = null

    private var audioLoudness: LoudnessEnhancer? = null
    private var subAudioLoudness: LoudnessEnhancer? = null
    private var globalAudioLoudness: LoudnessEnhancer? = null

    private var fadeOutJob: Job? = null
    private var dbMonitorJob: Job? = null

    private var playingSubAudioEnDes: String? = null

    var usbConnectionState by mutableStateOf("USB Disconnected")

    var isImporting by mutableStateOf(false)
    var importStatusMsg by mutableStateOf("importing")
    var importErrorMsg by mutableStateOf<String?>(null)
    var isCheckingIntegrity by mutableStateOf(false)

    private var usbSerialPort: UsbSerialPort? = null

    init {
        cleanOrphanTempFiles()
        loadImportedSequences()
        loadPersistedBgms()
        startDbMonitor()
        autoScanAndImportPackages()
    }

    private fun cleanOrphanTempFiles() {
        val rootDir = getApplication<Application>().filesDir
        rootDir.listFiles { file -> file.isDirectory && file.name.startsWith("seq_") && file.name.endsWith("_temp") }?.forEach {
            it.deleteRecursively()
        }
    }

    private fun autoScanAndImportPackages() {
        viewModelScope.launch(Dispatchers.IO) {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadDir != null && downloadDir.exists() && downloadDir.isDirectory) {
                val zipFiles = downloadDir.listFiles { file -> file.isFile && file.extension.equals("zip", ignoreCase = true) } ?: emptyArray()
                var hasNewImport = false
                zipFiles.forEach { zipFile ->
                    if (processZipFileDirect(zipFile)) {
                        hasNewImport = true
                    }
                }
                if (hasNewImport) {
                    withContext(Dispatchers.Main) { loadImportedSequences() }
                }
            }
        }
    }

    private fun processZipFileDirect(zipFile: File): Boolean {
        val timestamp = System.currentTimeMillis()
        val tempDir = File(getApplication<Application>().filesDir, "seq_${timestamp}_temp")
        val finalDir = File(getApplication<Application>().filesDir, "seq_${timestamp}")
        tempDir.mkdirs()

        var hasConfig = false
        var isLegacy = false
        var isValidZip = false

        try {
            FileInputStream(zipFile).use { fis ->
                ZipInputStream(fis).use { zis ->
                    var entry = zis.nextEntry
                    if (entry != null) isValidZip = true
                    while (entry != null) {
                        val file = File(tempDir, entry.name)
                        if (entry.isDirectory) {
                            file.mkdirs()
                        } else {
                            file.parentFile?.mkdirs()
                            FileOutputStream(file).use { fos -> zis.copyTo(fos) }
                            if (entry.name == "config.json" || entry.name.endsWith("/config.json")) {
                                hasConfig = true
                                val configStr = file.readText()
                                if (!configStr.contains("\"languages\"")) {
                                    isLegacy = true
                                }
                            }
                        }
                        entry = zis.nextEntry
                    }
                }
            }

            if (!isValidZip || !hasConfig || isLegacy) {
                tempDir.deleteRecursively()
                return false
            } else {
                tempDir.renameTo(finalDir)
                return true
            }
        } catch (e: Exception) {
            tempDir.deleteRecursively()
            return false
        }
    }

    private fun getSavedLanguage(): UILanguage {
        val name = prefs.getString("ui_lang", "JA") ?: "JA"
        return try { UILanguage.valueOf(name) } catch (e: Exception) { UILanguage.JA }
    }

    fun updateLanguage(lang: UILanguage) {
        uiLanguage = lang
        prefs.edit().putString("ui_lang", lang.name).apply()
    }

    fun toggleTopNavBar() {
        showTopNavBar = !showTopNavBar
        prefs.edit().putBoolean("show_top_nav", showTopNavBar).apply()
    }

    private fun getSavedTheme(): CustomTheme {
        val colorInt = prefs.getInt("theme_primary", Color(0xFFFF6699).toArgb())
        val defaultMatch = defaultThemes.find { it.primary.toArgb() == colorInt }
        if (defaultMatch != null) return defaultMatch
        val customMatch = getSavedCustomThemes().find { it.primary.toArgb() == colorInt }
        return customMatch ?: defaultThemes[0]
    }

    fun setTheme(theme: CustomTheme) {
        currentTheme = theme
        prefs.edit().putInt("theme_primary", theme.primary.toArgb()).apply()
    }

    private fun getSavedCustomThemes(): List<CustomTheme> {
        val json = prefs.getString("custom_themes", "[]")
        val type = object : TypeToken<List<Map<String, Int>>>() {}.type
        return try {
            val list: List<Map<String, Int>>? = Gson().fromJson(json, type)
            list?.map {
                CustomTheme(
                    id = UUID.randomUUID().toString(),
                    primary = Color(it["primary"] ?: 0),
                    light = Color(it["light"] ?: 0),
                    dark = Color(it["dark"] ?: 0)
                )
            } ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    fun saveNewCustomTheme(primary: Color) {
        val hsl = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(primary.toArgb(), hsl)
        val lightColor = Color(androidx.core.graphics.ColorUtils.HSLToColor(floatArrayOf(hsl[0], hsl[1], (hsl[2] + 0.3f).coerceAtMost(0.95f))))
        val darkColor = Color(androidx.core.graphics.ColorUtils.HSLToColor(floatArrayOf(hsl[0], hsl[1], (hsl[2] - 0.2f).coerceAtLeast(0.1f))))

        val newTheme = CustomTheme(UUID.randomUUID().toString(), primary, lightColor, darkColor)
        customThemes.add(newTheme)

        val saveList = customThemes.map { mapOf("primary" to it.primary.toArgb(), "light" to it.light.toArgb(), "dark" to it.dark.toArgb()) }
        prefs.edit().putString("custom_themes", Gson().toJson(saveList)).apply()
        setTheme(newTheme)
    }

    fun deleteCustomTheme(theme: CustomTheme) {
        customThemes.remove(theme)
        val saveList = customThemes.map { mapOf("primary" to it.primary.toArgb(), "light" to it.light.toArgb(), "dark" to it.dark.toArgb()) }
        prefs.edit().putString("custom_themes", Gson().toJson(saveList)).apply()
        if (currentTheme.id == theme.id) setTheme(defaultThemes[0])
    }

    fun checkContrastRatio(color: Color): Float {
        val r = if (color.red <= 0.03928f) color.red / 12.92f else ((color.red + 0.055f) / 1.055f).pow(2.4f)
        val g = if (color.green <= 0.03928f) color.green / 12.92f else ((color.green + 0.055f) / 1.055f).pow(2.4f)
        val b = if (color.blue <= 0.03928f) color.blue / 12.92f else ((color.blue + 0.055f) / 1.055f).pow(2.4f)
        val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b
        return (1.0f + 0.05f) / (luminance + 0.05f)
    }

    fun connectToEsp32(context: Context) {
        val manager = context.getSystemService(Context.USB_SERVICE) as UsbManager
        var availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)

        if (availableDrivers.isEmpty()) {
            val customTable = ProbeTable()
            customTable.addProduct(0x303A, 0x1001, CdcAcmSerialDriver::class.java)
            val customProber = UsbSerialProber(customTable)
            availableDrivers = customProber.findAllDrivers(manager)
        }

        if (availableDrivers.isEmpty()) {
            usbConnectionState = "未找到设备(检查OTG线)"
            return
        }

        val driver = availableDrivers[0]
        val device = driver.device

        if (!manager.hasPermission(device)) {
            usbConnectionState = "请授权USB后再次点击连接"
            val intent = Intent("com.example.robotflow.USB_PERMISSION").apply { setPackage(context.packageName) }
            val permissionIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            manager.requestPermission(device, permissionIntent)
            return
        }

        try {
            val connection = manager.openDevice(device)
            if (connection == null) {
                usbConnectionState = "连接被拒绝"
                return
            }

            usbSerialPort = driver.ports[0]
            usbSerialPort?.open(connection)
            usbSerialPort?.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            usbSerialPort?.dtr = true
            usbSerialPort?.rts = true

            usbConnectionState = "ESP32 Connected"
            Log.d("ESP32_USB", "Port opened successfully at 115200 baud")

        } catch (e: Exception) {
            usbConnectionState = "Error: ${e.message}"
            usbSerialPort?.close()
            usbSerialPort = null
        }
    }

    private fun sendEsp32Command(command: String) {
        val langIdx = currentSequence?.languages?.indexOf(currentLanguage)?.takeIf { it >= 0 } ?: 0
        val formattedCommand = "$command#$langIdx\n"

        Log.d("ESP32_COMMAND", "Ready to Tx: $formattedCommand")

        viewModelScope.launch(Dispatchers.IO) {
            var usbSuccess = false

            if (usbSerialPort != null && usbSerialPort?.isOpen == true) {
                try {
                    usbSerialPort?.write(formattedCommand.toByteArray(), 500)
                    usbSuccess = true
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { usbConnectionState = "Tx Failed (USB)" }
                }
            }

            if (!usbSuccess) {
                try {
                    val socket = java.net.Socket()
                    socket.connect(java.net.InetSocketAddress("10.0.3.2", 8080), 2000)
                    val outputStream = socket.getOutputStream()
                    outputStream.write(formattedCommand.toByteArray(Charsets.UTF_8))
                    outputStream.flush()
                    socket.close()
                    Log.d("ESP32_Bridge", "通过 Genymotion Socket 成功发送: $formattedCommand")
                } catch (e: Exception) {
                    Log.e("ESP32_Bridge", "Socket 通信失败: ${e.message}")
                }
            }
        }
    }

    private fun startDbMonitor() {
        dbMonitorJob?.cancel()
        dbMonitorJob = viewModelScope.launch {
            while (isActive) {
                val isMainPlaying = try { audioPlayer?.isPlaying == true || globalAudioPlayer?.isPlaying == true } catch (e: Exception) { false }
                val isSubPlaying = try { subAudioPlayer?.isPlaying == true } catch (e: Exception) { false }
                val isBgmPlayingNow = try { bgmPlayer?.isPlaying == true } catch (e: Exception) { false }

                if (isMainPlaying || isSubPlaying || isBgmPlayingNow) {
                    val maxVol = maxOf(if (isMainPlaying) mainVolume else 0f, if (isSubPlaying) subVolume else 0f, if (isBgmPlayingNow) bgmVolume else 0f)
                    val baseDb = if (maxVol > 0f) 20 * log10(maxVol) else -60f
                    val fluctuation = Random.nextFloat() * 8 - 4
                    currentDb = (baseDb + fluctuation).coerceIn(-60f, 10f)
                } else {
                    currentDb = -60f
                }
                delay(100)
            }
        }
    }

    private fun loadImportedSequences() {
        val rootDir = getApplication<Application>().filesDir
        val dirs = rootDir.listFiles { file -> file.isDirectory && file.name.startsWith("seq_") && !file.name.endsWith("_temp") }?.toList() ?: emptyList()

        importedSequences = dirs.map { dir ->
            val timestamp = dir.name.substringAfter("seq_").toLongOrNull() ?: 0L
            var seqName = "Unknown Package"
            try {
                val jsonFile = File(dir, "config.json")
                if (jsonFile.exists()) {
                    val jsonStr = jsonFile.readText()
                    val data = Gson().fromJson(jsonStr, SequenceData::class.java)
                    seqName = data.sequenceName
                }
            } catch (e: Exception) {}
            SequenceInfo(dir, seqName, timestamp)
        }.sortedByDescending { it.addDate }
    }

    fun importZipFile(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            var size = 0L
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst() && sizeIndex != -1) size = cursor.getLong(sizeIndex)
                }
            } catch (e: Exception) { e.printStackTrace() }

            val isLargeFile = size > 20 * 1024 * 1024

            withContext(Dispatchers.Main) {
                isImporting = true
                importStatusMsg = if (isLargeFile) "importing_large" else "importing"
            }

            val timestamp = System.currentTimeMillis()
            val tempDir = File(context.filesDir, "seq_${timestamp}_temp")
            val finalDir = File(context.filesDir, "seq_${timestamp}")
            tempDir.mkdirs()

            var hasConfig = false
            var isLegacy = false
            var isValidZip = false

            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    ZipInputStream(inputStream).use { zis ->
                        var entry = zis.nextEntry
                        if (entry != null) isValidZip = true

                        while (entry != null) {
                            val file = File(tempDir, entry.name)
                            if (entry.isDirectory) {
                                file.mkdirs()
                            } else {
                                file.parentFile?.mkdirs()
                                FileOutputStream(file).use { fos -> zis.copyTo(fos) }

                                if (entry.name == "config.json" || entry.name.endsWith("/config.json")) {
                                    hasConfig = true
                                    val configStr = file.readText()
                                    if (!configStr.contains("\"languages\"")) {
                                        isLegacy = true
                                    }
                                }
                            }
                            entry = zis.nextEntry
                        }
                    }
                }

                if (!isValidZip || !hasConfig) {
                    tempDir.deleteRecursively()
                    withContext(Dispatchers.Main) { importErrorMsg = "invalid_zip" }
                } else if (isLegacy) {
                    tempDir.deleteRecursively()
                    withContext(Dispatchers.Main) { importErrorMsg = "legacy_zip" }
                } else {
                    tempDir.renameTo(finalDir)
                    withContext(Dispatchers.Main) { loadImportedSequences() }
                }
            } catch (e: Exception) {
                tempDir.deleteRecursively()
                withContext(Dispatchers.Main) { importErrorMsg = "invalid_zip" }
            } finally {
                withContext(Dispatchers.Main) { isImporting = false }
            }
        }
    }

    fun deleteSequence(info: SequenceInfo) {
        info.dir.deleteRecursively()
        loadImportedSequences()
    }

    fun toggleActionDisabled(actionId: String) {
        disabledActions = if (disabledActions.contains(actionId)) {
            disabledActions - actionId
        } else {
            disabledActions + actionId
        }
        hapticFeedback()
    }

    fun cancelCurrentPlayback() {
        var canceled = false
        if (playingActionName != null) {
            playingActionEnDes?.let { sendEsp32Command("Stop $it") }
            try { audioPlayer?.pause(); audioPlayer?.seekTo(0) } catch (e: Exception) {}
            playingActionName = null
            playingActionEnDes = null
            canceled = true
        }
        if (isSubAudioPlaying) {
            playingSubAudioEnDes?.let { sendEsp32Command("Stop $it") }
            try { subAudioPlayer?.pause(); subAudioPlayer?.seekTo(0) } catch (e: Exception) {}
            isSubAudioPlaying = false
            playingSubAudioEnDes = null
            canceled = true
        }
        if (canceled) {
            try {
                val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 50, 30), intArrayOf(0, 255, 0, 255), -1))
            } catch (e: Exception) {}
        }
    }

    private fun saveBgmPlaylist() {
        val fileNames = bgmPlaylist.map { it.name }
        prefs.edit().putString("bgm_playlist", Gson().toJson(fileNames)).apply()
    }

    private fun loadPersistedBgms() {
        val bgmDir = File(getApplication<Application>().filesDir, "custom_bgms")
        if (!bgmDir.exists()) bgmDir.mkdirs()

        val json = prefs.getString("bgm_playlist", "[]")
        val type = object : TypeToken<List<String>>() {}.type
        val fileNames: List<String> = try { Gson().fromJson(json, type) ?: emptyList() } catch(e:Exception){ emptyList() }

        fileNames.forEach { name ->
            val f = File(bgmDir, name)
            if (f.exists() && !bgmPlaylist.contains(f)) bgmPlaylist.add(f)
        }
        bgmDir.listFiles()?.forEach { f ->
            if (!bgmPlaylist.contains(f)) bgmPlaylist.add(f)
        }
        if (bgmPlaylist.isNotEmpty()) isBgmLoaded = true
    }

    private fun savePersistedBroadcastsOrder() {
        val names = activeBroadcasts.map { it.name }
        val seqName = currentSequence?.sequenceName ?: "global"
        prefs.edit().putString("bc_order_$seqName", Gson().toJson(names)).apply()
    }

    private fun getCustomBroadcastsData(): List<PersistedBroadcast> {
        val json = prefs.getString("custom_broadcasts_data", "[]")
        val type = object : TypeToken<List<PersistedBroadcast>>() {}.type
        return try { Gson().fromJson(json, type) ?: emptyList() } catch(e:Exception){ emptyList() }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) result = cursor.getString(index)
                }
            } finally { cursor?.close() }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) result = result?.substring(cut + 1)
        }
        return result
    }

    fun loadExternalBgms(context: Context, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val toAdd = mutableListOf<Pair<Uri, String>>()
            val collisions = mutableListOf<Pair<Uri, String>>()
            val cNames = mutableListOf<String>()

            uris.forEach { uri ->
                val name = getFileName(context, uri) ?: "bgm_${System.currentTimeMillis()}.mp3"
                if (bgmPlaylist.any { it.name == name }) {
                    collisions.add(uri to name)
                    cNames.add(name)
                } else {
                    toAdd.add(uri to name)
                }
            }

            withContext(Dispatchers.Main) {
                if (collisions.isNotEmpty()) {
                    collisionNames = cNames
                    pendingToAdd = toAdd
                    pendingCollisions = collisions
                    showCollisionDialog = true
                } else {
                    processAddingBgms(context, toAdd)
                }
            }
        }
    }

    fun handleCollisionDecision(context: Context, overwrite: Boolean) {
        val finalAdd = pendingToAdd.toMutableList()
        if (overwrite) finalAdd.addAll(pendingCollisions)
        showCollisionDialog = false
        pendingToAdd.clear()
        pendingCollisions.clear()
        viewModelScope.launch(Dispatchers.IO) { processAddingBgms(context, finalAdd) }
    }

    fun cancelCollision() {
        showCollisionDialog = false
        pendingToAdd.clear()
        pendingCollisions.clear()
    }

    private suspend fun processAddingBgms(context: Context, items: List<Pair<Uri, String>>) {
        val bgmDir = File(context.filesDir, "custom_bgms")
        if (!bgmDir.exists()) bgmDir.mkdirs()

        val newlyAddedFiles = mutableListOf<File>()
        items.forEach { (uri, name) ->
            val targetFile = File(bgmDir, name)
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(targetFile).use { output -> input.copyTo(output) }
                }
                newlyAddedFiles.add(targetFile)
            } catch (e: Exception) {}
        }

        withContext(Dispatchers.Main) {
            newlyAddedFiles.forEach { file ->
                val existingIdx = bgmPlaylist.indexOfFirst { it.name == file.name }
                if (existingIdx == -1) bgmPlaylist.add(file)
            }
            if (bgmPlaylist.isNotEmpty()) {
                isBgmLoaded = true
                if (bgmPlayer == null && !isBgmPlaying) currentBgmIndex = 0
            }
            saveBgmPlaylist()
        }
    }

    fun playBgmAtIndex(index: Int) {
        if (bgmPlaylist.isEmpty() || index !in bgmPlaylist.indices) return
        try { bgmPlayer?.release() } catch (e: Exception) {}
        try {
            currentBgmIndex = index
            bgmPlayer = MediaPlayer().apply {
                setDataSource(bgmPlaylist[index].absolutePath)
                isLooping = false
                setVolume(bgmVolume, bgmVolume)
                prepare()
                start()
                setOnCompletionListener {
                    when (bgmMode) {
                        BgmMode.LOOP_ALL -> nextBgm(true)
                        BgmMode.SHUFFLE -> nextBgm(true)
                        BgmMode.SINGLE_LOOP -> playBgmAtIndex(currentBgmIndex)
                        BgmMode.SINGLE_ONCE -> { isBgmPlaying = false; bgmPlayer?.stop() }
                    }
                }
            }
            isBgmPlaying = true
        } catch (e: Exception) {}
    }

    fun cycleBgmMode() {
        bgmMode = when (bgmMode) {
            BgmMode.LOOP_ALL -> BgmMode.SHUFFLE
            BgmMode.SHUFFLE -> BgmMode.SINGLE_LOOP
            BgmMode.SINGLE_LOOP -> BgmMode.SINGLE_ONCE
            BgmMode.SINGLE_ONCE -> BgmMode.LOOP_ALL
        }
        hapticFeedback()
    }

    fun nextBgm(isAuto: Boolean = false) {
        if (bgmPlaylist.isEmpty()) {
            isBgmPlaying = false
            return
        }
        if (isAuto && bgmMode == BgmMode.SINGLE_LOOP) {
            playBgmAtIndex(currentBgmIndex)
            return
        }
        if (bgmMode == BgmMode.SHUFFLE && bgmPlaylist.size > 1) {
            var next = Random.nextInt(bgmPlaylist.size)
            while (next == currentBgmIndex) { next = Random.nextInt(bgmPlaylist.size) }
            currentBgmIndex = next
        } else {
            currentBgmIndex = (currentBgmIndex + 1) % bgmPlaylist.size
        }
        playBgmAtIndex(currentBgmIndex)
        if (!isAuto) hapticFeedback()
    }

    fun moveBgm(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex || fromIndex !in bgmPlaylist.indices || toIndex !in bgmPlaylist.indices) return
        val item = bgmPlaylist.removeAt(fromIndex)
        bgmPlaylist.add(toIndex, item)
        if (currentBgmIndex == fromIndex) currentBgmIndex = toIndex
        else if (currentBgmIndex in (fromIndex + 1)..toIndex) currentBgmIndex--
        else if (currentBgmIndex in toIndex until fromIndex) currentBgmIndex++
        saveBgmPlaylist()
    }

    fun removeBgm(file: File) {
        val idx = bgmPlaylist.indexOf(file)
        if (idx < 0) return
        val wasPlaying = isBgmPlaying && currentBgmIndex == idx

        if (wasPlaying) {
            try { bgmPlayer?.stop(); bgmPlayer?.release() } catch (e: Exception) {}
            bgmPlayer = null
            isBgmPlaying = false
        }

        bgmPlaylist.removeAt(idx)
        file.delete()
        saveBgmPlaylist()

        if (bgmPlaylist.isEmpty()) {
            isBgmLoaded = false
            currentBgmIndex = 0
        } else {
            if (currentBgmIndex > idx) currentBgmIndex--
            else if (currentBgmIndex >= bgmPlaylist.size) currentBgmIndex = 0
        }
        hapticFeedback()
    }

    fun toggleBgm() {
        if (!isBgmLoaded || bgmPlaylist.isEmpty()) return
        try {
            if (bgmPlayer?.isPlaying == true) {
                bgmPlayer?.pause(); isBgmPlaying = false
            } else {
                if (bgmPlayer == null) playBgmAtIndex(currentBgmIndex)
                else { bgmPlayer?.start(); isBgmPlaying = true }
            }
        } catch (e: Exception) {}
        hapticFeedback()
    }

    fun addCustomBroadcast(context: Context, uri: Uri, customName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val bcDir = File(context.filesDir, "custom_broadcasts")
            if (!bcDir.exists()) bcDir.mkdirs()
            val safeName = getFileName(context, uri) ?: "bc_${System.currentTimeMillis()}.wav"
            val targetFile = File(bcDir, safeName)
            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(targetFile).use { output -> input.copyTo(output) }
                }
                withContext(Dispatchers.Main) {
                    val list = getCustomBroadcastsData().toMutableList()
                    list.add(PersistedBroadcast(customName, safeName))
                    prefs.edit().putString("custom_broadcasts_data", Gson().toJson(list)).apply()
                    activeBroadcasts.add(BroadcastState(name = customName, isCustom = true, customFile = targetFile))
                    savePersistedBroadcastsOrder()
                }
            } catch (e: Exception) {}
        }
    }

    fun removeCustomBroadcast(state: BroadcastState) {
        if (playingGlobalBroadcastName == state.name) {
            try { globalAudioPlayer?.stop(); globalAudioPlayer?.release() } catch (e: Exception) {}
            globalAudioPlayer = null
            playingGlobalBroadcastName = null
        }
        activeBroadcasts.remove(state)
        state.customFile?.delete()

        val list = getCustomBroadcastsData().toMutableList()
        list.removeAll { it.fileName == state.customFile?.name }
        prefs.edit().putString("custom_broadcasts_data", Gson().toJson(list)).apply()
        savePersistedBroadcastsOrder()
    }

    fun moveBroadcast(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex || fromIndex !in activeBroadcasts.indices || toIndex !in activeBroadcasts.indices) return
        val item = activeBroadcasts.removeAt(fromIndex)
        activeBroadcasts.add(toIndex, item)
        savePersistedBroadcastsOrder()
    }

    fun openSequence(dir: File) {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(Dispatchers.Main) { isCheckingIntegrity = true }

            try {
                val jsonFile = File(dir, "config.json")
                if (!jsonFile.exists()) throw Exception("Missing config")

                val jsonStr = jsonFile.readText()
                val data = Gson().fromJson(jsonStr, SequenceData::class.java)

                var isComplete = true
                val requiredFiles = mutableListOf<File>()

                data.steps.forEach { step ->
                    step.actions.forEach { action ->
                        action.audioFileNames.values.forEach { list ->
                            list.forEach { fileName ->
                                if (fileName.isNotBlank()) requiredFiles.add(File(File(dir, "audio"), fileName))
                            }
                        }
                        action.subAudioFileName?.values?.forEach { subName ->
                            if (subName.isNotBlank()) requiredFiles.add(File(File(dir, "audio"), subName))
                        }
                        if (!action.imageFileName.isNullOrBlank()) {
                            requiredFiles.add(File(File(dir, "image"), action.imageFileName!!))
                        }
                    }
                }

                data.otherBroadcasts?.forEach { ob ->
                    ob.audioFileName.values.forEach { obName ->
                        if (obName.isNotBlank()) requiredFiles.add(File(File(dir, "audio"), obName))
                    }
                }

                for (file in requiredFiles) {
                    if (!file.exists()) {
                        isComplete = false
                        break
                    }
                }

                if (isComplete) {
                    withContext(Dispatchers.Main) {
                        currentSequence = data
                        currentDir = dir
                        currentStepIndex = 0
                        currentRound = 1
                        currentLanguage = data.languages?.firstOrNull() ?: "English"
                        skippedSteps = emptySet()
                        disabledActions = emptySet()
                        loopRegions.clear()
                        activeBroadcasts.clear()

                        data.otherBroadcasts?.forEach {
                            activeBroadcasts.add(BroadcastState(name = it.name, isCustom = false, originalBroadcast = it))
                        }

                        val bcDir = File(getApplication<Application>().filesDir, "custom_broadcasts")
                        getCustomBroadcastsData().forEach { pb ->
                            val f = File(bcDir, pb.fileName)
                            if (f.exists()) activeBroadcasts.add(BroadcastState(name = pb.name, isCustom = true, customFile = f))
                        }

                        val orderJson = prefs.getString("bc_order_${data.sequenceName}", null)
                        if (orderJson != null) {
                            val type = object : TypeToken<List<String>>() {}.type
                            val orderNames: List<String> = try { Gson().fromJson(orderJson, type) ?: emptyList() } catch(e:Exception){ emptyList() }
                            activeBroadcasts.sortBy { state ->
                                val idx = orderNames.indexOf(state.name)
                                if (idx == -1) 999 else idx
                            }
                        }
                        stopAllAudio()
                    }
                } else {
                    dir.deleteRecursively()
                    withContext(Dispatchers.Main) {
                        importErrorMsg = "package_corrupted"
                        loadImportedSequences()
                    }
                }
            } catch (e: Exception) {
                dir.deleteRecursively()
                withContext(Dispatchers.Main) {
                    importErrorMsg = "package_corrupted"
                    loadImportedSequences()
                }
            } finally {
                withContext(Dispatchers.Main) { isCheckingIntegrity = false }
            }
        }
    }

    fun closeSequence() {
        stopAllAudio()
        currentSequence = null
        currentDir = null
        skippedSteps = emptySet()
        disabledActions = emptySet()
        loopRegions.clear()
        activeBroadcasts.clear()
    }

    fun toggleSkipStep(index: Int): Boolean {
        if (skippedSteps.contains(index)) {
            skippedSteps = skippedSteps - index
            hapticFeedback()
            return true
        } else {
            val totalSteps = currentSequence?.steps?.size ?: 0
            if (skippedSteps.size >= totalSteps - 1) {
                return false
            }
            skippedSteps = skippedSteps + index
            hapticFeedback()
            return true
        }
    }

    fun recoverAllSteps() {
        skippedSteps = emptySet()
        hapticFeedback()
    }

    fun addLoopRegion(start: Int, end: Int, loops: Int) {
        if (start <= end && loops > 0) loopRegions.add(LoopRegion(startIndex = start, endIndex = end, targetLoops = loops))
    }

    fun removeLoopRegion(id: String) {
        loopRegions.removeAll { it.id == id }
    }

    fun goNext() {
        val steps = currentSequence?.steps ?: return
        if (steps.isEmpty()) return

        var nextIndex = currentStepIndex
        val activeLoop = loopRegions.find { it.endIndex == currentStepIndex }

        if (activeLoop != null && activeLoop.currentLoops < activeLoop.targetLoops - 1) {
            activeLoop.currentLoops++
            nextIndex = activeLoop.startIndex
        } else {
            if (activeLoop != null) activeLoop.currentLoops = 0
            nextIndex++
        }

        var loopGuard = 0
        while (loopGuard < steps.size * 2) {
            if (nextIndex >= steps.size) {
                currentRound++
                nextIndex = 0
            }
            val isSkipped = skippedSteps.contains(nextIndex)
            val isNotMandatoryRound2 = currentRound > 1 && steps[nextIndex].actions.all { !it.isMandatory }
            if (isSkipped || isNotMandatoryRound2) { nextIndex++; loopGuard++ } else break
        }

        currentStepIndex = nextIndex
        hapticFeedback()
    }

    fun goPrev() {
        val steps = currentSequence?.steps ?: return
        if (steps.isEmpty()) return

        var prevIndex = currentStepIndex
        prevIndex--

        var loopGuard = 0
        while (loopGuard < steps.size * 2) {
            if (prevIndex < 0) {
                if (currentRound > 1) currentRound--
                prevIndex = steps.size - 1
            }
            val isSkipped = skippedSteps.contains(prevIndex)
            val isNotMandatoryRound2 = currentRound > 1 && steps[prevIndex].actions.all { !it.isMandatory }
            if (isSkipped || isNotMandatoryRound2) { prevIndex--; loopGuard++ } else break
        }

        currentStepIndex = prevIndex
        hapticFeedback()
    }

    fun jumpToStep(index: Int) {
        val size = currentSequence?.steps?.size ?: 0
        if (index in 0 until size) { currentStepIndex = index; hapticFeedback() }
    }

    fun goFirst() = jumpToStep(0)
    fun goLast() {
        val size = currentSequence?.steps?.size ?: 0
        if (size > 0) jumpToStep(size - 1)
    }

    private fun setupPlayerVolume(player: MediaPlayer, targetVol: Float): LoudnessEnhancer? {
        var enhancer: LoudnessEnhancer? = null
        if (targetVol <= 1.0f) {
            try { player.setVolume(targetVol, targetVol) } catch (e: Exception) {}
        } else {
            try { player.setVolume(1.0f, 1.0f) } catch (e: Exception) {}
            try {
                enhancer = LoudnessEnhancer(player.audioSessionId).apply {
                    setTargetGain(((targetVol - 1.0f) * 2000).toInt())
                    enabled = true
                }
            } catch (e: Exception) {}
        }
        return enhancer
    }

    fun onMainVolumeChanged(vol: Float) {
        mainVolume = vol
        try { audioLoudness?.release() } catch (e: Exception) {}
        audioPlayer?.let { audioLoudness = setupPlayerVolume(it, mainVolume) }
        try { globalAudioLoudness?.release() } catch (e: Exception) {}
        globalAudioPlayer?.let { globalAudioLoudness = setupPlayerVolume(it, mainVolume) }
    }

    fun onSubVolumeChanged(vol: Float) {
        subVolume = vol
        try { subAudioLoudness?.release() } catch (e: Exception) {}
        subAudioPlayer?.let { subAudioLoudness = setupPlayerVolume(it, subVolume) }
    }

    fun onBgmVolumeChanged(vol: Float) {
        bgmVolume = vol
        try { bgmPlayer?.setVolume(bgmVolume, bgmVolume) } catch (e: Exception) {}
    }

    fun playSpecificAction(action: RobotAction) {
        action.en_des?.let {
            sendEsp32Command("SHOW: Start $it")
            playingActionEnDes = it
        }

        val audioList = action.audioFileNames[currentLanguage] ?: return
        val randomAudioName = audioList.randomOrNull() ?: return

        val audioFile = File(File(currentDir, "audio"), randomAudioName)
        if (audioFile.exists()) {
            try { audioPlayer?.release() } catch (e: Exception) {}
            playingActionName = action.name
            audioPlayer = MediaPlayer().apply {
                try {
                    setDataSource(audioFile.absolutePath)
                    prepare()
                    audioLoudness = setupPlayerVolume(this, mainVolume)
                    start()
                    setOnCompletionListener { mp ->
                        playingActionName = null
                        playingActionEnDes = null
                        try { audioLoudness?.release() } catch (e: Exception) {}
                        audioLoudness = null
                        try { mp.release() } catch (e: Exception) {}
                        if (audioPlayer == mp) audioPlayer = null
                    }
                } catch (e: Exception) { playingActionName = null }
            }
            hapticFeedback()
        }
    }

    private fun finalizeSubAudioState() {
        playingSubAudioEnDes?.let { sendEsp32Command("SHOW: Stop $it") }
        playingSubAudioEnDes = null
        isSubAudioPlaying = false
    }

    fun toggleSubAudio(action: RobotAction, scope: CoroutineScope) {
        val subFileName = action.subAudioFileName?.get(currentLanguage) ?: return
        val enDes = action.subAudioEnDes?.get(currentLanguage)

        if (isSubAudioPlaying) {
            enDes?.let { sendEsp32Command("SHOW: Stop $it") }

            fadeOutJob?.cancel()
            fadeOutJob = scope.launch {
                val player = subAudioPlayer ?: return@launch
                val timeLeft = try { player.duration - player.currentPosition } catch (e: Exception) { 0 }
                val fadeTimeMs = minOf(1500, timeLeft).coerceAtLeast(100)
                val steps = 15
                val delayPerStep = fadeTimeMs / steps.toLong()

                for (i in steps downTo 0) {
                    val volume = i / steps.toFloat()
                    try { player.setVolume(volume * subVolume.coerceAtMost(1.0f), volume * subVolume.coerceAtMost(1.0f)) } catch (e: Exception) { break }
                    delay(delayPerStep)
                }
                try { player.pause() } catch (e: Exception) {}
                finalizeSubAudioState()
            }
        } else {
            enDes?.let {
                sendEsp32Command("SHOW: Start $it")
                playingSubAudioEnDes = it
            }

            fadeOutJob?.cancel()
            val audioFile = File(File(currentDir, "audio"), subFileName)
            if (audioFile.exists()) {
                try { subAudioPlayer?.release() } catch (e: Exception) {}
                subAudioPlayer = MediaPlayer().apply {
                    try {
                        setDataSource(audioFile.absolutePath)
                        prepare()
                        subAudioLoudness = setupPlayerVolume(this, subVolume)
                        start()
                        setOnCompletionListener { mp ->
                            finalizeSubAudioState()
                            try { subAudioLoudness?.release() } catch (e: Exception) {}
                            subAudioLoudness = null
                            try { mp.release() } catch (e: Exception) {}
                            if (subAudioPlayer == mp) subAudioPlayer = null
                        }
                    } catch (e: Exception) { finalizeSubAudioState() }
                }
                isSubAudioPlaying = true
            }
        }
        hapticFeedback()
    }

    fun playBroadcast(state: BroadcastState) {
        if (playingGlobalBroadcastName == state.name) {
            try { globalAudioPlayer?.stop() } catch (e: Exception) {}
            try { globalAudioPlayer?.release() } catch (e: Exception) {}
            globalAudioPlayer = null
            playingGlobalBroadcastName = null
            hapticFeedback()
            return
        }

        val audioFile = if (state.isCustom) {
            state.customFile
        } else {
            val fName = state.originalBroadcast?.audioFileName?.get(currentLanguage)
            if (fName != null) File(File(currentDir, "audio"), fName) else null
        }

        if (audioFile != null && audioFile.exists()) {
            if (!state.isCustom) state.originalBroadcast?.en_des?.get(currentLanguage)?.let { sendEsp32Command("SHOW: Start $it") }

            try { globalAudioPlayer?.release() } catch (e: Exception) {}
            globalAudioPlayer = MediaPlayer().apply {
                try {
                    setDataSource(audioFile.absolutePath)
                    prepare()
                    globalAudioLoudness = setupPlayerVolume(this, mainVolume)
                    start()
                    setOnCompletionListener { mp ->
                        playingGlobalBroadcastName = null
                        try { globalAudioLoudness?.release() } catch (e: Exception) {}
                        globalAudioLoudness = null
                        try { mp.release() } catch (e: Exception) {}
                        if (globalAudioPlayer == mp) globalAudioPlayer = null
                    }
                } catch (e: Exception) { playingGlobalBroadcastName = null }
            }
            playingGlobalBroadcastName = state.name
        }
        hapticFeedback()
    }

    private fun stopAllAudio() {
        try { audioLoudness?.release() } catch (e: Exception) {}
        audioLoudness = null
        try { audioPlayer?.release() } catch (e: Exception) {}
        audioPlayer = null
        playingActionName = null
        playingActionEnDes = null

        fadeOutJob?.cancel()

        try { subAudioLoudness?.release() } catch (e: Exception) {}
        subAudioLoudness = null
        try { subAudioPlayer?.release() } catch (e: Exception) {}
        subAudioPlayer = null
        finalizeSubAudioState()

        try { globalAudioLoudness?.release() } catch (e: Exception) {}
        globalAudioLoudness = null
        try { globalAudioPlayer?.release() } catch (e: Exception) {}
        globalAudioPlayer = null
        playingGlobalBroadcastName = null

        try { bgmPlayer?.release() } catch (e: Exception) {}
        bgmPlayer = null
        isBgmPlaying = false
    }

    fun hapticFeedback() {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {}
    }
}