from __future__ import annotations

import re
from pathlib import Path
from typing import List

import joblib
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline


BASE_DIR = Path(__file__).resolve().parent
MODELS_DIR = BASE_DIR / "models"
MODELS_DIR.mkdir(exist_ok=True)


def extract_url_features(url: str) -> List[float]:
    url = str(url or "").strip()
    try:
        parts = url.split("/")
        domain = parts[2] if len(parts) > 2 else url
    except Exception:
        parts = [url]
        domain = url

    suspicious_tlds = [
        ".tk", ".ml", ".ga", ".cf", ".gq", ".xyz", ".top", ".club",
        ".online", ".site", ".win", ".info", ".biz", ".link", ".click",
    ]
    phishing_words = [
        "login", "signin", "verify", "secure", "account", "update", "confirm",
        "bank", "payment", "paypal", "amazon", "apple", "microsoft", "google",
        "facebook", "password", "credential", "urgent", "suspend", "alert",
        "recover", "billing",
    ]

    url_lower = url.lower()

    return [
        len(url),
        len(domain),
        1 if url.startswith("https") else 0,
        1 if re.match(r"\d{1,3}(\.\d{1,3}){3}$", domain) else 0,
        url.count("."),
        url.count("/"),
        url.count("-"),
        url.count("_"),
        url.count("@"),
        url.count("?"),
        url.count("="),
        url.count("&"),
        url.count("%"),
        url.count("#"),
        max(0, domain.count(".") - 1),
        1 if any(url_lower.split("?")[0].split("/")[0].endswith(t) for t in suspicious_tlds) else 0,
        sum(1 for w in phishing_words if w in url_lower),
        1 if any(
            brand in url_lower and not url_lower.endswith(f"{brand}.com")
            for brand in ["paypal", "amazon", "apple", "microsoft", "google", "facebook"]
        ) else 0,
        1 if any(url_lower.endswith(ext) for ext in [".exe", ".bat", ".cmd", ".scr", ".vbs", ".ps1"]) else 0,
        sum(c.isdigit() for c in domain) / max(len(domain), 1),
        1 if "-" in domain else 0,
        1 if any(s in url_lower for s in ["bit.ly", "tinyurl", "goo.gl", "t.co", "ow.ly"]) else 0,
        1 if any(h in url_lower for h in ["000webhostapp", "netlify.app", "github.io", "herokuapp.com", "glitch.me"]) else 0,
        len("/".join(parts[3:])) if len(parts) > 3 else 0,
        len(url.split("?")[1]) if "?" in url else 0,
        url.count("="),
        1 if re.search(r":\d{4,5}$", domain) else 0,
    ]


def train_url_model() -> dict:
    phishing_urls = [
        "http://paypal-verify-secure.tk/login",
        "http://192.168.1.1/bank/verify/account",
        "http://amazon-security-update.xyz/account",
        "http://secure-paypal-login.ml/signin",
        "http://microsoft-alert.ga/verify/account",
        "http://apple-id-suspended.cf/update",
        "http://google-security.gq/login/verify",
        "http://chase-bank-alert.tk/secure",
        "http://bit.ly/suspicious-link-abc123",
        "http://login-facebook-verify.xyz/account",
        "http://netflix-suspended.ml/update/billing",
        "http://ebay-alert-security.tk/signin",
        "http://dropbox-verify.ga/login/confirm",
        "http://wells-fargo-alert.xyz/account",
        "http://citibank-secure.cf/verify",
        "http://paypal.security-update.tk/login",
        "http://amazon.account-verify.ml/signin",
        "http://apple-id.update-now.ga/account",
        "http://secure.microsoft-alert.cf/verify",
        "http://login.google-update.gq/account",
        "http://bank-verify-now.xyz/login/secure",
        "http://account-suspended.tk/verify/now",
        "http://signin-amazon-alert.ml/update",
        "http://verify-apple-id.ga/confirm/now",
        "http://update-microsoft.cf/signin/verify",
        "http://secure-bank-login.gq/verify/now",
        "http://paypal-update-required.xyz/login",
        "http://amazon-suspended.tk/verify/account",
        "http://apple-security.ml/update/billing",
        "http://microsoft-verify.ga/login/confirm",
        "http://phishing-site.xyz/steal/credentials",
        "http://fake-bank.tk/login?next=/dashboard",
        "http://steal-password.ml/account/verify",
        "http://credential-harvest.ga/login/now",
        "http://malware-site.cf/download/virus.exe",
        "http://ransomware.gq/encrypt/files.bat",
        "http://fake-paypal.xyz/pay/now/urgent",
        "http://steal-amazon.tk/account?update=1",
        "http://phish-google.ml/verify/signin",
        "http://fake-microsoft.ga/office365/login",
        "http://account-compromised.xyz/verify",
        "http://security-alert-paypal.tk/update",
        "http://your-account-locked.ml/unlock",
        "http://update-billing-amazon.ga/now",
        "http://apple-id-locked.cf/restore/now",
        "http://microsoftalert-login.gq/verify",
        "http://secure-login-facebook.xyz/auth",
        "http://instagram-verify.tk/account/now",
        "http://netflix-billing.ml/update/card",
        "http://ebay-suspended.ga/restore/account",
    ]

    legitimate_urls = [
        "https://www.google.com/search?q=test",
        "https://www.amazon.com/products/books",
        "https://www.paypal.com/myaccount/home",
        "https://github.com/user/repository/main",
        "https://stackoverflow.com/questions/python",
        "https://www.microsoft.com/en-us/windows",
        "https://www.apple.com/iphone-15-pro",
        "https://www.facebook.com/marketplace",
        "https://www.netflix.com/browse/genre/34",
        "https://www.linkedin.com/in/johndoe",
        "https://www.twitter.com/home",
        "https://docs.python.org/3/library/os.html",
        "https://www.youtube.com/watch?v=dQw4w9",
        "https://www.reddit.com/r/programming",
        "https://en.wikipedia.org/wiki/Python",
        "https://www.chase.com/personal/banking",
        "https://www.wellsfargo.com/checking",
        "https://www.dropbox.com/home/documents",
        "https://www.ebay.com/sch/i.html?_nkw=test",
        "https://www.citibank.com/credit-cards",
        "https://mail.google.com/mail/u/0/inbox",
        "https://drive.google.com/drive/my-drive",
        "https://docs.google.com/spreadsheets",
        "https://www.coursera.org/learn/python",
        "https://www.udemy.com/course/spring-boot",
        "https://spring.io/projects/spring-boot",
        "https://mvnrepository.com/artifact/org",
        "https://www.npmjs.com/package/react",
        "https://react.dev/learn",
        "https://tailwindcss.com/docs/installation",
        "https://www.oracle.com/java/technologies",
        "https://www.mysql.com/products/community",
        "https://www.postman.com/downloads",
        "https://code.visualstudio.com/download",
        "https://www.jetbrains.com/idea/download",
        "https://hub.docker.com/search?q=python",
        "https://kubernetes.io/docs/home",
        "https://aws.amazon.com/ec2/pricing",
        "https://cloud.google.com/free",
        "https://azure.microsoft.com/en-us/pricing",
        "https://www.adobe.com/products/acrobat",
        "https://slack.com/intl/en-us/downloads",
        "https://zoom.us/download",
        "https://www.notion.so/product",
        "https://trello.com/home",
        "https://asana.com/features",
        "https://www.figma.com/downloads",
        "https://www.atlassian.com/software/jira",
        "https://www.salesforce.com/products/crm",
        "https://www.hubspot.com/products/crm",
    ]

    x = phishing_urls + legitimate_urls
    y = np.array([1] * len(phishing_urls) + [0] * len(legitimate_urls))
    features = np.array([extract_url_features(url) for url in x], dtype=float)

    model = RandomForestClassifier(
        n_estimators=250,
        max_depth=16,
        min_samples_split=2,
        min_samples_leaf=1,
        random_state=42,
        n_jobs=1,
    )
    model.fit(features, y)

    return {
        "model": model,
        "n_features": features.shape[1],
        "version": "2.0.0",
    }


def train_text_model() -> Pipeline:
    phishing_texts = [
        "URGENT Your account has been suspended Click here immediately to verify your credentials",
        "Dear Customer We detected unusual activity verify your identity by entering password and card details",
        "FINAL NOTICE Your PayPal account will be closed in 24 hours Click here to verify now",
        "Your Apple ID has been locked restore access by entering username and password",
        "SECURITY ALERT Unauthorized access detected confirm your identity with login credentials",
        "Your bank account has been compromised wire transfer gift cards immediately",
        "Amazon order suspended verify payment information now or account closes",
        "Enable macros to view this protected invoice attachment",
        "Netflix subscription expired update billing in 24 hours",
        "Your account flagged for suspicious activity click the link to verify now",
        "Your credit card used in unauthorized transaction verify account immediately",
        "Your password expires today reset immediately and verify identity",
        "Tax refund available click to claim and share account details",
        "Suspicious login from unknown device verify identity now",
        "Claim reward by providing bank details and social security number",
    ]

    legitimate_texts = [
        "Meeting scheduled for tomorrow at 3 PM please review attached agenda",
        "Order number 12345 has been shipped expected delivery this Friday",
        "Please find monthly report attached and review before Monday meeting",
        "Team lunch planned for Friday at noon please RSVP",
        "Software update version 2.1.0 is available during maintenance window",
        "Your subscription renews automatically next month no action required",
        "Link to requested documentation attached let me know if clarification is needed",
        "Thanks for your feedback we will include it in next sprint",
        "Conference call moved to 2 PM dial-in details unchanged",
        "Account statement for this month is ready in your portal",
        "Project deadline extended to next Friday update your tasks",
        "New policy guidelines published on intranet review by end of week",
        "Application approved please complete onboarding steps",
        "Server maintenance window scheduled Sunday 2 AM to 4 AM",
        "IT support ticket resolved please confirm everything works",
    ]

    x = phishing_texts + legitimate_texts
    y = [1] * len(phishing_texts) + [0] * len(legitimate_texts)

    model = Pipeline(
        steps=[
            (
                "tfidf",
                TfidfVectorizer(
                    max_features=10000,
                    ngram_range=(1, 3),
                    stop_words="english",
                    lowercase=True,
                    sublinear_tf=True,
                    min_df=1,
                ),
            ),
            (
                "clf",
                LogisticRegression(
                    max_iter=1000,
                    random_state=42,
                    C=2.0,
                    solver="lbfgs",
                ),
            ),
        ]
    )
    model.fit(x, y)
    return model


def save_models() -> None:
    url_model = train_url_model()
    text_model = train_text_model()
    joblib.dump(url_model, MODELS_DIR / "url_model.pkl")
    joblib.dump(text_model, MODELS_DIR / "text_model.pkl")
    print("Saved url_model.pkl and text_model.pkl")


if __name__ == "__main__":
    save_models()
