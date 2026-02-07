from flask import Flask, request, jsonify

app = Flask(__name__)

# Logic for GET and POST /add
@app.route('/add', methods=['GET', 'POST'])
def add():
    a = 0
    b = 0
    
    # Handle GET request (params in URL)
    if request.method == 'GET':
        a = request.args.get('a', type=float)
        b = request.args.get('b', type=float)
        
    # Handle POST request (JSON body)
    elif request.method == 'POST':
        data = request.json
        a = data.get('a', 0)
        b = data.get('b', 0)

    result = a + b
    
    # Return JSON matching AddResponse.kt
    return jsonify({
        'a': a,
        'b': b,
        'result': result
    })

if __name__ == '__main__':
    # Run on port 5000
    app.run(host='0.0.0.0', port=5000)