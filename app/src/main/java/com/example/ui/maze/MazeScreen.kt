package com.example.ui.maze

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.MazeCell
import com.example.ui.theme.MazePalettes
import com.example.ui.theme.WarmGold
import com.example.util.DifficultyCurve
import com.example.util.Localization
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.max
import androidx.compose.foundation.Canvas
import kotlinx.coroutines.delay

@Composable
fun MazeScreen(viewModel: MazeViewModel) {
    val layoutDir = Localization.getLayoutDirection(viewModel.currentLanguage)
    val isDark = viewModel.isDarkTheme

    // Dynamically retrieve the palette matching the current level difficulty tier
    val currentTier = DifficultyCurve.getTier(viewModel.currentLevel)
    val activePalette = MazePalettes.getPalette(currentTier, isDark)

    // Layout configuration enforcing RTL/LTR dynamically
    CompositionLocalProvider(LocalLayoutDirection provides layoutDir) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(activePalette.bgStart, activePalette.bgEnd)
                    )
                )
        ) {
            // Screen router
            AnimatedContent(
                targetState = viewModel.currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(350)) + scaleIn(initialScale = 0.95f, animationSpec = tween(350)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "screen_routing"
            ) { screen ->
                when (screen) {
                    GameScreen.WELCOME -> WelcomeView(viewModel, activePalette)
                    GameScreen.LEVEL_SELECT -> LevelSelectView(viewModel, activePalette)
                    GameScreen.GAME_PLAY -> GameplayView(viewModel, activePalette)
                    GameScreen.SETTINGS -> SettingsView(viewModel, activePalette)
                }
            }
        }
    }
}

/**
 * --- 1. WELCOME SCREEN ---
 */
@Composable
fun WelcomeView(viewModel: MazeViewModel, palette: com.example.ui.theme.MazeTierPalette) {
    val lang = viewModel.currentLanguage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // High-end Hero Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .border(1.5.dp, palette.wallColor.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .testTag("welcome_hero_card"),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.labyrinth_banner_1784371700675),
                    contentDescription = "Labyrinth Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title and localized descriptor text
        Text(
            text = Localization.getString("welcome_title", lang),
            color = palette.wallColor,
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("app_title_text")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = Localization.getString("welcome_subtitle", lang),
            color = palette.text.copy(alpha = 0.8f),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Premium Mode Selector Card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 4.dp)
                .testTag("mode_selector_card"),
            colors = CardDefaults.cardColors(containerColor = palette.cardBg.copy(alpha = 0.5f)),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, palette.wallColor.copy(alpha = 0.25f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (viewModel.isTimeTrialMode) Localization.getString("time_trial_mode", lang) else Localization.getString("standard_mode", lang),
                    color = palette.wallColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.bgStart.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Standard Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!viewModel.isTimeTrialMode) palette.accent else Color.Transparent)
                            .clickable {
                                viewModel.isTimeTrialMode = false
                            }
                            .padding(vertical = 8.dp)
                            .testTag("mode_tab_standard"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Localization.getString("standard_mode", lang),
                            color = if (!viewModel.isTimeTrialMode) (if (viewModel.isDarkTheme) Color.Black else Color.White) else palette.text.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Time Trial Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (viewModel.isTimeTrialMode) palette.accent else Color.Transparent)
                            .clickable {
                                viewModel.isTimeTrialMode = true
                            }
                            .padding(vertical = 8.dp)
                            .testTag("mode_tab_timetrial"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = Localization.getString("time_trial", lang),
                            color = if (viewModel.isTimeTrialMode) (if (viewModel.isDarkTheme) Color.Black else Color.White) else palette.text.copy(alpha = 0.6f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick Launch Buttons
        Button(
            onClick = { viewModel.startLevel(viewModel.currentLevel) },
            colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .testTag("play_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = Localization.getString("play", lang),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (viewModel.isDarkTheme) Color.Black else Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(0.8f),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.currentScreen = GameScreen.LEVEL_SELECT },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.wallColor),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("level_select_menu_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.GridView, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = Localization.getString("select_level", lang), fontSize = 14.sp)
            }

            OutlinedButton(
                onClick = { viewModel.currentScreen = GameScreen.SETTINGS },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.wallColor),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .testTag("settings_menu_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = Localization.getString("settings", lang), fontSize = 14.sp)
            }
        }
    }
}

/**
 * --- 2. SETTINGS SCREEN ---
 */
@Composable
fun SettingsView(viewModel: MazeViewModel, palette: com.example.ui.theme.MazeTierPalette) {
    val lang = viewModel.currentLanguage
    var showResetDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.currentScreen = GameScreen.WELCOME },
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = palette.wallColor
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = Localization.getString("settings", lang),
                color = palette.wallColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Options List Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(0.5.dp, palette.wallColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = palette.cardBg.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                // Option 1: Language Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = palette.wallColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = Localization.getString("language", lang), color = palette.text, fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.wallColor.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = Localization.getString("en_lang_name", lang),
                            color = if (viewModel.currentLanguage == Localization.Language.EN) Color.Black else palette.text,
                            modifier = Modifier
                                .background(if (viewModel.currentLanguage == Localization.Language.EN) palette.accent else Color.Transparent)
                                .clickable { viewModel.currentLanguage = Localization.Language.EN }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("lang_en_switch"),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = Localization.getString("fa_lang_name", lang),
                            color = if (viewModel.currentLanguage == Localization.Language.FA) Color.Black else palette.text,
                            modifier = Modifier
                                .background(if (viewModel.currentLanguage == Localization.Language.FA) palette.accent else Color.Transparent)
                                .clickable { viewModel.currentLanguage = Localization.Language.FA }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("lang_fa_switch"),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }

                Divider(color = palette.text.copy(alpha = 0.1f))

                // Option 2: Theme Select
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = palette.wallColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = Localization.getString("theme", lang), color = palette.text, fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.wallColor.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = Localization.getString("light_mode", lang),
                            color = if (!viewModel.isDarkTheme) Color.Black else palette.text,
                            modifier = Modifier
                                .background(if (!viewModel.isDarkTheme) palette.accent else Color.Transparent)
                                .clickable { viewModel.isDarkTheme = false }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("theme_light_switch"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = Localization.getString("dark_mode", lang),
                            color = if (viewModel.isDarkTheme) Color.Black else palette.text,
                            modifier = Modifier
                                .background(if (viewModel.isDarkTheme) palette.accent else Color.Transparent)
                                .clickable { viewModel.isDarkTheme = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("theme_dark_switch"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Divider(color = palette.text.copy(alpha = 0.1f))

                // Option 3: Controls Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SportsEsports, contentDescription = null, tint = palette.wallColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = Localization.getString("controls", lang), color = palette.text, fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.wallColor.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = Localization.getString("swipes", lang),
                            color = if (viewModel.controlScheme == ControlScheme.SWIPE) Color.Black else palette.text,
                            modifier = Modifier
                                .background(if (viewModel.controlScheme == ControlScheme.SWIPE) palette.accent else Color.Transparent)
                                .clickable { viewModel.controlScheme = ControlScheme.SWIPE }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("control_swipe_switch"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = Localization.getString("joystick", lang),
                            color = if (viewModel.controlScheme == ControlScheme.JOYSTICK) Color.Black else palette.text,
                            modifier = Modifier
                                .background(if (viewModel.controlScheme == ControlScheme.JOYSTICK) palette.accent else Color.Transparent)
                                .clickable { viewModel.controlScheme = ControlScheme.JOYSTICK }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("control_joystick_switch"),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Divider(color = palette.text.copy(alpha = 0.1f))

                // Option 4: Sound Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (viewModel.soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = null,
                            tint = palette.wallColor
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = Localization.getString("sound_effects", lang), color = palette.text, fontSize = 16.sp)
                    }
                    Switch(
                        checked = viewModel.soundEnabled,
                        onCheckedChange = { viewModel.soundEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = palette.accent, checkedTrackColor = palette.accent.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("sound_switch")
                    )
                }

                Divider(color = palette.text.copy(alpha = 0.1f))

                // Option 5: Haptic Feedback Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vibration, contentDescription = null, tint = palette.wallColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = Localization.getString("haptic_feedback", lang), color = palette.text, fontSize = 16.sp)
                    }
                    Switch(
                        checked = viewModel.hapticEnabled,
                        onCheckedChange = { viewModel.hapticEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = palette.accent, checkedTrackColor = palette.accent.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("haptic_switch")
                    )
                }

                Divider(color = palette.text.copy(alpha = 0.1f))

                // Option 6: Minimap Overlay Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = palette.wallColor)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = Localization.getString("minimap", lang), color = palette.text, fontSize = 16.sp)
                    }
                    Switch(
                        checked = viewModel.minimapEnabled,
                        onCheckedChange = { viewModel.minimapEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = palette.accent, checkedTrackColor = palette.accent.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("minimap_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Reset Progression Trigger
        Button(
            onClick = { showResetDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("reset_progress_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.DeleteForever, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = Localization.getString("reset_progress", lang), color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Modal Confirmation dialog for resetting stats
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = Localization.getString("reset", lang), fontWeight = FontWeight.Bold) },
            text = { Text(text = Localization.getString("reset_warning", lang)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetSavedProgress()
                        showResetDialog = false
                    },
                    modifier = Modifier.testTag("confirm_reset_action")
                ) {
                    Text(text = Localization.getString("reset", lang), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(text = Localization.getString("cancel", lang))
                }
            },
            properties = DialogProperties(usePlatformDefaultWidth = true),
            modifier = Modifier.testTag("reset_progress_confirm_dialog")
        )
    }
}

/**
 * --- 3. LEVEL SELECT SCREEN ---
 */
@Composable
fun LevelSelectView(viewModel: MazeViewModel, palette: com.example.ui.theme.MazeTierPalette) {
    val lang = viewModel.currentLanguage
    val progressList by viewModel.levelProgressList.collectAsState()

    // Map Room records for direct quick access
    val progressMap = remember(progressList) {
        progressList.associateBy { it.level }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.currentScreen = GameScreen.WELCOME }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = palette.wallColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = Localization.getString("select_level", lang),
                    color = palette.wallColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                if (viewModel.isTimeTrialMode) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = palette.accent.copy(alpha = 0.25f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, palette.accent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("timetrial_badge")
                    ) {
                        Text(
                            text = Localization.getString("time_trial", lang),
                            color = palette.accent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Level selection procedural grid (displaying up to 100 levels)
        val levels = (1..100).toList()

        LazyVerticalGrid(
            columns = GridCells.Adaptive(80.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(levels) { lvl ->
                // Assess progression record to lock/unlock level item
                val record = progressMap[lvl]
                val isUnlocked = lvl == 1 || record?.isUnlocked == true
                val stars = record?.stars ?: 0

                val tier = DifficultyCurve.getTier(lvl)
                val levelPalette = MazePalettes.getPalette(tier, viewModel.isDarkTheme)

                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isUnlocked) levelPalette.cardBg.copy(alpha = 0.6f)
                            else Color.Gray.copy(alpha = 0.15f)
                        )
                        .border(
                            width = if (isUnlocked) 1.5.dp else 0.5.dp,
                            color = if (isUnlocked) levelPalette.wallColor.copy(alpha = 0.8f) else Color.Gray.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable(enabled = isUnlocked) {
                            viewModel.startLevel(lvl)
                        }
                        .testTag("level_item_$lvl"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isUnlocked) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Level Identifier
                            Text(
                                text = Localization.formatNumbers(lvl.toString(), lang),
                                color = levelPalette.text,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Star badges
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(1.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (i in 1..3) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (i <= stars) WarmGold else Color.Gray.copy(alpha = 0.4f),
                                        modifier = Modifier.size(10.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // Locked Indicator
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.Gray.copy(alpha = 0.5f),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * --- 4. GAMEPLAY PLAYING VIEW ---
 */
@Composable
fun GameplayView(viewModel: MazeViewModel, palette: com.example.ui.theme.MazeTierPalette) {
    val lang = viewModel.currentLanguage
    val grid = viewModel.mazeGrid ?: return

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Stats Row: Timer, Level details
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.currentScreen = GameScreen.LEVEL_SELECT }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Levels",
                        tint = palette.wallColor
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = Localization.getString("level_number", lang, viewModel.currentLevel),
                        color = palette.wallColor,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = Localization.getString(
                            "grid_size",
                            lang,
                            DifficultyCurve.getGridSize(viewModel.currentLevel),
                            DifficultyCurve.getGridSize(viewModel.currentLevel)
                        ),
                        color = palette.text.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }

                // Quick Toggle Control Scheme & Dynamic Live Clock
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.controlScheme = if (viewModel.controlScheme == ControlScheme.SWIPE) {
                                ControlScheme.JOYSTICK
                            } else {
                                ControlScheme.SWIPE
                            }
                        },
                        modifier = Modifier.padding(end = 4.dp).testTag("quick_toggle_controls_button")
                    ) {
                        Icon(
                            imageVector = if (viewModel.controlScheme == ControlScheme.JOYSTICK) Icons.Default.SportsEsports else Icons.Default.TouchApp,
                            contentDescription = "Toggle Control Scheme",
                            tint = palette.wallColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = palette.cardBg.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp),
                        border = borderStroke(0.5.dp, palette.wallColor.copy(alpha = 0.3f))
                    ) {
                        val tenths = (viewModel.gameTimeTicks % 10).toInt()
                        val totalSec = (viewModel.gameTimeTicks / 10).toInt()
                        Text(
                            text = Localization.getString("timer", lang, totalSec, tenths),
                            color = palette.text,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("game_timer_ticks")
                        )
                    }
                }
            }

            // Central Canvas Area supporting touch swipe triggers
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(palette.cardBg.copy(alpha = 0.2f))
                    .border(0.5.dp, palette.wallColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .pointerInput(grid) {
                        if (viewModel.controlScheme == ControlScheme.SWIPE) {
                            detectDragGestures(
                                onDragEnd = {},
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    // Identify swipe direction on drag threshold exceedance
                                    val swipeThreshold = 15f
                                    if (abs(dragAmount.x) > abs(dragAmount.y)) {
                                        if (abs(dragAmount.x) > swipeThreshold) {
                                            if (dragAmount.x > 0) viewModel.movePlayer(1, 0) // right
                                            else viewModel.movePlayer(-1, 0) // left
                                        }
                                    } else {
                                        if (abs(dragAmount.y) > swipeThreshold) {
                                            if (dragAmount.y > 0) viewModel.movePlayer(0, 1) // down
                                            else viewModel.movePlayer(0, -1) // up
                                        }
                                    }
                                }
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Procedural maze drawing canvas
                MazeCanvas(
                    grid = grid,
                    playerX = viewModel.playerX,
                    playerY = viewModel.playerY,
                    exitX = viewModel.exitX,
                    exitY = viewModel.exitY,
                    trail = viewModel.playerTrail,
                    palette = palette,
                    particles = viewModel.activeParticles,
                    modifier = Modifier.fillMaxSize()
                )

                // Render Optional Corner Mini-map Overlay for complex mazes (Tiers 3+)
                if (viewModel.minimapEnabled && DifficultyCurve.getGridSize(viewModel.currentLevel) >= 21) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(palette.cardBg.copy(alpha = 0.9f))
                            .border(1.dp, palette.wallColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    ) {
                        MinimapCanvas(
                            grid = grid,
                            playerX = viewModel.playerX,
                            playerY = viewModel.playerY,
                            exitX = viewModel.exitX,
                            exitY = viewModel.exitY,
                            palette = palette
                        )
                    }
                }

                // Render Overlay Virtual Joystick D-Pad if preferred control
                if (viewModel.controlScheme == ControlScheme.JOYSTICK) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 24.dp)
                    ) {
                        JoystickControls(viewModel = viewModel, palette = palette)
                    }
                }

                // Render Time Trial Leaderboard Overlay
                if (viewModel.isTimeTrialMode) {
                    LeaderboardOverlay(
                        viewModel = viewModel,
                        palette = palette,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // LEVEL COMPLETE MODAL DIALOG
        if (viewModel.levelCompleted) {
            LevelCompleteView(viewModel = viewModel, palette = palette)
        }
    }
}

/**
 * --- 5. COMPACT MINIMAP DRAWING CANVAS ---
 */
@Composable
fun MinimapCanvas(
    grid: Array<Array<MazeCell>>,
    playerX: Int,
    playerY: Int,
    exitX: Int,
    exitY: Int,
    palette: com.example.ui.theme.MazeTierPalette
) {
    val gridSize = grid.size
    Canvas(modifier = Modifier.fillMaxSize()) {
        val mapCellSize = min(this.size.width, this.size.height) / gridSize.toFloat()

        // Draw exit point in red/gold
        drawCircle(
            color = palette.exitColor,
            radius = max(2f, mapCellSize * 0.4f),
            center = Offset(
                exitX * mapCellSize + mapCellSize / 2f,
                exitY * mapCellSize + mapCellSize / 2f
            )
        )

        // Draw solid wall outlines
        grid.forEach { row ->
            row.forEach { cell ->
                val cx = cell.x * mapCellSize
                val cy = cell.y * mapCellSize

                if (cell.topWall) {
                    drawLine(palette.wallColor.copy(alpha = 0.4f), Offset(cx, cy), Offset(cx + mapCellSize, cy), 1f)
                }
                if (cell.bottomWall) {
                    drawLine(palette.wallColor.copy(alpha = 0.4f), Offset(cx, cy + mapCellSize), Offset(cx + mapCellSize, cy + mapCellSize), 1f)
                }
                if (cell.leftWall) {
                    drawLine(palette.wallColor.copy(alpha = 0.4f), Offset(cx, cy), Offset(cx, cy + mapCellSize), 1f)
                }
                if (cell.rightWall) {
                    drawLine(palette.wallColor.copy(alpha = 0.4f), Offset(cx + mapCellSize, cy), Offset(cx + mapCellSize, cy + mapCellSize), 1f)
                }
            }
        }

        // Draw player marker as pulsing green orb
        drawCircle(
            color = palette.playerColor,
            radius = max(3f, mapCellSize * 0.5f),
            center = Offset(
                playerX * mapCellSize + mapCellSize / 2f,
                playerY * mapCellSize + mapCellSize / 2f
            )
        )
    }
}

/**
 * --- 6. GLASSMORPHIC JOYSTICK CONTROLS ---
 */
@Composable
fun JoystickControls(viewModel: MazeViewModel, palette: com.example.ui.theme.MazeTierPalette) {
    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(palette.cardBg.copy(alpha = 0.7f))
            .border(1.5.dp, palette.wallColor.copy(alpha = 0.3f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // D-Pad North
        IconButton(
            onClick = { viewModel.movePlayer(0, -1) },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(44.dp)
                .testTag("joystick_up")
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up", tint = palette.wallColor, modifier = Modifier.size(32.dp))
        }

        // D-Pad East
        IconButton(
            onClick = { viewModel.movePlayer(1, 0) },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(44.dp)
                .testTag("joystick_right")
        ) {
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Right", tint = palette.wallColor, modifier = Modifier.size(32.dp))
        }

        // D-Pad South
        IconButton(
            onClick = { viewModel.movePlayer(0, 1) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(44.dp)
                .testTag("joystick_down")
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down", tint = palette.wallColor, modifier = Modifier.size(32.dp))
        }

        // D-Pad West
        IconButton(
            onClick = { viewModel.movePlayer(-1, 0) },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(44.dp)
                .testTag("joystick_left")
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Left", tint = palette.wallColor, modifier = Modifier.size(32.dp))
        }

        // Center Indicator Orb
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(palette.accent.copy(alpha = 0.2f))
                .border(1.dp, palette.accent, CircleShape)
        )
    }
}

/**
 * --- 7. CELEBRATORY LEVEL COMPLETE MODAL ---
 */
@Composable
fun LevelCompleteView(viewModel: MazeViewModel, palette: com.example.ui.theme.MazeTierPalette) {
    val lang = viewModel.currentLanguage

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(2.dp, palette.wallColor, RoundedCornerShape(20.dp))
                .testTag("level_complete_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = palette.cardBg)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Complete Banner
                Text(
                    text = Localization.getString("level_completed", lang, viewModel.currentLevel),
                    color = palette.wallColor,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Stars display (with animated scales)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 1..3) {
                        val isEarned = i <= viewModel.starsEarned
                        val scale = remember { Animatable(0f) }

                        LaunchedEffect(Unit) {
                            delay(i * 150L)
                            scale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }

                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (isEarned) WarmGold else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("earned_star_$i")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Score Details Row: Time Taken, best record
                val displaySec = (viewModel.completionTimeMs / 1000f)
                val displaySecStr = Localization.formatNumbers(String.format("%.1f", displaySec), lang)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = palette.accent.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = Localization.getString("time_taken", lang), color = palette.text.copy(alpha = 0.7f), fontSize = 14.sp)
                            Text(text = "$displaySecStr s", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        if (viewModel.isTimeTrialMode) {
                            val finalEntries = viewModel.getLeaderboardEntries(viewModel.completionTimeMs)
                            val finalRank = finalEntries.find { it.isUser }?.rank ?: 5
                            val finalRankStr = when (finalRank) {
                                1 -> Localization.getString("rank_1", lang)
                                2 -> Localization.getString("rank_2", lang)
                                3 -> Localization.getString("rank_3", lang)
                                4 -> Localization.getString("rank_4", lang)
                                else -> Localization.getString("rank_5", lang)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = Localization.getString("rank", lang), color = palette.text.copy(alpha = 0.7f), fontSize = 14.sp)
                                Text(
                                    text = finalRankStr,
                                    color = if (finalRank == 1) palette.accent else palette.text,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        val pbTimeMs = viewModel.levelProgressList.value.find { it.level == viewModel.currentLevel }?.bestTimeMs ?: 0L
                        if (pbTimeMs > 0L) {
                            val pbSec = pbTimeMs / 1000f
                            val pbSecStr = Localization.formatNumbers(String.format("%.1f", pbSec), lang)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = Localization.getString("best_time", lang), color = palette.text.copy(alpha = 0.7f), fontSize = 14.sp)
                                Text(text = "$pbSecStr s", color = palette.text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action controls
                Button(
                    onClick = { viewModel.startLevel(viewModel.currentLevel + 1) },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.accent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("next_level_action"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = null, tint = if (viewModel.isDarkTheme) Color.Black else Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Localization.getString("next_level", lang),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (viewModel.isDarkTheme) Color.Black else Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.startLevel(viewModel.currentLevel) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.wallColor),
                        border = borderStroke(1.dp, palette.wallColor.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("try_again_action"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = Localization.getString("try_again", lang), fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.currentScreen = GameScreen.LEVEL_SELECT
                            viewModel.levelCompleted = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = palette.wallColor),
                        border = borderStroke(1.dp, palette.wallColor.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("back_to_menu_action"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = Localization.getString("level_menu", lang), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * Clean helper function to map border stroke configurations.
 */
private fun borderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)

@Composable
fun LeaderboardOverlay(
    viewModel: MazeViewModel,
    palette: com.example.ui.theme.MazeTierPalette,
    modifier: Modifier = Modifier
) {
    val lang = viewModel.currentLanguage
    var isExpanded by remember { mutableStateOf(false) }

    val activeTimeMs = viewModel.gameTimeTicks * 100L
    val entries = viewModel.getLeaderboardEntries(activeTimeMs)
    val userEntry = entries.find { it.isUser }
    val userRank = userEntry?.rank ?: 1

    val rankStr = when (userRank) {
        1 -> Localization.getString("rank_1", lang)
        2 -> Localization.getString("rank_2", lang)
        3 -> Localization.getString("rank_3", lang)
        4 -> Localization.getString("rank_4", lang)
        else -> Localization.getString("rank_5", lang)
    }

    Box(modifier = modifier) {
        if (!isExpanded) {
            // Collapsed: Pulsing glassmorphic mini-pill button
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { isExpanded = true }
                    .testTag("leaderboard_pill"),
                colors = CardDefaults.cardColors(containerColor = palette.cardBg.copy(alpha = 0.85f)),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, palette.accent.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Leaderboard",
                        tint = palette.accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = rankStr,
                        color = palette.text,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            // Expanded: Exquisite slide-out glassmorphic panel
            Card(
                modifier = Modifier
                    .width(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = false) {} // Consume clicks to prevent background swipe
                    .testTag("leaderboard_expanded_card"),
                colors = CardDefaults.cardColors(containerColor = palette.cardBg.copy(alpha = 0.92f)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, palette.wallColor.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header Row with collapse arrow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = palette.accent,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = Localization.getString("leaderboard", lang),
                                color = palette.wallColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { isExpanded = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Collapse",
                                tint = palette.text.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Divider line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(palette.wallColor.copy(alpha = 0.2f))
                    )

                    // Ranking rows
                    entries.take(5).forEach { entry ->
                        val displaySec = (entry.timeMs / 1000f)
                        val displaySecStr = Localization.formatNumbers(String.format("%.1f", displaySec), lang)

                        val isUser = entry.isUser
                        val entryBg = if (isUser) palette.accent.copy(alpha = 0.15f) else Color.Transparent
                        val entryBorderColor = if (isUser) palette.accent.copy(alpha = 0.4f) else Color.Transparent

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(entryBg, RoundedCornerShape(8.dp))
                                .border(
                                    if (isUser) 1.dp else 0.dp,
                                    entryBorderColor,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${entry.rank}.",
                                    color = if (isUser) palette.accent else palette.text.copy(alpha = 0.6f),
                                    fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = if (entry.isPersonalBest) Localization.getString("personal_best", lang) else if (isUser) "You" else entry.name,
                                    color = if (isUser) palette.accent else palette.text,
                                    fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 10.sp,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "${displaySecStr}s",
                                color = if (isUser) palette.accent else palette.text.copy(alpha = 0.8f),
                                fontWeight = if (isUser) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
