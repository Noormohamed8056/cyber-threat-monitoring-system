from __future__ import annotations

import base64
import io
import re
import tempfile
from pathlib import Path
from typing import Optional

import joblib
import numpy as np
import pytesseract
from fastapi import FastAPI
from pydantic import BaseModel
from PIL import Image
from tika import parser as tika_parser

from train_models import extract_url_features, save_models


BASE_DIR = Path(__file__).resolve().parent
MODELS_DIR = BASE_DIR / "models"
URL_MODEL_PATH = MODELS_DIR / "url_model.pkl"
TEXT_MODEL_PATH = MODELS_DIR / "text_model.pkl"

if not URL_MODEL_PATH.exists() or not TEXT_MODEL_PATH.exists():
    save_models()

url_model = joblib.load(URL_MODEL_PATH)
text_model = joblib.load(TEXT_MODEL_PATH)

app = FastAPI(title="Cyber Threat ML Service")


class PredictRequest(BaseModel):
    type: str
    url: str = ""
    text: str = ""
    image: str = ""
    document: str = ""


def extract_text_from_image(image_b64: str) -> str:
    if not image_b64:
        return ""
    try:
        image_bytes = base64.b64decode(image_b64)
        image = Image.open(io.BytesIO(image_bytes))
        return pytesseract.image_to_string(image)
    except Exception:
        return ""


def extract_text_from_document(doc_b64: str) -> str:
    if not doc_b64:
        return ""
    try:
        doc_bytes = base64.b64decode(doc_b64)
        with tempfile.NamedTemporaryFile(delete=True, suffix=".bin") as temp_file:
            temp_file.write(doc_bytes)
            temp_file.flush()
            parsed = tika_parser.from_file(temp_file.name)
            return (parsed or {}).get("content", "") or ""
    except Exception:
        return ""


def map_result(prob: float) -> dict:
    confidence = round(float(prob) * 100.0, 2)
    if prob >= 0.75:
        risk = "HIGH"
    elif prob >= 0.45:
        risk = "MEDIUM"
    else:
        risk = "LOW"

    prediction = "PHISHING" if prob >= 0.5 else "BENIGN"
    return {
        "prediction": prediction,
        "confidence": confidence,
        "riskLevel": risk,
    }


def extract_domains(text: str) -> list[str]:
    if not text:
        return []
    pattern = r"((?:https?://)?(?:[A-Za-z0-9-]+\.)+[A-Za-z]{2,}(?:/[^\s]*)?)"
    hits = re.findall(pattern, text)
    deduped = []
    seen = set()
    for item in hits:
        lowered = item.lower()
        if lowered not in seen:
            seen.add(lowered)
            deduped.append(item)
    return deduped[:10]


def detect_threat_category(input_type: str, text: str) -> str:
    t = (text or "").lower()

    if input_type == "url":
        if any(x in t for x in ["paypal", "amazon", "apple", "microsoft", "google", "facebook"]):
            return "PHISHING"
        if any(x in t for x in [".exe", ".bat", ".cmd", ".ps1", ".vbs", ".scr"]):
            return "MALWARE"
        if any(x in t for x in ["login", "verify", "signin", "password", "credential"]):
            return "CREDENTIAL_THEFT"
        return "URL_RISK"

    if any(x in t for x in ["enable macros", "enable content", "attachment", "download"]):
        return "MALWARE"
    if any(x in t for x in ["wire transfer", "gift card", "bitcoin", "lottery", "million"]):
        return "SOCIAL_ENGINEERING"
    if any(x in t for x in ["password", "credential", "verify", "account", "login", "ssn"]):
        return "CREDENTIAL_THEFT"
    if any(x in t for x in ["urgent", "final notice", "act now", "click here", "suspended"]):
        return "PHISHING"
    return "TEXT_RISK"


def extract_red_flags(input_type: str, text: str) -> list[str]:
    flags: list[str] = []
    t = (text or "").lower()

    if input_type == "url":
        if not t.startswith("https://"):
            flags.append("No HTTPS")
        if re.search(r"\b\d{1,3}(?:\.\d{1,3}){3}\b", t):
            flags.append("IP used as domain")
        for tld in [".tk", ".ml", ".ga", ".cf", ".gq", ".xyz", ".top", ".click"]:
            if tld in t:
                flags.append(f"Suspicious TLD: {tld}")
        for kw in ["login", "verify", "signin", "password", "secure", "update", "confirm"]:
            if kw in t:
                flags.append(f"Phishing keyword: {kw}")
    else:
        for phrase in ["urgent", "immediately", "act now", "final notice", "click here", "verify now"]:
            if phrase in t:
                flags.append(f"Urgency phrase: {phrase}")
        for phrase in ["password", "credential", "ssn", "social security", "credit card", "bank account"]:
            if phrase in t:
                flags.append(f"Credential request: {phrase}")
        for phrase in ["wire transfer", "gift card", "bitcoin", "lottery", "enable macros", "enable content"]:
            if phrase in t:
                flags.append(f"High-risk indicator: {phrase}")

    return flags[:10]


def build_response(input_type: str, payload_text: str, prob: float, model_used: str) -> dict:
    base = map_result(prob)
    confidence = float(base["confidence"])
    base["riskScore"] = int(round(confidence))
    base["threatCategory"] = detect_threat_category(input_type, payload_text)
    base["redFlags"] = extract_red_flags(input_type, payload_text)
    base["modelUsed"] = model_used
    base["detectedDomains"] = extract_domains(payload_text)
    return base


def predict_url(url: str) -> dict:
    if not url.strip():
        return {
            "prediction": "BENIGN",
            "confidence": 0,
            "riskLevel": "LOW",
            "riskScore": 0,
            "threatCategory": "URL_RISK",
            "redFlags": [],
            "modelUsed": "RandomForestClassifier",
            "detectedDomains": [],
        }

    if isinstance(url_model, dict) and "model" in url_model:
        features = np.array([extract_url_features(url)], dtype=float)
        model = url_model["model"]
        probs = model.predict_proba(features)[0]
        phishing_prob = float(probs[1]) if len(probs) > 1 else float(probs[0])
        return build_response("url", url, phishing_prob, "RandomForestClassifier")

    probs = url_model.predict_proba([url])[0]
    phishing_prob = float(probs[1]) if len(probs) > 1 else float(probs[0])
    return build_response("url", url, phishing_prob, "TF-IDF + RandomForestClassifier")


def predict_text(text: str) -> dict:
    if not text.strip():
        return {
            "prediction": "BENIGN",
            "confidence": 0,
            "riskLevel": "LOW",
            "riskScore": 0,
            "threatCategory": "TEXT_RISK",
            "redFlags": [],
            "modelUsed": "TF-IDF + LogisticRegression",
            "detectedDomains": [],
        }
    probs = text_model.predict_proba([text])[0]
    phishing_prob = float(probs[1]) if len(probs) > 1 else float(probs[0])
    return build_response("text", text, phishing_prob, "TF-IDF + LogisticRegression")


@app.post("/predict")
def predict(req: PredictRequest):
    input_type = (req.type or "").lower().strip()

    if input_type == "url":
        return predict_url(req.url)

    if input_type in {"email", "text"}:
        return predict_text(req.text)

    if input_type == "document":
        extracted = extract_text_from_document(req.document)
        merged_text = f"{req.text}\n{extracted}".strip()
        result = predict_text(merged_text)
        result["threatCategory"] = detect_threat_category("document", merged_text)
        result["modelUsed"] = "Document(Tika) + TF-IDF + LogisticRegression"
        return result

    if input_type == "image":
        extracted = extract_text_from_image(req.image)
        merged_text = f"{req.text}\n{extracted}".strip()
        result = predict_text(merged_text)
        result["threatCategory"] = detect_threat_category("image", merged_text)
        result["modelUsed"] = "Image(OCR) + TF-IDF + LogisticRegression"
        return result

    return {
        "prediction": "SUSPICIOUS",
        "confidence": 50,
        "riskLevel": "MEDIUM",
        "riskScore": 50,
        "threatCategory": "UNKNOWN",
        "redFlags": [],
        "modelUsed": "Fallback",
        "detectedDomains": [],
    }
