import os
import json
import base64
import cv2
import numpy as np
import tempfile
import logging
import sys

# Ensure logs go to the absolute path of the current directory to avoid confusion
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
LOG_FILE = os.path.join(BASE_DIR, "..", "face_service.log")

# Setup logger with immediate flush
logger = logging.getLogger("face_service")
logger.setLevel(logging.INFO)

if not logger.handlers:
    # File Handler with immediate flush
    fh = logging.FileHandler(LOG_FILE, mode='a', encoding='utf-8')
    fh.setFormatter(logging.Formatter('%(asctime)s - %(levelname)s - %(message)s'))
    logger.addHandler(fh)
    
    # Stream Handler
    sh = logging.StreamHandler(sys.stdout)
    sh.setFormatter(logging.Formatter('%(levelname)s: %(message)s'))
    logger.addHandler(sh)

def log_info(msg):
    logger.info(msg)
    for h in logger.handlers:
        h.flush()

log_info("--- Face Service Script Loaded ---")
log_info(f"Python version: {sys.version}")
log_info(f"Working directory: {os.getcwd()}")

# FastAPI App Initialization
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel

app = FastAPI()

# Allow CORS for Symfony frontend
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # Allow all for easier testing
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

DB_PATH = "faces_db.json"

def load_db():
    if not os.path.exists(DB_PATH):
        return {}
    with open(DB_PATH, "r") as f:
        return json.load(f)

def save_db(db):
    with open(DB_PATH, "w") as f:
        json.dump(db, f)

class EnrollRequest(BaseModel):
    image: str
    user_id: str

class RecognizeRequest(BaseModel):
    image: str

def base64_to_tempfile(base64_string):
    try:
        if "," in base64_string:
            base64_string = base64_string.split(",")[1]
        img_data = base64.b64decode(base64_string)
        
        # DeepFace usually takes a path or a numpy array
        # Creating a temporary file is often most reliable
        fd, path = tempfile.mkstemp(suffix=".jpg")
        with os.fdopen(fd, 'wb') as tmp:
            tmp.write(img_data)
        return path
    except Exception as e:
        raise HTTPException(status_code=400, detail="Invalid image data")

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/enroll")
async def enroll(req: EnrollRequest):
    log_info(f"Enrollment request for user: {req.user_id}")
    img_path = base64_to_tempfile(req.image)
    try:
        from deepface import DeepFace
        log_info("Extracting face representation (VGG-Face)...")
        objs = DeepFace.represent(img_path, model_name="VGG-Face", enforce_detection=True)
        if not objs:
            log_info("No face detected in enrollment image")
            raise HTTPException(status_code=400, detail="No face detected")
        
        encoding = objs[0]["embedding"]
        db = load_db()
        db[req.user_id] = encoding
        save_db(db)
        log_info(f"Successfully enrolled user: {req.user_id}")
        return {"status": "success", "user_id": req.user_id}
    except HTTPException as e:
        raise e
    except Exception as e:
        logger.error(f"Error during enrollment: {str(e)}", exc_info=True)
        raise HTTPException(status_code=400, detail=str(e))
    finally:
        if os.path.exists(img_path):
            os.remove(img_path)

@app.post("/recognize")
async def recognize(req: RecognizeRequest):
    img_path = base64_to_tempfile(req.image)
    db = load_db()
    
    if not db:
        raise HTTPException(status_code=401, detail="No faces enrolled")

    known_user_ids = list(db.keys())
    
    try:
        from deepface import DeepFace
        log_info("Extracting face representation for recognition...")
        objs = DeepFace.represent(img_path, model_name="VGG-Face", enforce_detection=True)
        if not objs:
            log_info("No face detected in recognition image")
            raise HTTPException(status_code=401, detail="No face detected")
        
        unknown_encoding = np.array(objs[0]["embedding"])
        
        best_match = None
        min_dist = 1000
        
        # Use simple Euclidean distance for matching
        for uid in known_user_ids:
            known_encoding = np.array(db[uid])
            dist = np.linalg.norm(known_encoding - unknown_encoding)
            if dist < min_dist:
                min_dist = dist
                best_match = uid
        
        # Threshold for VGG-Face Euclidean distance is around 0.4 - 0.6
        if best_match and min_dist < 0.55:
            log_info(f"Match found: {best_match} (dist: {min_dist:.4f})")
            return {"user_id": best_match, "confidence": float(1 - min_dist)}
        
        log_info(f"No match found. Best distance: {min_dist:.4f}")
        raise HTTPException(status_code=401, detail="Face not recognized")
    except HTTPException as e:
        raise e
    except Exception as e:
        log_info(f"Error during recognition: {str(e)}")
        raise HTTPException(status_code=401, detail=str(e))
    finally:
        if os.path.exists(img_path):
            os.remove(img_path)

if __name__ == "__main__":
    import uvicorn
    log_info("Starting Uvicorn server on 0.0.0.0...")
    uvicorn.run(app, host="0.0.0.0", port=8001, log_level="info")
