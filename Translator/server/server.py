from flask import Flask, request, jsonify
from googletrans import Translator
import asyncio

app = Flask(__name__)

# Initialize the translator
translator = Translator()

@app.route('/translate', methods=['POST'])
async def translate_text():
    """
    Async route using googletrans.
    """
    try:
        data = request.get_json(force=True)
        
        if not data or 'text' not in data:
            return jsonify({'error': 'Please provide "text" in JSON body'}), 400

        text_to_translate = data['text']
        print(f"Received: {text_to_translate}")

        result = translator.translate(text_to_translate, dest='zh-cn')
        
        print(f"Translated: {result.text}")

        return jsonify({
            'original_text': text_to_translate,
            'translated_text': result.text
        })

    except Exception as e:
        print(f"SERVER ERROR: {e}")
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    # Run the app
    app.run(host='0.0.0.0', port=5000, debug=True)