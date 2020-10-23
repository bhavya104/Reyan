package com.islamversity.surah.view.settings

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import com.islamversity.domain.model.QuranReadFontSize
import com.islamversity.surah.SurahIntent
import com.islamversity.surah.SurahState
import com.islamversity.surah.databinding.DialogSettingsBinding
import com.islamversity.surah.view.utils.OnSettings
import com.warkiz.widget.IndicatorSeekBar
import com.warkiz.widget.OnSeekChangeListener
import com.warkiz.widget.SeekParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class SurahSettingsView(
    context: Context,
    private val onSettings: OnSettings
) : Dialog(context), CoroutineScope by MainScope() {
    private var binding: DialogSettingsBinding? = null
    var isInitiated = false
    private var ayaInit = false
    private var translateInit = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSettingsBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        binding!!.ayaFontSizeSeekBar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams) {
                onSettings.offer(SurahIntent.SettingsChangeQuranFontSize(seekParams.progress))
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
                hideView()
                binding!!.dialogContainer.background = null
                window!!.setDimAmount(0F)
            }

            private fun hideView() {
                binding?.apply {
                    ayaFontSizeTitle.visibility = View.INVISIBLE
                    transLateFontSizeTitle.visibility = View.INVISIBLE
                    transLateFontSizeSeekBar.visibility = View.INVISIBLE
                }
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                dismiss()
            }
        }
        binding!!.transLateFontSizeSeekBar.onSeekChangeListener = object : OnSeekChangeListener {
            override fun onSeeking(seekParams: SeekParams) {
                onSettings.offer(SurahIntent.SettingsChangeTranslateFontSize(seekParams.progress))
            }

            override fun onStartTrackingTouch(seekBar: IndicatorSeekBar?) {
                hideView()
                binding!!.dialogContainer.background = null
                window!!.setDimAmount(0F)
            }

            private fun hideView() {
                binding?.apply {
                    ayaFontSizeTitle.visibility = View.INVISIBLE
                    ayaFontSizeSeekBar.visibility = View.INVISIBLE
                    transLateFontSizeTitle.visibility = View.INVISIBLE
                }
            }

            override fun onStopTrackingTouch(seekBar: IndicatorSeekBar?) {
                dismiss()
            }
        }

    }

    fun render(surahState: SurahState) {
        val state = surahState.settingsState
        state?.let {
            if (it.quranTextFontSize != QuranReadFontSize.DEFAULT.size && !ayaInit) {
                ayaInit = true
                binding!!.ayaFontSizeSeekBar.setProgress(it.quranTextFontSize.toFloat())
            }
            if (it.quranTextFontSize != QuranReadFontSize.DEFAULT.size && !translateInit) {
                translateInit = true
                binding!!.transLateFontSizeSeekBar.setProgress(it.translateTextFontSize.toFloat())
            }
            isInitiated = ayaInit && translateInit
        }

    }

}