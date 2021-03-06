package com.islamversity.search.view

import com.islamversity.core.Mapper
import com.islamversity.search.model.SurahUIModel
import com.islamversity.view_component.SurahItemModel

class SurahUIItemMapper : Mapper<SurahUIModel, SurahItemModel> {
    override fun map(item: SurahUIModel): SurahItemModel =
        SurahItemModel(
            item.id.id,
            item.arabicName,
            item.mainName,
            item.order.toString(),
            SurahItemModel.RevealedType(item.revealedType.rawName),
            item.ayaCount.toString(),
        )
}
