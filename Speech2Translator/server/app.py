from flask import Flask, request, jsonify, render_template_string
from googletrans import Translator

app = Flask(__name__)
translator = Translator()

HTML_PAGE = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Translator</title>
    <style>
        :root {
            --bg-color: #f1f8e9;
            --card-bg: #ffffff;
            --primary: #81c784;
            --primary-hover: #66bb6a;
            --text-color: #333333;
            --border-color: #e0e0e0;
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
            background-color: var(--bg-color);
            color: var(--text-color);
            display: flex;
            justify-content: center;
            align-items: center;
            min-height: 100vh;
            margin: 0;
        }

        .container {
            background-color: var(--card-bg);
            padding: 40px;
            border-radius: 16px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.05);
            width: 100%;
            max-width: 500px;
        }

        h1 {
            text-align: center;
            color: #2e7d32;
            margin-bottom: 30px;
            font-weight: 600;
            font-size: 1.5rem;
        }

        textarea {
            width: 100%;
            height: 120px;
            padding: 15px;
            border: 2px solid var(--border-color);
            border-radius: 12px;
            font-size: 16px;
            resize: vertical;
            box-sizing: border-box; 
            transition: border-color 0.3s ease;
            outline: none;
            font-family: inherit;
        }

        textarea:focus {
            border-color: var(--primary);
        }

        button {
            width: 100%;
            padding: 14px;
            margin-top: 20px;
            background-color: var(--primary);
            color: white;
            border: none;
            border-radius: 12px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: background-color 0.2s ease, transform 0.1s ease;
        }

        button:hover {
            background-color: var(--primary-hover);
        }

        button:active {
            transform: scale(0.98);
        }

        .result-box {
            margin-top: 30px;
            padding: 20px;
            background-color: #f9fbf9; 
            border-radius: 12px;
            border: 1px solid var(--border-color);
            min-height: 60px;
            font-size: 18px;
            color: #1b5e20;
        }
    </style>
</head>
<body>

    <div class="container">
        <h1>English to Chinese</h1>
        
        <textarea id="inputText" placeholder="Type something here..."></textarea>
        
        <button onclick="translateText()">Translate</button>
        
        <div class="result-box">
            <div id="result" />
        </div>
    </div>

    <script>
        async function translateText() {
            const textInput = document.getElementById('inputText');
            const resultDiv = document.getElementById('result');
            const btn = document.querySelector('button');

            if (!textInput.value.trim()) return;

            btn.innerText = "Translating...";
            btn.style.opacity = "0.7";
            
            try {
                const response = await fetch('/translate', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ text: textInput.value })
                });
                
                const data = await response.json();
                
                if (data.translated_text) {
                    resultDiv.innerText = data.translated_text;
                } else {
                    resultDiv.innerText = "Error: " + (data.error || "Unknown error");
                }
            } catch (err) {
                resultDiv.innerText = err;
            } finally {
                btn.innerText = "Translate";
                btn.style.opacity = "1";
            }
        }
    </script>
</body>
</html>
"""

@app.route('/')
def home():
    return render_template_string(HTML_PAGE)

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