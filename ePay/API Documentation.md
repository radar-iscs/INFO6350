# `POST /payments/create`
## Request
```json
{ "amount": 2500, "currency": "usd", "note": "Lab fee",
  "customer_name": "Jeff", "customer_email": "jeff@example.com",
  "google_email": "jeff@gmail.com" }
```
## Response
```json
{ "success": true, "payment_intent_id": "pi_xxx",
  "client_secret": "pi_xxx_secret_yyy",
  "status": "requires_payment_method",
  "message": "Payment intent created" }
```

# `GET /payments/<id>` — fetch status
# `POST /transactions/log` — client-driven log row
# `GET /health` — liveness