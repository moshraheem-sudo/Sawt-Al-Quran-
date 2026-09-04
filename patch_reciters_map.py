with open('app/src/main/java/com/example/ui/screens/reciters/RecitersScreen.kt', 'r') as f:
    content = f.read()

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

map_str = "val globalStyleMap = mapOf(\n"
for k, v in style_map.items():
    map_str += f'    {k} to "{v}",\n'
map_str += ")\n"

# Add it just before loadDownloadedItems
content = content.replace("fun loadDownloadedItems", map_str + "\nfun loadDownloadedItems")

old_fallback = """val defaultStyleName = if (sStyleId == 1) "المصحف المجود" else if (sStyleId == 2) "المصحف المرتل" else "قراءة $sStyleId"
                
                val rName = prefs.getString("reciter_name_$rId", recitersMap[rId] ?: "قارئ #$rId") ?: "قارئ #$rId"
                val styleName = prefs.getString("style_name_$sStyleId", defaultStyleName) ?: defaultStyleName"""

new_fallback = """val defaultStyleName = globalStyleMap[sStyleId] ?: "قراءة $sStyleId"
                
                val rName = prefs.getString("reciter_name_$rId", recitersMap[rId] ?: "قارئ #$rId") ?: "قارئ #$rId"
                val styleName = prefs.getString("style_name_$sStyleId", defaultStyleName) ?: defaultStyleName"""

content = content.replace(old_fallback, new_fallback)

old_fav_fallback = """val defaultStyle = if (styleId == 1) "المصحف المجود" else if (styleId == 2) "المصحف المرتل" else "قراءة $styleId"
                    styleName = prefs.getString("style_name_$styleId", defaultStyle) ?: defaultStyle"""

new_fav_fallback = """val defaultStyle = globalStyleMap[styleId] ?: "قراءة $styleId"
                    styleName = prefs.getString("style_name_$styleId", defaultStyle) ?: defaultStyle"""

content = content.replace(old_fav_fallback, new_fav_fallback)

with open('app/src/main/java/com/example/ui/screens/reciters/RecitersScreen.kt', 'w') as f:
    f.write(content)

