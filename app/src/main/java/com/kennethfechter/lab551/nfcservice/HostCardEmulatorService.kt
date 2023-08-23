package com.kennethfechter.lab551.nfcservice

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
import android.nfc.cardemulation.HostApduService
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.asLiveData
import com.kennethfechter.lab551.R
import com.kennethfechter.lab551.appcore.Dialogs
import com.kennethfechter.lab551.appcore.Utilities
import com.kennethfechter.lab551.appcore.readUID
import com.kennethfechter.lab551.appcore.setUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class HostCardEmulatorService: HostApduService() {
    companion object {
        val TAG = "Host Card Emulator"
        val STATUS_SUCCESS = "9000"
        val STATUS_FAILED = "6F00"
        val CLA_NOT_SUPPORTED = "6E00"
        val INS_NOT_SUPPORTED = "6D00"
        val AID = "A0000001020304"
        val SELECT_INS = "A4"
        val DEFAULT_CLA = "00"
        val MIN_APDU_LENGTH = 12
    }


    override fun onDeactivated(reason: Int) {
    }

    override fun onCreate() {
        super.onCreate()
        val channelID = "90ef5738-1412-443b-a2a6-15b981ee0849"
        val channelName = "Lab551 NFC Service"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(channelID, channelName, importance)
        mChannel.description = "NFC APDU Service"
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
        val notificationBuilder = NotificationCompat.Builder(this, channelID)
        val notification = notificationBuilder.setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()
        startForeground(1514, notification, FOREGROUND_SERVICE_TYPE_MANIFEST)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Dialogs.showToastPrompt(this@HostCardEmulatorService, "APDU Service Started", Toast.LENGTH_LONG)
        return super.onStartCommand(intent, flags, startId)
    }



    override fun processCommandApdu(commandApdu: ByteArray?,
                                    extras: Bundle?): ByteArray {
        Log.d(TAG, "APDU process command")

        if (commandApdu == null) {
            return Utilities.hexStringToByteArray(STATUS_FAILED)
        }

        val hexCommandApdu = Utilities.toHex(commandApdu)


        if (hexCommandApdu.length < MIN_APDU_LENGTH) {
            return Utilities.hexStringToByteArray(STATUS_FAILED)
        }

        if (hexCommandApdu.substring(0, 2) != DEFAULT_CLA) {
            return Utilities.hexStringToByteArray(CLA_NOT_SUPPORTED)
        }

        if (hexCommandApdu.substring(2, 4) != SELECT_INS) {
            return Utilities.hexStringToByteArray(INS_NOT_SUPPORTED)
        }

        if (hexCommandApdu.substring(10, 24) == AID) {
            val uid = runBlocking {
                applicationContext.readUID()
            }
            return Utilities.hexStringToByteArray(uid)

        } else {
            return Utilities.hexStringToByteArray(STATUS_FAILED)
        }
    }
}