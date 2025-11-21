from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/feedback', methods=['POST'])
def feedback():
    print("Incoming /feedback POST")
    print("Headers:", request.headers)
    print("Raw data:", request.data)
    try:
        data = request.get_json(force=True)
        print("Parsed JSON:", data)
        with open("feedbacks.jsonl", "a") as f:
            f.write(str(data) + "\n")
        return jsonify({"status": "success", "message": "Feedback received"}), 200
    except Exception as e:
        print("ERROR in /feedback:", e)
        return jsonify({"status": "error", "message": str(e)}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5003)

