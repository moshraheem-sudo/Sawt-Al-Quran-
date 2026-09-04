import re

with open("app/src/main/java/com/example/khatma/KhatmaViewModel.kt", "r") as f:
    content = f.read()

methods = """
    fun resetKhatma() {
        viewModelScope.launch { repository.startNewKhatma() }
    }

    fun resetJuz(juz: Int) {
        viewModelScope.launch { repository.resetJuz(juz) }
    }
}"""

content = content.replace("}", methods, 1) # Only replace the last brace

with open("app/src/main/java/com/example/khatma/KhatmaViewModel.kt", "w") as f:
    f.write(content)

with open("app/src/main/java/com/example/khatma/KhatmaRepository.kt", "r") as f:
    repo_content = f.read()

repo_methods = """
    suspend fun resetJuz(juz: Int) {
        dao.upsert(KhatmaProgress(juzNumber = juz, lastReadPage = 0, progressPercent = 0, isCompleted = false))
    }
}"""

repo_content = repo_content.replace("}", repo_methods, 1)

with open("app/src/main/java/com/example/khatma/KhatmaRepository.kt", "w") as f:
    f.write(repo_content)
