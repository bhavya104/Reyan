package com.islamversity.surah

import com.islamversity.core.mvi.MviIntent
import com.islamversity.surah.model.CalligraphyUIModel

sealed class SurahIntent : MviIntent {
    data class Initial(
        val surahId: String,
        val startingAyaPosition : Long,
    ) : SurahIntent()

    object SettingsInitial : SurahIntent()
    object OpenSettings : SurahIntent()
    data class SettingsNewSurahNameCalligraphySelected(
        val calligraphy : CalligraphyUIModel
    ) : SurahIntent()

    class SettingsNewAyaCalligraphy(
        val language: CalligraphyUIModel
    ) : SurahIntent()

    class SettingsChangeQuranFontSize(
        val size: Int
    ) : SurahIntent()

    class SettingsChangeTranslateFontSize(
        val size: Int
    ) : SurahIntent()
}
