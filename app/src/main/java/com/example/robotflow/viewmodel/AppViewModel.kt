package com.example.robotflow.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.media.audiofx.LoudnessEnhancer
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.OpenableColumns
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.robotflow.model.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipInputStream
import kotlin.math.log10
import kotlin.random.Random

data class SequenceInfo(
    val dir: File,
    val name: String,
    val addDate: Long
)

data class LoopRegion(
    val id: String = UUID.randomUUID().toString(),
    val startIndex: Int,
    val endIndex: Int,
    val targetLoops: Int,
    var currentLoops: Int = 0
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    var importedSequences by mutableStateOf<List<SequenceInfo>>(emptyList())
    var currentSequence by mutableStateOf<SequenceData?>(null)
    var currentDir by mutableStateOf<File?>(null)

    var currentStepIndex by mutableStateOf(0)
    var currentRound by mutableStateOf(1)
    var currentLanguage by mutableStateOf("English")

    var skippedSteps by mutableStateOf<Set<Int>>(emptySet())
    var loopRegions = mutableStateListOf<LoopRegion>()

    var mainVolume by mutableFloatStateOf(1.0f)
    var subVolume by mutableFloatStateOf(1.0f)
    var bgmVolume by mutableFloatStateOf(0.5f)
    var currentDb by mutableFloatStateOf(-60f)

    var playingActionName by mutableStateOf<String?>(null)
    var isSubAudioPlaying by mutableStateOf(false)
    var playingGlobalBroadcastName by mutableStateOf<String?>(null)

    var bgmPlaylist = mutableStateListOf<File>()
    var currentBgmIndex by mutableIntStateOf(0)
    var isBgmLoaded by mutableStateOf(false)
    var isBgmPlaying by mutableStateOf(false)

    var isBgmShuffle by mutableStateOf(false)
    var isBgmSingleLoop by mutableStateOf(false)

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

    init {
        loadImportedSequences()
        startDbMonitor()
    }

    private fun startDbMonitor() {
        dbMonitorJob?.cancel()
        dbMonitorJob = viewModelScope.launch {
            while (isActive) {
                val isMainPlaying = try { audioPlayer?.isPlaying == true || globalAudioPlayer?.isPlaying == true } catch (e: Exception) { false }
                val isSubPlaying = try { subAudioPlayer?.isPlaying == true } catch (e: Exception) { false }
                val isBgmPlayingNow = try { bgmPlayer?.isPlaying == true } catch (e: Exception) { false }

                if (isMainPlaying || isSubPlaying || isBgmPlayingNow) {
                    val maxVol = maxOf(
                        if (isMainPlaying) mainVolume else 0f,
                        if (isSubPlaying) subVolume else 0f,
                        if (isBgmPlayingNow) bgmVolume else 0f
                    )
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
        val dirs = rootDir.listFiles { file -> file.isDirectory && file.name.startsWith("seq_") }?.toList() ?: emptyList()

        importedSequences = dirs.map { dir ->
            val timestamp = dir.name.substringAfter("seq_").toLongOrNull() ?: 0L
            var seqName = "Unknown Package"
            try {
                val jsonFile = File(dir, "config.json")
                if (jsonFile.exists()) {
                    val jsonStr = jsonFile.readText()
                    val data = Gson().fromJson(jsonStr, SequenceData::class.java)
                    seqName = data.sequenceName ?: "Unknown"
                }
            } catch (e: Exception) { e.printStackTrace() }

            SequenceInfo(dir, seqName, timestamp)
        }.sortedByDescending { it.addDate }
    }

    fun importZipFile(context: Context, uri: Uri) {
        val destDir = File(context.filesDir, "seq_${System.currentTimeMillis()}")
        destDir.mkdirs()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                ZipInputStream(inputStream).use { zis ->
                    var entry = zis.nextEntry
                    while (entry != null) {
                        val file = File(destDir, entry.name)
                        if (entry.isDirectory) file.mkdirs()
                        else {
                            file.parentFile?.mkdirs()
                            FileOutputStream(file).use { fos -> zis.copyTo(fos) }
                        }
                        entry = zis.nextEntry
                    }
                }
            }
            loadImportedSequences()
        } catch (e: Exception) { e.printStackTrace() }
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
            } finally {
                cursor?.close()
            }
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
        if (overwrite) {
            finalAdd.addAll(pendingCollisions)
        }
        showCollisionDialog = false
        pendingToAdd.clear()
        pendingCollisions.clear()

        viewModelScope.launch(Dispatchers.IO) {
            processAddingBgms(context, finalAdd)
        }
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
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
                newlyAddedFiles.add(targetFile)
            } catch (e: Exception) { e.printStackTrace() }
        }

        withContext(Dispatchers.Main) {
            newlyAddedFiles.forEach { file ->
                val existingIdx = bgmPlaylist.indexOfFirst { it.name == file.name }
                if (existingIdx == -1) {
                    bgmPlaylist.add(file)
                }
            }
            if (bgmPlaylist.isNotEmpty()) {
                isBgmLoaded = true
                if (bgmPlayer == null && !isBgmPlaying) {
                    currentBgmIndex = 0
                }
            }
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
                    nextBgm(isAuto = true)
                }
            }
            isBgmPlaying = true
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun toggleBgmShuffle() {
        isBgmShuffle = !isBgmShuffle
        if (isBgmShuffle) isBgmSingleLoop = false
        hapticFeedback()
    }

    fun toggleBgmSingleLoop() {
        isBgmSingleLoop = !isBgmSingleLoop
        if (isBgmSingleLoop) isBgmShuffle = false
        hapticFeedback()
    }

    fun nextBgm(isAuto: Boolean = false) {
        if (bgmPlaylist.isEmpty()) {
            isBgmPlaying = false
            return
        }

        if (isAuto && isBgmSingleLoop) {
            playBgmAtIndex(currentBgmIndex)
            return
        }

        if (isBgmShuffle && bgmPlaylist.size > 1) {
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

        if (currentBgmIndex == fromIndex) {
            currentBgmIndex = toIndex
        } else if (currentBgmIndex in (fromIndex + 1)..toIndex) {
            currentBgmIndex--
        } else if (currentBgmIndex in toIndex until fromIndex) {
            currentBgmIndex++
        }
    }

    fun removeBgm(file: File) {
        val idx = bgmPlaylist.indexOf(file)
        if (idx < 0) return

        val wasPlaying = isBgmPlaying && currentBgmIndex == idx

        bgmPlaylist.removeAt(idx)
        file.delete()

        if (bgmPlaylist.isEmpty()) {
            try { bgmPlayer?.stop() } catch (e: Exception) {}
            try { bgmPlayer?.release() } catch (e: Exception) {}
            bgmPlayer = null
            isBgmPlaying = false
            isBgmLoaded = false
            currentBgmIndex = 0
        } else {
            if (currentBgmIndex > idx) {
                currentBgmIndex--
            } else if (currentBgmIndex == idx) {
                if (currentBgmIndex >= bgmPlaylist.size) {
                    currentBgmIndex = 0
                }
                if (wasPlaying) {
                    playBgmAtIndex(currentBgmIndex)
                } else {
                    try { bgmPlayer?.stop() } catch (e: Exception) {}
                    try { bgmPlayer?.release() } catch (e: Exception) {}
                    bgmPlayer = null
                }
            }
        }
        hapticFeedback()
    }

    fun toggleBgm() {
        if (!isBgmLoaded || bgmPlaylist.isEmpty()) return
        try {
            if (bgmPlayer?.isPlaying == true) {
                bgmPlayer?.pause()
                isBgmPlaying = false
            } else {
                if (bgmPlayer == null) {
                    playBgmAtIndex(currentBgmIndex)
                } else {
                    bgmPlayer?.start()
                    isBgmPlaying = true
                }
            }
        } catch (e: Exception) {}
        hapticFeedback()
    }

    fun deleteSequence(info: SequenceInfo) {
        info.dir.deleteRecursively()
        loadImportedSequences()
    }

    fun openSequence(dir: File) {
        try {
            val jsonFile = File(dir, "config.json")
            if (jsonFile.exists()) {
                val jsonStr = jsonFile.readText()
                val data = Gson().fromJson(jsonStr, SequenceData::class.java)
                currentSequence = data
                currentDir = dir
                currentStepIndex = 0
                currentRound = 1
                currentLanguage = data.languages?.firstOrNull() ?: "English"
                skippedSteps = emptySet()
                loopRegions.clear()
                stopAllAudio()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun closeSequence() {
        stopAllAudio()
        currentSequence = null
        currentDir = null
        skippedSteps = emptySet()
        loopRegions.clear()
    }

    fun toggleSkipStep(index: Int) {
        skippedSteps = if (skippedSteps.contains(index)) skippedSteps - index else skippedSteps + index
        hapticFeedback()
    }

    fun addLoopRegion(start: Int, end: Int, loops: Int) {
        if (start <= end && loops > 0) {
            loopRegions.add(LoopRegion(startIndex = start, endIndex = end, targetLoops = loops))
        }
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

            if (isSkipped || isNotMandatoryRound2) {
                nextIndex++
                loopGuard++
            } else {
                break
            }
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

            if (isSkipped || isNotMandatoryRound2) {
                prevIndex--
                loopGuard++
            } else {
                break
            }
        }

        currentStepIndex = prevIndex
        hapticFeedback()
    }

    fun jumpToStep(index: Int) {
        val size = currentSequence?.steps?.size ?: 0
        if (index in 0 until size) {
            currentStepIndex = index
            hapticFeedback()
        }
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
                        try { audioLoudness?.release() } catch (e: Exception) {}
                        audioLoudness = null
                        try { mp.release() } catch (e: Exception) {}
                        if (audioPlayer == mp) audioPlayer = null
                    }
                } catch (e: Exception) {
                    playingActionName = null
                }
            }
            hapticFeedback()
        }
    }

    fun toggleSubAudio(action: RobotAction, scope: CoroutineScope) {
        val subFileName = action.subAudioFileName?.get(currentLanguage) ?: return

        if (isSubAudioPlaying) {
            fadeOutJob?.cancel()
            fadeOutJob = scope.launch {
                val player = subAudioPlayer ?: return@launch
                val timeLeft = try { player.duration - player.currentPosition } catch (e: Exception) { 0 }
                val fadeTimeMs = minOf(1500, timeLeft).coerceAtLeast(100)
                val steps = 15
                val delayPerStep = fadeTimeMs / steps.toLong()

                for (i in steps downTo 0) {
                    val volume = i / steps.toFloat()
                    try {
                        player.setVolume(volume * subVolume.coerceAtMost(1.0f), volume * subVolume.coerceAtMost(1.0f))
                    } catch (e: Exception) { break }
                    delay(delayPerStep)
                }

                try { player.pause() } catch (e: Exception) {}
                isSubAudioPlaying = false
            }
        } else {
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
                            isSubAudioPlaying = false
                            try { subAudioLoudness?.release() } catch (e: Exception) {}
                            subAudioLoudness = null
                            try { mp.release() } catch (e: Exception) {}
                            if (subAudioPlayer == mp) subAudioPlayer = null
                        }
                    } catch (e: Exception) {
                        isSubAudioPlaying = false
                    }
                }
                isSubAudioPlaying = true
            }
        }
        hapticFeedback()
    }

    fun toggleGlobalBroadcast(broadcast: OtherBroadcast) {
        val fileName = broadcast.audioFileName[currentLanguage] ?: return

        if (playingGlobalBroadcastName == broadcast.name) {
            try { globalAudioPlayer?.stop() } catch (e: Exception) {}
            try { globalAudioPlayer?.release() } catch (e: Exception) {}
            globalAudioPlayer = null
            playingGlobalBroadcastName = null
        } else {
            try { globalAudioPlayer?.release() } catch (e: Exception) {}
            val audioFile = File(File(currentDir, "audio"), fileName)
            if (audioFile.exists()) {
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
                    } catch (e: Exception) {
                        playingGlobalBroadcastName = null
                    }
                }
                playingGlobalBroadcastName = broadcast.name
            }
        }
        hapticFeedback()
    }

    private fun stopAllAudio() {
        try { audioLoudness?.release() } catch (e: Exception) {}
        audioLoudness = null
        try { audioPlayer?.release() } catch (e: Exception) {}
        audioPlayer = null
        playingActionName = null

        fadeOutJob?.cancel()

        try { subAudioLoudness?.release() } catch (e: Exception) {}
        subAudioLoudness = null
        try { subAudioPlayer?.release() } catch (e: Exception) {}
        subAudioPlayer = null
        isSubAudioPlaying = false

        try { globalAudioLoudness?.release() } catch (e: Exception) {}
        globalAudioLoudness = null
        try { globalAudioPlayer?.release() } catch (e: Exception) {}
        globalAudioPlayer = null
        playingGlobalBroadcastName = null

        try { bgmPlayer?.release() } catch (e: Exception) {}
        bgmPlayer = null
        isBgmPlaying = false
    }

    private fun hapticFeedback() {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {}
    }
}