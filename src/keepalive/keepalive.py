import requests
import datetime
import time

FRONT_URL = "https://aqua-vision-frontend.onrender.com/login"
BACK_URL = "https://aqua-vision-backend.onrender.com/ping"

while True:
    now = datetime.datetime.now().isoformat()

    try:
        r = requests.get(FRONT_URL, timeout=10)
        print(f"[{now}] Front -> {r.status_code}")
    except Exception as e:
        print(f"[{now}] Front failed: {e}")

    try:
        r = requests.get(BACK_URL, timeout=300)
        print(f"[{now}] Back -> {r.status_code}")
    except Exception as e:
        print(f"[{now}] Back failed: {e}")

    time.sleep(240)
