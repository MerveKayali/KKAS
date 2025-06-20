from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.responses import JSONResponse
import os, uuid
from speech_to_text import transcribe_audio
from sentiment_analysis import analyze_sentiment
from topic_classification import classify_topic
from summarization import generate_summary

app = FastAPI()
UPLOAD_DIR = "temp"
os.makedirs(UPLOAD_DIR, exist_ok=True)

@app.post("/analyze")
async def analyze(file: UploadFile = File(...)):
    file_id = str(uuid.uuid4())
    file_path = os.path.join(UPLOAD_DIR, f"{file_id}_{file.filename}")

    try:
        # 1. Kaydet
        with open(file_path, "wb") as f:
            f.write(await file.read())

        # 2. Transkripsiyon
        transcript = transcribe_audio(file_path)

        # 3. Sentiment
        sentiment = analyze_sentiment(transcript)

        # 4. Topic
        topic = classify_topic(transcript)

        # 5. Summary (özet)
        #  metin çok uzunsa min_length/max_length parametrelerini ayarlayın
        summary = generate_summary(transcript, max_length=80, min_length=30)

        # Yanıtı hazırlayın
        result = {
            "transcript": transcript,
            "sentiment": sentiment,
            "topic": topic,
            "summary": summary
        }

        return JSONResponse(content=result)

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

    finally:
        # Temp dosyasını sil
        if os.path.exists(file_path):
            os.remove(file_path)
