import java.io.File

println("Adding custom CSS to mermaid.css...")

val customCss = File("style.css").readText()
val mermaidCss = File("mermaid.css").readText()

File("mermaid.css").writeText(
	"$mermaidCss\n\n" +
		"/* Custom CSS */\n\n" +
		customCss
)

println("Done!")
