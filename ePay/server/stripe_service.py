# wrapper around Stripe PaymentIntents
import os
import stripe
import logging

log = logging.getLogger(__name__)
stripe.api_key = os.getenv("STRIPE_SECRET_KEY", "")


def create_payment_intent(amount: int, currency: str, note: str,
                          customer_name: str, customer_email: str | None = None):
    
    # create a PaymentIntent. Retry once on transient API errors
    for attempt in range(2):
        try:
            intent = stripe.PaymentIntent.create(
                amount=amount,
                currency=currency,
                description=note or "ePay payment",
                receipt_email=customer_email,
                metadata={
                    "customer_name": customer_name,
                    "note": note,
                },
                automatic_payment_methods={"enabled": True},
            )
            return True, {
                "payment_intent_id": intent.id,
                "client_secret": intent.client_secret,
                "status": intent.status,
            }
        except stripe.error.CardError as e:
            return False, {"error": e.user_message or "Card declined", "status": "failed"}
        except stripe.error.APIConnectionError as e:
            log.warning("Stripe connection error attempt %d: %s", attempt, e)
            if attempt == 1:
                return False, {"error": "Network error reaching Stripe", "status": "failed"}
        except stripe.error.StripeError as e:
            return False, {"error": str(e), "status": "failed"}
    return False, {"error": "Unknown", "status": "failed"}


def retrieve_payment_intent(pi_id: str):
    try:
        intent = stripe.PaymentIntent.retrieve(pi_id)
        return True, {
            "payment_intent_id": intent.id,
            "status": intent.status,
        }
    except stripe.error.StripeError as e:
        return False, {"error": str(e)}