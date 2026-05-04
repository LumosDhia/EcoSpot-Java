import requests
import base64
import os

IMAGE_PATH = r"c:\Users\LumosDhia\Documents\School\Ecospot-Java\uploads\tickets\7e5c3624-58b8-40e1-87e7-f8e51964fb9a.jpg"
URL = "http://127.0.0.1:8001"

def test_api():
    if not os.path.exists(IMAGE_PATH):
        print(f"Error: Image not found at {IMAGE_PATH}")
        return

    with open(IMAGE_PATH, "rb") as f:
        img_base64 = base64.b64encode(f.read()).decode('utf-8')

    # 1. Test Enroll
    print("Testing /enroll...")
    enroll_data = {
        "user_id": "test_user@example.com",
        "image": img_base64
    }
    r = requests.post(f"{URL}/enroll", json=enroll_data)
    print(f"Status: {r.status_code}")
    print(f"Response: {r.json()}")

    if r.status_code != 200:
        print("Enrollment failed (likely no face found in the ticket image).")
        return

    # 2. Test Recognize
    print("\nTesting /recognize...")
    recognize_data = {
        "image": img_base64
    }
    r = requests.post(f"{URL}/recognize", json=recognize_data)
    print(f"Status: {r.status_code}")
    print(f"Response: {r.json()}")

if __name__ == "__main__":
    test_api()
