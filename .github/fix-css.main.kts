@file:DependsOn("com.squareup.okhttp3:okhttp:4.11.0")

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import kotlin.io.path.Path
import kotlin.io.path.writeText

val replacements: Map<String, String> = mapOf(
	// Globe emoji 🌐
	"\uD83C\uDF10" to "<a title=\"May be a translation key.\" href=\"/internationalisation.html\">\uD83C\uDF10</a>",

	// Tag emoji 🏷️
	"\uD83C\uDFF7\uFE0F" to "<a title=\"Translation bundle.\" href=\"/internationalisation.html\">\uD83C\uDF10</a>",
)

val docRoot = Path("docs/")
val client = OkHttpClient()

fun get(url: String): String {
	val request = Request.Builder()
		.url(url)
		.build()

	return client.newCall(request).execute().body!!.string()
}

val files: Array<File> = docRoot.toFile().listFiles { _, name ->
	name.endsWith(".html")
}!!

println("Found ${files.size} files")

fun getExistingCssLine(file: File): String {
	val lineStart = "<link href=\"https://resources.jetbrains.com/writerside/apidoc/"
	val lineEnd = "rel=\"stylesheet\">"

	val content = file.readText()
	val start = content.indexOf(lineStart)
	val end = content.indexOf(lineEnd, startIndex = start) + lineEnd.length

	return content.substring(start, end)
}

fun getExistingJsLine(file: File): String {
	val lineStart = "<script src=\"https://resources.jetbrains.com/writerside/apidoc"
	val lineEnd = "\"></script>"

	val content = file.readText()
	val start = content.indexOf(lineStart)
	val end = content.indexOf(lineEnd, startIndex = start) + lineEnd.length

	return content.substring(start, end)
}

//fun getExistingCopyrightLine(file: File): String {
//	val lineStart = "<p class=\"footer__copyright"
//	val lineEnd = "</p>"
//
//	val content = file.readText()
//	val start = content.indexOf(lineStart)
//	val end = content.indexOf(lineEnd, startIndex = start) + lineEnd.length
//
//	return content.substring(start, end)
//}

val customCssHtml = """
	<link href="/app.css" rel="stylesheet">
	<link href="/custom.css" rel="stylesheet">
""".trimIndent().trim('\n')

val customJsHtml = """
	<script src="/app.js" type="application/javascript"></script>
""".trimIndent()

//val customCopyrightHtml = """
//	<p class="footer__copyright rs-text-3 rs-text-3_theme_dark" data-test="footer-copyright">
//		License:
//		<a href="https://creativecommons.org/publicdomain/zero/1.0/" data-test="external-link" class="link link--dark">
//			<span class="notranslate">Creative Commons Zero</span>
//		</a>
//	</p>
//""".trimIndent()

println("Downloading WriterSide CSS...")

val jbCssLine = getExistingCssLine(files.first())
val jbCssUrl = jbCssLine.split("\"", limit = 3)[1]
val jbCss = get(jbCssUrl)

docRoot.resolve("app.css").writeText(jbCss)

println("Downloading WriterSide JS and respective license...")

val jbJsLine = getExistingJsLine(files.first())
val jbJsUrl = jbJsLine.split("\"", limit = 3)[1]

docRoot.resolve("app.js").writeText(
	get(jbJsUrl)
)

docRoot.resolve("app.js.LICENSE.txt").writeText(
	get("$jbJsUrl.LICENSE.txt")
)

println("WriterSide CSS/JS downloaded")

//val jbCopyrightLine = getExistingCopyrightLine(files.first())

files.forEach {
	println("Updating HTML for file: ${it.name}")

	var content = it.readText()
		.replace(jbCssLine, customCssHtml)
		.replace(jbJsLine, customJsHtml)
//		.replace(jbCopyrightLine, customCopyrightHtml)

	replacements.forEach { (string, replacement) ->
		content = content.replace(string, replacement)
	}

	it.writeText(content)
}

println("Done!")
