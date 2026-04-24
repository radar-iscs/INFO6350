# append a row to Google Sheets using a service account
import os
import logging
from datetime import datetime
from google.oauth2 import service_account
from googleapiclient.discovery import build

log = logging.getLogger(__name__)

SCOPES = ["https://www.googleapis.com/auth/spreadsheets"]
HEADERS = [
    "timestamp", "transaction_id", "amount", "currency",
    "note", "status", "customer_name", "google_email", "error_message"
]


def _service():
    key_file = os.getenv("GOOGLE_SERVICE_ACCOUNT_FILE")
    if not key_file or not os.path.exists(key_file):
        raise RuntimeError(f"Service account file not found: {key_file}")
    creds = service_account.Credentials.from_service_account_file(key_file, scopes=SCOPES)
    return build("sheets", "v4", credentials=creds, cache_discovery=False)


def append_transaction(payment_intent_id: str, amount: int, currency: str,
                       note: str, status: str, customer_name: str,
                       google_email: str | None, error_message: str | None):
    sheet_id = os.getenv("GOOGLE_SHEET_ID")
    tab = os.getenv("GOOGLE_SHEET_TAB", "Transactions")
    if not sheet_id:
        log.warning("GOOGLE_SHEET_ID not set — skipping log")
        return False

    row = [
        datetime.utcnow().isoformat() + "Z",
        payment_intent_id,
        f"{amount/100:.2f}",
        currency,
        note,
        status,
        customer_name,
        google_email or "",
        error_message or "",
    ]

    try:
        svc = _service()
        svc.spreadsheets().values().append(
            spreadsheetId=sheet_id,
            range=f"{tab}!A:I",
            valueInputOption="USER_ENTERED",
            insertDataOption="INSERT_ROWS",
            body={"values": [row]},
        ).execute()
        return True
    except Exception as e:
        log.error("Sheets append failed: %s", e)
        return False