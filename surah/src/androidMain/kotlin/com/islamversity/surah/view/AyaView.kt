package com.islamversity.surah.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.airbnb.epoxy.CallbackProp
import com.airbnb.epoxy.ModelProp
import com.airbnb.epoxy.ModelView
import com.islamversity.surah.databinding.RowAyaBinding
import com.islamversity.surah.model.AyaUIModel

@ModelView(
    autoLayout = ModelView.Size.MATCH_WIDTH_WRAP_HEIGHT,
    saveViewState = true
)
class AyaView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : LinearLayout(context, attributeSet) {

    private val binding = RowAyaBinding.inflate(LayoutInflater.from(context), this, true)

    @SuppressLint("SetTextI18n")
    @ModelProp
    fun model(surah: AyaUIModel){
        binding.tvAyaNumber.text = surah.order.toString()
        binding.tvAyaContent.text = surah.content

        binding.tvAyaContent.textSize = surah.fontSize.toFloat()
        binding.tvAyaNumber.textSize = surah.fontSize.toFloat()
    }

    @CallbackProp
    fun listener(listener: (() -> Unit)?) {
        if (listener == null) {
            binding.root.setOnClickListener(null)
        } else {
            binding.root.setOnClickListener {
                listener()
            }
        }
    }
}

