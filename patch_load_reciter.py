with open('app/src/main/java/com/example/ui/screens/reciters/ReciterDetailScreen.kt', 'r') as f:
    content = f.read()

old_loop = """                        if (words.size >= 4 && words.size % 2 == 0) {
                            val half = words.size / 2
                            val firstHalf = words.subList(0, half).joinToString(" ")
                            val secondHalf = words.subList(half, words.size).joinToString(" ")
                            if (firstHalf == secondHalf) {
                                cleanedName = firstHalf
                            }
                        }
                        styles.add(
                            ReciterStyle(
                                id = m.getInt("id"),
                                name = cleanedName,
                                server = m.getString("server"),
                                surahList = m.getString("surah_list")
                            )
                        )
                    }"""

new_loop = """                        if (words.size >= 4 && words.size % 2 == 0) {
                            val half = words.size / 2
                            val firstHalf = words.subList(0, half).joinToString(" ")
                            val secondHalf = words.subList(half, words.size).joinToString(" ")
                            if (firstHalf == secondHalf) {
                                cleanedName = firstHalf
                            }
                        }
                        val styleId = m.getInt("id")
                        styles.add(
                            ReciterStyle(
                                id = styleId,
                                name = cleanedName,
                                server = m.getString("server"),
                                surahList = m.getString("surah_list")
                            )
                        )
                        
                        sharedPrefs?.edit()?.apply {
                            putString("style_name_$styleId", cleanedName)
                            putString("reciter_name_$reciterId", name)
                        }?.apply()
                    }"""
content = content.replace(old_loop, new_loop)

with open('app/src/main/java/com/example/ui/screens/reciters/ReciterDetailScreen.kt', 'w') as f:
    f.write(content)
