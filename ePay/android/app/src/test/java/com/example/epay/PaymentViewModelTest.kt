package com.example.epay

import com.example.epay.data.PaymentRepository
import com.example.epay.data.PaymentResponse
import com.example.epay.data.TransactionDao
import com.example.epay.data.TransactionEntity
import com.example.epay.screens.PaymentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.*
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class PaymentViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    // Fake DAO + fake Api
    private val fakeDao = object : TransactionDao {
        override fun observeAll() = flowOf(emptyList<TransactionEntity>())
        override suspend fun upsert(entity: TransactionEntity) {}
    }

    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After  fun tearDown() { Dispatchers.resetMain() }

    @Test fun `empty amount disables submit`() {
        val repo = PaymentRepository(FakeApiSuccess(), fakeDao)
        val vm = PaymentViewModel(repo)
        assertFalse(vm.ui.value.canSubmit)
    }

    @Test fun `invalid amount produces error`() {
        val vm = PaymentViewModel(PaymentRepository(FakeApiSuccess(), fakeDao))
        vm.onAmount("abc")
        assertEquals("Invalid number", vm.ui.value.amountError)
        assertFalse(vm.ui.value.canSubmit)
    }

    @Test fun `valid amount enables submit`() {
        val vm = PaymentViewModel(PaymentRepository(FakeApiSuccess(), fakeDao))
        vm.onAmount("12.50")
        assertNull(vm.ui.value.amountError)
        assertTrue(vm.ui.value.canSubmit)
    }

    @Test fun `submit success populates result`() = runTest(dispatcher) {
        val vm = PaymentViewModel(PaymentRepository(FakeApiSuccess(), fakeDao))
        vm.onAmount("10.00"); vm.onCurrency("usd")
        vm.submit()
        advanceUntilIdle()
        assertTrue(vm.ui.value.result?.success == true)
        assertEquals("succeeded", vm.ui.value.result?.status)
    }
}

/** Minimal fakes — no MockK needed. */
private class FakeApiSuccess : com.example.epay.data.ApiService {
    override suspend fun health() = mapOf("status" to "ok")
    override suspend fun createPayment(req: com.example.epay.data.PaymentRequest) =
        PaymentResponse(true, "pi_test_123", "secret_xxx", "succeeded", "ok")
    override suspend fun logTransaction(req: com.example.epay.data.LogRequest) =
        com.example.epay.data.LogResponse(true, "logged")
}