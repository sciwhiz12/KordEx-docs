@file:DependsOn("com.squareup.okhttp3:okhttp:4.11.0")
@file:DependsOn("io.github.z4kn4fein:semver-jvm:1.4.1")

import io.github.z4kn4fein.semver.Version
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory
import kotlin.io.path.Path

val client = OkHttpClient()

val RELEASES_URL = "https://repo1.maven.org/maven2/com/kotlindiscord/kord/extensions/kord-extensions/maven-metadata.xml"
val SNAPSHOTS_URL ="https://s01.oss.sonatype.org/service/local/repositories/snapshots/content/" +
	"com/kotlindiscord/kord/extensions/kord-extensions" +
	"/maven-metadata.xml"

val FILE_PATH = Path("Writerside/v.list")

println("Finding the latest version...")

fun getLatest(url: String): String {
	val request = Request.Builder()
		.url(url)
		.build()

	val content = client.newCall(request).execute().body!!.string()

	val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		.parse(content.byteInputStream())

	return XPathFactory.newInstance().newXPath()
		.compile("metadata/versioning/latest")
		.evaluate(document, XPathConstants.STRING) as String
}

val latestSnapshot = Version.parse(getLatest(SNAPSHOTS_URL))
val latestRelease = Version.parse(getLatest(RELEASES_URL))

val latest = maxOf(latestSnapshot, latestRelease)

println("Latest snapshot version: $latestSnapshot")
println("Latest release version: $latestRelease")

println("Updating version in Writerside/v.list to $latest")

val file = FILE_PATH.toFile()
val fileContents = file.readText()

file.writeText(fileContents.replace("{VERSION}", latest.toString()))

println("Done!")
