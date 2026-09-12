package com.example.robotflow.ui

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.alpha
import coil.compose.AsyncImage
import com.example.robotflow.model.RobotAction
import com.example.robotflow.model.RobotStep
import com.example.robotflow.viewmodel.AppViewModel
import com.example.robotflow.viewmodel.SequenceInfo
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    if (viewModel.currentSequence == null) {
        HomeListScreen(viewModel)
    } else {
        PlayerScreen(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeListScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.importZipFile(context, uri)
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    var sequenceToDelete by remember { mutableStateOf<SequenceInfo?>(null) }

    if (sequenceToDelete != null) {
        AlertDialog(
            onDismissRequest = { sequenceToDelete = null },
            title = { Text("Confirmation", fontWeight = FontWeight.Bold) },
            text = { Text("Sure to delete「${sequenceToDelete!!.name}」? \nThis action cannot be undone. ") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSequence(sequenceToDelete!!)
                    sequenceToDelete = null
                }) {
                    Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sequenceToDelete = null }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Sequence Library", fontWeight = FontWeight.Black) }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { launcher.launch("application/zip") },
                containerColor = Color(0xFFFF6699),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Import ZIP")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {

            if (viewModel.importedSequences.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                    Text("Please Import Packages First. ", color = Color.Gray)
                }
            }

            viewModel.importedSequences.forEach { info ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable { viewModel.openSequence(info.dir) },
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = info.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Added ${dateFormat.format(Date(info.addDate))}",
                                fontSize = 13.sp, color = Color.Gray
                            )
                        }

                        IconButton(onClick = { sequenceToDelete = info }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun PlayerScreen(viewModel: AppViewModel) {
    val configuration = LocalConfiguration.current
    val isCompact = configuration.screenWidthDp < 600
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val sequence = viewModel.currentSequence ?: return
    val currentStep = sequence.steps.getOrNull(viewModel.currentStepIndex) ?: return

    val coroutineScope = rememberCoroutineScope()
    var expandedMenu by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF2F2F7))) {
        LightFluidBackground()

        Column(modifier = Modifier.padding(16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.closeSequence() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Home", tint = Color(0xFFFF6699))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Audio Panel", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.weight(1f))

                if (!sequence.languages.isNullOrEmpty() && sequence.languages.size > 1) {
                    Button(
                        onClick = {
                            val currentIndex = sequence.languages.indexOf(viewModel.currentLanguage)
                            val nextIndex = (currentIndex + 1) % sequence.languages.size
                            viewModel.currentLanguage = sequence.languages[nextIndex]
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFE6EE),
                            contentColor = Color(0xFFFF6699)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(32.dp).padding(end = 12.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = "Switch Language", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(viewModel.currentLanguage, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Text("Round ${viewModel.currentRound}", fontSize = 14.sp, color = Color.Gray)
            }

            Box {
                Button(
                    onClick = { expandedMenu = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFFF6699)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    val statusPrefix = if (viewModel.skippedSteps.contains(viewModel.currentStepIndex)) "[Skipped] " else ""
                    Text("${statusPrefix}Action ${currentStep.id} : ${currentStep.title}", fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Show List")
                }

                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false },
                    modifier = Modifier.fillMaxHeight(0.6f)
                ) {
                    sequence.steps.forEachIndexed { index, step ->
                        val statusLabel = buildString {
                            if (viewModel.loopStartIndex == index) append("[Start] ")
                            if (viewModel.loopEndIndex == index) append("[End] ")
                            if (viewModel.skippedSteps.contains(index)) append("[Skipped]")
                        }

                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Action ${step.id} - ${step.title} $statusLabel",
                                    color = if (viewModel.skippedSteps.contains(index)) Color.Gray else Color.Black
                                )
                            },
                            onClick = {
                                viewModel.jumpToStep(index)
                                expandedMenu = false
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isSkipped = viewModel.skippedSteps.contains(viewModel.currentStepIndex)
                Button(
                    onClick = { viewModel.toggleSkipStep(viewModel.currentStepIndex) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSkipped) Color.DarkGray else Color(0xFFFFE6EE),
                        contentColor = if (isSkipped) Color.White else Color(0xFFFF6699)
                    )
                ) {
                    Text(if (isSkipped) "Recover" else "Skip", fontWeight = FontWeight.Bold)
                }

                val isStart = viewModel.loopStartIndex == viewModel.currentStepIndex
                Button(
                    onClick = { viewModel.setLoopStart() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isStart) Color(0xFFFF6699) else Color.White,
                        contentColor = if (isStart) Color.White else Color(0xFFFF6699)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFFF6699))
                ) {
                    Text(if (isStart) "Starts Here" else "Set As Start")
                }

                val isEnd = viewModel.loopEndIndex == viewModel.currentStepIndex
                val canSetEnd = viewModel.loopStartIndex != null && viewModel.currentStepIndex >= viewModel.loopStartIndex!!
                Button(
                    onClick = { viewModel.setLoopEnd() },
                    enabled = canSetEnd,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEnd) Color(0xFFFF6699) else Color.White,
                        contentColor = if (isEnd) Color.White else Color(0xFFFF6699),
                        disabledContainerColor = Color.LightGray.copy(alpha=0.2f),
                        disabledContentColor = Color.Gray
                    ),
                    border = BorderStroke(1.dp, if (canSetEnd) Color(0xFFFF6699) else Color.Transparent)
                ) {
                    Text(if (isEnd) "Ends Here" else "Set As Ending")
                }

                if (viewModel.loopStartIndex != null || viewModel.loopEndIndex != null) {
                    Button(
                        onClick = { viewModel.clearLoop() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f), contentColor = Color.White)
                    ) {
                        Text("Exit Loop")
                    }
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 10.dp)) {
                if (currentStep.actions.size > 1) {
                    MultiActionLayout(viewModel, currentStep, isCompact, isPortrait, coroutineScope)
                } else {
                    SingleActionLayout(viewModel, currentStep.actions.first(), currentStep, isCompact, isPortrait, coroutineScope)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val navButtonColors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6699),
                    contentColor = Color.White,
                    disabledContainerColor = Color.LightGray
                )

                Button(onClick = { viewModel.goFirst() }, enabled = viewModel.currentStepIndex > 0, colors = navButtonColors) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "First Step")
                }
                Button(onClick = { viewModel.goPrev() }, enabled = viewModel.currentStepIndex > 0, colors = navButtonColors) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous Step")
                }
                Button(onClick = { viewModel.goNext() }, colors = navButtonColors) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next Step")
                }
                Button(onClick = { viewModel.goLast() }, enabled = viewModel.currentStepIndex < sequence.steps.size - 1, colors = navButtonColors) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Final Step")
                }
            }

            if (!sequence.otherBroadcasts.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = Color(0xFFFF6699).copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Text("Global Audios", fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    sequence.otherBroadcasts.forEach { broadcast ->
                        val isGlobalPlaying = viewModel.playingGlobalBroadcastName == broadcast.name

                        Button(
                            onClick = { viewModel.toggleGlobalBroadcast(broadcast) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isGlobalPlaying) Color(0xFFFF3366) else Color.White,
                                contentColor = if (isGlobalPlaying) Color.White else Color(0xFFFF6699)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFFF6699)),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = if (isGlobalPlaying) Icons.Default.Close else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(broadcast.name, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MultiActionLayout(viewModel: AppViewModel, step: RobotStep, isCompact: Boolean, isPortrait: Boolean, scope: kotlinx.coroutines.CoroutineScope) {
    if (isPortrait) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            step.actions.take(3).forEach { action ->
                ActionCardWithSubAudio(viewModel, action, step, isCompact, isPortrait = true, scope, modifier = Modifier.weight(1f))
            }
        }
    } else {
        Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            step.actions.take(3).forEach { action ->
                ActionCardWithSubAudio(viewModel, action, step, isCompact, isPortrait = false, scope, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SingleActionLayout(viewModel: AppViewModel, action: RobotAction, step: RobotStep, isCompact: Boolean, isPortrait: Boolean, scope: kotlinx.coroutines.CoroutineScope) {
    Column(modifier = Modifier.fillMaxSize()) {
        ActionCardWithSubAudio(viewModel, action, step, isCompact, isPortrait, scope, modifier = Modifier.weight(1f))
    }
}

@Composable
fun ActionCardWithSubAudio(
    viewModel: AppViewModel, action: RobotAction, step: RobotStep,
    isCompact: Boolean, isPortrait: Boolean, scope: kotlinx.coroutines.CoroutineScope,
    modifier: Modifier = Modifier
) {
    val isPlaying = viewModel.playingActionName == action.name
    val scale by animateFloatAsState(if (isPlaying) 0.97f else 1.0f, label = "ScaleAnimation")

    val borderColor = if (isPlaying) Color(0xFFFF6699) else Color.Gray.copy(alpha = 0.15f)

    val isSkipped = viewModel.skippedSteps.contains(viewModel.currentStepIndex)
    val cardAlpha = if (isSkipped) 0.5f else 1.0f

    Card(
        modifier = modifier.scale(scale).clickable { viewModel.playSpecificAction(action) }.alpha(cardAlpha),
        shape = RoundedCornerShape(if (isCompact) 25.dp else 40.dp),
        border = BorderStroke(if (isPlaying) 5.dp else 2.dp, borderColor),
        elevation = CardDefaults.cardElevation(if (isPlaying) 20.dp else 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Action ${step.id} | ${step.title}",
                modifier = Modifier.padding(top = 16.dp).padding(horizontal = 10.dp),
                color = Color.Gray, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, maxLines = 2
            )
            Spacer(modifier = Modifier.weight(1f))

            if (!isPortrait && !action.imageFileName.isNullOrEmpty()) {
                val imageFile = File(File(viewModel.currentDir, "image"), action.imageFileName!!)
                if (imageFile.exists()) {
                    AsyncImage(
                        model = imageFile, contentDescription = "Image",
                        modifier = Modifier.height(if (isCompact) 120.dp else 220.dp).padding(bottom = 10.dp)
                    )
                }
            }

            Text(
                text = action.name,
                fontSize = if (isCompact) 28.sp else 50.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = if (isCompact) 32.sp else 54.sp,
                modifier = Modifier.padding(horizontal = 10.dp),
                color = if (isSkipped) Color.Gray else Color.Black
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    val subAudioText = action.subAudioName?.get(viewModel.currentLanguage) ?: "Play Sub Audio"

    if (!action.subAudioFileName?.get(viewModel.currentLanguage).isNullOrEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { viewModel.toggleSubAudio(action, scope) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (viewModel.isSubAudioPlaying) Color(0xFFFF3366) else Color(0xFFFFE6EE),
                contentColor = if (viewModel.isSubAudioPlaying) Color.White else Color(0xFFFF6699)
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(imageVector = if (viewModel.isSubAudioPlaying) Icons.Default.Close else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = subAudioText, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun LightFluidBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "Fluid")
    val phase by infiniteTransition.animateFloat(initialValue = 0f, targetValue = (2 * Math.PI).toFloat(), animationSpec = infiniteRepeatable(animation = tween(15000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "FluidPhase")
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(color = Color(0xFFFF6699).copy(alpha = 0.05f), radius = 450f, center = Offset(size.width / 2 + 200f * kotlin.math.sin(phase), size.height / 2 - 200f * kotlin.math.cos(phase)))
        drawCircle(color = Color(0xFFFF88AA).copy(alpha = 0.05f), radius = 350f, center = Offset(size.width / 2 - 200f * kotlin.math.cos(phase), size.height / 2 + 200f * kotlin.math.sin(phase)))
    }
}