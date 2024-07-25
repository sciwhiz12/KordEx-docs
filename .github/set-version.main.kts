@file:DependsOn("com.squareup.okhttp3:okhttp:4.11.0")
@file:DependsOn("io.github.z4kn4fein:semver-jvm:1.4.1")
@file:DependsOn("com.google.code.gson:gson:2.10.1")

import com.google.gson.Gson
import com.google.gson.JsonObject
import io.github.z4kn4fein.semver.Version
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.io.path.Path

val client = OkHttpClient()
val gson = Gson()

val PLUGIN_URL = "https://plugins.gradle.org/m2/dev/kordex/gradle/plugins/kordex/maven-metadata.xml"
val RELEASES_URL = "https://repo1.maven.org/maven2/com/kotlindiscord/kord/extensions/kord-extensions/maven-metadata.xml"
val SNAPSHOTS_URL = "https://s01.oss.sonatype.org/service/local/repositories/snapshots/content/" +
	"com/kotlindiscord/kord/extensions/kord-extensions" +
	"/maven-metadata.xml"

val FILE_PATH = Path("Writerside/v.list")

println("Finding the latest version...")

fun get(url: String): String {
	val request = Request.Builder()
		.url(url)
		.build()

	return client.newCall(request).execute().body!!.string()
}

fun getLatest(url: String): String {
	val content = get(url)

	val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		.parse(content.byteInputStream())

	return XPathFactory.newInstance().newXPath()
		.compile("metadata/versioning/latest")
		.evaluate(document, XPathConstants.STRING) as String
}

fun getJavaFromModule(url: String): String {
	val moduleJson = get(url)
	println("URL: $url")
	val json = gson.fromJson(moduleJson, JsonObject::class.java)

	return json
		.get("variants").asJsonArray
		.get(0).asJsonObject
		.get("attributes").asJsonObject
		.get("org.gradle.jvm.version").asString
}

val latestSnapshot = Version.parse(getLatest(SNAPSHOTS_URL))
val latestRelease = Version.parse(getLatest(RELEASES_URL))

val latest = maxOf(latestSnapshot, latestRelease)
val latestPlugin = Version.parse(getLatest(PLUGIN_URL))

println("Latest snapshot version: $latestSnapshot")
println("Latest release version: $latestRelease")

println("Finding current java version...")

var javaVersion: String

if ("-SNAPSHOT" in latest.toString()) {
	val baseUrl = "https://s01.oss.sonatype.org/service/local/repositories/snapshots/content/com/" +
		"kotlindiscord/kord/extensions/kord-extensions/$latest"

	val metadataXml = get("$baseUrl/maven-metadata.xml")
	val metadataDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		.parse(metadataXml.byteInputStream())

	val latestBuild = XPathFactory.newInstance().newXPath()
		.compile("//snapshotVersions/snapshotVersion[1]/value")
		.evaluate(metadataDocument, XPathConstants.STRING) as String

	javaVersion = getJavaFromModule("$baseUrl/kord-extensions-$latestBuild.module")
} else {
	javaVersion = getJavaFromModule(
		"https://repo1.maven.org/maven2/com/kotlindiscord/kord/extensions/kord-extensions" +
			"/$latest/kord-extensions-$latest.module"
	)
}

println("Updating KordEx version in Writerside/v.list to $latest")
println("Updating Java version in Writerside/v.list to $javaVersion")

val file = FILE_PATH.toFile()
val fileContents = file.readText()

file.writeText(
	fileContents.replace("{VERSION}", latest.toString())
		.replace("{JAVA_VERSION}", javaVersion)
		.replace("{PLUGIN_VERSION}", latestPlugin.toString())
)

println("Done!")
