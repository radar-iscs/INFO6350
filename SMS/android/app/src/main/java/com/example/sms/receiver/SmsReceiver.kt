package com.example.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.sms.ui.ReceiveSmsActivity
import com.example.sms.utils.NotificationHelper

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        for (sms in messages) {
            val messageBody = sms.messageBody

            if (messageBody.contains("RadarApp", ignoreCase = true)) {
                val activityIntent = Intent(context, ReceiveSmsActivity::class.java)
                activityIntent.putExtra("sms_message", messageBody)
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(activityIntent)

                NotificationHelper.showNotification(context, messageBody)
            }
        }
    }
}
