with open('app/src/main/java/com/example/ui/screens/reciters/RecitersScreen.kt', 'r') as f:
    content = f.read()

import re

# Fix count initialization and isActive
old_loop = """                                            var count: Int
                                            
                                            while (kotlinx.coroutines.isActive && input.read(buffer).also { count = it } != -1) {"""

new_loop = """                                            var count: Int = 0
                                            
                                            while (kotlinx.coroutines.isActive && input.read(buffer).also { count = it } != -1) {"""
content = content.replace(old_loop, new_loop)

# Fix kotlinx.coroutines.isActive by adding import and using isActive directly
old_loop2 = """                                            while (kotlinx.coroutines.isActive && input.read(buffer).also { count = it } != -1) {"""
new_loop2 = """                                            while (kotlinx.coroutines.isActive && input.read(buffer).also { count = it } != -1) {""" # Wait, kotlinx.coroutines.isActive is an extension.

old_loop3 = """                                            while (kotlinx.coroutines.isActive && input.read(buffer).also { count = it } != -1) {
                                                total += count
                                                if (fileLength > 0) {
                                                    val progress = total.toFloat() / fileLength.toFloat()
                                                    withContext(Dispatchers.Main) {
                                                        downloadingProgress = progress
                                                    }
                                                }
                                                output.write(buffer, 0, count)
                                            }
                                            output.flush()
                                            output.close()
                                            input.close()
                                            
                                            if (!kotlinx.coroutines.isActive) {"""
new_loop3 = """                                            while (isActive && input.read(buffer).also { count = it } != -1) {
                                                total += count
                                                if (fileLength > 0) {
                                                    val progress = total.toFloat() / fileLength.toFloat()
                                                    withContext(Dispatchers.Main) {
                                                        downloadingProgress = progress
                                                    }
                                                }
                                                output.write(buffer, 0, count)
                                            }
                                            output.flush()
                                            output.close()
                                            input.close()
                                            
                                            if (!isActive) {"""
content = content.replace(old_loop3, new_loop3)

old_import = "import kotlinx.coroutines.withContext"
new_import = "import kotlinx.coroutines.withContext\nimport kotlinx.coroutines.isActive"
content = content.replace(old_import, new_import)

with open('app/src/main/java/com/example/ui/screens/reciters/RecitersScreen.kt', 'w') as f:
    f.write(content)

