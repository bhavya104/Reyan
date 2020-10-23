package com.islamversity.surah

import com.islamversity.core.mvi.BaseState
import com.islamversity.core.mvi.MviResult
import com.islamversity.surah.model.CalligraphyUIModel
import com.islamversity.surah.model.UIItem

sealed class SurahResult : MviResult {
    object LastStable : SurahResult()
    object Loading : SurahResult()

    data class Error(val err: BaseState.ErrorState) : SurahResult()

    data class Items(
        val items: List<UIItem>
    ) : SurahResult()

    data class ShowAyaNumber(
        val position: Int,
        val id: String,
        val orderID: Long,
    ) : SurahResult()

    data class SettingsSurahNameCalligraphies(val list: List<CalligraphyUIModel>) : SurahResult()
    data class SettingsAyaCalligraphies(val list: List<CalligraphyUIModel>) : SurahResult()

    data class SettingsQuranFontSize(val fontSize : Int) : SurahResult()
    data class SettingsTranslateFontSize(val fontSize : Int) : SurahResult()

    data class SettingsSurahCalligraphy(val calligraphy : CalligraphyUIModel) : SurahResult()
    data class SettingsFirstAyaTranslationCalligraphy(val calligraphy : CalligraphyUIModel?) : SurahResult()
    data class SettingsOpen(val showSettings : Boolean) : SurahResult()
}