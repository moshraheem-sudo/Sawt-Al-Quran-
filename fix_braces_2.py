import re

with open('app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt', 'r') as f:
    content = f.read()

# Extract functions: openFolder, installApk, formatBytes
utils_code = ""
match = re.search(r'fun openFolder\(.*', content, re.DOTALL)
if match:
    utils_code = match.group(0)
    content = content[:match.start()]

# Add one more brace just in case, we will use a small logic to balance braces
def balance_braces(text):
    open_count = text.count('{')
    close_count = text.count('}')
    if open_count > close_count:
        text += '}\n' * (open_count - close_count)
    elif close_count > open_count:
        # We need to remove closing braces from the end
        lines = text.split('\n')
        while close_count > open_count and lines:
            if lines[-1].strip() == '}':
                lines.pop()
                close_count -= 1
            else:
                # If the last line is not a simple brace, just remove braces from the end of the string
                break
        text = '\n'.join(lines)
    return text

content = balance_braces(content)

with open('app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/screens/settings/SettingsUtils.kt', 'w') as f:
    f.write("package com.example.ui.screens.settings\n\n")
    f.write("import android.content.Context\n")
    f.write("import android.content.Intent\n")
    f.write("import android.net.Uri\n")
    f.write("import android.widget.Toast\n")
    f.write("import java.io.File\n")
    f.write("import java.util.Locale\n")
    f.write(utils_code)
