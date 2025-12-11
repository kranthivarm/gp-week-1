#!/usr/bin/env python3
import requests
import sys
from datetime import datetime, timezone

# Your API endpoint inside the container
GENERATE_ENDPOINT = "http://localhost:8080/generate-2fa"

def utc_timestamp():
    """Return current UTC time formatted as YYYY-MM-DD HH:MM:SS"""
    return datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S")

def fetch_2fa_code():
    """Call the Spring Boot API to get a fresh TOTP"""
    try:
        resp = requests.get(GENERATE_ENDPOINT, timeout=5)
        if resp.status_code == 200:
            data = resp.json()
            code = data.get("code")
            return code
        else:
            sys.stderr.write(f"{utc_timestamp()} - Error {resp.status_code} from generate-2fa\n")
            return None
    except Exception as e:
        sys.stderr.write(f"{utc_timestamp()} - Request failed: {e}\n")
        return None

def main():
    code = fetch_2fa_code()
    if code:
        print(f"{utc_timestamp()} - 2FA Code: {code}")

if __name__ == "__main__":
    main()
