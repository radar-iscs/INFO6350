# Flask proxy between the Android app, Stripe and Google Sheets
import logging
from dotenv import load_dotenv
load_dotenv()

from flask import Flask, request, jsonify
from stripe_service import create_payment_intent, retrieve_payment_intent
from sheets_service import append_transaction

logging.basicConfig(level=logging.INFO)
app = Flask(__name__)


# helpers
def bad(msg, code=400):
    return jsonify(success=False, error=msg), code


# routes
@app.get("/health")
def health():
    return jsonify(status="ok")


@app.post("/payments/create")
def create_payment():
    data = request.get_json(silent=True) or {}
    try:
        amount = int(data.get("amount", 0))
        currency = str(data.get("currency", "usd")).lower()
        note = str(data.get("note", ""))
        name = str(data.get("customer_name", ""))
        email = data.get("customer_email")
        google_email = data.get("google_email")
    except (TypeError, ValueError):
        return bad("Invalid payload")

    if amount <= 0:
        return bad("Amount must be > 0")
    if not currency:
        return bad("Currency required")
    if not name:
        return bad("customer_name required")

    ok, result = create_payment_intent(amount, currency, note, name, email)

    if ok:
        return jsonify(
            success=True,
            payment_intent_id=result["payment_intent_id"],
            client_secret=result["client_secret"],
            status=result["status"],
            message="Payment intent created",
        )
    return jsonify(success=False, status="failed",
                   error=result.get("error", "Unknown")), 400


@app.get("/payments/<pi_id>")
def get_payment(pi_id):
    ok, data = retrieve_payment_intent(pi_id)
    if ok:
        return jsonify(success=True, **data)
    return jsonify(success=False, error=data.get("error")), 404


@app.post("/transactions/log")
def log_only():
    d = request.get_json(silent=True) or {}
    ok = append_transaction(
        payment_intent_id=d.get("payment_intent_id", ""),
        amount=int(d.get("amount", 0)),
        currency=d.get("currency", ""),
        note=d.get("note", ""),
        status=d.get("status", ""),
        customer_name=d.get("customer_name", ""),
        google_email=d.get("google_email"),
        error_message=d.get("error_message"),
    )
    return jsonify(success=ok, message="Logged" if ok else "Log failed")


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, debug=True)