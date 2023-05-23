import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava
import br.com.sankhya.extensions.actionbutton.ContextoAcao
import br.com.sankhya.ws.ServiceContext
import com.google.gson.JsonObject
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.stream.Collectors
import kotlin.Exception
import kotlin.text.isEmpty
import kotlin.text.split
import kotlin.text.toRegex

class BtnGetLog : AcaoRotinaJava {
    var values: JsonObject = JsonObject()
    @Throws(Exception::class)
    override fun doAction(contextoAcao: ContextoAcao?) {
        val path = System.getProperty("org.jboss.boot.log.file")
        try {
            val filePath = Paths.get(path)
            values.addProperty("filePath", filePath.toString())
            val bytes = Files.readAllBytes(filePath)
            val res = JsonObject()
            val lines = String(bytes).split(System.lineSeparator().toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val content = lines.reversed().take(1000).joinToString(System.lineSeparator())
            values.addProperty("content", content)
            res.addProperty("log", content)
            ServiceContext.getCurrent().jsonResponse = res
        } catch (e: Exception) {
            throw Exception("Erro: $path values:$values")
        }
    }
}