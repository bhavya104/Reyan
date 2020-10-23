package com.islamversity.surah.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.islamversity.base.CoroutineView
import com.islamversity.base.ext.setHidable
import com.islamversity.core.mvi.MviPresenter
import com.islamversity.daggercore.CoreComponent
import com.islamversity.navigation.model.SurahLocalModel
import com.islamversity.navigation.model.fromData
import com.islamversity.surah.SurahIntent
import com.islamversity.surah.SurahState
import com.islamversity.surah.databinding.ViewSurahBinding
import com.islamversity.surah.di.DaggerSurahComponent
import com.islamversity.surah.model.AyaUIModel
import com.islamversity.surah.model.SurahHeaderUIModel
import com.islamversity.surah.view.settings.SurahSettingsView
import com.islamversity.surah.view.utils.BuildFinishedScroller
import com.islamversity.surah.view.utils.OnSettings
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.merge
import javax.inject.Inject

class SurahView(
    bundle: Bundle
) : CoroutineView<ViewSurahBinding, SurahState, SurahIntent>(bundle) {

    private val settingsChannel = BroadcastChannel<SurahIntent>(Channel.BUFFERED)
    private var settingsDialog: SurahSettingsView? = null
    private val onSettings = object : OnSettings {
        override fun offer(settingsIntent: SurahIntent) {
            settingsChannel.offer(settingsIntent)
        }
    }
    private val surahLocal: SurahLocalModel =
        bundle
            .getString(SurahLocalModel.EXTRA_SURAH_DETAIL)!!
            .let {
                SurahLocalModel.fromData(it)
            }

    @Inject
    override lateinit var presenter: MviPresenter<SurahIntent, SurahState>


    override fun bindView(inflater: LayoutInflater, container: ViewGroup): ViewSurahBinding =
        ViewSurahBinding.inflate(inflater, container, false)

    override fun injectDependencies(core: CoreComponent) {
        DaggerSurahComponent
            .builder()
            .bindRouter(router)
            .coreComponent(core)
            .build()
            .inject(this)
    }

    override fun beforeBindingView(binding: ViewSurahBinding) {
        binding.ivBack.setOnClickListener { router.handleBack() }
        binding.fabUp.setOnClickListener { binding.ayaList.scrollToPosition(0) }
        binding.ayaList.setHidable(binding.fabUp, binding.tvSurahName,)
        binding.settings.setOnClickListener { settingsChannel.offer(SurahIntent.OpenSettings) }
        binding.tvSurahName.text = surahLocal.surahName
    }

    private fun openSettingsDialog(context: Context) {
        settingsDialog = SurahSettingsView(context, onSettings)
        settingsDialog?.show()
        settingsDialog?.setOnCancelListener { settingsDialog = null }
    }

    override fun intents(): Flow<SurahIntent> =
        listOf(
            flowOf(
                SurahIntent.Initial(surahLocal.surahID, surahLocal.startingAyaOrder)
            ), settingsChannel.asFlow()
        )
            .merge()

    override fun render(state: SurahState) {
        renderLoading(state.base)
        renderError(state.base)

        if (state.closeScreen) {
            router.popController(this)
        }

        binding.ayaList.withModelsAsync {
            state.items.forEach {
                if (it is SurahHeaderUIModel) {
                    surahHeader {
                        id(it.rowId)
                        model(
                            it
                        )
                    }
                }

                if (it is AyaUIModel) {
                    ayaView {
                        id(it.rowId)
                        model(it)
                    }
                }
            }

            if (state.scrollToAya != null) {
                addModelBuildListener(
                    BuildFinishedScroller(
                        state.scrollToAya.position,
                        this,
                        binding.ayaList
                    )
                )
            }
        }

        if (state.settingsState != null) {
            if (state.settingsState.showSettings && (settingsDialog == null || (settingsDialog != null && !settingsDialog!!.isShowing))) {
                openSettingsDialog(binding.ayaList.context)
                settingsChannel.offer(SurahIntent.SettingsInitial)
            } else if (settingsDialog != null && settingsDialog!!.isShowing && !settingsDialog!!.isInitiated) {
                settingsDialog!!.render(state)
            }
        }

    }
}


