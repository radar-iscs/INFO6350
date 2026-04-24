package com.example.sms.viewmodel

import android.telephony.SmsManager
import androidx.lifecycle.ViewModel
import com.example.sms.model.SmsModel

class SmsViewModel : ViewModel() {
    fun sendSms(smsModel: SmsModel) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(
            smsModel.phoneNumber,
            null,
            smsModel.message,
            null,
            null
        )
    }
}