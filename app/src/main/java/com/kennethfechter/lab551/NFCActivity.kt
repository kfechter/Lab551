package com.kennethfechter.lab551

import android.content.Intent
import android.nfc.NfcAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.asLiveData
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.kennethfechter.lab551.appcore.Dialogs
import com.kennethfechter.lab551.appcore.Theme
import com.kennethfechter.lab551.appcore.Utilities
import com.kennethfechter.lab551.appcore.readAnalytics
import com.kennethfechter.lab551.appcore.readTheme
import com.kennethfechter.lab551.appcore.readUID
import com.kennethfechter.lab551.appcore.setUID
import com.kennethfechter.lab551.nfcservice.HostCardEmulatorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NFCActivity : AppCompatActivity() {

    private lateinit var currentTheme: Theme
    private var analyticsEnabled = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfc)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        initializeAnalytics()
        initializeThemeObserver()
        checkOrGenerateUID()


        if(!Utilities.isRunningTest) {
            val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
            if (nfcAdapter != null) {
                if (!nfcAdapter.isEnabled) {
                    Dialogs.showNFCDisabledDialog(this@NFCActivity)
                }
                else {
                    val intent = Intent(this, HostCardEmulatorService::class.java) // Build the intent for the service
                    applicationContext.startForegroundService(intent)
                }
            }
        }
    }

    private fun initializeThemeObserver() {
        applicationContext.readTheme().asLiveData().observe(this) { theme ->
            when(theme) {
                Theme.Day -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Theme.Night -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Theme.PowerSave -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
                Theme.System -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                null -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }

            currentTheme = theme
        }
    }

    private fun initializeAnalytics() {
        applicationContext.readAnalytics().asLiveData().observe(this) { analyticsPreference ->
            when (analyticsPreference) {
                -1 -> if (!Utilities.isRunningTest) {
                    Dialogs.showAnalyticsDialog(
                        this@NFCActivity
                    )
                }
                0 -> if (!Utilities.isRunningTest) {
                    configureAnalytics(false)
                }
                1 -> if (!Utilities.isRunningTest) {
                    configureAnalytics(true)
                }
            }

            analyticsEnabled = analyticsPreference
        }
    }

    private fun configureAnalytics(analyticsEnabled: Boolean) {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(analyticsEnabled)
        FirebaseAnalytics.getInstance(this@NFCActivity).setAnalyticsCollectionEnabled(analyticsEnabled)
    }

    private fun checkOrGenerateUID() {
        CoroutineScope(Dispatchers.IO).launch {
            if (applicationContext.readUID() == "") {
                applicationContext.setUID(Utilities.generateID())
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_lab551_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.about_application -> Dialogs.showAboutDialog(this@NFCActivity)
            R.id.analytics_opt_status -> Dialogs.showAnalyticsDialog(
                this@NFCActivity
            )
            R.id.day_night_mode -> Dialogs.showThemeDialog(
                this@NFCActivity,
                currentTheme
            )
        }


        return super.onOptionsItemSelected(item)
    }
}