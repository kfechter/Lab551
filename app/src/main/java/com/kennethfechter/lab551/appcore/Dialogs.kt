package com.kennethfechter.lab551.appcore

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.kennethfechter.lab551.R
import com.kennethfechter.lab551.appcore.Utilities.getPackageInfo
import com.kennethfechter.lab551.databinding.ActivityLab551AboutBinding
import com.kennethfechter.lab551.databinding.DialogLab551DaynightModeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Dialogs {
    fun showToastPrompt(context: Context, message: String, length: Int) {
        Toast.makeText(context, message, length).show()
    }

    fun showNFCDisabledDialog(context: Context) {
        var dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        dialogBuilder.setMessage(R.string.nfc_disabled)
            .setCancelable(false)

            // positive button text and action
            .setPositiveButton(R.string.dialog_yes, DialogInterface.OnClickListener {
                    dialog, id -> dialog.dismiss()
                val intent = Intent(Settings.ACTION_NFC_SETTINGS)
                context.startActivity(intent)
            })

            // negative button text and action
            .setNegativeButton(R.string.dialog_no, DialogInterface.OnClickListener {
                    dialog, id -> dialog.cancel()
            })

            .show();
    }

    fun showAboutDialog(context: Context) {
        val layoutInflater = LayoutInflater.from(context)
        val developerProfiles = context.resources.getStringArray(R.array.developer_profiles)
        val binding = ActivityLab551AboutBinding.inflate(layoutInflater)
        binding.developersList.setOnItemClickListener{ _, _, position, _ ->
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(developerProfiles[position]))
                context.startActivity(browserIntent)
            } catch (e: ActivityNotFoundException) {
                Log.d("Calculendar", "Device does not have a browser available")
            }
        }
        val versionNameString = context.getPackageInfo().versionName
        binding.versionText.text = context.resources.getString(R.string.build_id_formatter).format(versionNameString)

        val aboutDialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .setTitle("Calculendar Developers")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }

        binding.developersList.adapter = ArrayAdapter(context, R.layout.developer_name_list_item, context.resources.getStringArray(R.array.developer_names))
        aboutDialog.show()
    }

    fun showThemeDialog(context: Context, currentTheme: Theme) {
        val themeDialogBuilder = AlertDialog.Builder(context)
        val layoutInflater = LayoutInflater.from(context)
        val binding = DialogLab551DaynightModeBinding.inflate(layoutInflater)
        var preferredDayNightMode = currentTheme

        when (preferredDayNightMode) {
            Theme.Day -> binding.radioDayMode.isChecked = true
            Theme.Night -> binding.radioNightMode.isChecked = true
            Theme.PowerSave -> binding.radioBatteryMode.isChecked = true
            Theme.System -> binding.radioAutoMode.isChecked = true
        }

        binding.radioDayMode.setOnCheckedChangeListener {
                _, isChecked -> if (isChecked) { preferredDayNightMode = Theme.Day }
        }

        binding.radioNightMode.setOnCheckedChangeListener {
                _, isChecked -> if (isChecked) { preferredDayNightMode = Theme.Night }
        }

        binding.radioBatteryMode.setOnCheckedChangeListener {
                _, isChecked -> if (isChecked) { preferredDayNightMode = Theme.PowerSave}
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            binding.radioAutoMode.visibility = View.VISIBLE

            binding.radioAutoMode.setOnCheckedChangeListener {
                    _, isChecked -> if (isChecked) { preferredDayNightMode = Theme.System }
            }
        }

        themeDialogBuilder.setPositiveButton("OK") { dialog, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                context.setAppTheme(preferredDayNightMode)
            }
            dialog.dismiss()
        }

        themeDialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        themeDialogBuilder.setTitle(R.string.theme_dialog_title)
        themeDialogBuilder.setView(binding.root)
        themeDialogBuilder.show()
    }

    fun showAnalyticsDialog(context: Context) {
        val analyticsDialogBuilder = AlertDialog.Builder(context)

        analyticsDialogBuilder.setTitle(R.string.opt_in_dialog_title)
        analyticsDialogBuilder.setMessage(R.string.opt_in_dialog_message)

        analyticsDialogBuilder.setNegativeButton("Opt-Out") { dialog, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                context.setAnalytics(false)
                dialog.dismiss()
            }
        }

        analyticsDialogBuilder.setPositiveButton("Opt-In") { dialog, _ ->
            CoroutineScope(Dispatchers.IO).launch {
                context.setAnalytics(true)
                dialog.dismiss()
            }
        }

        analyticsDialogBuilder.show()
    }
}