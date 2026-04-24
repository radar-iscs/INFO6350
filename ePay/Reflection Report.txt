# Reflection Report

## **Why MVVM?**
Compose rebuilds UI from state, and ViewModels outlive configuration changes. So putting validation + network orchestration in ViewModels and subscribing screens to `StateFlow` gives survival across rotation with no manual saving/restoring.

## **Async handling**
Every network call is a `suspend` function. ViewModels launch them in `viewModelScope`, which cancels automatically when the VM clears. The repository wraps Retrofit calls in `withContext(Dispatchers.IO)`, so UI threads never block.

## **Security**
Secret key and service account are server-only; Android reads only build-time config. Cleartext is limited to loopback.

## **Errors solved**
- Credential Manager needs the *Web* OAuth client ID, not the Android one.
- PaymentIntent stuck at `requires_payment_method`. Then introduced a separate `currentIntentId` field that survives across the PaymentSheet hand-off, so `onPaymentSheetResult` knows which intent to finalize. Also removed the eager `append_transaction` call from `/payments/create` in Flask, so the Sheet row is written exactly once.
- Toast replaying after navigation. The bug was using `LaunchedEffect(state.outcome)` as the toast trigger.Then replaced the state-keyed effect with a `Channel<PaymentEvent>` on the ViewModel, collected once via `LaunchedEffect(Unit)`.
- Input should be blockable during post-Stripe finalization. Added a `finalizing` flag to the UI state, set to `true` on entering `onPaymentSheetResult` and cleared when the background work completes.
