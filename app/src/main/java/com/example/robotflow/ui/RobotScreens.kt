package com.example.robotflow.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.robotflow.viewmodel.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object AboutContent {
    fun getIntro(lang: UILanguage): String = when (lang) {
        UILanguage.ZH_TW -> "這是一款基於原生Android開發的軟硬件互動中控系統。系統支援匯入設定檔以動態生成操作介面，內置專業級多聲道混音器（支援主副音訊、背景音樂等多種播放模式，以及自訂語音的獨立控制與混音）；並可透過Type-C OTG直連底層序列埠驅動程式，在播放特定步驟的瞬間向ESP32精準發送結構化控制訊號。結合實時分貝計、非線性循環控制、手勢緊急防誤觸機制、四語國際化支援，以及具備對比度檢測的自訂持久化主題引擎，為複雜的線下機械人實機展示、互動藝術裝置等場景，提供了一套零延遲、全掌控且具備高質感的現場操作完整解決方案。"
        UILanguage.EN -> "This is a hardware-software interactive central control system built natively on Android. The system supports dynamically generating operation interfaces by importing configuration profiles, and features a built-in professional-grade multi-channel mixer (supporting independent control and mixing for primary/secondary audio, multiple BGM playback modes, and custom voice clips). It can directly connect to low-level serial port drivers via Type-C OTG to precisely transmit structured control signals to ESP32 microcontrollers the moment specific steps are triggered. Combining real-time decibel meters, non-linear loop control, emergency anti-misoperation gesture mechanisms, 4-language internationalization support, and a custom persistent theme engine with contrast detection, it delivers a zero-latency, full-control, and refined end-to-end onsite operation solution for complex offline robot demonstrations and interactive art installations."
        UILanguage.JA -> "本システムは、ネイティブAndroidをベースに開発された、ハードウェア・ソフトウェア連携の集中制御システムです。プロファイル（設定ファイル）のインポートによる操作画面の動的生成に対応し、プロ仕様のマルチチャンネルミキサー（メイン/サブ音声、BGMの各種再生モード、カスタム音声の個別制御およびミキシング）を内蔵しています。また、Type-C OTGを介して低レイヤーのシリアルポートドライバと直接通信し、特定ステップの実行時にESP32へ構造化制御信号を高精度に送信可能です。リアルタイムデシベルメーター、非線形ループ制御、誤操作防止ジェスチャー機能、4言語の多言語対応、コントラスト検出機能を備えたカスタム永続化テーマエンジンを組み合わせることで、オフラインにおける複雑な実機ロボットのデモンストレーションやインタラクティブアート展示などの現場において、低遅延・完全制御・高品位な操作環境を提供します。"
        UILanguage.FR -> "Il s'agit d'un système de contrôle centralisé et d'interaction matérielle-logicielle développé nativement sous Android. Le système permet de générer dynamiquement l'interface utilisateur en important des profils de configuration. Il intègre une table de mixage multicanale de niveau professionnel (prenant en charge le contrôle indépendant et le mixage des pistes audio principales/secondaires, plusieurs modes de lecture pour la musique de fond BGM, ainsi que des voix personnalisées). Il peut se connecter directement aux pilotes de port série de bas niveau via Type-C OTG afin de transmettre avec précision des signaux de commande structurés vers l'ESP32 au moment précis où des étapes spécifiques sont déclenchées. Associant un décibelmètre en temps réel, un contrôle de boucle non linéaire, un mécanisme gestuel d'urgence contre les déclenchements accidentels, une prise en charge multilingue en quatre langues et un moteur de thèmes personnalisés persistants avec détection de contraste, il offre une solution d'exploitation sur site complète, sans latence, entièrement maîtrisée et haut de gamme pour les démonstrations complexes de robots en conditions réelles et les installations artistiques interactives hors ligne."
    }

    fun getChangelog(lang: UILanguage): String = when (lang) {
        UILanguage.ZH_TW -> """
            v1.0.3
            • 首發版本正式推出！本版本新增了基礎設定檔匯入與基本控制功能，內容包括：支援依既定順序循環播放動作庫，並透過「上一步」、「下一步」、「首步驟」、「最後步驟」四個按鈕以及中央主按鈕進行序列控制；支援附屬音訊功能，可在特定場景下播放或暫停音樂及其他語音；同時支援點擊上方按鈕開啟下拉式選單，以便快速跳轉至指定動作。

            v1.1.2
            • 新增回合紀錄及顯示功能；同時修復已知問題，全面提升介面操作與使用者體驗。

            v1.2.1-beta
            • 新增全域音訊功能，支援識別設定檔中的全域音訊並匯入至介面，方便使用者在整個動作流程中隨時手動觸發播放。

            v1.2.3-beta
            • 主頁現已支援顯示新增時間與設定檔名稱，並可直接刪除不需要的設定檔。

            v1.3.2-beta
            • 新增自訂起點與終點設定功能，且允許使用者能靈活跳過不需要的動作。

            v2.0.1-beta
            • 配合序列編輯器全面支援多語言，使用者可點擊介面右上角的語言切換按鈕進行即時切換。
            • 設定檔相容性提示：本版本起不再支援舊版設定檔，請使用包含languages欄位的新版設定檔。

            v2.1.8a
            • 新增控制台功能：支援加入背景音樂（BGM）並獨立播放。
            • 音量控制與監控：支援分別調節主音訊與背景音樂音量，並可透過分貝計即時監察音訊狀態。
            • 循環設定整合：將自訂循環區域功能整合至控制台中。
            • 異形螢幕適配：針對瀏海螢幕、動態島等機型進行介面最佳化，主動預留頂部安全區域以防止畫面被遮擋。

            v2.3.2c
            • 最佳化使用者介面，將控制台改為獨立圖層以釋放畫面空間，帶來更流暢的操作體驗。

            v2.4.1
            • 升級背景音樂播放功能，現已支援新增多首曲目；預設以清單循環模式播放，並支援單曲循環與隨機播放。

            v2.5.3
            • 支援多語言介面（目前支援繁體中文、英文及日文）。
            • 支援自訂更換介面色彩主題。
            • 完善無障礙體驗：提供高對比度模式以照顧視障人士需求，並新增無障礙標籤以支援語音導覽與螢幕閱讀器。

            v3.0.1
            • 支援向ESP32發射端傳輸指定格式的資料，以便接收端連接的裝置即時顯示所觸發的動作。

            v4.0.3b
            • 個人化主題色彩：支援透過RGB滑桿自訂介面色彩；當色彩對比度不足可能影響文字閱讀時，系統會自動發出警告提示。
            • 全域音訊管理升級：支援在全域音訊中新增項目，具備持久化儲存及刪除功能；並支援透過「長按並拖曳」快速調整播放順序。
            • 頂部導覽列：啟用「顯示頂部導覽列」選項後，畫面頂部將額外載入一組操作按鈕，同時加大核心控制區域的按鍵間距，更便於單手操作或非視覺依賴情境下的盲操。
            • 分支任務獨立停用：針對具備多個分支的動作，點擊動作卡片右上方的「眼睛圖示」即可啟用或停用該分支；停用後點擊該項目將不會觸發任何執行動作。
            • 手勢防誤觸即時取消機制：新增滑動取消手勢。若不慎誤觸按鈕，在語音實際播放前，只需將手指迅速向觸發區域外圍移開，即可即時釋放觸發狀態以防誤動作。
            • 背景音樂播放模式擴充：支援「單曲播放」（單曲播放完後自動停止，不重複亦不自動切下一首）。

            v4.0.4
            • 修復多語言顯示問題，並進一步最佳化使用者介面，提升整體操作體驗。
            • 增加關於界面。
            
            v4.0.6
            • 更新並優化咗用戶界面同相關操作，仲更新咗對虛擬機設備嘅支援。
            
            v4.0.7
            • 更新並優化咗用戶界面同互動體驗。
            
            v4.0.8
            • 更新咗自定義嘅互動模式選項，畀咗用戶更多自主同自訂嘅空間。
            • 新增導入ZIP檔校驗機制，並為大型文件加入進度提示，同時修復BGM列表滑動衝突等體驗問題。
            
            v4.1.1
            • 添加全域音頻時，若與包內已有或已添加嘅音頻重名，將會攔截操作。
            • 禁止屏蔽全部步驟序列，觸發時彈出提示。
            • 喺快速跳轉下拉菜單右側新增「恢復所有序列」功能。
            • 用戶界面同用戶體驗優化。
            • 大文件導入狀態提醒：導入體積超過20MB嘅壓縮包時，會自動彈出非同步加載提示並顯示加載圈。
            • 嚴格校驗導入文件格式，非規範zip或其他格式文件將統一攔截並提示導入正確嘅壓縮包（舊版PC端生成嘅壓縮包唔支援）。
            • 喺解包讀取前遍歷文件結構校驗完整性；若檢測到文件損壞或缺失，會自動清理已導入嘅殘留緩存並提示用戶重新導入。
            • 修復控制台導入多個BGM時列表無法向下滑動瀏覽、僅能觸發拖拽排序嘅滾動衝突問題。
            
            v4.1.2
            • 增加咗文件校驗，當包入面已聲明嘅文件唔存在時，就會當成包不完整，並會清除緩存同埋要求重新導入。
        """.trimIndent()
        UILanguage.EN -> """
            v1.0.3
            • Initial release! This version introduces basic profile importing and core control capabilities, including: sequential cycling through the action library; sequence control via "Previous", "Next", "First Step", "Last Step", and the central main button; auxiliary audio support for playing and pausing background music or other voice clips in specific scenarios; and top-button dropdown menu access for quick navigation to specified actions.

            v1.1.2
            • Added round logging and display features; resolved known issues to enhance interface interactions and overall user experience.

            v1.2.1-beta
            • Added Global Audio support: automatically detects and imports global audio from configuration profiles into the interface, allowing users to trigger playback manually at any point during the action sequence.

            v1.2.3-beta
            • The home screen now displays creation timestamps and profile names, and supports direct deletion of unwanted profiles.

            v1.3.2-beta
            • Added customizable start and end point settings, allowing users to flexibly skip unnecessary actions.

            v2.0.1-beta
            • Added full multilingual support in tandem with the Sequence Editor; users can switch languages in real-time via the button at the top-right corner.
            • Profile Compatibility Notice: Legacy profiles are no longer supported starting from this version; please use the updated profile format containing the `languages` field.

            v2.1.8a
            • Added Control Panel: supports adding and independently playing background music (BGM).
            • Volume Control & Monitoring: supports independent volume adjustment for primary audio and BGM, along with real-time audio level monitoring via a decibel meter.
            • Loop Settings Integration: merged custom loop range configuration into the Control Panel.
            • Cutout Screen Adaptation: optimized UI for notch and Dynamic Island displays by reserving top safe area margins to prevent interface clipping.

            v2.3.2c
            • Optimized user interface by converting the Control Panel into an independent overlay layer, freeing up screen space and delivering a smoother operating experience.

            v2.4.1
            • Upgraded BGM playback features: now supports adding multiple audio tracks, defaults to playlist loop mode, and includes single-track loop and shuffle modes.

            v2.5.3
            • Added multilingual interface support (currently supporting Traditional Chinese, English, and Japanese).
            • Added support for custom interface color themes.
            • Enhanced accessibility experience: added High Contrast mode for visually impaired users and introduced accessibility labels to support voice navigation and screen readers.

            v3.0.1
            • Added support for transmitting formatted data to ESP32 transmitters, enabling connected receiver devices to display triggered actions in real time.

            v4.0.3b
            • Personalized Theme Colors: supports custom interface colors via RGB sliders, with automatic warning prompts when low contrast might affect text readability.
            • Upgraded Global Audio Management: supports adding items to Global Audio with persistent storage and deletion; supports drag-and-drop reordering via long press.
            • Top Navigation Bar: enabling the "Show Top Navigation Bar" option renders an additional set of operation buttons at the top while widening key spacing across core control areas for easier one-handed or eyes-free operation.
            • Independent Branch Disabling: for multi-branch actions, tapping the "Eye" icon on the action card enables or disables that branch; disabled items will not execute any action when tapped.
            • Emergency Anti-Misoperation Gesture: added a slide-to-cancel gesture. If accidentally touched, quickly sliding your finger away from the trigger zone before audio playback begins releases the trigger state to prevent misfires.
            • Expanded BGM Modes: added support for "Single Playback" (stops automatically after the track finishes without looping or switching to the next track).

            v4.0.4
            • Fixed multilingual display issues and further refined UI details to enhance overall user experience.
            • Added the "About" screen.
            
            v4.0.6
            • Updated and optimized the user interface and related operations, and updated support for virtual machine devices.
            
            v4.0.7
            • Updated and optimized the user interface and interactive experience.
            
            v4.0.8
            • Updated custom interaction mode options, giving users more room for personalization.
            • Added ZIP validation mechanism, loading prompts for large files, and fixed BGM list scrolling conflicts.
            
            v4.1.1
            • When adding global audio, the operation will be intercepted if it has the same name as the audio that is already or has been added in the package.
            • Disables masking the full sequence of steps, popping up a prompt when triggered.
            • Added Recover All Sequences to the right side of the Quick Jump drop-down menu.
            • User Interface and User Experience Optimization.
            • Large Document Import Status Reminder: When importing compressed packages with a volume of more than 20MB, an asynchronous loading prompt will automatically pop up and a loading circle will be displayed.
            • Strictly verify the import file format, non-canonical zip or other formatted files will uniformly intercept and prompt to import the correct compressed package (the old PC-side generated compressed package is not supported).
            • It traverses the document structure check integrity before unpacking the read;If document damage or missingness is detected, the imported residual cache is automatically cleaned and the user is prompted to re-import.
            • Fixed scroll conflict issue where the list could not swipe down to browse when importing multiple BGMs in the console, only triggering drag sorting.
            
            v4.1.2
            • Added file verification. If a file declared in the package does not exist, the package is considered incomplete, and the cache will be cleared to prompt a re-import.
        """.trimIndent()
        UILanguage.JA -> """
            v1.0.3
            • 初回リリース！基本的なプロファイルのインポートおよび基本制御機能を実装しました。アクションライブラリの順次ループ再生、「前のステップ」「次のステップ」「最初のステップ」「最後のステップ」の4つのボタンと中央メインボタンによるシーケンス制御、特定シーンでの音楽・音声再生/一時停止を行う付属オーディオ機能、上部ボタンからドロップダウンメニューを開いて指定アクションへ素早く移動する機能に対応しています。

            v1.1.2
            • ラウンド（回数）の記録・表示機能を追加しました。また、既知の不具合を修正し、インターフェース操作性とユーザー体験を向上させました。

            v1.2.1-beta
            • グローバルオーディオ機能を追加しました。プロファイル内のグローバルオーディオを自動認識して画面に読み込み、アクションシーケンスの実行中いつでも手動で再生トリガーを実行できます。

            v1.2.3-beta
            • ホーム画面で追加日時とプロファイル名が表示されるようになり、不要なプロファイルを直接削除できるようになりました。

            v1.3.2-beta
            • 開始点および終了点のカスタム設定機能を追加し、不要なアクションを柔軟にスキップできるようになりました。

            v2.0.1-beta
            • シーケンスエディタと連携し、多言語表示に完全対応しました。画面右上の言語切り替えボタンからリアルタイムに言語を変更できます。
            • プロファイル互換性に関する注意：本バージョン以降、旧バージョンのプロファイルはサポート対象外となります。`languages` フィールドを含む新しい形式のプロファイルをご使用ください。

            v2.1.8a
            • コントロールパネル機能を追加：BGMの追加および個別再生に対応しました。
            • 音量制御とモニタリング：メイン音声とBGMの音量を個別に調整可能になり、デシベルメーターによる音声レベルのリアルタイム監視に対応しました。
            • ループ設定の統合：カスタムループ範囲設定機能をコントロールパネル内に統合しました。
            • 特殊ディスプレイへの最適化：ノッチやDynamic Island搭載端末向けにUIを最適化し、上部にセーフエリア余白を確保して画面の遮りを防止しました。

            v2.3.2c
            • コントロールパネルを独立したオーバーレイレイヤーに変更して画面スペースを確保し、よりスムーズな操作体験を実現しました。

            v2.4.1
            • BGM再生機能をアップグレードしました。複数曲の追加に対応し、デフォルトのリストループ再生に加え、1曲リピートやシャッフル再生に対応しました。

            v2.5.3
            • 多言語インターフェースに対応しました（現在、繁体字中国語、英語、日本語に対応）。
            • UIテーマカラーのカスタム変更に対応しました。
            • アクセシビリティの向上：視覚障害をお持ちの方に向けたハイコントラストモードを追加し、音声案内やスクリーンリーダーに対応するアクセシビリティラベルを導入しました。

            v3.0.1
            • ESP32送信側へ指定フォーマットのデータを送信する機能に対応し、受信側に接続された機器でトリガーされたアクションをリアルタイムに表示できるようになりました。

            v4.0.3b
            • カスタムテーマカラー：RGBスライダーによるUIカラーの調整に対応しました。コントラスト不足でテキストの視認性に影響が出る恐れがある場合、自動的に警告を表示します。
            • グローバルオーディオ管理の強化：項目の追加、永続化保存、削除に対応しました。また、長押し＆ドラッグによる再生順の並べ替えに対応しました。
            • トップナビゲーションバー：「トップナビゲーションバーを表示」オプションを有効にすると、画面上部に追加の操作ボタンが表示され、主要操作エリアのボタン間隔が広がるため、片手操作や画面を見ないブラインド操作がより容易になります。
            • 分岐タスクの個別無効化：複数の分岐を持つアクションにおいて、アクションカード右上の「目のアイコン」をタップすることで該当ブランチの有効/無効を切り替えられます。無効化された項目はタップしてもトリガーされません。
            • 誤操作防止の即時キャンセル機能：スライドキャンセルジェスチャーを追加しました。誤ってボタンに触れた場合でも、音声が再生される前に指をトリガー領域の外へ素早くスライドさせることで、トリガー状態を解除して誤作動を防げます。
            • BGM再生モードの拡張：「1曲再生（リピートなし）」（曲の終了後に自動停止し、リピートや次曲への自動切り替えを行わないモード）に対応しました。

            v4.0.4
            • 多言語表示の不具合を修正し、UIの細部を最適化して操作体験を向上させました。
            • 「アプリについて」画面を追加しました。
            
            v4.0.6
            • ユーザーインターフェースと関連操作を更新および最適化し、仮想マシンデバイスのサポートを更新しました。
            
            v4.0.7
            • ユーザーインターフェースとインタラクティブな体験を更新および最適化しました。
            
            v4.0.8
            • カスタムインタラクションモードのオプションを更新し、ユーザーのカスタマイズの自由度をさらに高めました。
            • ZIPファイルの検証メカニズムを追加し、大規模ファイルへの進捗プロンプトを追加しました。
            
            v4.1.1
            • グローバルオーディオを追加する際、パッケージ内に既に存在する（または以前追加された）オーディオと同じ名前である場合は、処理が中断されます。
            • 一連のステップ全体をマスクする動作を無効化し、トリガー時にプロンプ​​トが表示されるように変更しました。
            • 「クイックジャンプ」ドロップダウンメニューの右側に「すべてのシーケンスを復元」機能を追加しました。
            • ユーザーインターフェース（UI）およびユーザーエクスペリエンス（UX）の最適化を行いました。
            • 大容量ドキュメントのインポート状況に関する通知機能を追加：20MBを超える圧縮パッケージをインポートする際、非同期読み込みのプロンプトが自動的に表示され、読み込み中のインジケーター（円形アニメーション）が表示されます。
            • インポートファイルの形式を厳密に検証するようにしました。非標準のZIPファイルやその他の形式のファイルは一律でブロックされ、正しい圧縮パッケージをインポートするよう促すメッセージが表示されます（PC版で以前生成された圧縮パッケージはサポート対象外です）。
            • 展開・読み込みの前にドキュメント構造を走査して整合性を確認します。ドキュメントの破損や欠落が検出された場合、インポート時に生成された残存キャッシュが自動的に削除され、ユーザーに再インポートを促すメッセージが表示されます。
            • コンソールで複数のBGMをインポートする際、リストを下にスワイプして閲覧しようとしてもドラッグによる並べ替えが作動してしまい、スクロールできない不具合を修正しました。
        
            v4.1.2
            • ファイル検証を追加しました。パッケージ内で宣言されているファイルが存在しない場合、パッケージが不完全とみなされ、キャッシュがクリアされて再インポートが求められます。
        """.trimIndent()
        UILanguage.FR -> """
            v1.0.3
            • Première version officielle ! Cette version introduit l'importation de profils de configuration de base ainsi que les fonctionnalités de contrôle essentielles, notamment : la lecture en boucle séquentielle de la bibliothèque d'actions ; le contrôle de séquence via les boutons « Précédent », « Suivant », « Première étape », « Dernière étape » et le bouton central principal ; la prise en charge de l'audio auxiliaire pour lire et mettre en pause la musique ou d'autres pistes vocales dans des contextes précis ; ainsi que l'ouverture d'un menu déroulant via le bouton supérieur pour accéder rapidement à une action donnée.

            v1.1.2
            • Ajout de l'enregistrement et de l'affichage des tours ; correction des problèmes connus pour optimiser l'ergonomie de l'interface et l'expérience utilisateur globale.

            v1.2.1-beta
            • Ajout de la fonctionnalité Audio Global : reconnaissance automatique et importation de l'audio global depuis les fichiers de configuration vers l'interface, permettant aux utilisateurs de déclencher manuellement la lecture à tout moment pendant la séquence d'actions.

            v1.2.3-beta
            • L'écran d'accueil prend désormais en charge l'affichage de la date d'ajout et du nom des profils, et permet la suppression directe des profils inutiles.

            v1.3.2-beta
            • Ajout de la configuration personnalisée des points de départ et d'arrivée, permettant d'ignorer en toute flexibilité les actions superflues.

            v2.0.1-beta
            • Prise en charge multilingue complète en synergie avec l'éditeur de séquences ; les utilisateurs peuvent changer de langue en temps réel via le bouton situé en haut à droite de l'interface.
            • Note de compatibilité des profils : les anciennes versions des profils de configuration ne sont plus prises en charge à partir de cette version ; veuillez utiliser le nouveau format de profil incluant le champ `languages`.

            v2.1.8a
            • Ajout de la console de contrôle : prise en charge de l'ajout et de la lecture indépendante de la musique de fond (BGM).
            • Contrôle et surveillance du volume : réglage indépendant du volume pour l'audio principal et la musique de fond, avec surveillance du niveau sonore en temps réel via un décibelmètre.
            • Intégration des paramètres de boucle : centralisation de la configuration des zones de boucle personnalisées au sein de la console de contrôle.
            • Adaptation aux écrans à encoche : optimisation de l'interface pour les écrans à encoche et Dynamic Island, avec réservation d'une zone de sécurité supérieure pour éviter toute troncature de l'affichage.

            v2.3.2c
            • Optimisation de l'interface utilisateur en transformant la console de contrôle en un calque superposé indépendant afin de libérer de l'espace à l'écran et d'offrir une utilisation plus fluide.

            v2.4.1
            • Amélioration de la lecture de la musique de fond : prise en charge de l'ajout de plusieurs pistes avec lecture en boucle de la liste par défaut, ainsi que prise en charge de la boucle sur une piste et du mode aléatoire.

            v2.5.3
            • Prise en charge de l'interface multilingue (actuellement disponible en chinois traditionnel, anglais et japonais).
            • Prise en charge de la personnalisation du thème de couleurs de l'interface.
            • Amélioration de l'accessibilité : ajout d'un mode Contraste Élevé pour les personnes malvoyantes et introduction de balises d'accessibilité pour la navigation vocale et les lecteurs d'écran.

            v3.0.1
            • Prise en charge de la transmission de données au format spécifié vers l'émetteur ESP32, permettant aux appareils connectés au récepteur d'afficher instantanément les actions déclenchées.

            v4.0.3b
            • Couleurs de thème personnalisées : réglage des couleurs de l'interface via des curseurs RVB, avec avertissement automatique lorsque le contraste insuffisant risque de nuire à la lisibilité du texte.
            • Gestion avancée de l'Audio Global : prise en charge de l'ajout d'éléments à l'Audio Global avec stockage persistant et suppression ; prise en charge du réagencement de l'ordre de lecture par appui long et glissement.
            • Barre de navigation supérieure : l'activation de l'option « Afficher la barre de navigation supérieure » affiche un ensemble de boutons d'action en haut de l'écran et augmente l'espacement des touches principales pour faciliter l'utilisation à une main ou à l'aveugle.
            • Désactivation individuelle des branches : pour les actions comportant plusieurs branches, touchez l'icône « Œil » sur la carte d'action pour activer ou désactiver cette branche ; un élément désactivé ne déclenchera aucune exécution lors d'un appui.
            • Annulation d'urgence contre les erreurs de manipulation : ajout un geste de glissement pour annuler.
            
            v4.0.4
            • Correction des problèmes d'affichage multilingue et optimisation des détails de l'interface.
            
            v4.0.8
            • Ajout d'un mécanisme de validation ZIP et de vérification des fichiers volumineux.
            
            v4.1.1
            • Lors de l'ajout d'un élément audio global, l'opération est interrompue si le nom est identique à celui d'un élément audio déjà présent ou précédemment ajouté au package.
            • Désactive le masquage de la séquence complète d'étapes et affiche une invite lors du déclenchement.
            • Ajout de l'option « Récupérer toutes les séquences » sur le côté droit du menu déroulant « Accès rapide ».
            • Optimisation de l'interface utilisateur et de l'expérience utilisateur.
            • Rappel d'état pour l'importation de documents volumineux : lors de l'importation de packages compressés dépassant 20 Mo, une invite de chargement asynchrone s'affiche automatiquement et un indicateur de chargement (cercle) apparaît.
            • Vérification stricte du format du fichier d'importation : les fichiers ZIP non conformes ou autres formats invalides sont systématiquement rejetés, avec une invite demandant d'importer le package compressé correct (les anciens packages compressés générés sur PC ne sont pas pris en charge).
            • Une vérification de l'intégrité de la structure du document est effectuée avant la décompression ; en cas de détection de corruption ou d'absence de fichier, le cache résiduel de l'importation est automatiquement nettoyé et l'utilisateur est invité à relancer l'importation.
            • Correction d'un conflit de défilement : lors de l'importation de plusieurs musiques de fond (BGM) dans la console, il était impossible de faire défiler la liste vers le bas pour la parcourir, l'action déclenchant uniquement le tri par glisser-déposer.
        
            v4.1.2
            • Ajout de la vérification des fichiers : si un fichier déclaré dans le paquet n'existe pas, le paquet est considéré comme incomplet, ce qui effacera le cache et demandera une nouvelle importation.
        """.trimIndent()
    }
}

object I18n {
    fun get(key: String, lang: UILanguage): String {
        return dict[key]?.get(lang) ?: dict[key]?.get(UILanguage.EN) ?: key
    }

    private val dict = mapOf(
        "library" to mapOf(UILanguage.EN to "Sequence Library", UILanguage.ZH_TW to "動作序列庫", UILanguage.JA to "シーケンスライブラリ", UILanguage.FR to "Bibliothèque de séquences"),
        "import" to mapOf(UILanguage.EN to "Import ZIP", UILanguage.ZH_TW to "導入 ZIP", UILanguage.JA to "ZIPをインポート", UILanguage.FR to "Importer ZIP"),
        "empty" to mapOf(UILanguage.EN to "Please Import Packages First.", UILanguage.ZH_TW to "暫無動作序列，請先導入 ZIP 包", UILanguage.JA to "パッケージをインポートしてください", UILanguage.FR to "Veuillez d'abord importer les paquets."),
        "added" to mapOf(UILanguage.EN to "Added", UILanguage.ZH_TW to "添加於", UILanguage.JA to "追加日", UILanguage.FR to "Ajouté"),
        "delete_confirm" to mapOf(UILanguage.EN to "Confirmation", UILanguage.ZH_TW to "確認刪除", UILanguage.JA to "削除の確認", UILanguage.FR to "Confirmation"),
        "delete_text" to mapOf(UILanguage.EN to "Sure to delete this package? Cannot be undone.", UILanguage.ZH_TW to "確定要刪除此序列包嗎？此操作不可恢復。", UILanguage.JA to "このパッケージを削除しますか？元に戻せません。", UILanguage.FR to "Supprimer ce paquet ? Action irréversible."),
        "delete_audio_text" to mapOf(UILanguage.EN to "Sure to delete this audio?", UILanguage.ZH_TW to "確定要刪除此音頻嗎？", UILanguage.JA to "この音声を削除しますか？", UILanguage.FR to "Supprimer cet audio ?"),
        "delete" to mapOf(UILanguage.EN to "Delete", UILanguage.ZH_TW to "刪除", UILanguage.JA to "削除", UILanguage.FR to "Supprimer"),
        "cancel" to mapOf(UILanguage.EN to "Cancel", UILanguage.ZH_TW to "取消", UILanguage.JA to "キャンセル", UILanguage.FR to "Annuler"),
        "confirm" to mapOf(UILanguage.EN to "Confirm", UILanguage.ZH_TW to "確認", UILanguage.JA to "確認", UILanguage.FR to "Confirmer"),
        "audio_panel" to mapOf(UILanguage.EN to "Audio Panel", UILanguage.ZH_TW to "音頻控制台", UILanguage.JA to "オーディオパネル", UILanguage.FR to "Panneau Audio"),
        "action" to mapOf(UILanguage.EN to "Action", UILanguage.ZH_TW to "動作", UILanguage.JA to "アクション", UILanguage.FR to "Action"),
        "skipped" to mapOf(UILanguage.EN to "[Skipped]", UILanguage.ZH_TW to "[已跳過]", UILanguage.JA to "[スキップ]", UILanguage.FR to "[Ignoré]"),
        "start" to mapOf(UILanguage.EN to "[Start]", UILanguage.ZH_TW to "[起點]", UILanguage.JA to "[開始]", UILanguage.FR to "[Début]"),
        "end" to mapOf(UILanguage.EN to "[End]", UILanguage.ZH_TW to "[終點]", UILanguage.JA to "[終了]", UILanguage.FR to "[Fin]"),
        "recover" to mapOf(UILanguage.EN to "Recover Step", UILanguage.ZH_TW to "恢復此步", UILanguage.JA to "ステップを復元", UILanguage.FR to "Récupérer l'étape"),
        "skip" to mapOf(UILanguage.EN to "Skip Step", UILanguage.ZH_TW to "屏蔽跳過", UILanguage.JA to "ステップをスキップ", UILanguage.FR to "Passer l'étape"),
        "global_audios" to mapOf(UILanguage.EN to "Global Audios", UILanguage.ZH_TW to "全局語音", UILanguage.JA to "グローバル音声", UILanguage.FR to "Audios Globaux"),
        "play_sub" to mapOf(UILanguage.EN to "Play Sub Audio", UILanguage.ZH_TW to "播放附屬語音", UILanguage.JA to "サブ音声を再生", UILanguage.FR to "Jouer le sous-audio"),
        "mixer" to mapOf(UILanguage.EN to "Mixer & Volumes", UILanguage.ZH_TW to "調音台 & 音量", UILanguage.JA to "ミキサーと音量", UILanguage.FR to "Mixeur et Volumes"),
        "settings" to mapOf(UILanguage.EN to "Settings", UILanguage.ZH_TW to "全局設置", UILanguage.JA to "設定", UILanguage.FR to "Paramètres"),
        "ui_language" to mapOf(UILanguage.EN to "Interface Language", UILanguage.ZH_TW to "介面語言", UILanguage.JA to "表示言語", UILanguage.FR to "Langue de l'interface"),
        "theme" to mapOf(UILanguage.EN to "Theme Palette", UILanguage.ZH_TW to "主題調色板", UILanguage.JA to "テーマパレット", UILanguage.FR to "Palette de thèmes"),
        "contrast_warning" to mapOf(UILanguage.EN to "Warning: Low Contrast", UILanguage.ZH_TW to "警告：對比度過低", UILanguage.JA to "警告：コントラストが低すぎます", UILanguage.FR to "Attention: Faible contraste"),
        "top_nav" to mapOf(UILanguage.EN to "Show Top Navigation Bar", UILanguage.ZH_TW to "顯示頂部導航欄", UILanguage.JA to "トップナビゲーションを表示", UILanguage.FR to "Afficher la barre supérieure"),
        "custom_bc_name" to mapOf(UILanguage.EN to "Name the Audio", UILanguage.ZH_TW to "為語音命名", UILanguage.JA to "音声の名前", UILanguage.FR to "Nommer l'audio"),
        "accessibility" to mapOf(UILanguage.EN to "Accessibility", UILanguage.ZH_TW to "無障礙輔助", UILanguage.JA to "アクセシビリティ", UILanguage.FR to "Accessibilité"),
        "high_contrast" to mapOf(UILanguage.EN to "High Contrast Mode", UILanguage.ZH_TW to "高對比度模式", UILanguage.JA to "ハイコントラストモード", UILanguage.FR to "Mode contraste élevé"),
        "about" to mapOf(UILanguage.EN to "About", UILanguage.ZH_TW to "關於", UILanguage.JA to "について", UILanguage.FR to "À propos"),
        "app_name_trans" to mapOf(UILanguage.EN to "Audio Broadcasting Console", UILanguage.ZH_TW to "音頻播控台", UILanguage.JA to "オーディオ放送コンソール", UILanguage.FR to "Audio Broadcasting Console"),
        "version" to mapOf(UILanguage.EN to "Version", UILanguage.ZH_TW to "版本", UILanguage.JA to "バージョン", UILanguage.FR to "Version"),
        "developer" to mapOf(UILanguage.EN to "Developed by HEZ", UILanguage.ZH_TW to "HEZ開發", UILanguage.JA to "HEZ開発", UILanguage.FR to "Développé par HEZ"),
        "intro_title" to mapOf(UILanguage.EN to "Introduction", UILanguage.ZH_TW to "簡介", UILanguage.JA to "概要", UILanguage.FR to "Introduction"),
        "changelog_title" to mapOf(UILanguage.EN to "Changelog", UILanguage.ZH_TW to "更新日誌", UILanguage.JA to "更新履歴", UILanguage.FR to "Journal des modifications"),
        "swipe_action" to mapOf(UILanguage.EN to "Swipe-to-Action Mode", UILanguage.ZH_TW to "滑動即執行模式", UILanguage.JA to "スワイプ即実行モード", UILanguage.FR to "Mode Glisser-pour-agir"),
        "name_exists" to mapOf(UILanguage.EN to "Name already exists!", UILanguage.ZH_TW to "名稱已存在！", UILanguage.JA to "名前が既に存在します！", UILanguage.FR to "Ce nom existe déjà !"),
        "cannot_skip_all" to mapOf(UILanguage.EN to "Cannot skip all steps!", UILanguage.ZH_TW to "無法屏蔽所有步驟！", UILanguage.JA to "すべてのステップをスキップできません！", UILanguage.FR to "Impossible d'ignorer toutes les étapes !"),
        "recover_all" to mapOf(UILanguage.EN to "Recover All", UILanguage.ZH_TW to "恢復全部", UILanguage.JA to "すべて復元", UILanguage.FR to "Tout récupérer"),
        "recover_all_confirm" to mapOf(UILanguage.EN to "Sure to recover all skipped steps?", UILanguage.ZH_TW to "確定要恢復所有已屏蔽的步驟嗎？", UILanguage.JA to "スキップしたすべてのステップを復元しますか？", UILanguage.FR to "Récupérer toutes les étapes ignorées ?"),
        "importing" to mapOf(UILanguage.EN to "Importing package...", UILanguage.ZH_TW to "正在導入序列包...", UILanguage.JA to "パッケージをインポートしています...", UILanguage.FR to "Importation du paquet..."),
        "importing_large" to mapOf(
            UILanguage.EN to "This is a relatively big file, importing is taking a longer time, please do not close the software until the procedure is finished.",
            UILanguage.ZH_TW to "這是一個較大的檔案，導入需要較長時間，完成前請勿關閉軟體。",
            UILanguage.JA to "これは比較的大きなファイルです。インポートに時間がかかっています。完了するまでソフトウェアを閉じないでください。",
            UILanguage.FR to "C'est un fichier relativement volumineux, l'importation prend plus de temps. Veuillez ne pas fermer le logiciel avant la fin."
        ),
        "invalid_zip" to mapOf(UILanguage.EN to "Invalid package! Missing config.json or wrong format.", UILanguage.ZH_TW to "無效的壓縮包！缺少 config.json 或格式錯誤。", UILanguage.JA to "無効なパッケージです！config.json が見つからないか、形式が間違っています。", UILanguage.FR to "Paquet invalide ! config.json manquant ou format incorrect."),
        "legacy_zip" to mapOf(UILanguage.EN to "Legacy package detected. Please use the new format with multi-language support.", UILanguage.ZH_TW to "檢測到舊版設定檔。請使用包含多語言支援的新版格式。", UILanguage.JA to "旧バージョンのパッケージが検出されました。多言語対応の新しい形式を使用してください。", UILanguage.FR to "Ancien paquet détecté. Veuillez utiliser le nouveau format avec support multilingue."),
        "checking" to mapOf(UILanguage.EN to "Verifying files...", UILanguage.ZH_TW to "正在校驗文件完整性...", UILanguage.JA to "ファイルを検証しています...", UILanguage.FR to "Vérification des fichiers..."),
        "package_corrupted" to mapOf(UILanguage.EN to "Package incomplete or corrupted! It has been removed. Please re-import.", UILanguage.ZH_TW to "壓縮包不完整或已損壞！已自動清理，請重新導入。", UILanguage.JA to "パッケージが不完全または破損しています！削除されました。再インポートしてください。", UILanguage.FR to "Paquet incomplet ou corrompu ! Il a été supprimé. Veuillez réimporter.")
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

@Composable
fun AboutDialog(lang: UILanguage, theme: CustomTheme, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Audio Panel", fontWeight = FontWeight.Black, color = if(theme.id == "HIGH_CONTRAST") Color.Black else theme.primary, fontSize = 26.sp)
        },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                item {
                    Text(I18n.get("app_name_trans", lang), fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(I18n.get("version", lang) + " 4.1.2", fontSize = 12.sp, color = Color.Gray)
                    Text(I18n.get("developer", lang), fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(I18n.get("intro_title", lang), fontWeight = FontWeight.Black, fontSize = 18.sp, color = if(theme.id == "HIGH_CONTRAST") Color.Black else theme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(AboutContent.getIntro(lang), fontSize = 14.sp, lineHeight = 22.sp, color = if(theme.id == "HIGH_CONTRAST") Color.Black else Color.DarkGray)

                    Spacer(modifier = Modifier.height(24.dp))
                    Text(I18n.get("changelog_title", lang), fontWeight = FontWeight.Black, fontSize = 18.sp, color = if(theme.id == "HIGH_CONTRAST") Color.Black else theme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(AboutContent.getChangelog(lang), fontSize = 13.sp, lineHeight = 20.sp, color = if(theme.id == "HIGH_CONTRAST") Color.Black else Color.DarkGray)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(I18n.get("confirm", lang), fontWeight = FontWeight.Bold, color = if(theme.id == "HIGH_CONTRAST") Color.Black else theme.primary)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(20.dp)
    )
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SettingsPanel(viewModel: AppViewModel) {
    val lang = viewModel.uiLanguage
    val theme = viewModel.currentTheme

    var showColorPicker by remember { mutableStateOf(false) }
    var rSlider by remember { mutableFloatStateOf(0.5f) }
    var gSlider by remember { mutableFloatStateOf(0.5f) }
    var bSlider by remember { mutableFloatStateOf(0.5f) }

    var showAboutDialog by remember { mutableStateOf(false) }

    var themeToDelete by remember { mutableStateOf<CustomTheme?>(null) }

    if (showAboutDialog) {
        AboutDialog(lang = lang, theme = theme, onDismiss = { showAboutDialog = false })
    }

    if (themeToDelete != null) {
        val deleteTitle = when (lang) { UILanguage.EN -> "Delete Theme"; UILanguage.ZH_TW -> "刪除主題"; UILanguage.JA -> "テーマを削除"; UILanguage.FR -> "Supprimer le thème" }
        val deleteMsg = when (lang) { UILanguage.EN -> "Sure to delete this custom theme?"; UILanguage.ZH_TW -> "確定要刪除此自訂主題嗎？"; UILanguage.JA -> "このカスタムテーマを削除しますか？"; UILanguage.FR -> "Supprimer ce thème personnalisé ?" }
        AlertDialog(
            onDismissRequest = { themeToDelete = null },
            title = { Text(deleteTitle, fontWeight = FontWeight.Bold) },
            text = { Text(deleteMsg) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCustomTheme(themeToDelete!!)
                    themeToDelete = null
                }) { Text(I18n.get("delete", lang), color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { themeToDelete = null }) { Text(I18n.get("cancel", lang), color = Color.Gray) }
            }
        )
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp, top = 8.dp).verticalScroll(rememberScrollState())) {
        Text(I18n.get("settings", lang), fontSize = 28.sp, fontWeight = FontWeight.Black, color = if (theme.id == "HIGH_CONTRAST") Color.Black else theme.primary)
        Spacer(modifier = Modifier.height(32.dp))

        Text(I18n.get("ui_language", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            UILanguage.values().forEach { l ->
                val label = when(l) { UILanguage.EN -> "English"; UILanguage.ZH_TW -> "繁體中文"; UILanguage.JA -> "日本語"; UILanguage.FR -> "Français" }
                Button(
                    onClick = { viewModel.updateLanguage(l) },
                    colors = ButtonDefaults.buttonColors(containerColor = if (lang == l) theme.primary else theme.light, contentColor = if (lang == l) Color.White else theme.primary),
                    shape = RoundedCornerShape(12.dp)
                ) { Text(label, fontWeight = FontWeight.Bold) }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleTopNavBar() }.padding(vertical = 8.dp)) {
            Text(I18n.get("top_nav", lang), modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Switch(checked = viewModel.showTopNavBar, onCheckedChange = { viewModel.toggleTopNavBar() }, colors = SwitchDefaults.colors(checkedThumbColor = theme.primary, checkedTrackColor = theme.light))
        }

        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleSwipeMode() }.padding(vertical = 8.dp)) {
            Text(I18n.get("swipe_action", lang), modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Switch(checked = viewModel.isSwipeToAction, onCheckedChange = { viewModel.toggleSwipeMode() }, colors = SwitchDefaults.colors(checkedThumbColor = theme.primary, checkedTrackColor = theme.light))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(I18n.get("theme", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            viewModel.defaultThemes.filter { it.id != "HIGH_CONTRAST" }.forEach { t ->
                Box(
                    modifier = Modifier.size(56.dp).background(t.primary, RoundedCornerShape(28.dp)).border(if (viewModel.currentTheme.id == t.id) 4.dp else 0.dp, Color.Black.copy(alpha = 0.3f), RoundedCornerShape(28.dp)).clickable { viewModel.setTheme(t) }
                )
            }

            viewModel.customThemes.forEach { ct ->
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(ct.primary, RoundedCornerShape(28.dp))
                        .border(if (viewModel.currentTheme.id == ct.id) 4.dp else 0.dp, Color.Black.copy(alpha = 0.3f), RoundedCornerShape(28.dp))
                        .combinedClickable(
                            onClick = { viewModel.setTheme(ct) },
                            onLongClick = { themeToDelete = ct }
                        )
                )
            }
            IconButton(onClick = { showColorPicker = !showColorPicker }, modifier = Modifier.size(56.dp).background(Color(0xFFEBEBEB), RoundedCornerShape(28.dp))) {
                Icon(Icons.Default.Add, contentDescription = "Custom Color", tint = Color.Gray)
            }
        }

        AnimatedVisibility(visible = showColorPicker) {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 24.dp)) {
                val mixedColor = Color(rSlider, gSlider, bSlider)
                val contrast = viewModel.checkContrastRatio(mixedColor)

                Box(modifier = Modifier.fillMaxWidth().height(60.dp).background(mixedColor, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    Text("Sample Text", color = Color.White, fontWeight = FontWeight.Bold)
                }

                if (contrast < 3.0f) {
                    Text(I18n.get("contrast_warning", lang), color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }

                Slider(value = rSlider, onValueChange = { rSlider = it }, colors = SliderDefaults.colors(thumbColor = Color.Red, activeTrackColor = Color.Red))
                Slider(value = gSlider, onValueChange = { gSlider = it }, colors = SliderDefaults.colors(thumbColor = Color.Green, activeTrackColor = Color.Green))
                Slider(value = bSlider, onValueChange = { bSlider = it }, colors = SliderDefaults.colors(thumbColor = Color.Blue, activeTrackColor = Color.Blue))

                Button(onClick = { viewModel.saveNewCustomTheme(mixedColor); showColorPicker = false }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = mixedColor)) {
                    Text(I18n.get("confirm", lang), color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(I18n.get("accessibility", lang), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable {
            viewModel.setTheme(if (theme.id == "HIGH_CONTRAST") viewModel.defaultThemes[0] else viewModel.defaultThemes.find { it.id == "HIGH_CONTRAST" }!!)
        }.padding(vertical = 8.dp)) {
            Text(I18n.get("high_contrast", lang), modifier = Modifier.weight(1f), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Switch(
                checked = theme.id == "HIGH_CONTRAST",
                onCheckedChange = { isChecked ->
                    viewModel.setTheme(if (isChecked) viewModel.defaultThemes.find { it.id == "HIGH_CONTRAST" }!! else viewModel.defaultThemes[0])
                },
                colors = SwitchDefaults.colors(checkedThumbColor = theme.primary, checkedTrackColor = theme.light)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().clickable { showAboutDialog = true }.padding(vertical = 16.dp)
        ) {
            Icon(Icons.Default.Info, contentDescription = null, tint = if(theme.id == "HIGH_CONTRAST") Color.Black else theme.primary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(I18n.get("about", lang), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (theme.id == "HIGH_CONTRAST") Color.Black else Color.Unspecified)
                Text("v4.1.2", fontSize = 14.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeListScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val theme = viewModel.currentTheme
    val lang = viewModel.uiLanguage
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) viewModel.importZipFile(context, uri)
    }

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    var sequenceToDelete by remember { mutableStateOf<SequenceInfo?>(null) }

    var showSettings by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showSettings) {
        ModalBottomSheet(onDismissRequest = { showSettings = false }, sheetState = sheetState, containerColor = Color.White) {
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

    // 💡 提示：導入進度防斷閉彈窗
    if (viewModel.isImporting) {
        AlertDialog(
            onDismissRequest = { /* 阻斷取消，防止未導入完成退出 */ },
            title = { Text(I18n.get("import", lang), fontWeight = FontWeight.Bold) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = theme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(I18n.get(viewModel.importStatusMsg, lang))
                }
            },
            confirmButton = { }
        )
    }

    if (viewModel.isCheckingIntegrity) {
        AlertDialog(
            onDismissRequest = { /* 阻斷關閉，等待校驗完成 */ },
            title = { Text(I18n.get("checking", lang), fontWeight = FontWeight.Bold) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(color = theme.primary)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(I18n.get("checking", lang))
                }
            },
            confirmButton = { }
        )
    }

    // 💡 提示：錯誤導入彈窗
    if (viewModel.importErrorMsg != null) {
        AlertDialog(
            onDismissRequest = { viewModel.importErrorMsg = null },
            title = { Text("Error", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text(I18n.get(viewModel.importErrorMsg!!, lang)) },
            confirmButton = {
                TextButton(onClick = { viewModel.importErrorMsg = null }) { Text("OK", color = theme.primary) }
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                Spacer(modifier = Modifier.height(36.dp))
                TopAppBar(
                    title = {
                        Text(
                            text = I18n.get("library", lang),
                            fontWeight = FontWeight.Black,
                            fontSize = 26.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    actions = {
                        IconButton(onClick = { showSettings = true }, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = theme.primary)
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { launcher.launch("application/zip") }, containerColor = theme.primary, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            if (viewModel.importedSequences.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                    Text(I18n.get("empty", lang), color = Color.Gray, fontSize = 16.sp)
                }
            }
            viewModel.importedSequences.forEach { info ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp).clickable { viewModel.openSequence(info.dir) }
                        .semantics { contentDescription = "Open ${info.name}" },
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = info.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (theme.id == "HIGH_CONTRAST") Color.Black else Color.Unspecified)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "${I18n.get("added", lang)} ${dateFormat.format(Date(info.addDate))}", fontSize = 14.sp, color = Color.Gray)
                        }
                        IconButton(onClick = { sequenceToDelete = info }, modifier = Modifier.size(48.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color.Red.copy(alpha = 0.6f))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun DbMeter(currentDb: Float) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(16.dp)) {
            val cr = CornerRadius(4.dp.toPx())
            val totalBars = 40
            val barSpacing = 4.dp.toPx()
            val barWidth = (size.width - (totalBars - 1) * barSpacing) / totalBars
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
                drawRoundRect(color = finalColor, topLeft = Offset(i * (barWidth + barSpacing), 0f), size = Size(barWidth, size.height), cornerRadius = cr)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedControlPanel(viewModel: AppViewModel, sequence: SequenceData, onLaunchBgm: () -> Unit) {
    val theme = viewModel.currentTheme
    val lang = viewModel.uiLanguage
    var bgmToDelete by remember { mutableStateOf<File?>(null) }

    if (bgmToDelete != null) {
        AlertDialog(
            onDismissRequest = { bgmToDelete = null },
            title = { Text(I18n.get("delete_confirm", lang), fontWeight = FontWeight.Bold) },
            text = { Text("${I18n.get("delete_audio_text", lang)}\n${bgmToDelete!!.name}") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeBgm(bgmToDelete!!)
                    bgmToDelete = null
                }) { Text(I18n.get("delete", lang), color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { bgmToDelete = null }) { Text(I18n.get("cancel", lang), color = Color.Gray) }
            }
        )
    }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(I18n.get("mixer", lang), fontWeight = FontWeight.Black, fontSize = 20.sp, color = theme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            DbMeter(viewModel.currentDb)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Main Audio", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Slider(value = viewModel.mainVolume, onValueChange = { viewModel.onMainVolumeChanged(it) }, valueRange = 0f..2.0f, colors = SliderDefaults.colors(thumbColor = theme.primary, activeTrackColor = theme.primary), modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                Text(String.format(Locale.US, "%.1fx", viewModel.mainVolume), fontSize = 14.sp, color = Color.Gray, modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Sub Audio", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Slider(value = viewModel.subVolume, onValueChange = { viewModel.onSubVolumeChanged(it) }, valueRange = 0f..2.0f, colors = SliderDefaults.colors(thumbColor = theme.primary, activeTrackColor = theme.primary), modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                Text(String.format(Locale.US, "%.1fx", viewModel.subVolume), fontSize = 14.sp, color = Color.Gray, modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
            }
            HorizontalDivider(color = theme.primary.copy(alpha = 0.2f), thickness = 2.dp, modifier = Modifier.padding(vertical = 20.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("BGM Playlist", fontWeight = FontWeight.Black, fontSize = 18.sp, color = theme.primary)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onLaunchBgm, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = theme.primary, modifier = Modifier.size(28.dp))
                }
            }

            if (viewModel.bgmPlaylist.isEmpty()) {
                Text("No BGM loaded.", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
            } else {
                val listState = rememberLazyListState()
                var draggedIndex by remember { mutableStateOf<Int?>(null) }
                var dragOffset by remember { mutableFloatStateOf(0f) }
                val itemHeightPx = with(LocalDensity.current) { 56.dp.toPx() }

                LazyColumn(state = listState, modifier = Modifier.heightIn(max = 220.dp).fillMaxWidth().padding(vertical = 12.dp).background(Color(0xFFEBEBEB), RoundedCornerShape(12.dp)).padding(8.dp)) {
                    itemsIndexed(viewModel.bgmPlaylist) { index, file ->
                        val isCurrent = index == viewModel.currentBgmIndex
                        val isDragged = index == draggedIndex
                        val zIndex = if (isDragged) 1f else 0f
                        val yOffset = if (isDragged) dragOffset else 0f

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().height(56.dp).zIndex(zIndex).graphicsLayer { translationY = yOffset }
                                .background(if (isDragged) Color.White.copy(alpha = 0.8f) else Color.Transparent, RoundedCornerShape(12.dp))
                        ) {
                            // 💡 修复 BGM 滑动冲突：仅将手势绑定在这个 DragHandle 图标上！
                            Icon(
                                Icons.Default.DragHandle, contentDescription = null, tint = Color.Gray,
                                modifier = Modifier
                                    .size(36.dp)
                                    .padding(end = 8.dp)
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
                            )
                            Row(modifier = Modifier.weight(1f).fillMaxHeight().clickable { viewModel.playBgmAtIndex(index) }, verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = if (isCurrent && viewModel.isBgmPlaying) Icons.Default.GraphicEq else Icons.Default.MusicNote, contentDescription = null, tint = if (isCurrent) theme.primary else Color.Gray, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = file.name, fontSize = 14.sp, fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal, color = if (isCurrent) theme.primary else Color.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { bgmToDelete = file }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.toggleBgm() }, modifier = Modifier.size(56.dp).background(if (viewModel.isBgmPlaying) theme.dark else theme.light, RoundedCornerShape(28.dp))) {
                        Icon(if (viewModel.isBgmPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = null, tint = if (viewModel.isBgmPlaying) Color.White else theme.primary, modifier = Modifier.size(28.dp))
                    }
                    IconButton(onClick = { viewModel.nextBgm() }, modifier = Modifier.size(56.dp).background(Color(0xFFEBEBEB), RoundedCornerShape(28.dp))) {
                        Icon(Icons.Default.SkipNext, contentDescription = null, tint = theme.primary, modifier = Modifier.size(28.dp))
                    }
                    IconButton(onClick = { viewModel.cycleBgmMode() }, modifier = Modifier.size(56.dp).background(if (viewModel.bgmMode == BgmMode.LOOP_ALL) Color(0xFFEBEBEB) else theme.light, RoundedCornerShape(28.dp))) {
                        val icon = when (viewModel.bgmMode) {
                            BgmMode.LOOP_ALL -> Icons.Default.Repeat
                            BgmMode.SHUFFLE -> Icons.Default.Shuffle
                            BgmMode.SINGLE_LOOP -> Icons.Default.RepeatOne
                            BgmMode.SINGLE_ONCE -> Icons.Default.LooksOne
                        }
                        Icon(icon, contentDescription = null, tint = if (viewModel.bgmMode == BgmMode.LOOP_ALL) Color.Gray else theme.primary, modifier = Modifier.size(24.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.VolumeUp, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                    Slider(value = viewModel.bgmVolume, onValueChange = { viewModel.onBgmVolumeChanged(it) }, valueRange = 0f..1.0f, colors = SliderDefaults.colors(thumbColor = theme.primary, activeTrackColor = theme.primary), modifier = Modifier.weight(1f).padding(horizontal = 12.dp))
                    Text(String.format(Locale.US, "%.0f%%", viewModel.bgmVolume * 100), fontSize = 14.sp, color = Color.Gray, modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(viewModel: AppViewModel) {
    val context = LocalContext.current
    val theme = viewModel.currentTheme
    val lang = viewModel.uiLanguage
    val sequence = viewModel.currentSequence ?: return
    val currentStep = sequence.steps.getOrNull(viewModel.currentStepIndex) ?: return

    val coroutineScope = rememberCoroutineScope()
    var expandedMenu by remember { mutableStateOf(false) }
    var showConsole by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showCustomBroadcastDialog by remember { mutableStateOf(false) }
    var pendingCustomUri by remember { mutableStateOf<Uri?>(null) }
    var customBroadcastName by remember { mutableStateOf("") }

    var broadcastToDelete by remember { mutableStateOf<BroadcastState?>(null) }

    val bgmLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        if (uris.isNotEmpty()) viewModel.loadExternalBgms(context, uris)
    }

    val broadcastLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) { pendingCustomUri = uri; showCustomBroadcastDialog = true }
    }

    if (broadcastToDelete != null) {
        AlertDialog(
            onDismissRequest = { broadcastToDelete = null },
            title = { Text(I18n.get("delete_confirm", lang), fontWeight = FontWeight.Bold) },
            text = { Text("${I18n.get("delete_audio_text", lang)}\n${broadcastToDelete!!.name}") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.removeCustomBroadcast(broadcastToDelete!!)
                    broadcastToDelete = null
                }) { Text(I18n.get("delete", lang), color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { broadcastToDelete = null }) { Text(I18n.get("cancel", lang), color = Color.Gray) }
            }
        )
    }

    var nameError by remember { mutableStateOf(false) }

    if (showCustomBroadcastDialog && pendingCustomUri != null) {
        AlertDialog(
            onDismissRequest = { showCustomBroadcastDialog = false; pendingCustomUri = null; customBroadcastName = ""; nameError = false },
            title = { Text(I18n.get("custom_bc_name", lang), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = customBroadcastName,
                        onValueChange = { customBroadcastName = it; nameError = false },
                        singleLine = true,
                        isError = nameError
                    )
                    if (nameError) {
                        Text(I18n.get("name_exists", lang), color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (customBroadcastName.isNotBlank()) {
                        val exists = viewModel.activeBroadcasts.any { it.name == customBroadcastName }
                        if (exists) {
                            nameError = true
                        } else {
                            viewModel.addCustomBroadcast(context, pendingCustomUri!!, customBroadcastName)
                            showCustomBroadcastDialog = false
                            pendingCustomUri = null
                            customBroadcastName = ""
                            nameError = false
                        }
                    }
                }) { Text(I18n.get("confirm", lang), color = theme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showCustomBroadcastDialog = false; pendingCustomUri = null; customBroadcastName = ""; nameError = false }) {
                    Text(I18n.get("cancel", lang), color = Color.Gray)
                }
            }
        )
    }

    var showRecoverAllDialog by remember { mutableStateOf(false) }
    if (showRecoverAllDialog) {
        AlertDialog(
            onDismissRequest = { showRecoverAllDialog = false },
            title = { Text(I18n.get("recover_all", lang), fontWeight = FontWeight.Bold) },
            text = { Text(I18n.get("recover_all_confirm", lang)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.recoverAllSteps()
                    showRecoverAllDialog = false
                }) { Text(I18n.get("confirm", lang), color = theme.primary) }
            },
            dismissButton = {
                TextButton(onClick = { showRecoverAllDialog = false }) { Text(I18n.get("cancel", lang), color = Color.Gray) }
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
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(top = 48.dp, bottom = 16.dp).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.closeSequence() }, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = theme.primary, modifier = Modifier.size(32.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = I18n.get("audio_panel", lang),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = if (theme.id == "HIGH_CONTRAST") Color.Black else Color.Unspecified,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.connectToEsp32(context) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        val usbColor = if (viewModel.usbConnectionState == "ESP32 Connected") Color(0xFF2AC864) else Color.Gray
                        Icon(Icons.Default.Usb, contentDescription = "Connect USB ESP32", tint = usbColor, modifier = Modifier.size(28.dp))
                    }

                    if (!sequence.languages.isNullOrEmpty() && sequence.languages.size > 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val currentIndex = sequence.languages.indexOf(viewModel.currentLanguage)
                                viewModel.currentLanguage = sequence.languages[(currentIndex + 1) % sequence.languages.size]
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = theme.light, contentColor = theme.primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier.height(40.dp), shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(viewModel.currentLanguage, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { showConsole = true }, modifier = Modifier.size(40.dp).background(Color.White, RoundedCornerShape(12.dp))) {
                        Icon(imageVector = Icons.Default.Tune, contentDescription = null, tint = theme.primary, modifier = Modifier.size(24.dp))
                    }
                }
            }

            if (viewModel.showTopNavBar) {
                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    val navColors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = Color.White, disabledContainerColor = Color.LightGray)
                    Button(onClick = { viewModel.goFirst() }, enabled = viewModel.currentStepIndex > 0, colors = navColors, modifier = Modifier.size(64.dp), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.SkipPrevious, contentDescription = null, modifier = Modifier.size(36.dp)) }
                    Button(onClick = { viewModel.goPrev() }, enabled = viewModel.currentStepIndex > 0, colors = navColors, modifier = Modifier.size(64.dp), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, modifier = Modifier.size(40.dp)) }
                    Button(onClick = { viewModel.goNext() }, colors = navColors, modifier = Modifier.size(64.dp), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(40.dp)) }
                    Button(onClick = { viewModel.goLast() }, enabled = viewModel.currentStepIndex < sequence.steps.size - 1, colors = navColors, modifier = Modifier.size(64.dp), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(36.dp)) }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = { expandedMenu = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = theme.primary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        val statusPrefix = if (viewModel.skippedSteps.contains(viewModel.currentStepIndex)) I18n.get("skipped", lang) + " " else ""
                        Text("$statusPrefix${I18n.get("action", lang)} ${currentStep.id} : ${currentStep.title}", fontWeight = FontWeight.Bold, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(28.dp))
                    }
                    DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }, modifier = Modifier.fillMaxHeight(0.6f)) {
                        sequence.steps.forEachIndexed { index, step ->
                            val statusLabel = buildString {
                                if (viewModel.loopRegions.any { it.startIndex == index }) append("${I18n.get("start", lang)} ")
                                if (viewModel.loopRegions.any { it.endIndex == index }) append("${I18n.get("end", lang)} ")
                                if (viewModel.skippedSteps.contains(index)) append(I18n.get("skipped", lang))
                            }
                            DropdownMenuItem(
                                text = { Text("${I18n.get("action", lang)} ${step.id} - ${step.title} $statusLabel", color = if (viewModel.skippedSteps.contains(index)) Color.Gray else Color.Black, fontSize = 18.sp) },
                                onClick = { viewModel.jumpToStep(index); expandedMenu = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                val hasSkipped = viewModel.skippedSteps.isNotEmpty()
                Button(
                    onClick = { if (hasSkipped) showRecoverAllDialog = true },
                    enabled = hasSkipped,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.height(60.dp).width(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = theme.primary,
                        contentColor = Color.White,
                        disabledContainerColor = Color.LightGray.copy(alpha = 0.5f),
                        disabledContentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.SettingsBackupRestore, contentDescription = "Recover All", modifier = Modifier.size(28.dp))
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.Center) {
                val isSkipped = viewModel.skippedSteps.contains(viewModel.currentStepIndex)
                Button(
                    onClick = {
                        val success = viewModel.toggleSkipStep(viewModel.currentStepIndex)
                        if (!success) {
                            android.widget.Toast.makeText(context, I18n.get("cannot_skip_all", lang), android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (isSkipped) Color.DarkGray else theme.light, contentColor = if (isSkipped) Color.White else theme.primary),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.height(48.dp)
                ) { Text(if (isSkipped) I18n.get("recover", lang) else I18n.get("skip", lang), fontWeight = FontWeight.Bold, fontSize = 16.sp) }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (currentStep.actions.size > 1) {
                    val spacing = if (currentStep.actions.size >= 3) 8.dp else 20.dp
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(spacing)) {
                        currentStep.actions.take(3).forEach { action ->
                            ActionCardWithSubAudio(viewModel, action, currentStep, true, coroutineScope, modifier = Modifier.weight(1f))
                        }
                    }
                } else {
                    ActionCardWithSubAudio(viewModel, currentStep.actions.first(), currentStep, false, coroutineScope, modifier = Modifier.fillMaxSize())
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                val navColors = ButtonDefaults.buttonColors(containerColor = theme.primary, contentColor = Color.White, disabledContainerColor = Color.LightGray)
                Button(onClick = { viewModel.goFirst() }, enabled = viewModel.currentStepIndex > 0, colors = navColors, modifier = Modifier.size(64.dp), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.SkipPrevious, contentDescription = null, modifier = Modifier.size(36.dp)) }
                Button(onClick = { viewModel.goPrev() }, enabled = viewModel.currentStepIndex > 0, colors = navColors, modifier = Modifier.size(64.dp), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowLeft, contentDescription = null, modifier = Modifier.size(40.dp)) }
                Button(onClick = { viewModel.goNext() }, colors = navColors, modifier = Modifier.size(64.dp), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, modifier = Modifier.size(40.dp)) }
                Button(onClick = { viewModel.goLast() }, enabled = viewModel.currentStepIndex < sequence.steps.size - 1, colors = navColors, modifier = Modifier.size(64.dp), shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(0.dp)) { Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(36.dp)) }
            }

            HorizontalDivider(color = theme.primary.copy(alpha = 0.2f), thickness = 2.dp, modifier = Modifier.padding(bottom = 16.dp))

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(I18n.get("global_audios", lang), fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                IconButton(onClick = { broadcastLauncher.launch("audio/*") }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Add Broadcast", tint = theme.primary, modifier = Modifier.size(28.dp))
                }
            }

            var draggedBcIndex by remember { mutableStateOf<Int?>(null) }
            var dragBcOffset by remember { mutableFloatStateOf(0f) }
            val bcWidthPx = with(LocalDensity.current) { 140.dp.toPx() }

            LazyRow(modifier = Modifier.fillMaxWidth().height(60.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                itemsIndexed(viewModel.activeBroadcasts) { index, broadcast ->
                    val isGlobalPlaying = viewModel.playingGlobalBroadcastName == broadcast.name
                    val isDragged = draggedBcIndex == index
                    val offset = if (isDragged) dragBcOffset else 0f

                    Box(modifier = Modifier.zIndex(if(isDragged) 1f else 0f).graphicsLayer { translationX = offset }
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { draggedBcIndex = index },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragBcOffset += dragAmount.x
                                    val targetIndex = (draggedBcIndex!! + (dragBcOffset / bcWidthPx).toInt()).coerceIn(0, viewModel.activeBroadcasts.size - 1)
                                    if (targetIndex != draggedBcIndex) {
                                        viewModel.moveBroadcast(draggedBcIndex!!, targetIndex)
                                        draggedBcIndex = targetIndex
                                        dragBcOffset %= bcWidthPx
                                    }
                                },
                                onDragEnd = { draggedBcIndex = null; dragBcOffset = 0f },
                                onDragCancel = { draggedBcIndex = null; dragBcOffset = 0f }
                            )
                        }
                    ) {
                        Button(
                            onClick = { viewModel.playBroadcast(broadcast) },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isGlobalPlaying) theme.dark else Color.White, contentColor = if (isGlobalPlaying) Color.White else theme.primary),
                            border = BorderStroke(2.dp, theme.primary), shape = RoundedCornerShape(24.dp), modifier = Modifier.height(56.dp)
                        ) {
                            Icon(imageVector = if (isGlobalPlaying) Icons.Default.Close else Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(broadcast.name, fontWeight = FontWeight.Black, fontSize = 16.sp)
                        }
                        if (broadcast.isCustom) {
                            IconButton(onClick = { broadcastToDelete = broadcast }, modifier = Modifier.size(18.dp).align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp).background(Color.White, RoundedCornerShape(9.dp))) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(10.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionCardWithSubAudio(viewModel: AppViewModel, action: RobotAction, step: RobotStep, isCompact: Boolean, scope: kotlinx.coroutines.CoroutineScope, modifier: Modifier = Modifier) {
    val theme = viewModel.currentTheme
    val lang = viewModel.uiLanguage
    val actionUniqueId = "${step.id}_${action.id}"

    val isSwipeToAction = viewModel.isSwipeToAction

    val hasSubAudio = !action.subAudioFileName?.get(viewModel.currentLanguage).isNullOrEmpty()
    val isThisSubPlaying = viewModel.isSubAudioPlaying && hasSubAudio

    val isPlaying = viewModel.playingActionName == action.name || isThisSubPlaying
    val isActionDisabled = viewModel.disabledActions.contains(actionUniqueId)
    val isStepSkipped = viewModel.skippedSteps.contains(viewModel.currentStepIndex)
    val isFullyDisabled = isActionDisabled || isStepSkipped

    val scale by animateFloatAsState(targetValue = if (isPlaying) 0.95f else 1.0f)
    val borderColor = if (isPlaying) theme.primary else Color.Gray.copy(alpha = 0.2f)

    var isSubBtnArmed by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val swipeThresholdPx = remember(density) { with(density) { 70.dp.toPx() } }
    val maxDragPx = remember(density) { with(density) { 100.dp.toPx() } }

    var dragOffset by remember { mutableFloatStateOf(0f) }
    val animatedOffset by animateFloatAsState(
        targetValue = dragOffset,
        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.7f, stiffness = 400f)
    )

    val isReadyToToggle = kotlin.math.abs(dragOffset) > swipeThresholdPx
    val trackIconScale by animateFloatAsState(targetValue = if (isSwipeToAction && isReadyToToggle) 1.25f else 1.0f)

    LaunchedEffect(isThisSubPlaying) {
        if (!isThisSubPlaying) isSubBtnArmed = false
    }

    LaunchedEffect(isSubBtnArmed) {
        if (isSubBtnArmed) {
            kotlinx.coroutines.delay(3000)
            isSubBtnArmed = false
        }
    }

    LaunchedEffect(dragOffset) {
        if (dragOffset != 0f) {
            kotlinx.coroutines.delay(3000)
            dragOffset = 0f
        }
    }

    LaunchedEffect(isActionDisabled) {
        dragOffset = 0f
    }

    val cardBgColor = when {
        !isPlaying -> Color.White
        theme.id == "HIGH_CONTRAST" -> Color(0xFFD6D6D6)
        else -> theme.light
    }

    val trackColor = if (step.actions.size > 1) {
        if (!isActionDisabled) Color(0xFFEFEFEF) else theme.light
    } else {
        cardBgColor
    }

    val pointerModifier = if (step.actions.size > 1) {
        Modifier.pointerInput(isActionDisabled, isSwipeToAction) {
            detectDragGesturesAfterLongPress(
                onDragStart = { viewModel.hapticFeedback() },
                onDragEnd = {
                    if (isSwipeToAction) {
                        if (kotlin.math.abs(dragOffset) > swipeThresholdPx) {
                            viewModel.hapticFeedback()
                            viewModel.toggleActionDisabled(actionUniqueId)
                        }
                        dragOffset = 0f
                    } else {
                        if (dragOffset < -swipeThresholdPx) dragOffset = -maxDragPx
                        else if (dragOffset > swipeThresholdPx) dragOffset = maxDragPx
                        else dragOffset = 0f
                    }
                },
                onDragCancel = { dragOffset = 0f },
                onDrag = { change, dragAmount ->
                    change.consume()
                    val limit = if (isSwipeToAction) 180.dp.toPx() else maxDragPx + 20f
                    if (!isActionDisabled) {
                        dragOffset = (dragOffset + dragAmount.x).coerceIn(-limit, 0f)
                    } else {
                        dragOffset = (dragOffset + dragAmount.x).coerceIn(0f, limit)
                    }
                }
            )
        }
    } else Modifier

    Card(
        modifier = modifier
            .scale(scale)
            .alpha(if (isFullyDisabled) 0.4f else 1.0f)
            .pointerInput(isPlaying) {
                detectHorizontalDragGestures { change, dragAmount ->
                    if (isPlaying && kotlin.math.abs(dragAmount) > 30) {
                        viewModel.cancelCurrentPlayback()
                        change.consume()
                    }
                }
            },
        shape = RoundedCornerShape(if (isCompact) 20.dp else 36.dp),
        border = BorderStroke(if (isPlaying) (if (isCompact) 4.dp else 8.dp) else 2.dp, borderColor),
        elevation = CardDefaults.cardElevation(if (isPlaying) 16.dp else 6.dp),
        colors = CardDefaults.cardColors(containerColor = trackColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            if (step.actions.size > 1) {
                if (!isActionDisabled) {
                    val disableBg = if (isSwipeToAction && isReadyToToggle) Color(0xFFFF3B30) else Color.White
                    val disableTint = if (isSwipeToAction && isReadyToToggle) Color.White else Color.Gray

                    Box(modifier = Modifier.fillMaxSize().padding(end = 24.dp), contentAlignment = Alignment.CenterEnd) {
                        IconButton(
                            onClick = {
                                if (!isSwipeToAction) {
                                    viewModel.toggleActionDisabled(actionUniqueId)
                                    dragOffset = 0f
                                }
                            },
                            modifier = Modifier.size(56.dp).scale(trackIconScale).background(disableBg, RoundedCornerShape(50))
                        ) {
                            Icon(Icons.Default.VisibilityOff, contentDescription = "Disable", tint = disableTint, modifier = Modifier.size(28.dp))
                        }
                    }
                } else {
                    val enableBg = if (isSwipeToAction && isReadyToToggle) theme.primary else Color.White
                    val enableTint = if (isSwipeToAction && isReadyToToggle) Color.White else theme.primary

                    Box(modifier = Modifier.fillMaxSize().padding(start = 24.dp), contentAlignment = Alignment.CenterStart) {
                        IconButton(
                            onClick = {
                                if (!isSwipeToAction) {
                                    viewModel.toggleActionDisabled(actionUniqueId)
                                    dragOffset = 0f
                                }
                            },
                            modifier = Modifier.size(56.dp).scale(trackIconScale).background(enableBg, RoundedCornerShape(50))
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = "Enable", tint = enableTint, modifier = Modifier.size(28.dp))
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset { androidx.compose.ui.unit.IntOffset(animatedOffset.roundToInt(), 0) }
                    .background(cardBgColor)
                    .then(pointerModifier)
            ) {
                if (isCompact && hasSubAudio) {
                    // 💡 多分支 + 有附属音频：保持 1/3 左侧区域的布局
                    Row(modifier = Modifier.fillMaxSize()) {
                        val subBgColor = when {
                            isThisSubPlaying -> theme.dark
                            isSubBtnArmed -> if (theme.id == "HIGH_CONTRAST") Color(0xFFC0C0C0) else theme.light
                            else -> Color(0xFFF2F2F7)
                        }
                        val subAudioText = action.subAudioName?.get(viewModel.currentLanguage) ?: I18n.get("play_sub", lang)

                        Column(
                            modifier = Modifier
                                .weight(0.33f)
                                .fillMaxHeight()
                                .background(subBgColor)
                                .clickable(enabled = !isFullyDisabled || dragOffset != 0f) {
                                    if (dragOffset != 0f) {
                                        dragOffset = 0f
                                    } else if (!isFullyDisabled) {
                                        if (isThisSubPlaying) {
                                            viewModel.toggleSubAudio(action, scope)
                                            isSubBtnArmed = false
                                        } else {
                                            if (!isSubBtnArmed) isSubBtnArmed = true
                                            else viewModel.toggleSubAudio(action, scope)
                                        }
                                    }
                                }
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isThisSubPlaying) Icons.Default.Stop else Icons.Default.MusicNote,
                                contentDescription = "Play Sub",
                                tint = if (isThisSubPlaying) Color.White else theme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subAudioText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isThisSubPlaying) Color.White else theme.primary,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Box(modifier = Modifier.fillMaxHeight().width(1.dp).background(Color.White))

                        Column(
                            modifier = Modifier
                                .weight(0.67f)
                                .fillMaxHeight()
                                .clickable(enabled = !isFullyDisabled || dragOffset != 0f) {
                                    if (dragOffset != 0f) {
                                        dragOffset = 0f
                                    } else if (!isFullyDisabled) {
                                        viewModel.playSpecificAction(action)
                                    }
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (!action.imageFileName.isNullOrEmpty()) {
                                val imageFile = File(File(viewModel.currentDir, "image"), action.imageFileName!!)
                                if (imageFile.exists()) {
                                    AsyncImage(model = imageFile, contentDescription = null, modifier = Modifier.weight(1f, fill = false).padding(bottom = 8.dp))
                                }
                            }

                            Text(
                                text = action.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                textAlign = TextAlign.Center,
                                lineHeight = 30.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isFullyDisabled) Color.Gray else (if(theme.id == "HIGH_CONTRAST") Color.Black else Color.Unspecified)
                            )
                        }
                    }
                } else {
                    // 💡 单分支或无附属音频：修复，如果是单分支，恢复全长度底部按钮！
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(enabled = !isFullyDisabled || dragOffset != 0f) {
                                if (dragOffset != 0f) {
                                    dragOffset = 0f
                                } else if (!isFullyDisabled) {
                                    viewModel.playSpecificAction(action)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceEvenly
                    ) {
                        if (!action.imageFileName.isNullOrEmpty()) {
                            val imageFile = File(File(viewModel.currentDir, "image"), action.imageFileName!!)
                            if (imageFile.exists()) {
                                AsyncImage(model = imageFile, contentDescription = null, modifier = Modifier.weight(1f, fill = false).padding(bottom = 8.dp))
                            }
                        }

                        Text(
                            text = action.name,
                            fontSize = if (isCompact) 24.sp else 48.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            lineHeight = if (isCompact) 30.sp else 56.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = if (isFullyDisabled) Color.Gray else (if(theme.id == "HIGH_CONTRAST") Color.Black else Color.Unspecified)
                        )

                        // 🌟 修复：恢复单分支情况下的全长附属音频按钮
                        if (hasSubAudio && !isCompact) {
                            Spacer(modifier = Modifier.height(16.dp))
                            val subAudioText = action.subAudioName?.get(viewModel.currentLanguage) ?: I18n.get("play_sub", lang)
                            Button(
                                onClick = {
                                    if (!isFullyDisabled) {
                                        if (dragOffset != 0f) dragOffset = 0f
                                        else viewModel.toggleSubAudio(action, scope)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(0.9f).height(64.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isThisSubPlaying) theme.dark else theme.light,
                                    contentColor = if (isThisSubPlaying) Color.White else theme.primary
                                ),
                                elevation = ButtonDefaults.buttonElevation(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isThisSubPlaying) Icons.Default.Stop else Icons.Default.MusicNote,
                                    contentDescription = "Play Sub Audio",
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = subAudioText,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}