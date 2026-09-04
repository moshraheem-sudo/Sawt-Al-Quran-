with open('app/src/main/java/com/example/data/remote/AppUpdateManager.kt', 'r') as f:
    content = f.read()

old_client = """    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()"""

new_client = """    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .protocols(listOf(Protocol.HTTP_1_1))
        .followRedirects(true)
        .followSslRedirects(true)
        .build()"""

content = content.replace(old_client, new_client)

with open('app/src/main/java/com/example/data/remote/AppUpdateManager.kt', 'w') as f:
    f.write(content)
