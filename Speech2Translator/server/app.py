from flask import Flask, request, jsonify, render_template_string
from googletrans import Translator

app = Flask(__name__)
translator = Translator()

@app.route('/translate', methods=['POST'])
async def translate_text():
    try:
        data = request.get_json(force=True)
        if not data or 'text' not in data:
            return jsonify({'error': 'No text provided'}), 400

        text_to_translate = data['text']
        
        result = translator.translate(text_to_translate, dest='zh-cn')
        
        return jsonify({
            'original_text': text_to_translate,
            'translated_text': result.text
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)