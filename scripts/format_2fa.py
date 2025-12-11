#!/usr/bin/env python3
import requests
import datetime

try:
    # Call your Spring Boot endpoint
    res = requests.get("http://localhost:8080/generate-2fa", timeout=5)
    data = res.json()
    code = data.get("code", "------")
except:
    code = "ERROR"

# format UTC timestamp
utc_now = datetime.datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S")

print(f"{utc_now} - 2FA Code: {code}")
