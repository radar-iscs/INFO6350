from flask import Flask, request, jsonify
from flask_cors import CORS

app = Flask(__name__)
CORS(app) # allows requests from your Android app/emulator

@app.route("/translate", methods=["POST"])
def translate():
    data = request.get_json(silent=True) or {}
    
    text = data.get("text", "")
    source_language = data.get("sourceLanguage", "auto")
    target_language = data.get("targetLanguage", "en")
    provider = data.get("provider", "ChatGPT/OpenAI")

    if not text.strip():
        return jsonify({
            "translatedText": "",
            "providerUsed": provider,
            "detectedLanguage": source_language,
            "status": "error",
            "message": "Text is required"
        }), 400

    # Mock behavior depending on provider
    if provider == "ChatGPT/OpenAI":
        translated_text = f"[OpenAI Flask Mock] Translation to {target_language}: {text}"
    elif provider == "Google":
        translated_text = f"[Google Flask Mock] Translation to {target_language}: {text.upper()}"
    elif provider == "Open Source":
        translated_text = f"[Open Source Flask Mock] {text} -> translated to {target_language}"
    else:
        translated_text = f"[Unknown Provider Mock] Translation to {target_language}: {text}"
    
    return jsonify({
        "translatedText": translated_text,
        "providerUsed": provider,
        "detectedLanguage": source_language,
        "status": "success"
        })

@app.route("/", methods=["GET"])
def home():
    return jsonify({
        "message": "Flask translation mock backend is running",
        "endpoint": "/translate",
        "method": "POST"
    })

if __name__ == "__main__":
    # 0.0.0.0 makes it reachable from emulator/device on your local network
    app.run(host="0.0.0.0", port=8080, debug=True)