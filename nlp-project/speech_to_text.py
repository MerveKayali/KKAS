from faster_whisper import WhisperModel

model = WhisperModel("base",device="cpu")

def transcribe_audio(file_path):
    segments, info = model.transcribe(file_path)
    full_text = " ".join([segment.text for segment in segments])
    return full_text

