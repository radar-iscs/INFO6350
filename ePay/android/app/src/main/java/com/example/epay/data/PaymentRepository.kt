package com.example.epay.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// sit between ViewModels and data sources
// call Flask to create a PaymentIntent
// cache the outcome in Room so HistoryScreen works offline
class PaymentRepository(
    private val api: ApiService,
    private val dao: TransactionDao
) {
    fun observeTransactions(): Flow<List<TransactionEntity>> = dao.observeAll()

    suspend fun createPayment(
        amount: Long, currency: String, note: String,
        customerName: String, customerEmail: String?, googleEmail: String?
    ): PaymentResponse = withContext(Dispatchers.IO) {
        val response = api.createPayment(
            PaymentRequest(amount, currency.lowercase(), note,
                customerName, customerEmail, googleEmail)
        )

        // cache the outcome locally, success or failure
        dao.upsert(TransactionEntity(
            paymentIntentId = response.paymentIntentId
                ?: "local_${System.currentTimeMillis()}",
            amount = amount,
            currency = currency.lowercase(),
            note = note,
            status = response.status ?: "failed",
            customerName = customerName,
            googleEmail = googleEmail,
            errorMessage = if (!response.success) response.error ?: response.message else null,
            timestamp = System.currentTimeMillis()
        ))
        response
    }

    // update the status of an existing local row + re-log to the sheet
    suspend fun updateStatus(
        paymentIntentId: String,
        newStatus: String,
        errorMessage: String? = null
    ) = withContext(Dispatchers.IO) {
        // update local cache
        val existing = dao.observeAll()
        val row = findById(paymentIntentId) ?: return@withContext
        dao.upsert(row.copy(status = newStatus, errorMessage = errorMessage,
            timestamp = System.currentTimeMillis()))

        // re-log to the sheet so the row there reflects the final outcome too
        runCatching {
            api.logTransaction(LogRequest(
                paymentIntentId = paymentIntentId,
                amount = row.amount,
                currency = row.currency,
                note = row.note,
                status = newStatus,
                customerName = row.customerName,
                googleEmail = row.googleEmail,
                errorMessage = errorMessage
            ))
        }
    }

    private suspend fun findById(id: String): TransactionEntity? =
        dao.findById(id)
}