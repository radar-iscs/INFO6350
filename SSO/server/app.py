import os
from flask import Flask, request, jsonify, render_template_string, redirect, url_for, session
from googletrans import Translator
from authlib.integrations.flask_client import OAuth
from dotenv import load_dotenv

load_dotenv()

app = Flask(__name__)

# ---------------- SECURITY & OAUTH CONFIG ----------------
app.secret_key = os.getenv("FLASK_SECRET_KEY", "fallback_secret_key_for_dev")

app.config.update(
    SESSION_COOKIE_HTTPONLY=True,
    SESSION_COOKIE_SECURE=False,  
    SESSION_COOKIE_SAMESITE="Lax",
)

oauth = OAuth(app)
google = oauth.register(
    name='google',
    client_id=os.getenv("GOOGLE_CLIENT_ID"),
    client_secret=os.getenv("GOOGLE_CLIENT_SECRET"),
    server_metadata_url='https://accounts.google.com/.well-known/openid-configuration',
    client_kwargs={
        'scope': 'openid email profile'
    }
)

translator = Translator()

# ---------------- HTML TEMPLATES ----------------

# The main translator page, updated with user info and a logout button
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
        body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; background-color: var(--bg-color); color: var(--text-color); display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }
        .container { background-color: var(--card-bg); padding: 40px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); width: 100%; max-width: 500px; }
        h1 { text-align: center; color: #2e7d32; margin-bottom: 30px; font-weight: 600; font-size: 1.5rem; }
        textarea { width: 100%; height: 120px; padding: 15px; border: 2px solid var(--border-color); border-radius: 12px; font-size: 16px; resize: vertical; box-sizing: border-box; outline: none; font-family: inherit; }
        textarea:focus { border-color: var(--primary); }
        button { width: 100%; padding: 14px; margin-top: 20px; background-color: var(--primary); color: white; border: none; border-radius: 12px; font-size: 16px; font-weight: 600; cursor: pointer; }
        button:hover { background-color: var(--primary-hover); }
        .result-box { margin-top: 30px; padding: 20px; background-color: #f9fbf9; border-radius: 12px; border: 1px solid var(--border-color); min-height: 60px; font-size: 18px; color: #1b5e20; }
        
        /* User Profile Styles */
        .user-profile { text-align: right; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid var(--border-color); font-size: 14px;}
        .logout-link { color: #d32f2f; text-decoration: none; font-weight: 600; margin-left: 15px; }
        .logout-link:hover { text-decoration: underline; }
    </style>
</head>
<body>
    <div class="container">
        <div class="user-profile">
            Welcome, <strong>{{ user['name'] }}</strong><br>
            <span style="color: #666;">{{ user['email'] }}</span><br>
            <a href="/logout" class="logout-link">Logout</a>
        </div>

        <h1>English to Chinese</h1>
        
        <textarea id="inputText" placeholder="Type something here..."></textarea>
        <button onclick="translateText()">Translate</button>
        <div class="result-box">
            <div id="result"></div>
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
                
                // If the server says unauthorized, redirect back to home/login
                if (response.status === 401) {
                    window.location.href = '/';
                    return;
                }
                
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

# A simple landing page if the user is not logged in
LOGIN_PAGE = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Login Required</title>
    <style>
        body { font-family: sans-serif; background-color: #f1f8e9; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
        .login-card { background: white; padding: 40px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); text-align: center; }
        h2 { color: #2e7d32; }
        .login-btn { display: inline-block; margin-top: 20px; padding: 12px 24px; background-color: #4285F4; color: white; text-decoration: none; border-radius: 8px; font-weight: bold; }
    </style>
</head>
<body>
    <div class="login-card">
        <h2>Translator App</h2>
        <p>You must be logged in to use the translator.</p>
        <a href="/login" class="login-btn">Sign in with Google</a>
    </div>
</body>
</html>
"""

# ---------------- ROUTES ----------------

@app.route('/')
def home():
    # Check if user is logged in
    user = session.get('user')
    if not user:
        # If not logged in, show the login button page
        return render_template_string(LOGIN_PAGE)
    
    # If logged in, show the translator and pass the user info to the HTML
    return render_template_string(HTML_PAGE, user=user)

@app.route('/login')
def login():
    redirect_uri = url_for('callback', _external=True)
    return google.authorize_redirect(redirect_uri, prompt='select_account')

@app.route('/callback')
def callback():
    token = google.authorize_access_token()
    resp = google.get('https://www.googleapis.com/oauth2/v3/userinfo', token=token)
    user_info = resp.json()
    
    session['user'] = {
        'name': user_info['name'],
        'email': user_info['email']
    }
    return redirect(url_for('home'))

@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('home'))

@app.route('/translate', methods=['POST'])
async def translate_text():
    # Protect the API route: Ensure only logged-in users can translate
    if 'user' not in session:
        return jsonify({'error': 'Unauthorized'}), 401

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