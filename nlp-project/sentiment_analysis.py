from transformers import pipeline

# Model: distilbert-base-uncased-finetuned-sst-2-english
# Çıktılar: 'POSITIVE', 'NEGATIVE'
classifier = pipeline("sentiment-analysis" , model="distilbert-base-uncased-finetuned-sst-2-english")

def analyze_sentiment(text: str) -> str:
    result = classifier(text[:512])[0]  # 512 karaktere sınırla (model limiti)
    return result["label"].lower()
