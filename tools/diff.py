import re
from pathlib import Path

import requests

token = "fill this in"
urls = {
    "prod": f"http://aquadx.hydev.org/gs/{token}/chu3/2.27",
    "staging": f"http://staging.aquadx.net/gs/{token}/chu3/2.27"
}

def decode_to_dict(s: str) -> dict:
    # Remove the surrounding braces
    s = s.strip('{}')
    # Use regex to match key-value pairs
    pairs = re.findall(r'(\w+)=([^,]+)', s)
    # Convert the list of tuples into a dictionary
    decoded_dict = {}
    for key, value in pairs:
        if key == 'version':
            continue
        decoded_dict[key] = value
        # # Try to convert numeric values to int or float
        # if value.isdigit():
        #     decoded_dict[key] = int(value)
        # else:
        #     try:
        #         decoded_dict[key] = float(value)
        #     except ValueError:
        #         decoded_dict[key] = value  # Keep it as a string if not a number
    return decoded_dict


def save_resp(i, idx, api, data):
    url = urls[idx]
    resp = requests.post(f"{url}/{api}", json=data)
    txt = f"""
Request: {api}
{data}

Response: {resp.status_code}
{resp.text}
"""
    print(txt)
    Path(f"test-diff/{idx}").mkdir(exist_ok=True, parents=True)
    Path(f"test-diff/{idx}/{i}-{api}.json").write_text(txt)


if __name__ == '__main__':
    # Read chusan.log
    d = Path("chusan.log").read_text('utf-8').splitlines()
    d = [l.split("|", 1)[1] for l in d if '|' in l]
    d = [l.split(": ", 1)[1] for l in d if ': ' in l]
    d = [l for l in d if l.startswith("Chu3 <")]
    d = [l.split("< ", 1)[1] for l in d]
    d = [l.split(" : ") for l in d if l.count("{") == 1]  # (api, data)
    d = [(api, decode_to_dict(data)) for api, data in d]

    print(d)

    for i, (a, dt) in enumerate(d):
        save_resp(i, "prod", a, dt)
        save_resp(i, "staging", a, dt)


