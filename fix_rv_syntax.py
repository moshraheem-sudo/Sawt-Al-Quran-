with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace("import androidx.lifecycle.ViewModelProvider\npackage com.example.ui.viewmodels", "package com.example.ui.viewmodels\nimport androidx.lifecycle.ViewModelProvider")

with open('app/src/main/java/com/example/ui/viewmodels/ReaderViewModel.kt', 'w') as f:
    f.write(content)
