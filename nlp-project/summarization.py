from transformers import pipeline

# 3-4 cümlelik özetler için BART tabanlı model
# Alternatif: "facebook/bart-large-cnn"
summarizer = pipeline("summarization", model="facebook/bart-large-cnn")

def generate_summary(text: str, max_length: int = 100, min_length: int = 30) -> str:
    """
    text: özetlenecek tam metin
    max_length: özetin en fazla token sayısı (~kelime)
    min_length: özetin en az token sayısı
    """
    # pipeline metodu listeyle de çalışır; biz tek özet için [text] veriyoruz
    summary_list = summarizer(
        text,
        max_length=max_length,
        min_length=min_length,
        do_sample=False
    )
    # summary_list[0]['summary_text'] özet metni içerir
    return summary_list[0]['summary_text']
