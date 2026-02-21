from flask import Flask, request, jsonify, render_template
import gspread
from google.oauth2.service_account import Credentials
from datetime import datetime

app = Flask(__name__)

SCOPES = ["https://www.googleapis.com/auth/spreadsheets"]
creds = Credentials.from_service_account_file(
    "gen-lang-client-0426038503-6e431dbd08af.json", 
    scopes=SCOPES
)
client = gspread.authorize(creds)
spreadsheet = client.open_by_key("1KneeEbJFC4ypigGfV--_vHGyTnNiJgwz0uUWJgyrmCY")
sheet = spreadsheet.worksheet("Sheet1")

@app.route('/')
def index():
    return render_template('index.html')

@app.route('/api/record', methods=['POST'])
def add_record():
    data = request.json
    
    now = datetime.now()
    date_today = now.strftime("%Y-%m-%d")
    current_time = now.strftime("%H:%M:%S")

    first_name = data.get('first_name', '')
    last_name = data.get('last_name', '')
    time_in = data.get('time_in', '')
    time_out = data.get('time_out', '')
    notes = data.get('notes', '')

    try:
        sheet.append_row([
            date_today,
            current_time,
            first_name,
            last_name,
            time_in,
            time_out,
            notes
        ])
        return jsonify({"status": "success", "message": "Row successfully added!"}), 201
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, port=5000)