with open('app/src/main/java/com/example/ui/viewmodels/HomeViewModel.kt', 'r') as f:
    content = f.read()

new_state = """    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _lastReadAyahText = MutableStateFlow<String?>(null)
    val lastReadAyahText: StateFlow<String?> = _lastReadAyahText.asStateFlow()

    fun loadLastReadAyahText(surahId: Int, ayahNumber: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val ayah = repository.getAyahByNumber(surahId, ayahNumber)
            if (ayah != null) {
                _lastReadAyahText.value = ayah.textUthmani
            }
        }
    }"""

content = content.replace("""    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()""", new_state)

with open('app/src/main/java/com/example/ui/viewmodels/HomeViewModel.kt', 'w') as f:
    f.write(content)
