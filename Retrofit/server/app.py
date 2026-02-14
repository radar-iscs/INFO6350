from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/add', methods=['GET', 'POST'])
def add():
    a = 0
    b = 0
    
    if request.method == 'GET':
        a = request.args.get('a', type=float)
        b = request.args.get('b', type=float)
        
    elif request.method == 'POST':
        data = request.json
        a = data.get('a', 0)
        b = data.get('b', 0)

    result = a + b
    
    return jsonify({
        'a': a,
        'b': b,
        'result': result
    })

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)