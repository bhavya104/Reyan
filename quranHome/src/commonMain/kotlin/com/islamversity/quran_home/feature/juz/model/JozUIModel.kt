package com.islamversity.quran_home.feature.juz.model

data class JozUIModel(
    val number: Long,
    val startingSurahID: String,
    val startingSurahName : String,
    val endingSurahName : String,
    val startingAyaOrder : Long,
    val endingAyaOrder : Long,
)