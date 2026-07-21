package com.example.util

import androidx.compose.ui.unit.LayoutDirection

/**
 * Dynamic localisation dictionary supporting English (en) and Persian (fa).
 * Provides layout direction mappings and Persian digit conversion to display proper RTL texts.
 */
object Localization {

    enum class Language { EN, FA }

    fun getLayoutDirection(lang: Language): LayoutDirection {
        return if (lang == Language.FA) LayoutDirection.Rtl else LayoutDirection.Ltr
    }

    /**
     * Helper to convert Western numbers to Persian digits when Persian language is selected.
     */
    fun formatNumbers(text: String, lang: Language): String {
        if (lang == Language.EN) return text
        return text.map { char ->
            when (char) {
                '0' -> '۰'
                '1' -> '۱'
                '2' -> '۲'
                '3' -> '۳'
                '4' -> '۴'
                '5' -> '۵'
                '6' -> '۶'
                '7' -> '۷'
                '8' -> '۸'
                '9' -> '۹'
                else -> char
            }
        }.joinToString("")
    }

    /**
     * Dynamic localized string getter.
     */
    fun getString(key: String, lang: Language, vararg args: Any): String {
        val raw = when (lang) {
            Language.EN -> EN_STRINGS[key] ?: key
            Language.FA -> FA_STRINGS[key] ?: key
        }
        val formatted = if (args.isNotEmpty()) {
            try {
                String.format(raw, *args)
            } catch (e: Exception) {
                raw
            }
        } else {
            raw
        }
        return formatNumbers(formatted, lang)
    }

    private val EN_STRINGS = mapOf(
        "app_name" to "Labyrinth",
        "welcome_title" to "LABYRINTH",
        "welcome_subtitle" to "Solve your way through 1000+ procedural mazes",
        "play" to "Play Game",
        "settings" to "Settings",
        "language" to "Language",
        "theme" to "Theme",
        "sound_effects" to "Sound Effects",
        "haptic_feedback" to "Haptic Feedback",
        "reset_progress" to "Reset Saved Progress",
        "reset_warning" to "Are you sure you want to delete all saved levels and stars? This cannot be undone.",
        "cancel" to "Cancel",
        "reset" to "Reset",
        "back" to "Back",
        "select_level" to "Select Level",
        "grid_size" to "Grid Size: %dx%d",
        "level_number" to "Level %d",
        "timer" to "Time: %d.%01ds",
        "best_time_label" to "Best: %s",
        "no_best_time" to "Best: --",
        "stars_desc" to "%d Stars",
        "level_completed" to "LEVEL %d COMPLETED!",
        "time_taken" to "Time Taken",
        "best_time" to "Best Time",
        "steps_taken" to "Steps Taken",
        "score" to "Score",
        "high_score" to "High Score",
        "stars_earned" to "Stars Earned",
        "next_level" to "Next Level",
        "try_again" to "Try Again",
        "level_menu" to "Level Menu",
        "system_default" to "System Default",
        "light_mode" to "Light Mode",
        "dark_mode" to "Dark Mode",
        "controls" to "Control Scheme",
        "swipes" to "Swipe Gestures",
        "joystick" to "Virtual D-Pad",
        "minimap" to "Mini-map Overlay",
        "show_minimap" to "Enabled",
        "hide_minimap" to "Disabled",
        "unlocked_palettes" to "Progress Palettes unlocked",
        "how_to_play" to "Slide your glowing orb to find the hidden exit doorway along the maze borders!",
        "fa_lang_name" to "فارسی",
        "en_lang_name" to "English",
        "time_trial" to "Time Trial",
        "standard_mode" to "Standard Mode",
        "time_trial_mode" to "Time Trial Mode",
        "leaderboard" to "Leaderboard",
        "rank" to "Rank",
        "personal_best" to "Personal Best",
        "personal_best_short" to "PB: %s",
        "ghost_ahead" to "Ahead",
        "ghost_passed" to "Passed",
        "rank_1" to "1st",
        "rank_2" to "2nd",
        "rank_3" to "3rd",
        "rank_4" to "4th",
        "rank_5" to "5th",
        "tab_beginner" to "Beginner (1-50)",
        "tab_medium" to "Medium (51-150)",
        "tab_hard" to "Hard (151-400)",
        "tab_expert" to "Expert (401-1000)",
        "tab_endless" to "Endless (1001+)",
        "pause" to "Pause",
        "resume" to "Resume",
        "game_paused" to "Game Paused",
        "paused_subtitle" to "Keep searching or take a deep breath!",
        "restart_level" to "Restart",
        "main_menu" to "Main Menu",
        "ball_style" to "Ball Appearance",
        "trail_style" to "Trail Color",
        "special_section" to "Special Mazes",
        "generate_random_maze" to "Generate Random Maze",
        "no_special_mazes" to "No custom mazes generated yet.",
        "special_title" to "Special Arena",
        "special_desc" to "Generate infinitely randomized procedurals of any scale and replay them anytime!",
        "level_complete_brief" to "Level Complete!",
        "next_maze_in" to "Next maze in %d...",
        "locked" to "Locked (Reach Level %d)",
        "unlocked" to "Unlocked!"
    )

    private val FA_STRINGS = mapOf(
        "app_name" to "مارپیچ",
        "welcome_title" to "لابـیـرنـت",
        "welcome_subtitle" to "راه خود را از میان بیش از ۱۰۰۰ مارپیچ الگوریتمی پیدا کنید",
        "play" to "شروع بازی",
        "settings" to "تنظیمات",
        "language" to "زبان",
        "theme" to "پوسته",
        "sound_effects" to "جلوه‌های صوتی",
        "haptic_feedback" to "لرزش بازخورد (هپتیک)",
        "reset_progress" to "بازنشانی تمام مراحل بازی",
        "reset_warning" to "آیا مطمئن هستید که می‌خواهید تمام مراحل ذخیره‌شده و ستاره‌ها را پاک کنید؟ این عملیات غیرقابل بازگشت است.",
        "cancel" to "لغو",
        "reset" to "بازنشانی",
        "back" to "بازگشت",
        "select_level" to "انتخاب مرحله",
        "grid_size" to "ابعاد ماز: %d در %d",
        "level_number" to "مرحله %d",
        "timer" to "زمان: %d.%01d ثانیه",
        "best_time_label" to "بهترین زمان: %s",
        "no_best_time" to "بهترین زمان: --",
        "stars_desc" to "%d ستاره",
        "level_completed" to "مرحله %d با موفقیت تمام شد!",
        "time_taken" to "زمان ثبت شده",
        "best_time" to "بهترین رکورد",
        "steps_taken" to "تعداد گام‌ها",
        "score" to "امتیاز این مرحله",
        "high_score" to "بیشترین امتیاز",
        "stars_earned" to "ستاره‌های دریافت شده",
        "next_level" to "مرحله بعدی",
        "try_again" to "تلاش دوباره",
        "level_menu" to "منوی مراحل",
        "system_default" to "پیش‌فرض سیستم",
        "light_mode" to "حالت روشن",
        "dark_mode" to "حالت تاریک",
        "controls" to "نوع کنترل بازی",
        "swipes" to "کنترل کشیدنی (سایپ)",
        "joystick" to "جهت‌نمای مجازی صفحه",
        "minimap" to "نقشه کوچک کمکی",
        "show_minimap" to "فعال",
        "hide_minimap" to "غیرفعال",
        "unlocked_palettes" to "رنگ‌بندی‌های پویا باز شده",
        "how_to_play" to "توپ درخشان خود را حرکت دهید تا درگاه خروجی پنهان را در دیوارهای بیرونی پیدا کنید!",
        "fa_lang_name" to "فارسی",
        "en_lang_name" to "English",
        "time_trial" to "رقابت زمانی",
        "standard_mode" to "حالت استاندارد",
        "time_trial_mode" to "حالت رقابت زمانی",
        "leaderboard" to "جدول رده‌بندی",
        "rank" to "رتبه",
        "personal_best" to "بهترین رکورد فردی",
        "personal_best_short" to "رکورد شما: %s",
        "ghost_ahead" to "جلوتر",
        "ghost_passed" to "پشت سر",
        "rank_1" to "اول",
        "rank_2" to "دوم",
        "rank_3" to "سوم",
        "rank_4" to "چهارم",
        "rank_5" to "پنجم",
        "tab_beginner" to "مبتدی (۱-۵۰)",
        "tab_medium" to "متوسط (۵۱-۱۵۰)",
        "tab_hard" to "سخت (۱۵۱-۴۰۰)",
        "tab_expert" to "حرفه‌ای (۴۰۱-۱۰۰۰)",
        "tab_endless" to "بی‌پایان (۱۰۰۱+)",
        "pause" to "توقف",
        "resume" to "ادامه",
        "game_paused" to "بازی متوقف شد",
        "paused_subtitle" to "به جستجو ادامه دهید یا کمی نفس تازه کنید!",
        "restart_level" to "شروع مجدد",
        "main_menu" to "منوی اصلی",
        "ball_style" to "ظاهر توپ",
        "trail_style" to "رنگ رد حرکت",
        "special_section" to "مارپیچ‌های ویژه",
        "generate_random_maze" to "ساخت مارپیچ رندوم",
        "no_special_mazes" to "هنوز مارپیچ ویژه‌ای ساخته نشده است.",
        "special_title" to "بخش ویژه",
        "special_desc" to "مارپیچ‌های کاملاً تصادفی بسازید و آن‌ها را در هر زمان دوباره بازی کنید!",
        "level_complete_brief" to "مرحله به پایان رسید!",
        "next_maze_in" to "مارپیچ بعدی در %d...",
        "locked" to "قفل شده (مرحله %d)",
        "unlocked" to "باز شده!"
    )
}
