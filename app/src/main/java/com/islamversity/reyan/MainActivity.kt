package com.islamversity.reyan

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akexorcist.localizationactivity.ui.LocalizationActivity
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.facebook.drawee.drawable.ScalingUtils
import com.facebook.drawee.view.DraweeTransition
import com.islamversity.core.Logger
import com.islamversity.core.Severity
import com.islamversity.daggercore.coreComponent
import com.islamversity.daggercore.helpers.ARABIC_LOCALE
import com.islamversity.daggercore.helpers.ENGLISH_LOCALE
import com.islamversity.daggercore.helpers.FARSI_LOCALE
import com.islamversity.daggercore.helpers.LanguageConfigure
import com.islamversity.daggercore.helpers.LanguageLocale
import com.islamversity.daggercore.lifecycle.LifecycleComponentProvider
import com.islamversity.daggercore.lifecycle.LifecycleEvent
import com.islamversity.daggercore.lifecycle.Permissions
import com.islamversity.daggercore.lifecycle.PermissionsResult
import com.islamversity.quran_home.feature.home.QuranHomeView
import com.islamversity.reyan.di.ActivityComponent
import com.islamversity.reyan.di.DaggerActivityComponent
import com.islamversity.reyan.service.NotificationDataType
import com.islamversity.reyan.service.NotificationDataType.Companion.NOTIFICATION_DATA_KEY
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.channels.Channel
import java.util.*
import javax.inject.Inject

class MainActivity : LocalizationActivity(),
    LifecycleComponentProvider,
    LanguageConfigure {

    private val LOGTAG = "MainActivity"

    @Inject
    lateinit var events: Channel<LifecycleEvent>

    private lateinit var router: Router

    private val component: ActivityComponent by lazyComponent {
        createComponent()
    }

    lateinit var appLanguages : List<LanguageLocale>

    override fun onCreate(savedInstanceState: Bundle?) {
        component.inject(this)
        appLanguages = resources.getStringArray(com.islamversity.daggercore.R.array.app_languages).toList().mapIndexed { index, s ->
            when (index) {
                0 -> LanguageLocale(ENGLISH_LOCALE, s)
                1 -> LanguageLocale(ARABIC_LOCALE, s)
                2 -> LanguageLocale(FARSI_LOCALE, s)
                else -> error("other languages are not supported please remove unintentional= $s at $index")
            }
        }

        window.sharedElementEnterTransition = DraweeTransition.createTransitionSet(
            ScalingUtils.ScaleType.CENTER_CROP, ScalingUtils.ScaleType.CENTER_CROP
        )
        window.sharedElementEnterTransition = DraweeTransition.createTransitionSet(
            ScalingUtils.ScaleType.CENTER_CROP, ScalingUtils.ScaleType.CENTER_CROP
        )

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        router = Conductor.attachRouter(this, root, savedInstanceState)
        val view = QuranHomeView()
        if (!router.hasRootController()) {
            router.setRoot(RouterTransaction.with(view))
        }

        handleIntents(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        intent?.also {
            handleIntents(it)
        }
    }

    private fun handleIntents(intent: Intent) {

        val intentExtra = intent.extras
        val notifData = intentExtra?.getBundle(NOTIFICATION_DATA_KEY)

        if (notifData != null ) {

            val notifType = notifData.get("type")

            if (notifType != null) {

                val nType = NotificationDataType(notifType.toString())

                handlePushNotifications(nType, notifData)
            }
        }
    }

    private fun handlePushNotifications(
        notifType: NotificationDataType,
        notifBundle: Bundle
    ){
        when(notifType) {
            NotificationDataType.NEW_VERSION -> {
                val appUrl = notifBundle["url"]
                Logger.log (Severity.Debug, LOGTAG, null, "Notification : googleStoreUrl = $appUrl")

                if (isPackageInstalled("com.android.vending", packageManager)) {
                    showAppOnGoogleStore()
                } else {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(appUrl.toString()))
                    startActivity(browserIntent)
                }
            }
            NotificationDataType.FORCE_UPDATE -> {
            }
            NotificationDataType.GENERIC -> {
                Logger.log (Severity.Debug, LOGTAG, null, "Notification : unknown notification Type string")
            }
        }
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getApplicationInfo(packageName, 0).enabled
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    private fun showAppOnGoogleStore() {

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://play.google.com/store/apps/details?id=$packageName"
            )
            setPackage("com.android.vending")
        }
        startActivity(intent)
    }

    override fun onBackPressed() {
        if (!router.handleBack()) {
            super.onBackPressed()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        events.offer(
            LifecycleEvent.Permission(
                requestCode,
                permissions.map { Permissions.fromRawValue(it) },
                grantResults.map { PermissionsResult.fromRawValue(it) }
            ))
    }

    override fun lifecycleComponent(): ActivityComponent =
        component

    private fun createComponent(): ActivityComponent =
        DaggerActivityComponent.builder().baseComponent(coreComponent()).build()

    override fun getSupportedLanguages(): List<LanguageLocale> =
        appLanguages

    override fun getCurrentLocale(): LanguageLocale =
        appLanguages.find { it.locale.language == getCurrentLanguage().language }!!

    override fun setNewLanguage(locale: Locale) {
        setLanguage(locale)
    }
}

internal class StoreViewModel : ViewModel() {

    var component: ActivityComponent? = null

    companion object {

        fun factory() = object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T =
                if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
                    StoreViewModel() as T
                } else {
                    throw IllegalArgumentException("this factory can only provide StoreViewModel")
                }
        }
    }
}

internal fun AppCompatActivity.lazyComponent(
    createComponent: () -> ActivityComponent
): Lazy<ActivityComponent> = lazy(LazyThreadSafetyMode.NONE) {
    val store = ViewModelProvider(
        this,
        StoreViewModel.factory()
    ).get(StoreViewModel::class.java)

    if (store.component != null) {
        store.component!!
    } else {
        store.component = createComponent()
        store.component!!
    }
}
