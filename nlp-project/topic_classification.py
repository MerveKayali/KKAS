from transformers import pipeline

classifier = pipeline("zero-shot-classification", model="facebook/bart-large-mnli")


def classify_topic(text):
    labels = ["return request", "technical issue", "shipping delay", "product defect", "billing", "other"]
    result = classifier(text, labels)

    print("Metin:", text)
    print("Sonuç:", result)

    return result["labels"][0]
