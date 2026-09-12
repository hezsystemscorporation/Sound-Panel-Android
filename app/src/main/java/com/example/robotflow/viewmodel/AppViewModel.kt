package com.example.robotflow.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.example.robotflow.model.*
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

data class SequenceInfo(
    val dir: File,
    val name: String,
    val addDate: Long
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    var importedSequences by mutableStateOf<List<SequenceInfo>>(emptyList())

    var currentSequence by mutableStateOf<SequenceData?>(null)
    var currentDir by mutableStateOf<File?>(null)

    var currentStepIndex by mutableStateOf(0)
    var currentRound by mutableStateOf(1)

    var skippedSteps by mutableStateOf<Set<Int>>(emptySet())
    var loopStartIndex by mutableStateOf<Int?>(null)
    var loopEndIndex by mutableStateOf<Int?>(null)

    var playingActionName by mutableStateOf<String?>(null)
    var isSubAudioPlaying by mutableStateOf(false)
    var playingGlobalBroadcastName by mutableStateOf<String?>(null)

    private var audioPlayer: MediaPlayer? = null
    private var subAudioPlayer: MediaPlayer? = null
    private var globalAudioPlayer: MediaPlayer? = null
    private var fadeOutJob: Job? = null

    init {
        loadImportedSequences()
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
                    seqName = data.sequenceName ?: "Unknown Sequence"
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

    fun deleteSequence(info: SequenceInfo) {
        info.dir.deleteRecursively()
        loadImportedSequences()
    }

    fun openSequence(dir: File) {
        try {
            val jsonFile = File(dir, "config.json")
            if (jsonFile.exists()) {
                val jsonStr = jsonFile.readText()
                currentSequence = Gson().fromJson(jsonStr, SequenceData::class.java)
                currentDir = dir
                currentStepIndex = 0
                currentRound = 1

                skippedSteps = emptySet()
                loopStartIndex = null
                loopEndIndex = null

                stopAllAudio()
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun closeSequence() {
        stopAllAudio()
        currentSequence = null
        currentDir = null
        skippedSteps = emptySet()
        loopStartIndex = null
        loopEndIndex = null
    }

    fun toggleSkipStep(index: Int) {
        skippedSteps = if (skippedSteps.contains(index)) {
            skippedSteps - index
        } else {
            skippedSteps + index
        }
        hapticFeedback()
    }

    fun setLoopStart() {
        loopStartIndex = currentStepIndex
        if (loopEndIndex != null && loopStartIndex!! > loopEndIndex!!) {
            loopEndIndex = null
        }
        hapticFeedback()
    }

    fun setLoopEnd() {
        if (loopStartIndex != null && currentStepIndex >= loopStartIndex!!) {
            loopEndIndex = currentStepIndex
            hapticFeedback()
        }
    }

    fun clearLoop() {
        loopStartIndex = null
        loopEndIndex = null
        hapticFeedback()
    }

    fun goNext() {
        val steps = currentSequence?.steps ?: return
        if (steps.isEmpty()) return

        var nextIndex = currentStepIndex

        if (loopStartIndex != null && loopEndIndex != null && currentStepIndex == loopEndIndex) {
            nextIndex = loopStartIndex!!
        } else {
            nextIndex++
        }

        var loopGuard = 0
        while (loopGuard < steps.size * 2) {
            if (nextIndex >= steps.size) {
                currentRound++
                nextIndex = 0
            }

            if (loopStartIndex != null && loopEndIndex != null && nextIndex > loopEndIndex!!) {
                nextIndex = loopStartIndex!!
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

        if (loopStartIndex != null && loopEndIndex != null && currentStepIndex == loopStartIndex) {
            prevIndex = loopEndIndex!!
        } else {
            prevIndex--
        }

        var loopGuard = 0
        while (loopGuard < steps.size * 2) {
            if (prevIndex < 0) {
                if (currentRound > 1) currentRound--
                prevIndex = steps.size - 1
            }

            if (loopStartIndex != null && loopEndIndex != null && prevIndex < loopStartIndex!!) {
                prevIndex = loopEndIndex!!
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

    fun playSpecificAction(action: RobotAction) {
        val randomAudioName = action.audioFileNames.randomOrNull() ?: return
        val audioFile = File(File(currentDir, "audio"), randomAudioName)
        if (audioFile.exists()) {
            audioPlayer?.release()
            playingActionName = action.name
            audioPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                prepare()
                start()
                setOnCompletionListener {
                    playingActionName = null
                    it.release()
                }
            }
            hapticFeedback()
        }
    }

    fun toggleSubAudio(action: RobotAction, scope: CoroutineScope) {
        val subFileName = action.subAudioFileName ?: return

        if (isSubAudioPlaying) {
            fadeOutJob?.cancel()
            fadeOutJob = scope.launch {
                val player = subAudioPlayer ?: return@launch
                val timeLeft = player.duration - player.currentPosition
                val fadeTimeMs = minOf(3000, timeLeft).coerceAtLeast(100)
                val steps = 30
                val delayPerStep = fadeTimeMs / steps.toLong()

                for (i in steps downTo 0) {
                    val volume = i / steps.toFloat()
                    player.setVolume(volume, volume)
                    delay(delayPerStep)
                }

                player.pause()
                isSubAudioPlaying = false
            }
        } else {
            fadeOutJob?.cancel()
            val audioFile = File(File(currentDir, "audio"), subFileName)
            if (audioFile.exists()) {
                subAudioPlayer?.release()
                subAudioPlayer = MediaPlayer().apply {
                    setDataSource(audioFile.absolutePath)
                    prepare()
                    setVolume(1.0f, 1.0f)
                    start()
                    setOnCompletionListener {
                        isSubAudioPlaying = false
                        it.release()
                    }
                }
                isSubAudioPlaying = true
            }
        }
        hapticFeedback()
    }

    fun toggleGlobalBroadcast(broadcast: OtherBroadcast) {
        if (playingGlobalBroadcastName == broadcast.name) {
            globalAudioPlayer?.stop()
            globalAudioPlayer?.release()
            globalAudioPlayer = null
            playingGlobalBroadcastName = null
        } else {
            globalAudioPlayer?.release()
            val audioFile = File(File(currentDir, "audio"), broadcast.audioFileName)
            if (audioFile.exists()) {
                globalAudioPlayer = MediaPlayer().apply {
                    setDataSource(audioFile.absolutePath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        playingGlobalBroadcastName = null
                        it.release()
                    }
                }
                playingGlobalBroadcastName = broadcast.name
            }
        }
        hapticFeedback()
    }

    private fun stopAllAudio() {
        audioPlayer?.release()
        audioPlayer = null
        playingActionName = null

        fadeOutJob?.cancel()
        subAudioPlayer?.release()
        subAudioPlayer = null
        isSubAudioPlaying = false

        globalAudioPlayer?.release()
        globalAudioPlayer = null
        playingGlobalBroadcastName = null
    }

    private fun hapticFeedback() {
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {}
    }
}