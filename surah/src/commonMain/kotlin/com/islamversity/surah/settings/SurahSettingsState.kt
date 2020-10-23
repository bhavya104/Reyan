package com.islamversity.surah.settings

import com.islamversity.domain.model.QuranReadFontSize
import com.islamversity.surah.model.CalligraphyUIModel

data class SurahSettingsState(
    val ayaCalligraphies : List<CalligraphyUIModel> = emptyList(),
    val surahNameCalligraphies : List<CalligraphyUIModel> = emptyList(),
    val selectedAyaCalligraphy : CalligraphyUIModel? = null,
    val selectedSurahNameCalligraphy : CalligraphyUIModel? = null,
    val quranTextFontSize : Int = QuranReadFontSize.DEFAULT.size,
    val translateTextFontSize : Int = QuranReadFontSize.DEFAULT.size,
    val showSettings: Boolean = false
)