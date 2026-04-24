import json
from unittest.mock import patch
import app as flask_app


def client():
    flask_app.app.config["TESTING"] = True
    return flask_app.app.test_client()


def test_health():
    r = client().get("/health")
    assert r.status_code == 200
    assert r.get_json()["status"] == "ok"


def test_create_payment_rejects_zero_amount():
    r = client().post("/payments/create",
                      data=json.dumps({"amount": 0, "currency": "usd",
                                       "customer_name": "X"}),
                      content_type="application/json")
    assert r.status_code == 400
    assert r.get_json()["success"] is False


@patch("app.append_transaction", return_value=True)
@patch("app.create_payment_intent")
def test_create_payment_happy_path(mock_create, _mock_log):
    mock_create.return_value = (True, {
        "payment_intent_id": "pi_test",
        "client_secret": "secret",
        "status": "succeeded",
    })
    r = client().post("/payments/create",
                      data=json.dumps({
                          "amount": 2500, "currency": "usd",
                          "note": "lab fee", "customer_name": "Jeff"
                      }),
                      content_type="application/json")
    body = r.get_json()
    assert r.status_code == 200
    assert body["success"] is True
    assert body["payment_intent_id"] == "pi_test"


@patch("app.append_transaction", return_value=True)
@patch("app.create_payment_intent", return_value=(False, {"error": "Card declined", "status": "failed"}))
def test_create_payment_declined(_mock_create, _mock_log):
    r = client().post("/payments/create",
                      data=json.dumps({"amount": 100, "currency": "usd",
                                       "customer_name": "Jeff"}),
                      content_type="application/json")
    assert r.status_code == 400
    assert r.get_json()["error"] == "Card declined"