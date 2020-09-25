package com.islamversity.domain.repo

import com.islamversity.core.Logger
import com.islamversity.core.Mapper
import com.islamversity.db.datasource.CalligraphyLocalDataSource
import com.islamversity.db.datasource.SettingsDataSource
import com.islamversity.db.model.CalligraphyId
import com.islamversity.domain.model.Calligraphy
import com.islamversity.domain.model.QuranReadFontSize
import com.islamversity.domain.model.TranslateReadFontSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlin.coroutines.CoroutineContext
import com.islamversity.db.Calligraphy as CalligraphyEntity

interface SettingRepo {

    fun getCurrentSurahCalligraphy(context: CoroutineContext = Dispatchers.Default): Flow<Calligraphy>
    fun getCurrentQuranReadCalligraphy(context: CoroutineContext = Dispatchers.Default): Flow<Calligraphy>
    fun getQuranFontSize(context: CoroutineContext = Dispatchers.Default): Flow<QuranReadFontSize>
    fun getTranslateFontSize(context: CoroutineContext = Dispatchers.Default): Flow<TranslateReadFontSize>

    suspend fun setSurahCalligraphy(
        calligraphy: Calligraphy,
        context: CoroutineContext = Dispatchers.Default
    )

    suspend fun setQuranReadCalligraphy(
        calligraphy: Calligraphy,
        context: CoroutineContext = Dispatchers.Default
    )

    suspend fun setQuranReadFont(
        font: QuranReadFontSize,
        context: CoroutineContext = Dispatchers.Default
    )

    suspend fun setTranslateReadFont(
        font: TranslateReadFontSize,
        context: CoroutineContext = Dispatchers.Default
    )
}

private const val KEY_SURAH_CALLIGRAPHY = "KEY_SURAH_CALLIGRAPHY"
private const val KEY_QURAN_READ_CALLIGRAPHY = "KEY_QURAN_READ_CALLIGRAPHY"
private const val KEY_QURAN_READ_FONT_SIZE = "KEY_QURAN_READ_FONT_SIZE"
private const val KEY_TRANSLATE_READ_FONT_SIZE = "KEY_TRANSLATE_READ_FONT_SIZE"

class SettingRepoImpl(
    private val settingsDataSource: SettingsDataSource,
    private val calligraphyDS: CalligraphyLocalDataSource,
    private val mapper: Mapper<CalligraphyEntity, Calligraphy>,
) : SettingRepo {

    override fun getCurrentSurahCalligraphy(context: CoroutineContext): Flow<Calligraphy> =
        settingsDataSource.observeKey(
            KEY_SURAH_CALLIGRAPHY,
            {
                calligraphyDS.getArabicSurahCalligraphy().first().id.id
            },
            context
        )
            .flatMapMerge {
                calligraphyDS.getCalligraphyById(CalligraphyId(it))
            }
            .map {
                mapper.map(it!!)
            }

    override fun getCurrentQuranReadCalligraphy(context: CoroutineContext): Flow<Calligraphy> =
        settingsDataSource.observeKey(
            KEY_QURAN_READ_CALLIGRAPHY,
            {
                calligraphyDS.getArabicSimpleAyaCalligraphy().first().id.id
            },
            context
        )
            .flatMapMerge {
                calligraphyDS.getCalligraphyById(CalligraphyId(it))
            }
            .map {
                mapper.map(it!!)
            }


    override fun getQuranFontSize(context: CoroutineContext): Flow<QuranReadFontSize> =
        settingsDataSource.observeKey(
            KEY_QURAN_READ_FONT_SIZE,
            {
                QuranReadFontSize.DEFAULT.size.toString()
            },
            context
        )
            .map {
                QuranReadFontSize(it.toInt())
            }
            .onEach {
                Logger.log { "SettingRepo found quran fontSize=$it" }
            }

    override fun getTranslateFontSize(context: CoroutineContext): Flow<TranslateReadFontSize> =
        settingsDataSource.observeKey(
            KEY_TRANSLATE_READ_FONT_SIZE,
            {
                TranslateReadFontSize.DEFAULT.size.toString()
            },
            context
        )
            .map {
                TranslateReadFontSize(it.toInt())
            }
            .onEach {
                Logger.log { "SettingRepo found translate fontSize=$it" }
            }


    override suspend fun setSurahCalligraphy(calligraphy: Calligraphy, context: CoroutineContext) {
        settingsDataSource.put(KEY_SURAH_CALLIGRAPHY, calligraphy.id.id, context)
    }

    override suspend fun setQuranReadCalligraphy(
        calligraphy: Calligraphy,
        context: CoroutineContext
    ) {
        settingsDataSource.put(KEY_QURAN_READ_CALLIGRAPHY, calligraphy.id.id, context)
    }

    override suspend fun setQuranReadFont(font: QuranReadFontSize, context: CoroutineContext) {
        settingsDataSource.put(KEY_QURAN_READ_FONT_SIZE, font.size.toString(), context)
    }

    override suspend fun setTranslateReadFont(font: TranslateReadFontSize, context: CoroutineContext) {
        settingsDataSource.put(KEY_TRANSLATE_READ_FONT_SIZE, font.size.toString(), context)
    }
}