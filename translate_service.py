from flask import Flask, request, jsonify
from deep_translator import GoogleTranslator
import os

app = Flask(__name__)

@app.route('/translate', methods=['POST'])
def translate_text():
    try:
        data = request.get_json(force=True)
        text = data.get('text', '')
        target_lang = data.get('target', 'en')
        
        if not text:
            return jsonify({"status": "error", "message": "No text provided"}), 400
            
        print(f"[Translate Service] Translating to {target_lang}...")
        translated = GoogleTranslator(source='auto', target=target_lang).translate(text)
        return jsonify({"status": "success", "translated_text": translated})
    except Exception as e:
        print(f"[Translate Service] Error: {str(e)}")
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    port = 8002
    print(f"🚀 EcoSpot Translation Service starting on port {port}...")
    app.run(port=port, debug=True)
