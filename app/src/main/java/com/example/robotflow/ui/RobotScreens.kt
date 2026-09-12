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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
import com.example.robotflow.viewmodel.AppTheme
import com.example.robotflow.viewmodel.AppViewModel
import com.example.robotflow.viewmodel.SequenceInfo
import com.example.robotflow.viewmodel.UILanguage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object I18n {
    fun get(key: String, lang: UILanguage): String {
        return dict[key]?.get(lang) ?: dict[key]?.get(UILanguage.EN) ?: key
    }

/*    private val dict = mapOf(
        "library" to mapOf(UILanguage.EN to "Sequence Library", UILanguage.ZH_CN to "动作序列库", UILanguage.ZH_HK to "動作序列庫", UILanguage.JA to "シーケンスライブラリ"),
        "import" to mapOf(UILanguage.EN to "Import ZIP", UILanguage.ZH_CN to "导入 ZIP", UILanguage.ZH_HK to "導入 ZIP", UILanguage.JA to "ZIPをインポート"),
        "empty" to mapOf(UILanguage.EN to "Please Import Packages First.", UILanguage.ZH_CN to "暂无动作序列，请先导入 ZIP 包", UILanguage.ZH_HK to "暫無動作序列，請先導入 ZIP 包", UILanguage.JA to "パッケージをインポートしてください"),
        "added" to mapOf(UILanguage.EN to "Added", UILanguage.ZH_CN to "添加于", UILanguage.ZH_HK to "添加於", UILanguage.JA to "追加日"),
        "delete_confirm" to mapOf(UILanguage.EN to "Confirmation", UILanguage.ZH_CN to "确认删除", UILanguage.ZH_HK to "確認刪除", UILanguage.JA to "削除の確認"),
        "delete_text" to mapOf(UILanguage.EN to "Sure to delete this package? Cannot be undone.", UILanguage.ZH_CN to "确定要删除此序列包吗？此操作不可恢复。", UILanguage.ZH_HK to "確定要刪除此序列包嗎？此操作不可恢復。", UILanguage.JA to "このパッケージを削除しますか？元に戻せません。"),
        "delete" to mapOf(UILanguage.EN to "Delete", UILanguage.ZH_CN to "删除", UILanguage.ZH_HK to "刪除", UILanguage.JA to "削除"),
        "cancel" to mapOf(UILanguage.EN to "Cancel", UILanguage.ZH_CN to "取消", UILanguage.ZH_HK to "取消", UILanguage.JA to "キャンセル"),
        "confirm" to mapOf(UILanguage.EN to "Confirm", UILanguage.ZH_CN to "确认", UILanguage.ZH_HK to "確認", UILanguage.JA to "確認"),
        "audio_panel" to mapOf(UILanguage.EN to "Audio Panel", UILanguage.ZH_CN to "音频控制台", UILanguage.ZH_HK to "音頻控制台", UILanguage.JA to "オーディオパネル"),
        "round" to mapOf(UILanguage.EN to "Round", UILanguage.ZH_CN to "第", UILanguage.ZH_HK to "第", UILanguage.JA to "ラウンド"),
        "round_suffix" to mapOf(UILanguage.EN to "", UILanguage.ZH_CN to "轮", UILanguage.ZH_HK to "輪", UILanguage.JA to ""),
        "action" to mapOf(UILanguage.EN to "Action", UILanguage.ZH_CN to "动作", UILanguage.ZH_HK to "動作", UILanguage.JA to "アクション"),
        "skipped" to mapOf(UILanguage.EN to "[Skipped]", UILanguage.ZH_CN to "[已跳过]", UILanguage.ZH_HK to "[已跳過]", UILanguage.JA to "[スキップ]"),
        "start" to mapOf(UILanguage.EN to "[Start]", UILanguage.ZH_CN to "[起点]", UILanguage.ZH_HK to "[起點]", UILanguage.JA to "[開始]"),
        "end" to mapOf(UILanguage.EN to "[End]", UILanguage.ZH_CN to "[终点]", UILanguage.ZH_HK to "[終點]", UILanguage.JA to "[終了]"),
        "recover" to mapOf(UILanguage.EN to "Recover Step", UILanguage.ZH_CN to "恢复此步", UILanguage.ZH_HK to "恢復此步", UILanguage.JA to "ステップを復元"),
        "skip" to mapOf(UILanguage.EN to "Skip Step", UILanguage.ZH_CN to "屏蔽跳过", UILanguage.ZH_HK to "屏蔽跳過", UILanguage.JA to "ステップをスキップ"),
        "global_audios" to mapOf(UILanguage.EN to "Global Audios", UILanguage.ZH_CN to "全局逗趣语音", UILanguage.ZH_HK to "全局逗趣語音", UILanguage.JA to "グローバル音声"),
        "play_sub" to mapOf(UILanguage.EN to "Play Sub Audio", UILanguage.ZH_CN to "播放附属语音", UILanguage.ZH_HK to "播放附屬語音", UILanguage.JA to "サブ音声を再生"),
        "mixer" to mapOf(UILanguage.EN to "Mixer & Volumes", UILanguage.ZH_CN to "调音台 & 音量", UILanguage.ZH_HK to "調音台 & 音量", UILanguage.JA to "ミキサーと音量"),
        "main_vol" to mapOf(UILanguage.EN to "Main Audio Volume", UILanguage.ZH_CN to "主音频音量", UILanguage.ZH_HK to "主音頻音量", UILanguage.JA to "メイン音量"),
        "sub_vol" to mapOf(UILanguage.EN to "Sub Audio Volume", UILanguage.ZH_CN to "附属音频音量", UILanguage.ZH_HK to "附屬音頻音量", UILanguage.JA to "サブ音量"),
        "bgm_vol" to mapOf(UILanguage.EN to "BGM Volume", UILanguage.ZH_CN to "BGM 音量", UILanguage.ZH_HK to "BGM 音量", UILanguage.JA to "BGM音量"),
        "bgm_playlist" to mapOf(UILanguage.EN to "BGM Playlist", UILanguage.ZH_CN to "BGM 列表", UILanguage.ZH_HK to "BGM 列表", UILanguage.JA to "BGMプレイリスト"),
        "load_bgm" to mapOf(UILanguage.EN to "Load BGM Playlist", UILanguage.ZH_CN to "导入 BGM 音乐", UILanguage.ZH_HK to "導入 BGM 音樂", UILanguage.JA to "BGMをインポート"),
        "no_bgm" to mapOf(UILanguage.EN to "No BGM loaded.", UILanguage.ZH_CN to "暂无 BGM，请点击 + 导入", UILanguage.ZH_HK to "暫無 BGM，請點擊 + 導入", UILanguage.JA to "BGMがありません"),
        "loop_regions" to mapOf(UILanguage.EN to "Loop Regions", UILanguage.ZH_CN to "循环区间控制", UILanguage.ZH_HK to "循環區間控制", UILanguage.JA to "ループ領域"),
        "no_loops" to mapOf(UILanguage.EN to "No loops active. Sequence runs linearly.", UILanguage.ZH_CN to "当前无循环区间，动作将顺序执行", UILanguage.ZH_HK to "當前無循環區間，動作將順序執行", UILanguage.JA to "ループなし。シーケンスは線形に実行されます。"),
        "cycle" to mapOf(UILanguage.EN to "Cycle", UILanguage.ZH_CN to "循环", UILanguage.ZH_HK to "循環", UILanguage.JA to "サイクル"),
        "add_loop" to mapOf(UILanguage.EN to "Add Loop Region", UILanguage.ZH_CN to "添加循环区间", UILanguage.ZH_HK to "添加循環區間", UILanguage.JA to "ループ領域を追加"),
        "start_step" to mapOf(UILanguage.EN to "Start Step:", UILanguage.ZH_CN to "起点动作:", UILanguage.ZH_HK to "起點動作:", UILanguage.JA to "開始ステップ:"),
        "end_step" to mapOf(UILanguage.EN to "End Step:", UILanguage.ZH_CN to "终点动作:", UILanguage.ZH_HK to "終點動作:", UILanguage.JA to "終了ステップ:"),
        "target_cycles" to mapOf(UILanguage.EN to "Target Cycles", UILanguage.ZH_CN to "目标循环次数", UILanguage.ZH_HK to "目標循環次數", UILanguage.JA to "目標サイクル"),
        "conflict" to mapOf(UILanguage.EN to "File Conflict", UILanguage.ZH_CN to "文件冲突", UILanguage.ZH_HK to "文件衝突", UILanguage.JA to "ファイルの競合"),
        "conflict_msg" to mapOf(UILanguage.EN to "These files already exist. Overwrite?", UILanguage.ZH_CN to "以下文件已存在，是否覆盖？", UILanguage.ZH_HK to "以下文件已存在，是否覆蓋？", UILanguage.JA to "これらのファイルは既に存在します。上書きしますか？"),
        "skip_file" to mapOf(UILanguage.EN to "Skip", UILanguage.ZH_CN to "跳过", UILanguage.ZH_HK to "跳過", UILanguage.JA to "スキップ"),
        "overwrite" to mapOf(UILanguage.EN to "Overwrite", UILanguage.ZH_CN to "覆盖", UILanguage.ZH_HK to "覆蓋", UILanguage.JA to "上書き"),
        // 新增的设置面板相关词条
        "settings" to mapOf(UILanguage.EN to "Settings", UILanguage.ZH_CN to "全局设置", UILanguage.ZH_HK to "全局設置", UILanguage.JA to "設定"),
        "ui_language" to mapOf(UILanguage.EN to "Interface Language", UILanguage.ZH_CN to "界面语言", UILanguage.ZH_HK to "介面語言", UILanguage.JA to "表示言語"),
        "theme" to mapOf(UILanguage.EN to "Theme Color", UILanguage.ZH_CN to "主题颜色", UILanguage.ZH_HK to "主題顏色", UILanguage.JA to "テーマカラー"),
        "accessibility" to mapOf(UILanguage.EN to "Accessibility", UILanguage.ZH_CN to "无障碍辅助", UILanguage.ZH_HK to "無障礙輔助", UILanguage.JA to "アクセシビリティ"),
        "high_contrast" to mapOf(UILanguage.EN to "High Contrast Mode", UILanguage.ZH_CN to "高对比度模式", UILanguage.ZH_HK to "高對比度模式", UILanguage.JA to "ハイコントラストモード")
    )
}*/

    private val dict = mapOf(
        "library" to mapOf(UILanguage.EN to "Sequence Library", UILanguage.ZH_HK to "動作序列庫", UILanguage.JA to "シーケンスライブラリ"),
        "import" to mapOf(UILanguage.EN to "Import ZIP", UILanguage.ZH_HK to "導入 ZIP", UILanguage.JA to "ZIPをインポート"),
        "empty" to mapOf(UILanguage.EN to "Please Import Packages First.", UILanguage.ZH_HK to "暫無動作序列，請先導入 ZIP 包", UILanguage.JA to "パッケージをインポートしてください"),
        "added" to mapOf(UILanguage.EN to "Added", UILanguage.ZH_HK to "添加於", UILanguage.JA to "追加日"),
        "delete_confirm" to mapOf(UILanguage.EN to "Confirmation", UILanguage.ZH_HK to "確認刪除", UILanguage.JA to "削除の確認"),
        "delete_text" to mapOf(UILanguage.EN to "Sure to delete this package? Cannot be undone.", UILanguage.ZH_HK to "確定要刪除此序列包嗎？此操作不可恢復。", UILanguage.JA to "このパッケージを削除しますか？元に戻せません。"),
        "delete" to mapOf(UILanguage.EN to "Delete", UILanguage.ZH_HK to "刪除", UILanguage.JA to "削除"),
        "cancel" to mapOf(UILanguage.EN to "Cancel", UILanguage.ZH_HK to "取消", UILanguage.JA to "キャンセル"),
        "confirm" to mapOf(UILanguage.EN to "Confirm", UILanguage.ZH_HK to "確認", UILanguage.JA to "確認"),
        "audio_panel" to mapOf(UILanguage.EN to "Audio Panel", UILanguage.ZH_HK to "音頻控制台", UILanguage.JA to "オーディオパネル"),
        "round" to mapOf(UILanguage.EN to "Round", UILanguage.ZH_HK to "第", UILanguage.JA to "ラウンド"),
        "round_suffix" to mapOf(UILanguage.EN to "", UILanguage.ZH_HK to "輪", UILanguage.JA to ""),
        "action" to mapOf(UILanguage.EN to "Action", UILanguage.ZH_HK to "動作", UILanguage.JA to "アクション"),
        "skipped" to mapOf(UILanguage.EN to "[Skipped]", UILanguage.ZH_HK to "[已跳過]", UILanguage.JA to "[スキップ]"),
        "start" to mapOf(UILanguage.EN to "[Start]", UILanguage.ZH_HK to "[起點]", UILanguage.JA to "[開始]"),
        "end" to mapOf(UILanguage.EN to "[End]", UILanguage.ZH_HK to "[終點]", UILanguage.JA to "[終了]"),
        "recover" to mapOf(UILanguage.EN to "Recover Step", UILanguage.ZH_HK to "恢復此步", UILanguage.JA to "ステップを復元"),
        "skip" to mapOf(UILanguage.EN to "Skip Step", UILanguage.ZH_HK to "屏蔽跳過", UILanguage.JA to "ステップをスキップ"),
        "global_audios" to mapOf(UILanguage.EN to "Global Audios", UILanguage.ZH_HK to "全局逗趣語音", UILanguage.JA to "グローバル音声"),
        "play_sub" to mapOf(UILanguage.EN to "Play Sub Audio", UILanguage.ZH_HK to "播放附屬語音", UILanguage.JA to "サブ音声を再生"),
        "mixer" to mapOf(UILanguage.EN to "Mixer & Volumes", UILanguage.ZH_HK to "調音台 & 音量", UILanguage.JA to "ミキサーと音量"),
        "main_vol" to mapOf(UILanguage.EN to "Main Audio Volume", UILanguage.ZH_HK to "主音頻音量", UILanguage.JA to "メイン音量"),
        "sub_vol" to mapOf(UILanguage.EN to "Sub Audio Volume", UILanguage.ZH_HK to "附屬音頻音量", UILanguage.JA to "サブ音量"),
        "bgm_vol" to mapOf(UILanguage.EN to "BGM Volume", UILanguage.ZH_HK to "BGM 音量", UILanguage.JA to "BGM音量"),
        "bgm_playlist" to mapOf(UILanguage.EN to "BGM Playlist", UILanguage.ZH_HK to "BGM 列表", UILanguage.JA to "BGMプレイリスト"),
        "load_bgm" to mapOf(UILanguage.EN to "Load BGM Playlist", UILanguage.ZH_HK to "導入 BGM 音樂", UILanguage.JA to "BGMをインポート"),
        "no_bgm" to mapOf(UILanguage.EN to "No BGM loaded.", UILanguage.ZH_HK to "暫無 BGM，請點擊 + 導入", UILanguage.JA to "BGMがありません"),
        "loop_regions" to mapOf(UILanguage.EN to "Loop Regions", UILanguage.ZH_HK to "循環區間控制", UILanguage.JA to "ループ領域"),
        "no_loops" to mapOf(UILanguage.EN to "No loops active. Sequence runs linearly.", UILanguage.ZH_HK to "當前無循環區間，動作將順序執行", UILanguage.JA to "ループなし。シーケンスは線形に実行されます。"),
        "cycle" to mapOf(UILanguage.EN to "Cycle", UILanguage.ZH_HK to "循環", UILanguage.JA to "サイクル"),
        "add_loop" to mapOf(UILanguage.EN to "Add Loop Region", UILanguage.ZH_HK to "添加循環區間", UILanguage.JA to "ループ領域を追加"),
        "start_step" to mapOf(UILanguage.EN to "Start Step:", UILanguage.ZH_HK to "起點動作:", UILanguage.JA to "開始ステップ:"),
        "end_step" to mapOf(UILanguage.EN to "End Step:", UILanguage.ZH_HK to "終點動作:", UILanguage.JA to "終了ステップ:"),
        "target_cycles" to mapOf(UILanguage.EN to "Target Cycles", UILanguage.ZH_HK to "目標循環次數", UILanguage.JA to "目標サイクル"),
        "conflict" to mapOf(UILanguage.EN to "File Conflict", UILanguage.ZH_HK to "文件衝突", UILanguage.JA to "ファイルの競合"),
        "conflict_msg" to mapOf(UILanguage.EN to "These files already exist. Overwrite?", UILanguage.ZH_HK to "以下文件已存在，是否覆蓋？", UILanguage.JA to "これらのファイルは既に存在します。上書きしますか？"),
        "skip_file" to mapOf(UILanguage.EN to "Skip", UILanguage.ZH_HK to "跳過", UILanguage.JA to "スキップ"),
        "overwrite" to mapOf(UILanguage.EN to "Overwrite", UILanguage.ZH_HK to "覆蓋", UILanguage.JA to "上書き"),
        // 新增的设置面板相关词条
        "settings" to mapOf(UILanguage.EN to "Settings", UILanguage.ZH_HK to "全局設置", UILanguage.JA to "設定"),
        "ui_language" to mapOf(UILanguage.EN to "Interface Language", UILanguage.ZH_HK to "介面語言", UILanguage.JA to "表示言語"),
        "theme" to mapOf(UILanguage.EN to "Theme Color", UILanguage.ZH_HK to "主題顏色", UILanguage.JA to "テーマカラー"),
        "accessibility" to mapOf(UILanguage.EN to "Accessibility", UILanguage.ZH_HK to "無障礙輔助", UILanguage.JA to "アクセシビリティ"),
        "high_contrast" to mapOf(UILanguage.EN to "High Contrast Mode", UILanguage.ZH_HK to "高對比度模式", UILanguage.JA to "ハイコントラストモード")
    )
}

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

// 💡 新增：独立的全局设置面板组件
@Composable
fun SettingsPanel(viewModel: AppViewModel) {
    val lang = viewModel.uiLanguage
    val theme = viewModel.appTheme

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp, top = 8.dp)) {
        Text(I18n.get("settings", lang), fontSize = 24.sp, fontWeight = FontWeight.Black, color = if (theme == AppTheme.HIGH_CONTRAST) Color.Black else theme.primary)
        Spacer(modifier = Modifier.height(24.dp))

        // 1. UI 语言选项
        Text(I18n.get("ui_language", lang), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UILanguage.values().forEach { l ->
                val label = when(l) {
                    UILanguage.EN -> "English"
                    UILanguage.ZH_HK -> "繁體中文"
                    // UILanguage.ZH_CN -> "简体中文"
                    UILanguage.JA -> "日本語"
                    else -> l.name
                }
                Button(
                    onClick = { viewModel.uiLanguage = l },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (lang == l) theme.primary else theme.light,
                        contentColor = if (lang == l) Color.White else theme.primary
                    )
                ) { Text(label, fontWeight = FontWeight.Bold) }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 2. 主题色选项
        Text(I18n.get("theme", lang), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AppTheme.values().filter { it != AppTheme.HIGH_CONTRAST }.forEach { t ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(t.primary, RoundedCornerShape(24.dp))
                        .border(if (theme == t) 4.dp else 0.dp, Color.Black.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                        .clickable { viewModel.appTheme = t }
                        .semantics { contentDescription = "Theme ${t.name}" }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. 无障碍选项 (高对比度模式)
        Text(I18n.get("accessibility", lang), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
            viewModel.appTheme = if (theme == AppTheme.HIGH_CONTRAST) AppTheme.PINK else AppTheme.HIGH_CONTRAST
        }.padding(vertical = 8.dp)) {
            Text(I18n.get("high_contrast", lang), modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Switch(
                checked = theme == AppTheme.HIGH_CONTRAST,
                onCheckedChange = { isChecked ->
                    viewModel.appTheme = if (isChecked) AppTheme.HIGH_CONTRAST else AppTheme.PINK
                },
                colors = SwitchDefaults.colors(checkedThumbColor = theme.primary, checkedTrackColor = theme.light)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeListScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val theme = viewModel.appTheme
    val lang = viewModel.uiLanguage
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.importZipFile(context, uri)
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    var sequenceToDelete by remember { mutableStateOf<SequenceInfo?>(null) }

    // 💡 控制主页设置面板的显示状态
    var showSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showSettings) {
        ModalBottomSheet(
            onDismissRequest = { showSettings = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            SettingsPanel(viewModel)
        }
    }

    if (sequenceToDelete != null) {
        AlertDialog(
            onDismissRequest = { sequenceToDelete = null },
            title = { Text(I18n.get("delete_confirm", lang), fontWeight = FontWeight.Bold) },
            text = { Text(I18n.get("delete_text", lang)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteSequence(sequenceToDelete!!); sequenceToDelete = null }) {
                    Text(I18n.get("delete", lang), color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sequenceToDelete = null }) { Text(I18n.get("cancel", lang), color = Color.Gray) }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(36.dp))
                TopAppBar(
                    title = { Text(I18n.get("library", lang), fontWeight = FontWeight.Black) },
                    actions = {
                        // 💡 主页右上角的统一设置按钮
                        IconButton(onClick = { showSettings = true }, modifier = Modifier.semantics { contentDescription = "Settings" }) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = theme.primary)
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { launcher.launch("application/zip") },
                containerColor = theme.primary,
                contentColor = Color.White,
                modifier = Modifier.semantics { contentDescription = I18n.get("import", lang) }
            ) { Icon(Icons.Default.Add, contentDescription = null) }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            if (viewModel.importedSequences.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                    Text(I18n.get("empty", lang), color = Color.Gray)
                }
            }
            viewModel.importedSequences.forEach { info ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { viewModel.openSequence(info.dir) }
                        .semantics { contentDescription = "Open ${info.name}" },
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = info.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (theme == AppTheme.HIGH_CONTRAST) Color.Black else Color.Unspecified)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "${I18n.get("added", lang)} ${dateFormat.format(Date(info.addDate))}", fontSize = 13.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { sequenceToDelete = info }, modifier = Modifier.semantics { contentDescription = "Delete ${info.name}" }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f))
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
    Column(modifier = Modifier.fillMaxWidth().semantics { contentDescription = "Volume Meter: ${currentDb.toInt()} decibels" }) {
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
                drawRoundRect(color = finalColor, topLeft = Offset(i * (barWidth + barSpacing), 0f), size = Size(barWidth, h), cornerRadius = cr)
            }
        }
        Text("${currentDb.toInt()} dB", fontSize = 10.sp, color = Color.Gray, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
    }
}

@Composable
fun LoopRegionDialog(viewModel: AppViewModel, sequence: SequenceData, onDismiss: () -> Unit, onAdd: (Int, Int, Int) -> Unit) {
    val lang = viewModel.uiLanguage
    val theme = viewModel.appTheme
    var startIdx by remember { mutableIntStateOf(0) }
    var endIdx by remember { mutableIntStateOf(0) }
    var loopCountStr by remember { mutableStateOf("2") }
    var startExpanded by remember { mutableStateOf(false) }
    var endExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(I18n.get("add_loop", lang), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(I18n.get("start_step", lang), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton(onClick = { startExpanded = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)) {
                        val step = sequence.steps.getOrNull(startIdx)
                        Text("Step ${step?.id ?: ""} - ${step?.title ?: ""}", maxLines = 1)
                    }
                    DropdownMenu(expanded = startExpanded, onDismissRequest = { startExpanded = false }, modifier = Modifier.fillMaxHeight(0.5f)) {
                        sequence.steps.forEachIndexed { index, step ->
                            DropdownMenuItem(text = { Text("Step ${step.id} - ${step.title}") }, onClick = { startIdx = index; startExpanded = false; if (endIdx < startIdx) endIdx = startIdx })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(I18n.get("end_step", lang), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Box {
                    OutlinedButton(onClick = { endExpanded = true }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)) {
                        val step = sequence.steps.getOrNull(endIdx)
                        Text("Step ${step?.id ?: ""} - ${step?.title ?: ""}", maxLines = 1)
                    }
                    DropdownMenu(expanded = endExpanded, onDismissRequest = { endExpanded = false }, modifier = Modifier.fillMaxHeight(0.5f)) {
                        sequence.steps.forEachIndexed { index, step ->
                            DropdownMenuItem(text = { Text("Step ${step.id} - ${step.title}") }, onClick = { endIdx = index; endExpanded = false; if (startIdx > endIdx) startIdx = endIdx })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(value = loopCountStr, onValueChange = { loopCountStr = it }, label = { Text(I18n.get("target_cycles", lang)) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = { val count = loopCountStr.toIntOrNull() ?: 2; if (startIdx <= endIdx && count > 0) onAdd(startIdx, endIdx, count) }) {
                Text(I18n.get("confirm", lang), color = theme.primary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(I18n.get("cancel", lang), color = Color.Gray) }
        }
    )
}

@Composable
fun AdvancedControlPanel(viewModel: AppViewModel, sequence: SequenceData, onLaunchBgm: () -> Unit) {
    val theme = viewModel.appTheme
    val lang = viewModel.uiLanguage
    val context = LocalContext.current

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(I18n.get("mixer", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = theme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            DbMeter(viewModel.currentDb)
            Spacer(modifier = Modifier.height(16.dp))
            Text(I18n.get("main_vol", lang), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Slider(value = viewModel.mainVolume, onValueChange = { viewModel.onMainVolumeChanged(it) }, valueRange = 0f..2.0f, colors = SliderDefaults.colors(thumbColor = theme.primary, activeTrackColor = theme.primary), modifier = Modifier.weight(1f).padding(horizontal = 8.dp).semantics { contentDescription = "Adjust Main Volume" })
                Text(String.format(Locale.US, "%.1fx", viewModel.mainVolume), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(I18n.get("sub_vol", lang), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Slider(value = viewModel.subVolume, onValueChange = { viewModel.onSubVolumeChanged(it) }, valueRange = 0f..2.0f, colors = SliderDefaults.colors(thumbColor = theme.primary, activeTrackColor = theme.primary), modifier = Modifier.weight(1f).padding(horizontal = 8.dp).semantics { contentDescription = "Adjust Sub Volume" })
                Text(String.format(Locale.US, "%.1fx", viewModel.subVolume), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
            }
            HorizontalDivider(color = theme.primary.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(I18n.get("bgm_playlist", lang), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = theme.primary)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onLaunchBgm, modifier = Modifier.size(32.dp).semantics { contentDescription = "Add BGM" }) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = theme.primary)
                }
            }

            if (viewModel.bgmPlaylist.isEmpty()) {
                Text(I18n.get("no_bgm", lang), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
            } else {
                val listState = rememberLazyListState()
                var draggedIndex by remember { mutableStateOf<Int?>(null) }
                var dragOffset by remember { mutableFloatStateOf(0f) }
                val itemHeightPx = with(LocalDensity.current) { 48.dp.toPx() }

                LazyColumn(state = listState, modifier = Modifier.heightIn(max = 180.dp).fillMaxWidth().padding(vertical = 8.dp).background(Color(0xFFEBEBEB), RoundedCornerShape(8.dp)).padding(8.dp)) {
                    itemsIndexed(viewModel.bgmPlaylist) { index, file ->
                        val isCurrent = index == viewModel.currentBgmIndex
                        val isDragged = index == draggedIndex
                        val zIndex = if (isDragged) 1f else 0f
                        val yOffset = if (isDragged) dragOffset else 0f

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().height(48.dp).zIndex(zIndex).graphicsLayer { translationY = yOffset }
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
                                }.semantics { contentDescription = "Track ${file.name}" }
                        ) {
                            Icon(Icons.Default.DragHandle, contentDescription = "Drag to reorder", tint = Color.Gray, modifier = Modifier.size(24.dp).padding(end = 8.dp))
                            Row(modifier = Modifier.weight(1f).fillMaxHeight().clickable { viewModel.playBgmAtIndex(index) }, verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = if (isCurrent && viewModel.isBgmPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote, contentDescription = null, tint = if (isCurrent) theme.primary else Color.Gray, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = file.name, fontSize = 12.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal, color = if (isCurrent) theme.primary else Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { viewModel.removeBgm(file) }, modifier = Modifier.size(24.dp).semantics { contentDescription = "Delete ${file.name}" }) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.toggleBgm() }, modifier = Modifier.size(48.dp).background(if (viewModel.isBgmPlaying) theme.dark else theme.light, RoundedCornerShape(24.dp)).semantics { contentDescription = "Play or Pause BGM" }) {
                        Icon(if (viewModel.isBgmPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = if (viewModel.isBgmPlaying) Color.White else theme.primary, modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = { viewModel.nextBgm() }, modifier = Modifier.size(48.dp).background(Color(0xFFEBEBEB), RoundedCornerShape(24.dp)).semantics { contentDescription = "Next BGM" }) {
                        Icon(Icons.Default.SkipNext, contentDescription = null, tint = theme.primary, modifier = Modifier.size(24.dp))
                    }
                    IconButton(onClick = { viewModel.toggleBgmSingleLoop() }, modifier = Modifier.size(48.dp).background(if (viewModel.isBgmSingleLoop) theme.light else Color(0xFFEBEBEB), RoundedCornerShape(24.dp)).semantics { contentDescription = "Toggle Single Loop" }) {
                        Icon(Icons.Default.RepeatOne, contentDescription = null, tint = if (viewModel.isBgmSingleLoop) theme.primary else Color.Gray, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { viewModel.toggleBgmShuffle() }, modifier = Modifier.size(48.dp).background(if (viewModel.isBgmShuffle) theme.light else Color(0xFFEBEBEB), RoundedCornerShape(24.dp)).semantics { contentDescription = "Toggle Shuffle" }) {
                        Icon(Icons.Default.Shuffle, contentDescription = null, tint = if (viewModel.isBgmShuffle) theme.primary else Color.Gray, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(I18n.get("bgm_vol", lang), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Slider(value = viewModel.bgmVolume, onValueChange = { viewModel.onBgmVolumeChanged(it) }, valueRange = 0f..1.0f, colors = SliderDefaults.colors(thumbColor = theme.primary, activeTrackColor = theme.primary), modifier = Modifier.weight(1f).padding(horizontal = 8.dp).semantics { contentDescription = "Adjust BGM Volume" })
                    Text(String.format(Locale.US, "%.0f%%", viewModel.bgmVolume * 100), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.width(36.dp), textAlign = TextAlign.End)
                }
            }

            HorizontalDivider(color = theme.primary.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))

            var showDialog by remember { mutableStateOf(false) }
            if (showDialog) {
                LoopRegionDialog(viewModel, sequence, onDismiss = { showDialog = false }, onAdd = { start, end, loops -> viewModel.addLoopRegion(start, end, loops); showDialog = false })
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(I18n.get("loop_regions", lang), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = theme.primary, modifier = Modifier.weight(1f))
                IconButton(onClick = { showDialog = true }, modifier = Modifier.size(32.dp).semantics { contentDescription = I18n.get("add_loop", lang) }) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = theme.primary, modifier = Modifier.size(24.dp))
                }
            }

            if (viewModel.loopRegions.isEmpty()) {
                Text(I18n.get("no_loops", lang), fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
            } else {
                viewModel.loopRegions.forEach { loop ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp).semantics { contentDescription = "Loop from step ${loop.startIndex} to ${loop.endIndex}" }) {
                        Text(text = "Step ${sequence.steps.getOrNull(loop.startIndex)?.id ?: "-"} ➔ ${sequence.steps.getOrNull(loop.endIndex)?.id ?: "-"}", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${I18n.get("cycle", lang)}: ${loop.currentLoops}/${loop.targetLoops}", fontSize = 12.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = { viewModel.removeLoopRegion(loop.id) }, modifier = Modifier.size(24.dp).semantics { contentDescription = "Delete Loop" }) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
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
    val theme = viewModel.appTheme
    val lang = viewModel.uiLanguage
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
            title = { Text(I18n.get("conflict", lang), fontWeight = FontWeight.Bold) },
            text = { Text("${I18n.get("conflict_msg", lang)}\n\n${viewModel.collisionNames.joinToString(", ")}", fontSize = 13.sp) },
            confirmButton = {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { viewModel.cancelCollision() }) { Text(I18n.get("cancel", lang), color = Color.Gray) }
                    TextButton(onClick = { viewModel.handleCollisionDecision(context, overwrite = false) }) { Text(I18n.get("skip_file", lang), color = theme.primary) }
                    TextButton(onClick = { viewModel.handleCollisionDecision(context, overwrite = true) }) { Text(I18n.get("overwrite", lang), color = Color.Red) }
                }
            }
        )
    }

    if (showConsole) {
        ModalBottomSheet(onDismissRequest = { showConsole = false }, sheetState = sheetState, containerColor = Color.White) {
            Box(modifier = Modifier.padding(bottom = 32.dp, start = 8.dp, end = 8.dp)) {
                AdvancedControlPanel(viewModel = viewModel, sequence = sequence, onLaunchBgm = { bgmLauncher.launch(arrayOf("audio/*")) })
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF2F2F7))) {
        LightFluidBackground(theme)

        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(top = 48.dp, bottom = 16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.closeSequence() }, modifier = Modifier.semantics { contentDescription = "Go Back" }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = theme.primary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(I18n.get("audio_panel", lang), fontSize = 22.sp, fontWeight = FontWeight.Black, color = if (theme == AppTheme.HIGH_CONTRAST) Color.Black else Color.Unspecified)
                Spacer(modifier = Modifier.weight(1f))

                if (!sequence.languages.isNullOrEmpty() && sequence.languages.size > 1) {
                    Button(
                        onClick = {
                            val currentIndex = sequence.languages.indexOf(viewModel.currentLanguage)
                            viewModel.currentLanguage = sequence.languages[(currentIndex + 1) % sequence.languages.size]
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = theme.light, contentColor = theme.primary),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp).padding(end = 8.dp).semantics { contentDescription = "Switch Voice Language" }
                    ) {
                        Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(viewModel.currentLanguage, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                IconButton(onClick = { showConsole = true }, modifier = Modifier.size(40.dp).background(Color.White, RoundedCornerShape(12.dp)).semantics { contentDescription = "Open Mixer Settings" }) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = theme.primary)
                }
            }

            Box {
                Button(
                    onClick = { expandedMenu = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = theme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    modifier = Modifier.height(48.dp).semantics { contentDescription = "Step Selector" }
                ) {
                    val statusPrefix = if (viewModel.skippedSteps.contains(viewModel.currentStepIndex)) I18n.get("skipped", lang) + " " else ""
                    Text("$statusPrefix${I18n.get("action", lang)} ${currentStep.id} : ${currentStep.title}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }, modifier = Modifier.fillMaxHeight(0.6f)) {
                    sequence.steps.forEachIndexed { index, step ->
                        val statusLabel = buildString {
                            if (viewModel.loopRegions.any { it.startIndex == index }) append("${I18n.get("start", lang)} ")
                            if (viewModel.loopRegions.any { it.endIndex == index }) append("${I18n.get("end", lang)} ")
                            if (viewModel.skippedSteps.contains(index)) append(I18n.get("skipped", lang))
                        }
                        DropdownMenuItem(
                            text = { Text("${I18n.get("action", lang)} ${step.id} - ${step.title} $statusLabel", color = if (viewModel.skippedSteps.contains(index)) Color.Gray else Color.Black) },
                            onClick = { viewModel.jumpToStep(index); expandedMenu = false }
                        )
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                val isSkipped = viewModel.skippedSteps.contains(viewModel.currentStepIndex)
                Button(
                    onClick = { viewModel.toggleSkipStep(viewModel.currentStepIndex) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSkipped) Color.DarkGray else theme.light, contentColor = if (isSkipped) Color.White else theme.primary),
                    modifier = Modifier.semantics { contentDescription = "Toggle Skip Status" }
                ) { Text(if (isSkipped) I18n.get("recover", lang) else I18n.get("skip", lang), fontWeight = FontWeight.Bold) }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth().padding(vertical = 4.dp)) {
                if (currentStep.actions.size > 1) {
                    MultiActionLayout(viewModel, currentStep, coroutineScope)
                } else {
                    SingleActionLayout(viewModel, currentStep.actions.first(), currentStep, coroutineScope)
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                val navColors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = Color.White, disabledContainerColor = Color.LightGray)
                Button(onClick = { viewModel.goFirst() }, enabled = viewModel.currentStepIndex > 0, colors = navColors, modifier = Modifier.size(56.dp).semantics { contentDescription = "First Step" }, shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.SkipPrevious, contentDescription = null, modifier = Modifier.size(28.dp)) }
                Button(onClick = { viewModel.goPrev() }, enabled = viewModel.currentStepIndex > 0, colors = navColors, modifier = Modifier.size(56.dp).semantics { contentDescription = "Previous Step" }, shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, modifier = Modifier.size(32.dp)) }
                Button(onClick = { viewModel.goNext() }, colors = navColors, modifier = Modifier.size(56.dp).semantics { contentDescription = "Next Step" }, shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(32.dp)) }
                Button(onClick = { viewModel.goLast() }, enabled = viewModel.currentStepIndex < sequence.steps.size - 1, colors = navColors, modifier = Modifier.size(56.dp).semantics { contentDescription = "Last Step" }, shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(28.dp)) }
            }

            if (!sequence.otherBroadcasts.isNullOrEmpty()) {
                HorizontalDivider(color = theme.primary.copy(alpha = 0.2f), thickness = 1.dp, modifier = Modifier.padding(vertical = 8.dp))
                Text(I18n.get("global_audios", lang), fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    sequence.otherBroadcasts.forEach { broadcast ->
                        val isGlobalPlaying = viewModel.playingGlobalBroadcastName == broadcast.name
                        Button(
                            onClick = { viewModel.toggleGlobalBroadcast(broadcast) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isGlobalPlaying) theme.dark else Color.White, contentColor = if (isGlobalPlaying) Color.White else theme.primary),
                            border = BorderStroke(1.dp, theme.primary), shape = RoundedCornerShape(20.dp), modifier = Modifier.height(44.dp).semantics { contentDescription = "Play global audio ${broadcast.name}" }
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
    val theme = viewModel.appTheme
    val lang = viewModel.uiLanguage
    val isPlaying = viewModel.playingActionName == action.name
    val scale by animateFloatAsState(if (isPlaying) 0.97f else 1.0f, label = "ScaleAnimation")
    val borderColor = if (isPlaying) theme.primary else Color.Gray.copy(alpha = 0.15f)
    val isSkipped = viewModel.skippedSteps.contains(viewModel.currentStepIndex)
    val cardAlpha = if (isSkipped) 0.5f else 1.0f

    Card(
        modifier = modifier.scale(scale).clickable { viewModel.playSpecificAction(action) }.alpha(cardAlpha).semantics { contentDescription = "Play Action ${action.name}" },
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(if (isPlaying) 6.dp else 2.dp, borderColor),
        elevation = CardDefaults.cardElevation(if (isPlaying) 24.dp else 6.dp),
        colors = CardDefaults.cardColors(containerColor = if (theme == AppTheme.HIGH_CONTRAST && isPlaying) theme.light else Color.White)
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (!action.imageFileName.isNullOrEmpty()) {
                val imageFile = File(File(viewModel.currentDir, "image"), action.imageFileName!!)
                if (imageFile.exists()) {
                    AsyncImage(model = imageFile, contentDescription = null, modifier = Modifier.height(if (isCompact) 100.dp else 200.dp).padding(bottom = 16.dp))
                }
            }
            Text(text = action.name, fontSize = if (isCompact) 32.sp else 52.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, lineHeight = if (isCompact) 36.sp else 56.sp, modifier = Modifier.padding(horizontal = 16.dp), color = if (isSkipped) Color.Gray else (if(theme == AppTheme.HIGH_CONTRAST) Color.Black else Color.Unspecified))
        }
    }

    val subAudioText = action.subAudioName?.get(viewModel.currentLanguage) ?: I18n.get("play_sub", lang)
    if (!action.subAudioFileName?.get(viewModel.currentLanguage).isNullOrEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = { viewModel.toggleSubAudio(action, scope) },
            modifier = Modifier.fillMaxWidth().height(56.dp).semantics { contentDescription = subAudioText },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (viewModel.isSubAudioPlaying) theme.dark else theme.light, contentColor = if (viewModel.isSubAudioPlaying) Color.White else theme.primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(imageVector = if (viewModel.isSubAudioPlaying) Icons.Default.Close else Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = subAudioText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}

@Composable
fun LightFluidBackground(theme: AppTheme) {
    val infiniteTransition = rememberInfiniteTransition(label = "Fluid")
    val phase by infiniteTransition.animateFloat(initialValue = 0f, targetValue = (2 * Math.PI).toFloat(), animationSpec = infiniteRepeatable(animation = tween(15000, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "FluidPhase")
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(color = theme.primary.copy(alpha = 0.05f), radius = 450f, center = Offset(size.width / 2 + 200f * kotlin.math.sin(phase), size.height / 2 - 200f * kotlin.math.cos(phase)))
        drawCircle(color = theme.dark.copy(alpha = 0.05f), radius = 350f, center = Offset(size.width / 2 - 200f * kotlin.math.cos(phase), size.height / 2 + 200f * kotlin.math.sin(phase)))
    }
}