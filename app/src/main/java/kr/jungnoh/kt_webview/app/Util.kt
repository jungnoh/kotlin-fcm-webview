package kr.jungnoh.kt_webview.app

fun extractCookie(originalCookie: String?, key: String = Config.sessionKey): String? {
    if (originalCookie == null) {
        return null
    }
    val items = originalCookie.split(';')
    for (item: String in items) {
        val parts = item.split('=')
        if (parts.size < 2) {
            continue
        }
        if (parts[0].trim().compareTo(key) == 0) {
            return parts[1].trim()
        }
    }
    return null
}
