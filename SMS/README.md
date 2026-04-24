# SMS demo - Jetpack Compose

A simple SMS Android app, allowing sending and intercepting messeges.

## Screenshot

- Test with sending sms

  ![App Screenshot](assets/send_sms.gif)

- Test with intercepting sms

  ![App Screenshot](assets/intercept_sms.gif)

## Run
```bash
telnet localhost 5554
auth <your_emulator_auth_token>
sms send +15555215554 This is RadarApp
```

