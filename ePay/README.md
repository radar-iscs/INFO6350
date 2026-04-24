# ePay — Kotlin Android + Flask Proxy + Stripe + Google Sheets

A minimal MVVM payments app. Android app collects amount/note, Flask creates a Stripe PaymentIntent, every transaction is appended to a Google Sheet.

## Screenshot

- A complete run with successful payment

  ![App Screenshot](assets/pay_success.gif)

- Test with classic Stripe card numbers

  ![App Screenshot](assets/pay_stripe_cards.gif)

- Test with payment failure

  ![App Screenshot](assets/pay_fail.gif)

- Test with dark mode

  ![App Screenshot](assets/dark_mode.gif)

- Test with other functions, like input validation, clearing payment history, information modification, logging out

  ![App Screenshot](assets/other_functions.gif)

## Architecture

![App Screenshot](assets/Architecture%20Diagram.png)

## Run
```bash
cd server
pip install -r requirements.txt
cp .env.example .env   # fill in values
python app.py
```
