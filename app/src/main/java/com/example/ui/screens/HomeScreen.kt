package com.example.ui.screens

import androidx.compose.animation.togetherWith
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.ui.window.DialogProperties
import com.example.audio.AVAILABLE_RECITERS
import com.example.audio.AudioPlayerManager
import com.example.audio.AyahAudioState
import com.example.audio.Reciter
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.data.local.BookmarkManager
import com.example.data.local.QuranBookmark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AyahEntity
import com.example.data.local.SurahEntity
import com.example.ui.screens.settings.SettingsDialog
import com.example.data.local.LastReadManager
import com.example.data.local.LastReadItem
import com.example.ui.viewmodels.HomeViewModel

private val SURAH_NAMES_AR = listOf(
    "الفاتحة", "البقرة", "آل عمران", "النساء", "المائدة", "الأنعام", "الأعراف", "الأنفال", "التوبة", "يونس",
    "هود", "يوسف", "الرعد", "إبراهيم", "الحجر", "النحل", "الإسراء", "الكهف", "مريم", "طه",
    "الأنبياء", "الحج", "المؤمنون", "النور", "الفرقان", "الشعراء", "النمل", "القصص", "العنكبوت", "الروم",
    "لقمان", "السجدة", "الأحزاب", "سبأ", "فاطر", "يس", "الصافات", "ص", "الزمر", "غافر", "فصلت",
    "الشورى", "الزخرف", "الدخان", "الجاثية", "الأحقاف", "محمد", "الفتح", "الحجرات", "ق", "الذاريات",
    "الطور", "النجم", "القمر", "الرحمن", "الواقعة", "الحديد", "المجادلة", "الحشر", "الممتحنة", "الصف",
    "الجمعة", "المنافقون", "التغابن", "الطلاق", "التحريم", "الملك", "القلم", "الحاقة", "المعارج", "نوح",
    "الجن", "المزمل", "المدثر", "القيامة", "الإنسان", "المرسلات", "النبأ", "النازعات", "عبس", "التكوير",
    "الانفطار", "المطففين", "الانشقاق", "البروج", "الطارق", "الأعلى", "الغاشية", "الفجر", "البلد", "الشمس",
    "الليل", "الضحى", "الشرح", "التين", "العلق", "القدر", "البينة", "الزلزلة", "العاديات", "القارعة",
    "التكاثر", "العصر", "الهمزة", "الفيل", "قريش", "الماعون", "الكوثر", "الكافرون", "النصر", "المسد", "الإخلاص", "الفلق", "الناس"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSurahSelected: (Int) -> Unit,
    onAyahSelected: (Int, Int) -> Unit = { _, _ -> }
) {
    fun Int.toArabicNumerals(): String {
        val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return this.toString().map { if (it.isDigit()) arabicNumerals[it - '0'] else it }.joinToString("")
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val surahs by viewModel.surahs.collectAsStateWithLifecycle()
    val syncProgress by viewModel.syncProgress.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
    
    var lastRead by remember { mutableStateOf<LastReadItem?>(null) }
    var isAyahCardExpanded by remember { mutableStateOf(true) }
    val lastReadAyahText by viewModel.lastReadAyahText.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        val lr = LastReadManager.getLastRead(context)
        lastRead = lr
        if (lr != null) {
            viewModel.loadLastReadAyahText(lr.surahId, lr.ayahNumber)
        }
    }

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var showBookmarksSheet by remember { mutableStateOf(false) }
    var bookmarks by remember { mutableStateOf(emptyList<QuranBookmark>()) }

    var showPosterDialog by remember { mutableStateOf(false) }
    var showReciterDialog by remember { mutableStateOf(false) }
    var posterAyah by remember { mutableStateOf<AyahEntity?>(null) }
    var posterSurahName by remember { mutableStateOf("") }

    val audioManager = remember { AyahAudioState.manager }
    val isPlayingAudio by (audioManager?.isPlaying?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(false) })
    val currentPlayingAyah by (audioManager?.currentPlayingAyah?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(null) })
    val currentReciter by (audioManager?.currentReciter?.collectAsStateWithLifecycle() ?: remember { mutableStateOf(AVAILABLE_RECITERS.first()) })

    // If user opens reciter dialog, pause loop
    LaunchedEffect(showReciterDialog) {
        if (showReciterDialog) {
            viewModel.pauseRandomAyahLoop()
        }
    }

    // Keep loop paused while audio is playing, and resume when it stops (if reciter dialog isn't open)
    LaunchedEffect(isPlayingAudio) {
        if (isPlayingAudio) {
            viewModel.pauseRandomAyahLoop()
        } else if (!showReciterDialog) {
            viewModel.resumeRandomAyahLoop()
        }
    }

    LaunchedEffect(currentPlayingAyah, isPlayingAudio) {
        if (isPlayingAudio && currentPlayingAyah != null) {
            val (surahId, ayahNumber) = currentPlayingAyah!!
            viewModel.updateRandomAyahTo(surahId, ayahNumber)
        }
    }
    
    androidx.compose.runtime.LaunchedEffect(showBookmarksSheet) {
        if (showBookmarksSheet) {
            bookmarks = BookmarkManager.loadBookmarks(context)
        }
    }

    // Speech recognition launcher for voice search
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                viewModel.onSearchQueryChanged(spokenText)
            }
        }
    }

    fun launchVoiceSearch() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-SA")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث بالآية الكريمة أو الكلمة للبحث...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "خاصية البحث الصوتي غير متاحة على هذا الجهاز", Toast.LENGTH_SHORT).show()
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(onDismiss = { showSettingsDialog = false })
    }
    if (showDownloadDialog) {
        DownloadFullQuranDialog(onDismiss = { showDownloadDialog = false })
    }


    if (showBookmarksSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBookmarksSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "العلامات المحفوظة",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${bookmarks.size} علامة",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (bookmarks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "لا توجد علامات مرجعية محفوظة بعد",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(bookmarks.size) { index ->
                            val bookmark = bookmarks[index]
                            val dateFormatted = androidx.compose.runtime.remember(bookmark.timestamp) {
                                try {
                                    val sdf = SimpleDateFormat("d MMMM yyyy • hh:mm a", Locale("ar"))
                                    sdf.format(Date(bookmark.timestamp))
                                } catch (e: Exception) {
                                    ""
                                }
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showBookmarksSheet = false
                                        onAyahSelected(bookmark.surahId, bookmark.ayahNumber)
                                    },
                                shape = RoundedCornerShape(14.dp),
                                colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${bookmark.surahName} — الآية ${bookmark.ayahNumber}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        IconButton(
                                            onClick = {
                                                val newList = bookmarks.filter { it.id != bookmark.id }
                                                BookmarkManager.saveBookmarks(context, newList)
                                                bookmarks = newList
                                                Toast.makeText(context, "تم حذف العلامة", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "حذف العلامة",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = bookmark.ayahText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif
                                    )
                                    if (dateFormatted.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "تاريخ الحفظ: $dateFormatted",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
    if (showPosterDialog && posterAyah != null) {
        AyahPosterDialog(
            ayah = posterAyah!!,
            surahName = posterSurahName,
            onDismiss = { showPosterDialog = false }
        )
    }

    if (showReciterDialog) {
        AlertDialog(
            onDismissRequest = { 
                showReciterDialog = false 
                if (!isPlayingAudio) {
                    viewModel.resumeRandomAyahLoop()
                }
            },
            modifier = Modifier
                .widthIn(max = 300.dp)
                .fillMaxWidth(0.85f),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            title = {
                Text(
                    text = "اختر القارئ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    itemsIndexed(AVAILABLE_RECITERS, key = { _, reciter -> reciter.id }) { _, reciter ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    audioManager?.setReciter(reciter)
                                    showReciterDialog = false
                                    if (!isPlayingAudio) {
                                        viewModel.resumeRandomAyahLoop()
                                    }
                                }
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentReciter.id == reciter.id,
                                onClick = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = reciter.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (currentReciter.id == reciter.id) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { 
                    showReciterDialog = false 
                    if (!isPlayingAudio) {
                        viewModel.resumeRandomAyahLoop()
                    }
                }) {
                    Text("إغلاق")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .statusBarsPadding(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showDownloadDialog = true }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.CloudDownload,
                            contentDescription = "تحميل المصحف كاملاً",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    IconButton(onClick = { showBookmarksSheet = true }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Bookmark,
                            contentDescription = "العلامات المحفوظة",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
                Text(
                    text = "صوت القرءان",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                IconButton(onClick = { showSettingsDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "الإعدادات",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar Component (Text & Voice Search)
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    placeholder = {
                        Text(
                            "ابحث عن سورة أو آية قرآنية...",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "بحث",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "حذف الكلمة",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            IconButton(
                                onClick = { launchVoiceSearch() },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "بحث صوتي",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(18.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (searchQuery.isBlank()) {
                if (lastRead != null) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onAyahSelected(lastRead!!.surahId, lastRead!!.ayahNumber)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "مواصلة التلاوة",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                if (lastReadAyahText != null && lastRead != null) {
                                    com.example.ui.components.AyahText(
                                        textUthmani = lastReadAyahText!!,
                                        ayahNumber = lastRead!!.ayahNumber,
                                        fontSize = 22.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "سورة ${lastRead!!.surahName} • الآية ${lastRead!!.ayahNumber}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                item {
                    val randomAyah by viewModel.randomAyah.collectAsStateWithLifecycle()
                    val randomSurahName by viewModel.randomAyahSurahName.collectAsStateWithLifecycle()
                    
                    if (randomAyah != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isAyahCardExpanded = !isAyahCardExpanded }
                                        .padding(16.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "قبس من كتاب الله تعالى",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Icon(
                                        imageVector = if (isAyahCardExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (isAyahCardExpanded) "طي" else "توسيع",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                androidx.compose.animation.AnimatedVisibility(visible = isAyahCardExpanded) {
                                    androidx.compose.animation.AnimatedContent(
                                        targetState = randomAyah,
                                        transitionSpec = {
                                            androidx.compose.animation.fadeIn(animationSpec = androidx.compose.animation.core.tween(500)).togetherWith(androidx.compose.animation.fadeOut(animationSpec = androidx.compose.animation.core.tween(500)))
                                        },
                                        label = "ayah_animation"
                                    ) { targetAyah ->
                                        if (targetAyah != null) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable {
                                                            onAyahSelected(targetAyah.surahId, targetAyah.ayahNumber)
                                                        },
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    com.example.ui.components.AyahText(
                                                        textUthmani = targetAyah.textUthmani,
                                                        ayahNumber = targetAyah.ayahNumber,
                                                        fontSize = 22.sp,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.padding(bottom = 12.dp)
                                                    )
                                                    
                                                    Surface(
                                                        shape = RoundedCornerShape(12.dp),
                                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                    ) {
                                                        Text(
                                                            text = "سورة $randomSurahName • الآية ${targetAyah.ayahNumber}",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                        )
                                                    }
                                                }
                                                
                                                Spacer(modifier = Modifier.height(14.dp))
                                                HorizontalDivider(
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                                    thickness = 1.dp
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))

                                                val isThisAyahPlaying = isPlayingAudio && currentPlayingAyah?.first == targetAyah.surahId && currentPlayingAyah?.second == targetAyah.ayahNumber

                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    // زر مشاركة الآية كصورة
                                                    TextButton(
                                                        onClick = {
                                                            posterAyah = targetAyah
                                                            posterSurahName = randomSurahName
                                                            showPosterDialog = true
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Share,
                                                            contentDescription = "مشاركة الآية كصورة",
                                                            modifier = Modifier.size(17.dp),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text(
                                                            text = "مشاركة كصورة",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }

                                                    // زر استماع للآية
                                                    TextButton(
                                                        onClick = {
                                                            if (isThisAyahPlaying) {
                                                                audioManager?.stop()
                                                                viewModel.resumeRandomAyahLoop()
                                                            } else {
                                                                viewModel.pauseRandomAyahLoop()
                                                                audioManager?.playAyah(
                                                                    surahId = targetAyah.surahId,
                                                                    ayahNumber = targetAyah.ayahNumber,
                                                                    continuous = false,
                                                                    onCompletion = {
                                                                        viewModel.playNextRandomAyah(audioManager)
                                                                    }
                                                                )
                                                            }
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isThisAyahPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                                                            contentDescription = if (isThisAyahPlaying) "إيقاف الاستماع" else "استماع للآية",
                                                            modifier = Modifier.size(18.dp),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text(
                                                            text = if (isThisAyahPlaying) "إيقاف" else "استماع للآية",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }

                                                    // زر تغيير القارئ
                                                    TextButton(
                                                        onClick = {
                                                            viewModel.pauseRandomAyahLoop()
                                                            showReciterDialog = true
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.RecordVoiceOver,
                                                            contentDescription = "تغيير القارئ",
                                                            modifier = Modifier.size(17.dp),
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                        Spacer(modifier = Modifier.width(5.dp))
                                                        Text(
                                                            text = currentReciter.name.split(" ").firstOrNull() ?: "القارئ",
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "السور الكريمة",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (surahs.isEmpty()) {
                    item {
                        Text(
                            text = "جاري التحميل...",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                } else if (syncProgress in 1..113) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "جاري تحميل السور للقراءة بدون إنترنت: $syncProgress من 114",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { syncProgress / 114f },
                                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                )
                            }
                        }
                    }
                }

                items(surahs, key = { it.id }) { surah ->
                    SurahItem(
                        surah = surah,
                        onClick = { onSurahSelected(surah.id) },
                        onExportPdfClick = {
                            coroutineScope.launch {
                                val ayahs = viewModel.getAyahsForSurah(surah.id)
                                com.example.utils.PdfExporter.exportSurahToPdf(context, surah.nameAr, ayahs)
                            }
                        },
                        onExportWordClick = {
                            coroutineScope.launch {
                                val ayahs = viewModel.getAyahsForSurah(surah.id)
                                com.example.utils.WordExporter.exportSurahToWord(context, surah.nameAr, ayahs)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else {
                // Search Results View
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "نتائج البحث عن: \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "${searchResults.size} آية",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Matching Surahs if any
                val matchingSurahs = surahs.filter { it.nameAr.contains(searchQuery, ignoreCase = true) }
                if (matchingSurahs.isNotEmpty()) {
                    item {
                        Text(
                            text = "السور المطابقة:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(matchingSurahs, key = { "match_surah_${it.id}" }) { surah ->
                        SurahItem(
                            surah = surah,
                            onClick = { onSurahSelected(surah.id) },
                            onExportPdfClick = {
                                coroutineScope.launch {
                                    val ayahs = viewModel.getAyahsForSurah(surah.id)
                                    com.example.utils.PdfExporter.exportSurahToPdf(context, surah.nameAr, ayahs)
                                }
                            },
                            onExportWordClick = {
                                coroutineScope.launch {
                                    val ayahs = viewModel.getAyahsForSurah(surah.id)
                                    com.example.utils.WordExporter.exportSurahToWord(context, surah.nameAr, ayahs)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (searchResults.isEmpty() && matchingSurahs.isEmpty() && !isSearching) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لم يتم العثور على نتائج للبحث \"$searchQuery\"\nتأكد من كتابة الكلمة بشكل صحيح أو جرب البحث الصوتي 🎙️",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(searchResults, key = { "${it.surahId}_${it.ayahNumber}" }) { ayah ->
                        SearchAyahItem(
                            ayah = ayah,
                            onClick = { onAyahSelected(ayah.surahId, ayah.ayahNumber) }
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SearchAyahItem(
    ayah: AyahEntity,
    onClick: () -> Unit
) {
    val surahName = SURAH_NAMES_AR.getOrElse(ayah.surahId - 1) { "سورة رقم ${ayah.surahId}" }
    
    fun Int.toArabicNumerals(): String {
        val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return this.toString().map { if (it.isDigit()) arabicNumerals[it - '0'] else it }.joinToString("")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "سورة $surahName • الآية ${ayah.ayahNumber.toArabicNumerals()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = ayah.textUthmani,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 20.sp,
                    lineHeight = 36.sp,
                    fontFamily = FontFamily.Serif
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SurahItem(
    surah: SurahEntity,
    onClick: () -> Unit,
    onExportPdfClick: () -> Unit,
    onExportWordClick: () -> Unit
) {
    fun Int.toArabicNumerals(): String {
        val arabicNumerals = arrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        return this.toString().map { if (it.isDigit()) arabicNumerals[it - '0'] else it }.joinToString("")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%02d", surah.id).toInt().toArabicNumerals().padStart(2, '٠'),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.nameAr,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val revelationAr = when(surah.revelationType.lowercase()) {
                    "makkah" -> "مكية"
                    "madinah" -> "مدنية"
                    else -> surah.revelationType
                }
                val ayahCountAr = surah.ayahCount.toArabicNumerals()
                Text(
                    text = "$revelationAr • $ayahCountAr آية",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onExportWordClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.Description,
                        contentDescription = "مشاركة السورة كملف Word",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(
                    onClick = onExportPdfClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.PictureAsPdf,
                        contentDescription = "مشاركة السورة كملف PDF",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
