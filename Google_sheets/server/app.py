import os
from flask import Flask, request, jsonify, render_template_string, redirect, url_for, session
from authlib.integrations.flask_client import OAuth
from dotenv import load_dotenv
import gspread
from google.oauth2.service_account import Credentials
from google.oauth2 import id_token
from google.auth.transport import requests as google_requests
from datetime import datetime
from enum import Enum

load_dotenv()

app = Flask(__name__)
app.secret_key = os.getenv("FLASK_SECRET_KEY", "fallback_secret_key_for_dev")
app.config.update(
    SESSION_COOKIE_HTTPONLY=True,
    SESSION_COOKIE_SECURE=False,  
    SESSION_COOKIE_SAMESITE="Lax",
)

# SSO setup
oauth = OAuth(app)
google = oauth.register(
    name='google',
    client_id=os.getenv("GOOGLE_CLIENT_ID"),
    client_secret=os.getenv("GOOGLE_CLIENT_SECRET"),
    server_metadata_url='https://accounts.google.com/.well-known/openid-configuration',
    client_kwargs={'scope': 'openid email profile'}
)

# sheets setup
SCOPES = ["https://www.googleapis.com/auth/spreadsheets"]
creds = Credentials.from_service_account_file(
    "gen-lang-client-0426038503-6e431dbd08af.json", 
    scopes=SCOPES
)
client = gspread.authorize(creds)
spreadsheet = client.open_by_key("1KneeEbJFC4ypigGfV--_vHGyTnNiJgwz0uUWJgyrmCY")
sheet1 = spreadsheet.worksheet("Sheet1")
sheet2 = spreadsheet.worksheet("Sheet2")

LOGIN_PAGE = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Login Required</title>
    <style>
        body { font-family: sans-serif; background-color: #f1f8e9; display: flex; justify-content: center; align-items: center; height: 100vh; margin: 0; }
        .login-card { background: white; padding: 40px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0,0,0,0.05); text-align: center; }
        .login-btn { display: inline-block; margin-top: 20px; padding: 12px 24px; background-color: #4285F4; color: white; text-decoration: none; border-radius: 8px; font-weight: bold; }
    </style>
</head>
<body>
    <div class="login-card">
        <h2>Sheets Logger App</h2>
        <p>You must be logged in to submit records.</p>
        <a href="/login" class="login-btn">Sign in with Google</a>
    </div>
</body>
</html>
"""

HTML_PAGE = """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Google Sheets Logger</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f1f8e9; max-width: 400px; margin: 40px auto; padding: 20px; }
        .user-profile { text-align: right; margin-bottom: 20px; padding-bottom: 15px; border-bottom: 1px solid #ccc; font-size: 14px;}
        .logout-link { color: #d32f2f; text-decoration: none; font-weight: 600; margin-left: 15px; }
        label { display: block; margin-top: 10px; font-weight: bold; }
        input, textarea { width: 100%; padding: 8px; margin-top: 5px; box-sizing: border-box; }
        button { margin-top: 20px; padding: 10px 20px; background-color: #28a745; color: white; border: none; cursor: pointer; }
        #message { margin-top: 15px; font-weight: bold; }
    </style>
</head>
<body>
    <div class="user-profile">
        Welcome, <strong>{{ user['name'] }}</strong><br>
        <span style="color: #666;">{{ user['email'] }}</span><br>
        <a href="/logout" class="logout-link">Logout</a>
    </div>

    <h2>Log New Record</h2>
    <form id="recordForm">
        <label>First Name:</label><input type="text" id="first_name" required>
        <label>Last Name:</label><input type="text" id="last_name" required>
        <label>Time In:</label><input type="time" id="time_in" required>
        <label>Time Out:</label><input type="time" id="time_out" required>
        <label>Notes:</label><textarea id="notes" rows="3"></textarea>
        <button type="submit">Submit to Sheets</button>
    </form>
    <div id="message"></div>

    <script>
        document.getElementById('recordForm').addEventListener('submit', async function(e) {
            e.preventDefault();
            const data = {
                first_name: document.getElementById('first_name').value,
                last_name: document.getElementById('last_name').value,
                time_in: document.getElementById('time_in').value,
                time_out: document.getElementById('time_out').value,
                notes: document.getElementById('notes').value,
                resource: "Web",
            };

            const response = await fetch('/api/record', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(data)
            });

            if (response.status === 401) {
                window.location.href = '/';
                return;
            }

            const result = await response.json();
            const messageDiv = document.getElementById('message');
            if (response.ok) {
                messageDiv.style.color = 'green';
                messageDiv.textContent = result.message;
                document.getElementById('recordForm').reset();
            } else {
                messageDiv.style.color = 'red';
                messageDiv.textContent = "Error: " + result.message;
            }
        });
    </script>
</body>
</html>
"""

@app.route('/')
def home():
    user = session.get('user')
    if not user:
        return render_template_string(LOGIN_PAGE)
    return render_template_string(HTML_PAGE, user=user)

@app.route('/login')
def login():
    redirect_uri = url_for('callback', _external=True)
    return google.authorize_redirect(redirect_uri, prompt='select_account')

@app.route('/callback')
def callback():
    token = google.authorize_access_token()
    resp = google.get('https://www.googleapis.com/oauth2/v3/userinfo', token=token)
    session['user'] = resp.json()
    return redirect(url_for('home'))

@app.route('/logout')
def logout():
    session.clear()
    return redirect(url_for('home'))

@app.route('/api/record', methods=['POST'])
def add_record():
    user_email = None
    user_name = "Unknown User"

    auth_header = request.headers.get('Authorization')
    if auth_header and auth_header.startswith('Bearer '):
        token = auth_header.split(' ')[1]
        try:
            idinfo = id_token.verify_oauth2_token(
                token, 
                google_requests.Request(), 
                os.getenv("GOOGLE_CLIENT_ID")
            )
            user_email = idinfo.get('email')
            user_name = idinfo.get('name', 'Android User')
        except ValueError as e:
            return jsonify({'error': f'Invalid token: {str(e)}'}), 401
    
    elif 'user' in session:
        user_email = session['user']['email']
        user_name = session['user']['name']

    if not user_email:
        return jsonify({'error': 'Unauthorized. Please log in.'}), 401

    try:
        data = request.json
        now = datetime.now()
        date_today = now.strftime("%Y-%m-%d")
        current_time = now.strftime("%H:%M:%S")

        first_name = data.get('first_name', '')
        last_name = data.get('last_name', '')
        time_in = data.get('time_in', '')
        time_out = data.get('time_out', '')
        notes = data.get('notes', '')
        resource = data.get('resource', '')

        sheet1.append_row([
            date_today, current_time, first_name, last_name, time_in, time_out, notes
        ])

        sheet2.append_row([
            f"{date_today}@{current_time}", user_email, user_name, first_name, last_name, date_today, current_time, time_in, time_out, "Edit", notes, resource
        ])

        return jsonify({"status": "success", "message": "Row successfully added to both sheets!"}), 201
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, port=5000)