# Sawt AL Quran (صوت القرءان)

**Version: 3.40.0**  
**Developer: Architect Mohammed Shaheed Al-Ajajibi**  
**Repository:** [moshraheem-sudo/Sawt-Al-Quran-](https://github.com/moshraheem-sudo/Sawt-Al-Quran-)

> **Sawt AL Quran** is a complete Android Quran application built with Jetpack Compose and Material Design 3, providing a calm, reliable, and offline-capable experience for listening to famous reciters, reading the Holy Quran with verse-by-verse highlighting, tracking Khatmah progress with automatic local backup & restore, following a live Hijri calendar, and receiving persistent Ayah reminder notifications.

---

## Key Features

- 🎙️ **Famous Reciters & Offline Audio** — Stream recitations from renowned reciters (`mp3quran.net` / `EveryAyah`), download Surahs for offline listening, and control playback seamlessly with a persistent Media3 foreground service & mini-player.
- 📖 **Quran Reader & Auto-Highlighting** — Read the Holy Quran in Uthmani script with search, bookmarks, adjustable typography, and visual verse highlighting synchronized with playback.
- 📚 **Quran Khatmah Tracking & Auto Backup** — Track completion across all 30 Juz' with automatic multi-location local backup (`KhatmaBackupManager`) and instant restore across updates or database migrations.
- 🗓️ **Live Hijri Calendar Card** — Displayed directly on the Home screen and synchronized automatically from [moshraheem-sudo/hijri](https://github.com/moshraheem-sudo/hijri), featuring intensified moon-sighting checks during days 28–30 and the 1st of the Hijri month, plus manual day-offset controls and instant remote refresh in Settings.
- 🔔 **Reliable Ayah Notifications (Post-Reboot Resilient)** — Customizable hourly reminder frequency (1–20 times/hour) powered by exact `AlarmManager` scheduling, `BOOT_COMPLETED` / `USER_UNLOCKED` / `QUICKBOOT_POWERON` / `TIME_SET` recovery, self-healing `WorkManager` watchdog, and built-in exact-alarm / battery-optimization settings shortcuts.
- 🎨 **Modern Multi-Theme Islamic UI** — Supports multiple curated Light and Dark themes (Emerald, Night Gold, Warm Parchment, etc.) crafted with Material Design 3.
- 🔄 **In-App GitHub Release Updater** — Checks GitHub Releases (`Sawt-Al-Quran-`) directly in Settings and supports one-tap APK download and installation.

---

## Project Information

| Item | Details |
|---|---|
| **App Name** | Sawt AL Quran (صوت القرءان) |
| **Version** | `3.40.0` (`versionCode: 34000`) |
| **Category** | Religious / Quran Application |
| **Developer** | Architect Mohammed Shaheed Al-Ajajibi |
| **Primary Language** | Arabic |
| **Tech Stack** | Kotlin, Jetpack Compose, Material 3, Room, WorkManager, Media3, OkHttp/Retrofit |
| **Hijri Feed** | `https://raw.githubusercontent.com/moshraheem-sudo/hijri/main/hijri.json` |
| **Releases** | [Latest GitHub Releases](https://github.com/moshraheem-sudo/Sawt-Al-Quran-/releases) |

---

## Main Screens

- **Home — Sawt AL Quran:** App header, live Hijri Calendar card, last-read shortcut, search, and quick navigation. (`screenshots/01-home.png`)
- **Reciters:** Browse reciters, styles (Hafs, Warsh, Qalun, etc.), stream online, or download Surahs offline. (`screenshots/02-reciters.png`)
- **Quran Khatmah:** Track completion across the 30 Juz' with persistent progress backup. (`screenshots/03-khatmah.png`)

---

## Developer & Contact

**Architect Mohammed Shaheed Al-Ajajibi**  
WhatsApp: `+9647831452279`  
GitHub: [https://github.com/moshraheem-sudo/Sawt-Al-Quran-](https://github.com/moshraheem-sudo/Sawt-Al-Quran-)

## Copyright

© 2026 Architect Mohammed Shaheed Al-Ajajibi. All rights reserved.
