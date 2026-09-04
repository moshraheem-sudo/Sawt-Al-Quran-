import re

with open('app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt', 'r') as f:
    text = f.read()

# Change the texts
text = text.replace('إشعارات آية اليوم', 'باقة إشعارات الآيات')
text = text.replace('إرسال 10 إشعارات بالآيات القرآنية القصيرة خلال الساعة (إشعار كل 6 دقائق)', 'إرسال إشعارين في الساعة بآيات قرآنية مميزة')
text = text.replace('.setInitialDelay(6, TimeUnit.MINUTES)', '.setInitialDelay(30, TimeUnit.MINUTES)')
text = text.replace('تم تفعيل الإشعارات (إشعار كل 6 دقائق)', 'تم تفعيل الإشعارات (إشعار كل 30 دقيقة)')

with open('app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt', 'w') as f:
    f.write(text)

