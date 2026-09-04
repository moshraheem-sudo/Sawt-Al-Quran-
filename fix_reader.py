import re

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'r') as f:
    content = f.read()

# I need to clean up duplicated pageMap and pageGroups declarations.
# Let's just remove the first one if it exists.
# We have:
# val pageMap = remember(surahId, ayahs) { buildPageMap(surahId, ayahs) }
# val pageMap = remember(surahId, ayahs) { buildPageMap(surahId, ayahs) }

lines = content.split('\n')
new_lines = []
page_map_count = 0
skip_next_brace = False

for i in range(len(lines)):
    line = lines[i]
    if "val pageMap = remember(surahId, ayahs) { buildPageMap(surahId, ayahs) }" in line:
        page_map_count += 1
        if page_map_count > 1:
            continue
    new_lines.append(line)

with open('app/src/main/java/com/example/ui/screens/ReaderScreen.kt', 'w') as f:
    f.write('\n'.join(new_lines))

