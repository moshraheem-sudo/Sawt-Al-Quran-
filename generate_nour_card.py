import re

with open("app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt", "r") as f:
    content = f.read()

# Extract the Quran Card
start_idx = content.find("// CARD 2: Quran App Update Card (تحديث تطبيق صوت القرءان)")
end_idx = content.find("// CARD 3: Nour Al-Itrah App (تطبيق نور العترة)")

if start_idx != -1 and end_idx != -1:
    quran_card = content[start_idx:end_idx]
    
    # Replace variables
    nour_card = quran_card.replace("CARD 2: Quran App Update Card (تحديث تطبيق صوت القرءان)", "CARD 3: Nour Al-Itrah App (تطبيق نور العترة)")
    nour_card = nour_card.replace("quranUpdateStatus", "nourUpdateStatus")
    nour_card = nour_card.replace("quranUpcomingVersion", "nourUpcomingVersion")
    nour_card = nour_card.replace("quranCurrentVersion", "nourCurrentVersion")
    nour_card = nour_card.replace("quranUpdateInfo", "nourUpdateInfo")
    nour_card = nour_card.replace("isQuranUpdateExpanded", "isNourUpdateExpanded")
    nour_card = nour_card.replace("quranDownloadProgress", "nourDownloadProgress")
    nour_card = nour_card.replace("quranDownloadedMB", "nourDownloadedMB")
    nour_card = nour_card.replace("quranTotalMB", "nourTotalMB")
    nour_card = nour_card.replace("quranSpeedMBs", "nourSpeedMBs")
    nour_card = nour_card.replace("quranDownloadedApkFile", "nourDownloadedApkFile")
    nour_card = nour_card.replace("quranDownloadJob", "nourDownloadJob")
    nour_card = nour_card.replace("checkQuranUpdate", "checkNourUpdate")
    
    nour_card = nour_card.replace("صوت القرءان", "نور العترة")
    nour_card = nour_card.replace("صوت القرآن", "نور العترة")
    nour_card = nour_card.replace("تحديث التطبيق", "تطبيق نور العترة")
    
    # Now replace the original CARD 3 with nour_card
    start_idx_2 = content.find("// CARD 3: Nour Al-Itrah App (تطبيق نور العترة)")
    end_idx_2 = content.find("// CARD 4: Developer Card")
    
    if start_idx_2 != -1 and end_idx_2 != -1:
        new_content = content[:start_idx_2] + nour_card + "                    " + content[end_idx_2:]
        with open("app/src/main/java/com/example/ui/screens/settings/SettingsDialog.kt", "w") as f:
            f.write(new_content)
        print("Success")
    else:
        print("Could not find CARD 3 or CARD 4")
else:
    print("Could not find CARD 2 or CARD 3")
