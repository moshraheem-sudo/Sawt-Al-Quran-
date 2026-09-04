import re
with open('app/src/main/AndroidManifest.xml', 'r') as f:
    text = f.read()

service_tag = r"""        <service
            android:name=".audio.AyahAudioService"
            android:exported="false"
            android:foregroundServiceType="mediaPlayback" />"""

text = text.replace('</application>', service_tag + '\n    </application>')

with open('app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(text)
