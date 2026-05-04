import os
import base64
import numpy as np
import cv2
import face_recognition
from flask import Flask, request, jsonify

app = Flask(__name__)

FACES_DIR = "registered_faces"
os.makedirs(FACES_DIR, exist_ok=True)

def decode_image(base64_string):
    # The string may contain a data URI prefix, e.g., 'data:image/jpeg;base64,'
    if "," in base64_string:
        base64_string = base64_string.split(",")[1]
    image_data = base64.b64decode(base64_string)
    nparr = np.frombuffer(image_data, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)
    return img

@app.route('/enroll', methods=['POST'])
def enroll():
    try:
        data = request.get_json(force=True)
    except Exception as e:
        return jsonify({"status": "error", "message": "Invalid JSON payload"}), 400

    if not data or 'image' not in data or 'user_id' not in data:
        return jsonify({"status": "error", "message": "Missing image or user_id"}), 400
    
    img = decode_image(data['image'])
    rgb_img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    encodings = face_recognition.face_encodings(rgb_img)
    
    if len(encodings) == 0:
        return jsonify({"status": "error", "message": "No face found in image"}), 400
    
    encoding = encodings[0]
    user_id = data['user_id']
    np.save(os.path.join(FACES_DIR, f"{user_id}.npy"), encoding)
    
    return jsonify({"status": "success", "message": "Face enrolled successfully"})

@app.route('/recognize', methods=['POST'])
def recognize():
    try:
        data = request.get_json(force=True)
    except Exception as e:
        return jsonify({"user_id": None, "message": "Invalid JSON payload"}), 400

    if not data or 'image' not in data:
        return jsonify({"user_id": None, "message": "Missing image"}), 400
    
    img = decode_image(data['image'])
    rgb_img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    encodings = face_recognition.face_encodings(rgb_img)
    
    if len(encodings) == 0:
        return jsonify({"user_id": None, "message": "No face found in image"}), 400
    
    unknown_encoding = encodings[0]
    
    best_match = None
    best_distance = 0.6  # Default threshold for face_recognition
    
    for filename in os.listdir(FACES_DIR):
        if filename.endswith(".npy"):
            user_id = filename[:-4]
            known_encoding = np.load(os.path.join(FACES_DIR, filename))
            
            distances = face_recognition.face_distance([known_encoding], unknown_encoding)
            if len(distances) > 0 and distances[0] < best_distance:
                best_distance = distances[0]
                best_match = user_id
                
    if best_match:
        return jsonify({"user_id": best_match})
    else:
        return jsonify({"user_id": None, "message": "Face not recognized"})

if __name__ == '__main__':
    print("Starting Face Recognition Service on port 8001...")
    app.run(port=8001, debug=True)
