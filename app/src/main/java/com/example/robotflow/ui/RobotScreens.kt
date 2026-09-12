package com.example.robotflow.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.robotflow.model.RobotAction
import com.example.robotflow.model.RobotStep
import com.example.robotflow.model.SequenceData
import com.example.robotflow.viewmodel.AppViewModel
import com.example.robotflow.viewmodel.SequenceInfo
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val context = LocalContext.current

    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {}
    }

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
                }) { Text("Delete", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { sequenceToDelete = null }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(36.dp))
                TopAppBar(title = { Text("Sequence Library", fontWeight = FontWeight.Black) })
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { launcher.launch("application/zip") },
                containerColor = Color(0xFFFF6699),
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, contentDescription = "Import ZIP") }
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
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { viewModel.openSequence(info.dir) },
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = info.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Added ${dateFormat.format(Date(info.addDate))}", fontSize = 13.sp, color = Color.Gray)
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
fun DbMeter(currentDb: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(14.dp)) {
            val cr = CornerRadius(2.dp.toPx())
            val w = size.width
            val h = size.height

            val totalBars = 40
            val barSpacing = 2.dp.toPx()
            val barWidth = (w - (totalBars - 1) * barSpacing) / totalBars

            val dbRange = 70f
            val norm = (currentDb + 60f).coerceIn(0f, dbRange)
            val fraction = norm / dbRange
            val activeBars = (fraction * totalBars).toInt()

            for (i in 0 until totalBars) {
                val isLit = i < activeBars
                val barColor = when {
                    i < totalBars * 0.7f -> Color.Green
                    i < totalBars * 0.85f -> Color.Yellow
                    else -> Color.Red
                }
                val finalColor = if (isLit) barColor else barColor.copy(alpha = 0.2f)

                drawRoundRect(
                    color = finalColor,
                    topLeft = Offset(i * (barWidth + barSpacing), 0f),
                    size = Size(barWidth, h),
                    cornerRadius = cr
                )
            }
        }
        Text("${currentDb.toInt()} dB", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
    }
}

@Composable
fun LoopRegionDialog(sequence: SequenceData, onDismiss: () -> Unit, onAdd: (Int, Int, Int) -> Unit) {
    var startIdx by remember { mutableIntStateOf(0) }
    var endIdx by remember { mutableIntStateOf(0) }
    var loopCountStr by remember { mutableStateOf("2") }
    var startExpanded by remember { mutableStateOf(false) }
    var endExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Loop Region", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Start Step:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton(
                        onClick = { startExpanded = true },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        val step = sequence.steps.getOrNull(startIdx)
                        Text("Step ${step?.id ?: ""} - ${step?.title ?: ""}", maxLines = 1)
                    }
                    DropdownMenu(expanded = startExpanded, onDismissRequest = { startExpanded = false }, modifier = Modifier.fillMaxHeight(0.5f)) {
                        sequence.steps.forEachIndexed { index, step ->
                            DropdownMenuItem(
                                text = { Text("Step ${step.id} - ${step.title}") },
                                onClick = { startIdx = index; startExpanded = false; if (endIdx < startIdx) endIdx = startIdx }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("End Step:", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton(
                        onClick = { endExpanded = true },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        val step = sequence.steps.getOrNull(endIdx)
                        Text("Step ${step?.id ?: ""} - ${step?.title ?: ""}", maxLines = 1)
                    }
                    DropdownMenu(expanded = endExpanded, onDismissRequest = { endExpanded = false }, modifier = Modifier.fillMaxHeight(0.5f)) {
                        sequence.steps.forEachIndexed { index, step ->
                            DropdownMenuItem(
                                text = { Text("Step ${step.id} - ${step.title}") },
                                onClick = { endIdx = index; endExpanded = false; if (startIdx > endIdx) startIdx = endIdx }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = loopCountStr,
                    onValueChange = { loopCountStr = it },
                    label = { Text("Target Cycles") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val count = loopCountStr.toIntOrNull() ?: 2
                if (startIdx <= endIdx && count > 0) onAdd(startIdx, endIdx, count)
            }) { Text("Confirm", color = Color(0xFFFF6699), fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = Color.Gray) }
        }
    )
}

@Composable
fun AdvancedControlPanel(viewModel: AppViewModel, sequence: SequenceData, onLaunchBgm: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Mixer & Volumes", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFFF6699))
            Spacer(modifier = Modifier.height(8.dp))
            DbMeter(viewModel.currentDb)

            Spacer(modifier = Modifier.height(16.dp))
            Text("Main Audio Volume", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Slider(
                    value = viewModel.mainVolume,
                    onValueChange = { viewModel.onMainVolumeChanged(it) },
                    valueRange = 0f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFF6699), activeTrackColor = Color(0xFFFF6699)),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Text(String.format(Locale.US, "%.1fx", viewModel.mainVolume), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Sub Audio Volume", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Slider(
                    value = viewModel.subVolume,
                    onValueChange = { viewModel.onSubVolumeChanged(it) },
                    valueRange = 0f..2.0f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFFFF6699), activeTrackColor = Color(0xFFFF6699)),
                    modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                )
                Text(String.format(Locale.US, "%.1fx", viewModel.subVolume), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }

            HorizontalDivider(color = Color(0xFFFF6699).copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("BGM Playlist", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFFF6699))
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onLaunchBgm, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Add BGM", tint = Color(0xFFFF6699))
                }
            }

            if (viewModel.bgmPlaylist.isEmpty()) {
                Text("No BGM loaded. Click + to import.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
            } else {
                val listState = rememberLazyListState()
                var draggedIndex by remember { mutableStateOf<Int?>(null) }
                var dragOffset by remember { mutableFloatStateOf(0f) }
                val itemHeightPx = with(LocalDensity.current) { 48.dp.toPx() }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.heightIn(max = 180.dp).fillMaxWidth().padding(vertical = 8.dp).background(Color(0xFFEBEBEB), RoundedCornerShape(8.dp)).padding(8.dp)
                ) {
                    itemsIndexed(viewModel.bgmPlaylist) { index, file ->
                        val isCurrent = index == viewModel.currentBgmIndex
                        val isDragged = index == draggedIndex

                        val zIndex = if (isDragged) 1f else 0f
                        val yOffset = if (isDragged) dragOffset else 0f

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .zIndex(zIndex)
                                .graphicsLayer { translationY = yOffset }
                                .background(if (isDragged) Color.White.copy(alpha = 0.8f) else Color.Transparent, RoundedCornerShape(8.dp))
                                .pointerInput(Unit) {
                                    detectVerticalDragGestures(
                                        onDragStart = { draggedIndex = index },
                                        onVerticalDrag = { change, dragAmount ->
                                            change.consume()
                                            dragOffset += dragAmount
                                            val targetIndex = (draggedIndex!! + (dragOffset / itemHeightPx).toInt()).coerceIn(0, viewModel.bgmPlaylist.size - 1)
                                            if (targetIndex != draggedIndex) {
                                                viewModel.moveBgm(draggedIndex!!, targetIndex)
                                                draggedIndex = targetIndex
                                                dragOffset %= itemHeightPx
                                            }
                                        },
                                        onDragEnd = { draggedIndex = null; dragOffset = 0f },
                                        onDragCancel = { draggedIndex = null; dragOffset = 0f }
                                    )
                                }
                        ) {
                            Icon(Icons.Default.DragHandle, contentDescription = "Drag to reorder", tint = Color.Gray, modifier = Modifier.size(24.dp).padding(end = 8.dp))

                            Row(
                                modifier = Modifier.weight(1f).fillMaxHeight().clickable { viewModel.playBgmAtIndex(index) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isCurrent && viewModel.isBgmPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = if (isCurrent) Color(0xFFFF6699) else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = file.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) Color(0xFFFF6699) else Color.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            IconButton(onClick = { viewModel.removeBgm(file) }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.toggleBgm() },
                        modifier = Modifier.size(48.dp).background(if (viewModel.isBgmPlaying) Color(0xFFFF3366) else Color(0xFFFFE6EE), RoundedCornerShape(24.dp))
                    ) {
                        Icon(if (viewModel.isBgmPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = if (viewModel.isBgmPlaying) Color.White else Color(0xFFFF6699), modifier = Modifier.size(24.dp))
                    }

                    IconButton(
                        onClick = { viewModel.nextBgm() },
                        modifier = Modifier.size(48.dp).background(Color(0xFFEBEBEB), RoundedCornerShape(24.dp))
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color(0xFFFF6699), modifier = Modifier.size(24.dp))
                    }

                    IconButton(
                        onClick = { viewModel.toggleBgmSingleLoop() },
                        modifier = Modifier.size(48.dp).background(if (viewModel.isBgmSingleLoop) Color(0xFFFFE6EE) else Color(0xFFEBEBEB), RoundedCornerShape(24.dp))
                    ) {
                        Icon(Icons.Default.RepeatOne, contentDescription = "Single Loop", tint = if (viewModel.isBgmSingleLoop) Color(0xFFFF6699) else Color.Gray, modifier = Modifier.size(20.dp))
                    }

                    IconButton(
                        onClick = { viewModel.toggleBgmShuffle() },
                        modifier = Modifier.size(48.dp).background(if (viewModel.isBgmShuffle) Color(0xFFFFE6EE) else Color(0xFFEBEBEB), RoundedCornerShape(24.dp))
                    ) {
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = if (viewModel.isBgmShuffle) Color(0xFFFF6699) else Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("BGM Volume", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Slider(
                        value = viewModel.bgmVolume,
                        onValueChange = { viewModel.onBgmVolumeChanged(it) },
                        valueRange = 0f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = Color(0xFFFF6699), activeTrackColor = Color(0xFFFF6699)),
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    )
                    Text(String.format(Locale.US, "%.0f%%", viewModel.bgmVolume * 100), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                }
            }

            HorizontalDivider(color = Color(0xFFFF6699).copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

            var showDialog by remember { mutableStateOf(false) }
            if (showDialog) {
                LoopRegionDialog(sequence, onDismiss = { showDialog = false }, onAdd = { start, end, loops ->
                    viewModel.addLoopRegion(start, end, loops)
                    showDialog = false
                })
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Loop Regions", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFFF6699), modifier = Modifier.weight(1f))
                IconButton(onClick = { showDialog = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Loop", tint = Color(0xFFFF6699), modifier = Modifier.size(24.dp))
                }
            }

            if (viewModel.loopRegions.isEmpty()) {
                Text("No loops active. Sequence runs linearly.", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            } else {
                viewModel.loopRegions.forEach { loop ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        Text(
                            text = "Step ${sequence.steps.getOrNull(loop.startIndex)?.id ?: "-"} ➔ ${sequence.steps.getOrNull(loop.endIndex)?.id ?: "-"}",
                            fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)
                        )
                        Text("Cycle: ${loop.currentLoops}/${loop.targetLoops}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = { viewModel.removeLoopRegion(loop.id) }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val sequence = viewModel.currentSequence ?: return
    val currentStep = sequence.steps.getOrNull(viewModel.currentStepIndex) ?: return

    val coroutineScope = rememberCoroutineScope()
    var expandedMenu by remember { mutableStateOf(false) }
    var showConsole by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bgmLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) viewModel.loadExternalBgms(context, uris)
    }

    if (viewModel.showCollisionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelCollision() },
            title = { Text("File Conflict", fontWeight = FontWeight.Bold) },
            text = { Text("The following files already exist in your playlist:\n\n${viewModel.collisionNames.joinToString(", ")}\n\nChoose how to proceed:", fontSize = 13.sp) },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { viewModel.cancelCollision() }) { Text("Cancel", color = Color.Gray) }
                    TextButton(onClick = { viewModel.handleCollisionDecision(context, overwrite = false) }) { Text("Skip", color = Color(0xFFFF6699)) }
                    TextButton(onClick = { viewModel.handleCollisionDecision(context, overwrite = true) }) { Text("Overwrite", color = Color.Red) }
                }
            }
        )
    }

    if (showConsole) {
        ModalBottomSheet(
            onDismissRequest = { showConsole = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            Box(modifier = Modifier.padding(bottom = 32.dp, start = 8.dp, end = 8.dp)) {
                AdvancedControlPanel(
                    viewModel = viewModel,
                    sequence = sequence,
                    onLaunchBgm = {
                        bgmLauncher.launch(arrayOf("audio/flac", "audio/x-wav", "audio/wav", "audio/mpeg", "audio/mp4", "audio/x-m4a", "audio/aac", "audio/*"))
                    }
                )
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF2F2F7))) {
        LightFluidBackground()

        Column(
            modifier = Modifier.padding(horizontal = 16.dp).padding(top = 48.dp, bottom = 16.dp).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.closeSequence() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Home", tint = Color(0xFFFF6699))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Audio Panel", fontSize = 22.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.weight(1f))

                if (!sequence.languages.isNullOrEmpty() && sequence.languages.size > 1) {
                    Button(
                        onClick = {
                            val currentIndex = sequence.languages.indexOf(viewModel.currentLanguage)
                            viewModel.currentLanguage = sequence.languages[(currentIndex + 1) % sequence.languages.size]
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFE6EE), contentColor = Color(0xFFFF6699)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp).padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Language, contentDescription = "Switch Language", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(viewModel.currentLanguage, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                IconButton(onClick = { showConsole = true }, modifier = Modifier.size(40.dp).background(Color.White, RoundedCornerShape(12.dp))) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = "Mixer Console", tint = Color(0xFFFF6699))
                }
            }

            Box {
                Button(
                    onClick = { expandedMenu = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFFFF6699)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    val statusPrefix = if (viewModel.skippedSteps.contains(viewModel.currentStepIndex)) "[Skipped] " else ""
                    Text("${statusPrefix}Action ${currentStep.id} : ${currentStep.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = "Show List")
                }

                DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }, modifier = Modifier.fillMaxHeight(0.6f)) {
                    sequence.steps.forEachIndexed { index, step ->
                        val statusLabel = buildString {
                            if (viewModel.loopRegions.any { it.startIndex == index }) append("[Start] ")
                            if (viewModel.loopRegions.any { it.endIndex == index }) append("[End] ")
                            if (viewModel.skippedSteps.contains(index)) append("[Skipped]")
                        }
                        DropdownMenuItem(
                            text = { Text(text = "Action ${step.id} - ${step.title} $statusLabel", color = if (viewModel.skippedSteps.contains(index)) Color.Gray else Color.Black) },
                            onClick = { viewModel.jumpToStep(index); expandedMenu = false }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isSkipped = viewModel.skippedSteps.contains(viewModel.currentStepIndex)
                Button(
                    onClick = { viewModel.toggleSkipStep(viewModel.currentStepIndex) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSkipped) Color.DarkGray else Color(0xFFFFE6EE),
                        contentColor = if (isSkipped) Color.White else Color(0xFFFF6699)
                    )
                ) { Text(if (isSkipped) "Recover This Step" else "Skip This Step", fontWeight = FontWeight.Bold) }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 4.dp)) {
                if (currentStep.actions.size > 1) {
                    MultiActionLayout(viewModel, currentStep, coroutineScope)
                } else {
                    SingleActionLayout(viewModel, currentStep.actions.first(), currentStep, coroutineScope)
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                val navColors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6699), contentColor = Color.White, disabledContainerColor = Color.LightGray)
                Button(onClick = { viewModel.goFirst() }, enabled = viewModel.currentStepIndex > 0, colors = navColors, modifier = Modifier.size(56.dp), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.SkipPrevious, contentDescription = "First Step", modifier = Modifier.size(28.dp)) }
                Button(onClick = { viewModel.goPrev() }, enabled = viewModel.currentStepIndex > 0, colors = navColors, modifier = Modifier.size(56.dp), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous", modifier = Modifier.size(32.dp)) }
                Button(onClick = { viewModel.goNext() }, colors = navColors, modifier = Modifier.size(56.dp), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", modifier = Modifier.size(32.dp)) }
                Button(onClick = { viewModel.goLast() }, enabled = viewModel.currentStepIndex < sequence.steps.size - 1, colors = navColors, modifier = Modifier.size(56.dp), shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.SkipNext, contentDescription = "Final Step", modifier = Modifier.size(28.dp)) }
            }

            if (!sequence.otherBroadcasts.isNullOrEmpty()) {
                HorizontalDivider(color = Color(0xFFFF6699).copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    sequence.otherBroadcasts.forEach { broadcast ->
                        val isGlobalPlaying = viewModel.playingGlobalBroadcastName == broadcast.name
                        Button(
                            onClick = { viewModel.toggleGlobalBroadcast(broadcast) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isGlobalPlaying) Color(0xFFFF3366) else Color.White, contentColor = if (isGlobalPlaying) Color.White else Color(0xFFFF6699)),
                            border = BorderStroke(1.dp, Color(0xFFFF6699)), shape = RoundedCornerShape(20.dp), modifier = Modifier.height(44.dp)
                        ) {
                            Icon(imageVector = if (isGlobalPlaying) Icons.Default.Close else Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(broadcast.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MultiActionLayout(viewModel: AppViewModel, step: RobotStep, scope: kotlinx.coroutines.CoroutineScope) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        step.actions.take(3).forEach { action -> ActionCardWithSubAudio(viewModel, action, step, true, scope, modifier = Modifier.weight(1f)) }
    }
}

@Composable
fun SingleActionLayout(viewModel: AppViewModel, action: RobotAction, step: RobotStep, scope: kotlinx.coroutines.CoroutineScope) {
    Column(modifier = Modifier.fillMaxSize()) { ActionCardWithSubAudio(viewModel, action, step, false, scope, modifier = Modifier.weight(1f)) }
}

@Composable
fun ActionCardWithSubAudio(viewModel: AppViewModel, action: RobotAction, step: RobotStep, isCompact: Boolean, scope: kotlinx.coroutines.CoroutineScope, modifier: Modifier = Modifier) {
    val isPlaying = viewModel.playingActionName == action.name
    val scale by animateFloatAsState(if (isPlaying) 0.97f else 1.0f, label = "ScaleAnimation")
    val borderColor = if (isPlaying) Color(0xFFFF6699) else Color.Gray.copy(alpha = 0.15f)
    val isSkipped = viewModel.skippedSteps.contains(viewModel.currentStepIndex)
    val cardAlpha = if (isSkipped) 0.5f else 1.0f

    Card(
        modifier = modifier.scale(scale).clickable { viewModel.playSpecificAction(action) }.alpha(cardAlpha),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(if (isPlaying) 6.dp else 2.dp, borderColor),
        elevation = CardDefaults.cardElevation(if (isPlaying) 24.dp else 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (!action.imageFileName.isNullOrEmpty()) {
                val imageFile = File(File(viewModel.currentDir, "image"), action.imageFileName!!)
                if (imageFile.exists()) {
                    AsyncImage(model = imageFile, contentDescription = "Image", modifier = Modifier.height(if (isCompact) 100.dp else 200.dp).padding(bottom = 16.dp))
                }
            }
            Text(text = action.name, fontSize = if (isCompact) 32.sp else 52.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, lineHeight = if (isCompact) 36.sp else 56.sp, modifier = Modifier.padding(horizontal = 16.dp), color = if (isSkipped) Color.Gray else Color.Black)
        }
    }

    val subAudioText = action.subAudioName?.get(viewModel.currentLanguage) ?: "Play Sub Audio"
    if (!action.subAudioFileName?.get(viewModel.currentLanguage).isNullOrEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { viewModel.toggleSubAudio(action, scope) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.isSubAudioPlaying) Color(0xFFFF3366) else Color(0xFFFFE6EE), contentColor = if (viewModel.isSubAudioPlaying) Color.White else Color(0xFFFF6699)),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(imageVector = if (viewModel.isSubAudioPlaying) Icons.Default.Close else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = subAudioText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
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