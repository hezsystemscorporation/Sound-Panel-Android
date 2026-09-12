package com.example.robotflow.model

import androidx.annotation.Keep

@Keep
data class SequenceData(
    val sequenceName: String,
    val steps: List<RobotStep>,
    val otherBroadcasts: List<OtherBroadcast>? = null
)

@Keep
data class RobotStep(
    val id: String,
    val title: String,
    val actions: List<RobotAction>
)

@Keep
data class RobotAction(
    val id: String,
    val name: String,
    val imageFileName: String? = null,
    val audioFileNames: List<String>,
    val subAudioName: String? = null,
    val subAudioFileName: String? = null,
    val isMandatory: Boolean = true
)

@Keep
data class OtherBroadcast(
    val name: String,
    val audioFileName: String
)