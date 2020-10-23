package com.islamversity.surah.settings

import com.islamversity.core.FlowBlock
import com.islamversity.core.Mapper
import com.islamversity.core.listMap
import com.islamversity.core.ofType
import com.islamversity.domain.model.*
import com.islamversity.domain.repo.CalligraphyRepo
import com.islamversity.domain.repo.SettingRepo
import com.islamversity.surah.SurahIntent
import com.islamversity.surah.SurahResult
import com.islamversity.surah.model.CalligraphyUIModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

class SurahSettingsProcessor(
    settingsRepo: SettingRepo,
    calligraphyRepo: CalligraphyRepo,
    uiMapper: Mapper<Calligraphy, CalligraphyUIModel>
) {

    val getSurahNameCalligraphies: FlowBlock<SurahIntent, SurahResult> = {
        ofType<SurahIntent.SettingsInitial>()
            .flatMapMerge {
                calligraphyRepo.getAllSurahNameCalligraphies()
            }
            .map {
                SurahResult.SettingsSurahNameCalligraphies(
                    it.map {
                        uiMapper.map(it)
                    }
                )
            }
    }

    val getAllAyaCalligraphies: FlowBlock<SurahIntent, SurahResult> = {
        ofType<SurahIntent.SettingsInitial>()
            .flatMapMerge {
                calligraphyRepo.getAllAyaCalligraphies()
            }
            .map {
                SurahResult.SettingsAyaCalligraphies(
                    uiMapper.listMap(it)
                )
            }
    }

    val getQuranFontSize: FlowBlock<SurahIntent, SurahResult> = {
        ofType<SurahIntent.SettingsInitial>()
            .flatMapMerge {
                settingsRepo.getAyaMainFontSize()
            }
            .map {
                SurahResult.SettingsQuranFontSize(it.size)
            }
    }

    val getTranslateFontSize: FlowBlock<SurahIntent, SurahResult> = {
        ofType<SurahIntent.SettingsInitial>()
            .flatMapMerge {
                settingsRepo.getAyaTranslateFontSize()
            }
            .map {
                SurahResult.SettingsTranslateFontSize(it.size)
            }
    }

    val getCurrentSurahNameCalligraphy: FlowBlock<SurahIntent, SurahResult> = {
        ofType<SurahIntent.SettingsInitial>()
            .flatMapMerge {
                settingsRepo.getSecondarySurahNameCalligraphy()
            }
            .map {
                SurahResult.SettingsSurahCalligraphy(
                    uiMapper.map(it)
                )
            }
    }

    val getCurrentAyaCalligraphy: FlowBlock<SurahIntent, SurahResult> = {
        ofType<SurahIntent.SettingsInitial>()
            .flatMapMerge {
                settingsRepo.getFirstAyaTranslationCalligraphy()
            }
            .map {
                SurahResult.SettingsFirstAyaTranslationCalligraphy(
                    if (it is SettingsCalligraphy.Selected) {
                        uiMapper.map(it.cal)
                    } else {
                        null
                    }
                )
            }
    }

    val changeAyaCalligraphy: Flow<SurahIntent>.() -> Flow<SurahResult> = {
        ofType<SurahIntent.SettingsNewAyaCalligraphy>()
            .flatMapMerge {
                calligraphyRepo.getCalligraphy(CalligraphyId(it.language.id))
                    .map {
                        it!!
                    }
            }
            .transform {
                settingsRepo.setFirstAyaTranslationCalligraphy(it)
                emit(SurahResult.SettingsFirstAyaTranslationCalligraphy(uiMapper.map(it)))
            }
    }

    val changeSurahNameCalligraphy: Flow<SurahIntent>.() -> Flow<SurahResult> = {
        ofType<SurahIntent.SettingsNewSurahNameCalligraphySelected>()
            .flatMapMerge {
                calligraphyRepo.getCalligraphy(CalligraphyId(it.calligraphy.id))
                    .map {
                        it!!
                    }
            }
            .transform {
                settingsRepo.setSecondarySurahNameCalligraphy(it)
                emit(SurahResult.SettingsSurahCalligraphy(uiMapper.map(it)))
            }
    }

    val setQuranFontSize: Flow<SurahIntent>.() -> Flow<SurahResult> = {
        ofType<SurahIntent.SettingsChangeQuranFontSize>()
            .transform {
                settingsRepo.setAyaMainFontSize(QuranReadFontSize(it.size))
                emit(SurahResult.SettingsQuranFontSize(it.size))
            }
    }

    val setTranslateFontSize: Flow<SurahIntent>.() -> Flow<SurahResult> = {
        ofType<SurahIntent.SettingsChangeTranslateFontSize>()
            .transform {
                settingsRepo.setAyaTranslateFontSize(TranslateReadFontSize(it.size))
                emit(SurahResult.SettingsTranslateFontSize(it.size))
            }
    }

    val openSettings: Flow<SurahIntent>.() -> Flow<SurahResult> = {
        ofType<SurahIntent.OpenSettings>()
            .transform {
                emit(SurahResult.SettingsOpen(showSettings = true))
            }
    }
}