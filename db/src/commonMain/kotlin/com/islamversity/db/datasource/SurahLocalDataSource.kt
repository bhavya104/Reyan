package com.islamversity.db.datasource

import com.islamversity.db.*
import com.islamversity.db.model.*
import com.islamversity.db.model.Calligraphy
import com.islamversity.db.model.Surah
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

interface SurahLocalDataSource {
    suspend fun insertSurah(
        surah: SurahWithFullName,
        context: CoroutineContext = Dispatchers.Default
    )

    suspend fun insertSurah(
        surahs: List<SurahWithFullName>,
        context: CoroutineContext = Dispatchers.Default
    )

    fun observeAllSurahs(
        arabicCalligraphy: CalligraphyId,
        mainCalligraphy: CalligraphyId,
        context: CoroutineContext = Dispatchers.Default
    ): Flow<List<SurahWithTwoName>>

    fun getSurahWithId(
        entityId: SurahId,
        arabicCalligraphy: CalligraphyId,
        mainCalligraphy: CalligraphyId,
        context: CoroutineContext = Dispatchers.Default
    ): Flow<SurahWithTwoName?>

    fun getSurahWithOrderAndCalligraphy(
        order: SurahOrderId,
        calligraphy: CalligraphyId,
        context: CoroutineContext = Dispatchers.Default
    ): Flow<Surah?>

    fun findSurahByName(
        nameQuery: String,
        arabicCalligraphy: CalligraphyId,
        mainCalligraphy: CalligraphyId,
        context: CoroutineContext = Dispatchers.Default
    ): Flow<List<SurahWithTwoName>>
}

class SurahLocalDataSourceImpl(
    private val surahQueries: SurahQueries,
    private val nameQueries: NameQueries
) : SurahLocalDataSource {

    override suspend fun insertSurah(surah: SurahWithFullName, context: CoroutineContext) {
        withContext(context) {
            //order is important
            surahQueries.insertSurah(
                surah.id,
                surah.order,
                surah.revealTypeId,
                surah.revealTypeFlag,
                surah.bismillahTypeFlag
            )
            insertName(surah.name)
        }
    }

    override suspend fun insertSurah(
        surahs: List<SurahWithFullName>,
        context: CoroutineContext
    ) {
        withContext(context) {
            surahQueries.transaction {
                surahs.forEach {
                    //order is important
                    surahQueries.insertSurah(
                        it.id,
                        it.order,
                        it.revealTypeId,
                        it.revealTypeFlag,
                        it.bismillahTypeFlag
                    )
                    insertName(it.name)
                }
            }
        }
    }

    override fun observeAllSurahs(
        arabicCalligraphy: CalligraphyId,
        mainCalligraphy: CalligraphyId,
        context: CoroutineContext
    ): Flow<List<SurahWithTwoName>> =
        surahQueries.getAllSurah(
            arabicCalligraphy,
            mainCalligraphy,
            surahWithTwoNameMapper
        )
            .asFlow()
            .mapToList(context)

    override fun getSurahWithId(
        entityId: SurahId,
        arabicCalligraphy: CalligraphyId,
        mainCalligraphy: CalligraphyId,
        context: CoroutineContext
    ): Flow<SurahWithTwoName?> =
        surahQueries.getSurahWithId(arabicCalligraphy, mainCalligraphy, entityId, surahWithTwoNameMapper)
            .asFlow()
            .mapToOneOrNull(context)

    override fun getSurahWithOrderAndCalligraphy(
        order: SurahOrderId,
        calligraphy: CalligraphyId,
        context: CoroutineContext
    ): Flow<Surah?> =
        surahQueries.getSurahWithOrder(calligraphy, order, surahMapper)
            .asFlow()
            .mapToOneOrNull(context)

    override fun findSurahByName(
        nameQuery: String,
        arabicCalligraphy: CalligraphyId,
        mainCalligraphy: CalligraphyId,
        context: CoroutineContext
    ): Flow<List<SurahWithTwoName>> =
        surahQueries.findSurahByName(arabicCalligraphy, mainCalligraphy, nameQuery, surahWithTwoNameMapper)
            .asFlow()
            .mapToList(context)

    private fun insertName(surahName: No_rowId_name) {
        nameQueries.insertName(
            surahName.id,
            surahName.rowId,
            surahName.calligraphy,
            surahName.content
        )
    }

    private val surahWithTwoNameMapper =
        { rowIndex: Long,
          id: SurahId,
          orderIndex: SurahOrderId,
          arabicName: String?,
          mainName: String?,
          revealFlag: RevealTypeFlag,
          bismillahFlag: BismillahTypeFlag,
          ayaCount: AyaOrderId ->
            SurahWithTwoName(
                rowIndex,
                id,
                orderIndex,
                arabicName!!,
                mainName!!,
                revealFlag,
                bismillahFlag,
                ayaCount.order
            )
        }

    private val surahMapper: (
        index: Long,
        id: SurahId,
        orderIndex: SurahOrderId,
        name: String?,
        revealFlag: RevealTypeFlag,
        bismillahFlag: BismillahTypeFlag
    ) -> Surah = { index, id, orderIndex, name, revealFlag, bismillahFlag ->
        Surah(
            index,
            id,
            orderIndex,
            name!!,
            revealFlag,
            bismillahFlag
        )
    }
}
