with open('app/build.gradle.kts', 'r') as f:
    content = f.read()

content = content.replace('dependencies {', 'dependencies {\n  implementation("androidx.media:media:1.7.0")')

with open('app/build.gradle.kts', 'w') as f:
    f.write(content)
