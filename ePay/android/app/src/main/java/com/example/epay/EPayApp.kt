package com.example.epay

import android.app.Application
import com.example.epay.data.EPayDatabase
import com.example.epay.data.PaymentRepository
import com.example.epay.data.RetrofitClient
import com.stripe.android.PaymentConfiguration

class EPayApp : Application() {
    lateinit var repository: PaymentRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = EPayDatabase.get(this)
        repository = PaymentRepository(RetrofitClient.api, db.transactionDao())
        PaymentConfiguration.init(this, BuildConfig.STRIPE_PUBLISHABLE_KEY)
    }
}