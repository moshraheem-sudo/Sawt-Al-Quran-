import urllib.request
import json

url = "https://www.mp3quran.net/api/v3/reciters?language=ar"
response = urllib.request.urlopen(url)
data = json.loads(response.read())

reciters = data['reciters']
style_map = {}

for reciter in reciters:
    for moshaf in reciter['moshaf']:
        style_id = moshaf['id']
        name = moshaf['name']
        
        # Apply the same cleaning logic
        is_other = any(x in name for x in ["ورش", "قالون", "الدوري", "شعبة", "السوسي", "البزي", "قنبل", "خلف", "روح", "رويس", "أبي الحارث", "ابن ذكوان", "هشام", "يعقوب", "ابن وردان", "ابن جماز"])
        if not is_other:
            parts = [p.strip() for p in name.split('-') if p.strip()]
            cleaned_name = " - ".join(dict.fromkeys(parts)) # distinct
            
            words = [w.strip() for w in cleaned_name.split(' ') if w.strip()]
            if len(words) >= 4 and len(words) % 2 == 0:
                half = len(words) // 2
                first_half = " ".join(words[:half])
                second_half = " ".join(words[half:])
                if first_half == second_half:
                    cleaned_name = first_half
                    
            style_map[style_id] = cleaned_name

print("val globalStyleMap = mapOf(")
for k, v in style_map.items():
    print(f'    {k} to "{v}",')
print(")")

