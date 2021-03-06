package com.islamversity.settings

import com.islamversity.core.*
import com.islamversity.core.mvi.BaseProcessor
import com.islamversity.domain.model.Calligraphy
import com.islamversity.domain.model.CalligraphyId
import com.islamversity.domain.model.QuranReadFontSize
import com.islamversity.domain.model.SettingsCalligraphy
import com.islamversity.domain.model.TranslateReadFontSize
import com.islamversity.domain.repo.CalligraphyRepo
import com.islamversity.domain.repo.SettingRepo
import com.islamversity.settings.models.CalligraphyUIModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

class SettingsProcessor(
    settingsRepo: SettingRepo,
    calligraphyRepo: CalligraphyRepo,
    uiMapper: Mapper<Calligraphy, CalligraphyUIModel>
) : BaseProcessor<SettingsIntent, SettingsResult>() {

    override fun transformers(): List<FlowBlock<SettingsIntent, SettingsResult>> = listOf(
        getSurahNameCalligraphies,
        getAllFirstTranslationCalligraphy,
        getAllSecondTranslationCalligraphies,
        getQuranFontSize,
        getTranslateFontSize,

        getCurrentSurahNameCalligraphy,
        getSecondTranslationCalligraphy,
        getFirstTranslationCalligraphy,

        changeSurahNameCalligraphy,
        changeAyaCalligraphy,
        changeSecondTranslationCalligraphy,
        setQuranFontSize,
        setTranslateFontSize
    )

    private val getSurahNameCalligraphies: FlowBlock<SettingsIntent, SettingsResult> = {
        ofType<SettingsIntent.Initial>()
            .flatMapMerge {
                calligraphyRepo.getAllSurahNameCalligraphies()
            }
            .map {
                SettingsResult.SecondSurahNameCalligraphies(
                    it.map {
                        uiMapper.map(it)
                    }
                )
            }
    }

    private val getAllFirstTranslationCalligraphy: FlowBlock<SettingsIntent, SettingsResult> = {
        ofType<SettingsIntent.Initial>()
            .flatMapMerge {
                calligraphyRepo.getAllAyaCalligraphies()
            }
            .map {
                SettingsResult.FirstTranslationCalligraphies(
                    uiMapper.listMap(it)
                )
            }
    }
    private val getAllSecondTranslationCalligraphies: FlowBlock<SettingsIntent, SettingsResult> = {
        ofType<SettingsIntent.Initial>().flatMapMerge {
            calligraphyRepo.getAllAyaCalligraphies()
        }
            .map {
                SettingsResult.SecondTranslationCalligraphies(
                    it.map {
                        uiMapper.map(it)
                    }
                )
            }
    }
    private val getSecondTranslationCalligraphy: FlowBlock<SettingsIntent, SettingsResult> = {
        ofType<SettingsIntent.Initial>().flatMapMerge {
            settingsRepo.getSecondAyaTranslationCalligraphy()
        }
            .map {
                SettingsResult.SecondTranslationCalligraphy(
                    if (it is SettingsCalligraphy.Selected) {
                        uiMapper.map(it.cal)
                    } else {
                        null
                    }
                )
            }
    }

    private val getQuranFontSize: FlowBlock<SettingsIntent, SettingsResult> = {
        ofType<SettingsIntent.Initial>()
            .flatMapMerge {
                settingsRepo.getAyaMainFontSize()
            }
            .map {
                SettingsResult.QuranFontSize(it.size)
            }
    }


    private val getTranslateFontSize: FlowBlock<SettingsIntent, SettingsResult> = {
        ofType<SettingsIntent.Initial>()
            .flatMapMerge {
                settingsRepo.getAyaTranslateFontSize()
            }
            .map {
                SettingsResult.TranslateFontSize(it.size)
            }
    }

    private val getCurrentSurahNameCalligraphy: FlowBlock<SettingsIntent, SettingsResult> = {
        ofType<SettingsIntent.Initial>()
            .flatMapMerge {
                settingsRepo.getSecondarySurahNameCalligraphy()
            }
            .map {
                SettingsResult.SecondSurahCalligraphy(
                    uiMapper.map(it)
                )
            }
    }

    private val getFirstTranslationCalligraphy: FlowBlock<SettingsIntent, SettingsResult> = {
        ofType<SettingsIntent.Initial>()
            .flatMapMerge {
                settingsRepo.getFirstAyaTranslationCalligraphy()
            }
            .map {
                SettingsResult.FirstTranslationCalligraphy(
                    if (it is SettingsCalligraphy.Selected) {
                        uiMapper.map(it.cal)
                    } else {
                        null
                    }
                )
            }
    }

    private val changeAyaCalligraphy: Flow<SettingsIntent>.() -> Flow<SettingsResult> = {
        ofType<SettingsIntent.NewFirstTranslation>()
            .flatMapMerge {
                calligraphyRepo.getCalligraphy(CalligraphyId(it.language.id))
                    .map {
                        it!!
                    }
            }
            .transform {
                settingsRepo.setFirstAyaTranslationCalligraphy(it)
                emit(SettingsResult.FirstTranslationCalligraphy(uiMapper.map(it)))
            }
    }

    private val changeSecondTranslationCalligraphy: Flow<SettingsIntent>.() -> Flow<SettingsResult> =
        {
            ofType<SettingsIntent.NewSecondTranslation>().flatMapMerge {
                calligraphyRepo.getCalligraphy(CalligraphyId(it.language.id)).map {
                    it!!
                }
            }.transform {
                settingsRepo.setSecondAyaTranslationCalligraphy(it)
                emit(SettingsResult.SecondTranslationCalligraphy(uiMapper.map(it)))
            }
        }

    private val changeSurahNameCalligraphy: Flow<SettingsIntent>.() -> Flow<SettingsResult> = {
        ofType<SettingsIntent.NewSecondSurahNameCalligraphySelected>()
            .flatMapMerge {
                calligraphyRepo.getCalligraphy(CalligraphyId(it.calligraphy.id))
                    .map {
                        it!!
                    }
            }
            .transform {
                settingsRepo.setSecondarySurahNameCalligraphy(it)
                emit(SettingsResult.SecondSurahCalligraphy(uiMapper.map(it)))
                Logger.log { it.toString() }
            }
    }

    private val setQuranFontSize: Flow<SettingsIntent>.() -> Flow<SettingsResult> = {
        ofType<SettingsIntent.ChangeQuranFontSize>()
            .transform {
                settingsRepo.setAyaMainFontSize(QuranReadFontSize(it.size))
                emit(SettingsResult.QuranFontSize(it.size))
            }
    }

    private val setTranslateFontSize: Flow<SettingsIntent>.() -> Flow<SettingsResult> = {
        ofType<SettingsIntent.ChangeTranslateFontSize>()
            .transform {
                settingsRepo.setAyaTranslateFontSize(TranslateReadFontSize(it.size))
                emit(SettingsResult.TranslateFontSize(it.size))
            }
    }
}