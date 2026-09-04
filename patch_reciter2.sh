#!/bin/bash
sed -i 's/text = "سورة $surahId",/val name = surahNames.getOrElse(surahId - 1) { "" }\n                                        text = "سورة $name",/' app/src/main/java/com/example/ui/screens/reciters/ReciterDetailScreen.kt
sed -i 's/onPlaySurah(url, "سورة $surahId", surahId)/val name = surahNames.getOrElse(surahId - 1) { "" }\n                                        onPlaySurah(url, "سورة $name", surahId)/' app/src/main/java/com/example/ui/screens/reciters/ReciterDetailScreen.kt
