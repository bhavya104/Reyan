package com.islamversity.surah

import com.islamversity.core.FlowBlock
import com.islamversity.core.mvi.BasePresenter
import com.islamversity.core.mvi.BaseState
import com.islamversity.core.mvi.MviProcessor
import com.islamversity.core.notOfType
import com.islamversity.core.ofType
import com.islamversity.surah.settings.SurahSettingsIntent
import com.islamversity.surah.settings.SurahSettingsResult
import com.islamversity.surah.settings.SurahSettingsState
import kotlinx.coroutines.flow.take

class SurahPresenter(
    processor: MviProcessor<SurahIntent, SurahResult>
) : BasePresenter<SurahIntent, SurahState, SurahResult>(
    processor,
    SurahState.idle()
) {

    override fun filterIntent(): List<FlowBlock<SurahIntent, SurahIntent>> = listOf(
        {
            ofType<SurahIntent.Initial>().take(1)
        },
        {
            notOfType(SurahIntent.Initial::class)
        },
        {
            ofType<SurahIntent.SettingsInitial>().take(1)
        },
        {
            notOfType(SurahIntent.SettingsInitial::class)
        }
    )

    override fun reduce(preState: SurahState, result: SurahResult): SurahState =
        when (result) {
            SurahResult.LastStable ->
                preState.copy(
                    base = BaseState.stable(),
                    scrollToAya = null
                )
            is SurahResult.Error ->
                preState.copy(
                    base = BaseState.withError(result.err)
                )
            SurahResult.Loading ->
                preState.copy(
                    base = BaseState.loading()
                )
            is SurahResult.Items ->
                preState.copy(
                    items = result.items
                )
            is SurahResult.ShowAyaNumber ->
                preState.copy(
                    scrollToAya = ScrollToAya(result.id, result.orderID, result.position)
                )

            is SurahResult.SettingsSurahNameCalligraphies ->
                preState.copy(settingsState = SurahSettingsState(surahNameCalligraphies = result.list))
            is SurahResult.SettingsAyaCalligraphies ->
                    preState.copy(settingsState = SurahSettingsState(ayaCalligraphies = result.list))
            is SurahResult.SettingsQuranFontSize ->
                preState.copy(
                    settingsState = SurahSettingsState(quranTextFontSize = result.fontSize)
                )
            is SurahResult.SettingsTranslateFontSize ->
                preState.copy(
                    settingsState = SurahSettingsState(translateTextFontSize = result.fontSize)
                )
            is SurahResult.SettingsSurahCalligraphy ->
                preState.copy(
                    settingsState = SurahSettingsState(selectedSurahNameCalligraphy = result.calligraphy)
                )
            is SurahResult.SettingsFirstAyaTranslationCalligraphy ->
                preState.copy(
                    settingsState = SurahSettingsState(selectedAyaCalligraphy = result.calligraphy)
                )

            is SurahResult.SettingsOpen ->
                preState.copy(
                    settingsState = SurahSettingsState(showSettings = true)
                )

        }
}