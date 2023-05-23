@file:Suppress("RegExpRedundantEscape", "RegExpSimplifiable", "unused")

package utilitarios

import br.com.sankhya.jape.core.JapeSession
import br.com.sankhya.jape.core.JapeSession.SessionHandle
import br.com.sankhya.jape.dao.JdbcWrapper
import br.com.sankhya.jape.event.PersistenceEvent
import br.com.sankhya.jape.sql.NativeSql
import br.com.sankhya.jape.vo.DynamicVO
import br.com.sankhya.jape.wrapper.JapeFactory
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO
import br.com.sankhya.modelcore.MGEModelException
import br.com.sankhya.modelcore.util.EntityFacadeFactory
import br.com.sankhya.ws.ServiceContext
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sankhya.util.JdbcUtils
import com.sankhya.util.SessionFile
import okhttp3.*
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import utilitarios.jsons.*
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.math.BigDecimal
import java.nio.charset.StandardCharsets
import java.sql.ResultSet
import java.sql.ResultSetMetaData
import java.sql.Timestamp
import java.text.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import javax.script.Invocable
import javax.script.ScriptEngineManager
import kotlin.collections.HashMap


val gson: Gson = GsonBuilder().setPrettyPrinting().create()

class DiversosKT

/**
 * Alterar o valor do campo informado
 * @author Luis Ricardo Alves Santos
 * @param name Nome do campo
 * @param value Valor
 * @param vo DynamicVO do item a ser atualizado
 * @param instance Instância - Default: Instância atual
 * @return Unit
 */

fun setCampo(name: String, value: Any?, vo: DynamicVO, instance: String? = getInstancia()) {
    var hnd: JapeSession.SessionHandle? = null
    try {
        hnd = JapeSession.open()
        val instanciaDAO = JapeFactory.dao(instance)
        val fluidupdate = instanciaDAO.prepareToUpdateByPK(vo.primaryKey)
        fluidupdate.set(name, value)
        fluidupdate.update()
    } catch (e: Exception) {
        throw MGEModelException("setCampo Error:${e.message}")
    } finally {
        JapeSession.close(hnd)
    }
}

/**
 * Alterar o valor do campo informado
 * @author Luis Ricardo Alves Santos
 * @param name Nome do campo
 * @param value Valor
 * @param where condição a ser cumprida
 * @param instance Instância - Default: Instância atual
 * @return Unit
 */
fun setCampo(name: String, value: Any?, where: String, instance: String? = getInstancia()) {
    var hnd: JapeSession.SessionHandle? = null
    try {
        hnd = JapeSession.open()
        val instanciaDAO = JapeFactory.dao(instance)
        val fluidGetVO = instanciaDAO.findOne(where)
        val fluidupdate = instanciaDAO.prepareToUpdateByPK(fluidGetVO.primaryKey)
        fluidupdate.set(name, value)
        fluidupdate.update()
    } catch (e: Exception) {
        throw MGEModelException("setCampo Error:${e.message}")
    } finally {
        JapeSession.close(hnd)
    }
}

/**
 * Alterar o valor de mutiplos campos informados
 * @author Luis Ricardo Alves Santos
 * @param values Hashmap com nome e valor do campo
 * @param vo DynamicVO do item a ser atualizado
 * @param instance Instância - Default: Instância atual
 * @return Unit
 */
fun setCampos(
    values: HashMap<String, Any?>,
    vo: DynamicVO,
    instance: String? = getInstancia(),
    before: Boolean = false
): DynamicVO? {
    var hnd: JapeSession.SessionHandle? = null

    if (before) {
        values.forEach { (name, value) ->
            vo.setProperty(name, value)
        }
        return vo
    }
    try {
        hnd = JapeSession.open()
        val instanciaDAO = JapeFactory.dao(instance)
        val fluidupdate = instanciaDAO.prepareToUpdate(vo)
        values.forEach { (name, value) ->
            fluidupdate.set(name, value)
        }
        fluidupdate.update()
        return instanciaDAO.findByPK(vo.primaryKey)
    } catch (e: Exception) {
        throw MGEModelException("setCampos Error:${e.message}")
    } finally {
        JapeSession.close(hnd)
    }
}

/**
 * Alterar o valor de mutiplos campos informados
 * @author Luis Ricardo Alves Santos
 * @param values Hashmap com nome e valor do campo
 * @param where condição do item a ser atualizado
 * @param instance Instância - Default: Instância atual
 * @return Unit
 */
fun setCampos(values: HashMap<String, Any?>, where: String, instance: String? = getInstancia()): DynamicVO? {
    var hnd: JapeSession.SessionHandle? = null
    try {
        hnd = JapeSession.open()
        val instanciaDAO = JapeFactory.dao(instance)
        val fluidupdate = instanciaDAO.prepareToUpdate(instanciaDAO.findOne(where))
        values.forEach { (name, value) ->
            fluidupdate.set(name, value)
        }
        fluidupdate.update()
        return instanciaDAO.findOne(where)
    } catch (e: Exception) {
        throw MGEModelException("setCampos Error:${e.message}")
    } finally {
        JapeSession.close(hnd)
    }
}

/**
 * Classe para retornar o instância do resource atual
 * @author Luis Ricardo Alves Santos
 * @return Retorna a instância atual
 */
fun getInstancia(): String {
    val resourceID = ServiceContext.getCurrent().resourceId.split(".")

    return resourceID[resourceID.lastIndex]
}

/**
 * Retorna o valor de um json, incluindo, objetos aninhados(Nested object)
 * @sample getElementSample
 * @author Luis Ricardo Alves Santos
 * @param name  Nome da propriedade
 * @param json JSON
 * @return [JSONprop]
 */
@JvmName("GetElement")
fun getElement(name: String, json: Any): JSONprop {
    var jsonObject = json
    if (jsonObject is String) {
        jsonObject = try {
            JSONObject(jsonObject)
        } catch (e: JSONException) {
            JSONObject().put("data", json)
        }
        var element: Any? = null
        val props =
            name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() // Separa a notação de objeto
        for (ketyObj in jsonObject.keySet()) {
            //based on you key types
            val key = ketyObj as String?
            if (key == name) {
                element = jsonObject[key]
                return JSONprop(element)
            } else if ("JSONObject" == jsonObject[key].javaClass.simpleName) {
                val newProps = props.copyOfRange(1, props.size) // Remove a propriedade já checada
                element = getElement(java.lang.String.join(".", *newProps), jsonObject.getJSONObject(key))
            }
        }
        return JSONprop(element)
    }

    jsonObject as JSONObject
    var element: Any? = null
    val props =
        name.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() // Separa a notação de objeto
    for (ketyObj in jsonObject.keySet()) {
        //based on you key types
        val key = ketyObj as String?
        if (key == name) {
            element = jsonObject[key]
            return JSONprop(element)
        } else if ("JSONObject" == jsonObject[key].javaClass.simpleName) {
            val newProps = props.copyOfRange(1, props.size) // Remove a propriedade já checada
            element = getElement(java.lang.String.join(".", *newProps), jsonObject.getJSONObject(key))
        }
    }
    return JSONprop(element)
}

private fun getElementSample() = getElement(
    "Objeto.codigo", "{\n" +
            "  \"Objeto\":{\n" +
            "    \"codigo\":\"001\"\n" +
            "  }\n" +
            "}"
).toString()

/**
 * Retorna o valor de um campo(PK)
 * @author Luis Ricardo Alves Santos
 * @param name  Nome da propriedade
 * @param it DynamicVO do registro
 * @param instancia Instância - Default: Instância atual
 * @return [T]
 */
inline fun <reified T> getCampo(name: String, it: DynamicVO, instancia: String? = getInstancia()): T {
    var hnd: JapeSession.SessionHandle? = null
    try {
        hnd = JapeSession.open()
        val instanciaDAO = JapeFactory.dao(instancia)
        val fluidGetVO = instanciaDAO.findByPK(it.primaryKey)
        return fluidGetVO.getProperty(name) as T
    } catch (e: Exception) {
        throw MGEModelException("getCampo Error:${e.message}")
    } finally {
        JapeSession.close(hnd)
    }
}

/**
 * Retorna o valor de um campo(Where)
 * @author Luis Ricardo Alves Santos
 * @param name  Nome do campo
 * @param where Condição para retornar o registro
 * @param instancia Instância - Default: Instância atual
 * @return [T]
 */
inline fun <reified T> getCampo(name: String, where: String, instancia: String? = getInstancia()): T {
    var hnd: JapeSession.SessionHandle? = null
    try {
        hnd = JapeSession.open()
        val instanciaDAO = JapeFactory.dao(instancia)
        val fluidGetVO = instanciaDAO.findOne(where)
        return fluidGetVO.getProperty(name) as T

    } catch (e: Exception) {
        throw MGEModelException("getCampo Error:${e.message}")
    } finally {
        JapeSession.close(hnd)
    }
}

inline fun <reified T> getCampo(name: String, where: String, instancia: String? = "", canBeNull: Boolean = true): T? {
    var hnd: JapeSession.SessionHandle? = null
    try {
        hnd = JapeSession.open()
        val instanciaDAO = JapeFactory.dao(instancia)
        val fluidGetVO = instanciaDAO.findOne(where)
        return try {
            fluidGetVO.getProperty(name) as T
        } catch (e: Exception) {
            null
        }
    } catch (e: Exception) {
        throw MGEModelException("getCampo Error:${e.message}")
    } finally {
        JapeSession.close(hnd)
    }
}


/**
 * Retorna o DynamicVO baseado em uma consulta
 * @author Luis Ricardo Alves Santos
 * @param instancia  Nome da instancia
 * @param where Condição para retornar o registro
 * @throws MGEModelException
 * @return [DynamicVO?]
 */
@Throws(MGEModelException::class)
fun getVO(instancia: String? = getInstancia(), where: String?): DynamicVO? {
    var dynamicVo: DynamicVO? = null
    var hnd: SessionHandle? = null
    try {
        hnd = JapeSession.open()
        val instanciaDAO = JapeFactory.dao(instancia)
        dynamicVo = instanciaDAO.findOne(where)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
        throw MGEModelException("Erro getVO: ${e.message}")
    } finally {
        JapeSession.close(hnd)
    }
    return dynamicVo
}

/**
 * Retorna os registros baseados em uma consulta
 * @author Luis Ricardo Alves Santos
 * @param instancia  Nome da instancia
 * @param where Condição para retornar o registro
 * @throws MGEModelException
 * @return [DynamicVO?]
 */
@Throws(MGEModelException::class)
fun getVOs(where: String?, instancia: String? = getInstancia()): MutableCollection<DynamicVO>? {
    var dynamicVo: MutableCollection<DynamicVO>? = null
    var hnd: SessionHandle? = null
    try {
        hnd = JapeSession.open()
        val instanciaDAO = JapeFactory.dao(instancia)
        dynamicVo = instanciaDAO.find(where)
    } catch (e: java.lang.Exception) {
        throw MGEModelException("Erro getVO: ${e.message}")
    } finally {
        JapeSession.close(hnd)
    }
    return dynamicVo
}

/**
 * Converte um valor em BRL(com ",") para [BigDecimal]
 * @author Luis Ricardo Alves Santos
 * @param str  Texto a ser convertido
 * @return [BigDecimal]
 */
fun convertBrlToBigDecimal(str: String): BigDecimal {
    val string = str.replace("\"", "")
    val inId = Locale("pt", "BR")
    val nf: DecimalFormat = NumberFormat.getInstance(inId) as DecimalFormat
    nf.isParseBigDecimal = true
    return nf.parse(string, ParsePosition(0)) as BigDecimal
}

/**
 * Converte uma data dd/mm/yyyy ou dd-mm-yyyy em timestamp
 * @author Luis Ricardo Alves Santos
 * @param strDate  Texto a ser convertido
 * @return [Timestamp]
 */
fun stringToTimeStamp(strDate: String, format: String = "dd/MM/yyyy"): Timestamp? {
    return try {
        val formatter: DateFormat = SimpleDateFormat(format)
        val date: Date = formatter.parse(strDate)
        Timestamp(date.time)
    } catch (e: Exception) {
        null
    }
}

/**
 * Retorna o jsession e cookie da sessão corrente
 * @author Luis Ricardo Alves Santos
 * @return Pair<String, String>
 */
@JvmName("getLoginInfo1")
fun getLoginInfo(job: Boolean = false): Pair<String, String> {

    val cookie = if (!job) ServiceContext.getCurrent().httpRequest?.cookies?.find { cookie ->
        cookie.name == "JSESSIONID"
    } else null

    val session = ServiceContext.getCurrent().httpSessionId

    return Pair(session, "${cookie?.value}")
}

/**
 * TODO: Finalizar esse método
 * @author Luis Ricardo Alves Santos
 * @return Pair<String, String>
 */
fun loginSankhya(username: String, password: String): Pair<String, String> {
    val json = LoginJSON(
        "MobileLoginSP.login", utilitarios.jsons.RequestBody(
            Nomusu(username), Interno(password),
            Keepconnected("S")
        )
    )

    val (loginInfo, _, cookies) = post<LoginResponseJSON>(
        "http://localhost:8280/mge/service.sbr?serviceName=MobileLoginSP.login&outputType=json",
        json,
        interno = false
    )
    if (loginInfo.status != "1") throw Exception("Erro no login: ${loginInfo.statusMessage}")
    val jsession = loginInfo.responseBody.jsessionid.field
    val cookie = cookies.joinToString()

    return Pair(jsession, "$cookie")
}


val functionNameRegex = "(function)+[\\s]([a-zA-Z_{1}][a-zA-Z0-9_]+)(?=\\()".toRegex()
val removeCommentsRegex = "\"(^(\\/\\*+[\\s\\S]*?\\*\\/)|(\\/\\*+.*\\*\\/)|\\/\\/.*?[\\r\\n])[\\r\\n]*\"gm".toRegex()

/**
 * Executa uma função javascript e retorna o valor
 * @author Luis Ricardo Alves Santos
 * @param script  Nome da propriedade
 * @param args JSON
 * @return [Any?]
 */
fun runJSFunction(script: String, vararg args: Any?): Any? {
    val manager = ScriptEngineManager()
    val engine = manager.getEngineByName("JavaScript")
    val name = functionNameRegex.find(script)?.groupValues?.get(2)
    val inputScript = script.replace(removeCommentsRegex, "")
    engine.eval(inputScript)
    val invoker = engine as Invocable
    return invoker.invokeFunction(name, *args)
}


/*
* * Métodos para utilizar resources
* ========================================================================================
* * Métodos para utilizar resources
* ========================================================================================
*/
@Throws(java.lang.Exception::class)
fun getContentFromResource(baseClass: Class<*>, resourcePath: String): String {
    val stream = baseClass.getResourceAsStream(resourcePath)
        ?: throw IllegalArgumentException("Arquivo não nencontrado(${baseClass.name}):$resourcePath")

    return BufferedReader(
        InputStreamReader(stream, StandardCharsets.UTF_8)
    )
        .lines()
        .collect(Collectors.joining("\n"))
}

@Throws(java.lang.Exception::class)
fun loadResource(
    baseClass: Class<*> = Class.forName(Thread.currentThread().stackTrace[2].className),
    resourcePath: String
): String {
    return getContentFromResource(baseClass, resourcePath)
}

@Throws(java.lang.Exception::class)
fun loadResource(resourcePath: String): String {
    return getContentFromResource(Class.forName(Thread.currentThread().stackTrace[2].className), resourcePath)
}


/*
* * Métodos que utilizam Javascript
* ========================================================================================
* * Métodos que utilizam Javascript
* ========================================================================================
*/

/**
 * Retorna o valor de um json
 * @sample getElementSample
 * @author Luis Ricardo Alves Santos
 * @param prop  Nome da propriedade
 * @param json JSON
 * @return [JSONprop]
 */
inline fun <reified T : Any?> getPropFromJSON(prop: String, json: String): T {
    val script = loadResource(DiversosKT::class.java, "resources/getPropertyFromObject.js")
    val value: String = runJSFunction(script, json, prop) as String
    val valueObject = gson.fromJson<GetPropertyFromObject>(value, GetPropertyFromObject::class.java)
    if (valueObject.type == "array") {
        val mutableList = checkJsonType<T>(valueObject)
        return mutableList as T
    }
    return valueObject.data as T
}

fun getMutableList(valueObject: GetPropertyFromObject): MutableList<String> {
    val array = JSONArray("${valueObject.data}")
    val mutableList = mutableListOf<String>()
    for (i in 0 until array.length()) {
        mutableList.add(array.get(i) as String)
    }
    return mutableList
}

fun isJSONObject(item: Any): Boolean {
    var isObject = false
    isObject = try {
        JSONObject(item)
        true
    } catch (e: JSONException) {
        false
    }
    return isObject
}


/**
 * Retorna o valor de um json
 * @author Luis Ricardo Alves Santos
 * @param prop  Nome da propriedade
 * @param json JSON
 * @return [String]
 */
fun getPropFromJSON(prop: String, json: String): String {
    val script = loadResource(DiversosKT::class.java, "resources/getPropertyFromObject.js")
    val value = runJSFunction(script, json, prop)
    val valueObject = gson.fromJson<GetPropertyFromObject>("$value", GetPropertyFromObject::class.java)
    return "${valueObject.data}"
}

/**
 * Retorna o nome das propriedades de um JSON
 * @author Luis Ricardo Alves Santos
 * @param prop  Nome da propriedade
 * @param json JSON
 * @return [MutableList]
 */
fun getJSONkeys(json: String): MutableList<String> {
    val script = loadResource(DiversosKT::class.java, "resources/getKeys.js")
    val value: String = runJSFunction(script, json) as String
    val valueObject = gson.fromJson(value, GetPropertyFromObject::class.java)
    return getMutableList(valueObject)
}

inline fun <reified T> checkJsonType(valueObject: GetPropertyFromObject): Any {
    val json = "${valueObject.data}"
    return when (valueObject.type) {
        "string" -> gson.fromJson(json, String::class.java);
        "boolean" -> gson.fromJson(json, Boolean::class.java)
        "number" -> gson.fromJson(json, Number::class.java)
        "bigint" -> gson.fromJson(json, Number::class.java)
        "object" -> gson.fromJson(json, Any::class.java)
        "array" -> getMutableList(valueObject)
        else -> gson.fromJson(json, String::class.java)
    }
}

data class GetPropertyFromObject(val data: Any?, val type: String)


/*
* * Métodos para Webservice
* ========================================================================================
* * Métodos para Webservice
* ========================================================================================
*/
val baseurl: String = ServiceContext.getCurrent().httpRequest.localAddr
val porta = "${ServiceContext.getCurrent().httpRequest.localPort}"
val protocol = ServiceContext.getCurrent().httpRequest.protocol.split("/")[0].toLowerCase()
val localHost = "$protocol://$baseurl:$porta"
val regexContainsProtocol = """"(^http://)|(^https://)"gm""".toRegex()

/**
 * Método para realizar requisição POST HTTP/HTTPS
 * @author Luis Ricardo Alves Santos
 * @param  url: String: URL de destino para a requisição
 * @param reqBody: Any: Corpo da requisição
 * @param headersParams:  Map<String, String> - Default - emptyMap(): Cabeçalhos adicionais
 * @param queryParams: Map<String, String> - Default - emptyMap(): Parâmetros de query adicionais
 * @param contentType: String - Default - "application/json; charset=utf-8": Content type do corpo da requisição(MIME)
 * @param interno: Boolean - Default - false: Valida se é um requisição interna(Sankhya) ou externa
 * @return [T]
 */
inline fun <reified T> post(
    url: String,
    reqBody: Any,
    headersParams: Map<String, String> = emptyMap(),
    queryParams: Map<String, String> = emptyMap(),
    contentType: String = "application/json; charset=utf-8",
    interno: Boolean = false
): Triple<T, Headers, List<String>> {
    // Tratamento de paramentros query
    val query = queryParams.toMutableMap()
    val headers = headersParams.toMutableMap()
    var reqUrl = url
    if (interno || !url.matches(regexContainsProtocol)) {
        val loginInfo = getLoginInfo()
        if (url[0] != '/' && !url.contains("http")) reqUrl = "$localHost/$url"
        if (url[0] == '/' && !url.contains("http")) reqUrl = "$localHost$url"
        query += mapOf("jsessionid" to loginInfo.first, "mgeSession" to loginInfo.first)
//        headers["cookie"] = "JSESSIONID=${loginInfo.second}"
    }

    val httpBuilder: HttpUrl.Builder =
        HttpUrl.parse(reqUrl)?.newBuilder() ?: throw IllegalStateException("URL invalida")

    val jsonBody = gson.toJson(reqBody)
    query.forEach { (name, value) ->
        httpBuilder.addQueryParameter(name, value)
    }

    val urlWithQueryParams = httpBuilder.build()

    // Instância o client
    val client = OkHttpClient()

    // Define o contentType
    val mediaTypeParse = MediaType.parse(contentType)

    // Constrói o corpo da requisição
    val body = RequestBody.create(mediaTypeParse, jsonBody)

    val requestBuild = Request.Builder().url(urlWithQueryParams).post(body)
    headers.forEach { (name, value) ->
        requestBuild.addHeader(name, value)
    }

    val request = requestBuild.build()
    client.newCall(request).execute().use { response ->
        assert(response.body() != null)
        val responseJson = response.body()!!.string()
        return Triple(
            gson.fromJson(responseJson, T::class.java),
            response.headers(),
            response.headers().values("Set-Cookie")
        )
    }
}

/**
 * Método para realizar requisição POST HTTP/HTTPS
 * @author Luis Ricardo Alves Santos
 * @param  url: String: URL de destino para a requisição
 * @param reqBody: String: Corpo da requisição
 * @param headersParams:  Map<String, String> - Default - emptyMap(): Cabeçalhos adicionais
 * @param queryParams: Map<String, String> - Default - emptyMap(): Parâmetros de query adicionais
 * @param contentType: String - Default - "application/json; charset=utf-8": Content type do corpo da requisição(MIME)
 * @param interno: Boolean - Default - false: Valida se é um requisição interna(Sankhya) ou externa
 * @return [String]
 */
fun post(
    url: String,
    reqBody: String,
    headersParams: Map<String, String> = emptyMap(),
    queryParams: Map<String, String> = emptyMap(),
    contentType: String = "application/json; charset=utf-8",
    interno: Boolean = false
): Triple<String, Headers, List<String>> {

    // Tratamento de paramentros query
    val query = queryParams.toMutableMap()
    val headers = headersParams.toMutableMap()
    var reqUrl = url

    if (interno || !url.matches(regexContainsProtocol)) {
        val loginInfo = getLoginInfo()
        if (url[0] != '/' && !url.contains("http")) reqUrl = "$localHost/$url"
        if (url[0] == '/' && !url.contains("http")) reqUrl = "$localHost$url"
        query += mapOf("jsessionid" to loginInfo.first, "mgeSession" to loginInfo.first)
//        headers["cookie"] = "JSESSIONID=${loginInfo.second}"
    }
    val httpBuilder: HttpUrl.Builder =
        HttpUrl.parse(reqUrl)?.newBuilder() ?: throw IllegalStateException("URL invalida")
    query.forEach { (name, value) ->
        httpBuilder.addQueryParameter(name, value)
    }
    val urlWithQueryParams = httpBuilder.build()

    // Instância o client
    val client = OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS).build()

    // Define o contentType
    val mediaTypeParse = MediaType.parse(contentType)

    // Constrói o corpo da requisição
    val body = RequestBody.create(mediaTypeParse, reqBody)

    val requestBuild = Request.Builder().url(urlWithQueryParams).post(body)
    headers.forEach { (name, value) ->
        requestBuild.addHeader(name, value)
    }
    val request = requestBuild.build()
    client.newCall(request).execute().use { response ->
        assert(response.body() != null)
        return Triple(response.body()!!.string(), response.headers(), response.headers().values("Set-Cookie"))
    }
}


/**
 * Método para realizar requisição POST HTTP/HTTPS
 * @author Luis Ricardo Alves Santos
 * @param  url: String: URL de destino para a requisição
 * @param reqBody: String: Corpo da requisição
 * @param headersParams:  Map<String, String> - Default - emptyMap(): Cabeçalhos adicionais
 * @param queryParams: Map<String, String> - Default - emptyMap(): Parâmetros de query adicionais
 * @param contentType: String - Default - "application/json; charset=utf-8": Content type do corpo da requisição(MIME)
 * @param interno: Boolean - Default - false: Valida se é um requisição interna(Sankhya) ou externa
 * @return [String]
 */
fun post(
    url: String,
    reqBody: RequestBody,
    headersParams: Map<String, String> = emptyMap(),
    queryParams: Map<String, String> = emptyMap(),
    interno: Boolean = false
): Triple<String, Headers, List<String>> {

    // Tratamento de paramentros query
    val query = queryParams.toMutableMap()
    val headers = headersParams.toMutableMap()
    var reqUrl = url

    if (interno || !url.matches(regexContainsProtocol)) {
        val loginInfo = getLoginInfo()
        if (url[0] != '/' && !url.contains("http")) reqUrl = "$localHost/$url"
        if (url[0] == '/' && !url.contains("http")) reqUrl = "$localHost$url"
        query += mapOf("jsessionid" to loginInfo.first, "mgeSession" to loginInfo.first)
//        headers["cookie"] = "JSESSIONID=${loginInfo.second}"
    }
    val httpBuilder: HttpUrl.Builder =
        HttpUrl.parse(reqUrl)?.newBuilder() ?: throw IllegalStateException("URL invalida")
    query.forEach { (name, value) ->
        httpBuilder.addQueryParameter(name, value)
    }
    val urlWithQueryParams = httpBuilder.build()

    // Instância o client
    val client = OkHttpClient().newBuilder().connectTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS).build()

    // Define o contentType


    val requestBuild = Request.Builder().url(urlWithQueryParams).post(reqBody)
    headers.forEach { (name, value) ->
        requestBuild.addHeader(name, value)
    }
    val request = requestBuild.build()
    client.newCall(request).execute().use { response ->
        assert(response.body() != null)
        return Triple(response.body()!!.string(), response.headers(), response.headers().values("Set-Cookie"))
    }
}

/**
 * Método para realizar requisição GET HTTP/HTTPS
 * @author Luis Ricardo Alves Santos
 * @param  url: String: URL de destino para a requisição
 * @param headersParams:  Map<String, String> - Default - emptyMap(): Cabeçalhos adicionais
 * @param queryParams: Map<String, String> - Default - emptyMap(): Parâmetros de query adicionais
 * @param interno: Boolean - Default - false: Valida se é um requisição interna(Sankhya) ou externa
 * @return [String]
 */
@JvmName("Get1")
inline fun <reified T> get(
    url: String,
    headersParams: Map<String, String> = emptyMap(),
    queryParams: Map<String, String> = emptyMap(),
    interno: Boolean = false
): Triple<T, Headers, List<String>> {

    // Tratamento de paramentros query
    val query = queryParams.toMutableMap()
    val headers = headersParams.toMutableMap()
    var reqUrl = url
    if (interno || !url.matches(regexContainsProtocol)) {
        val loginInfo = getLoginInfo()
        if (url[0] != '/' && !url.contains("http")) reqUrl = "$localHost/$url"
        if (url[0] == '/' && !url.contains("http")) reqUrl = "$localHost$url"
        query += mapOf("jsessionid" to loginInfo.first, "mgeSession" to loginInfo.first)
        headers["cookie"] = "JSESSIONID=${loginInfo.second}"
    }

    val httpBuilder: HttpUrl.Builder =
        HttpUrl.parse(reqUrl)?.newBuilder() ?: throw IllegalStateException("URL invalida")

    query.forEach { (name, value) ->
        httpBuilder.addQueryParameter(name, value)
    }

    val urlWithQueryParams = httpBuilder.build()
    // Instância o client
    val client = OkHttpClient()

    val requestBuild = Request.Builder().url(urlWithQueryParams).get()
    headers.forEach { (name, value) ->
        requestBuild.addHeader(name, value)
    }
    val request = requestBuild.build()
    client.newCall(request).execute().use { response ->
        assert(response.body() != null)
        val responseJson = response.body()!!.string()
        return Triple(
            gson.fromJson(responseJson, T::class.java),
            response.headers(),
            response.headers().values("Set-Cookie")
        )
    }
}

/**
 * Método para realizar requisição GET HTTP/HTTPS
 * @author Luis Ricardo Alves Santos
 * @param  url: String: URL de destino para a requisição
 * @param headersParams:  Map<String, String> - Default - emptyMap(): Cabeçalhos adicionais
 * @param queryParams: Map<String, String> - Default - emptyMap(): Parâmetros de query adicionais
 * @param interno: Boolean - Default - false: Valida se é um requisição interna(Sankhya) ou externa
 * @return [String]
 */
fun get(
    url: String,
    headersParams: Map<String, String> = emptyMap(),
    queryParams: Map<String, String> = emptyMap(),
    interno: Boolean = false
): Triple<String, Headers, List<String>> {
    // Tratamento de paramentros query
    val query = queryParams.toMutableMap()
    val headers = headersParams.toMutableMap()
    var reqUrl = url

    if (interno || !url.matches(regexContainsProtocol)) {
        val loginInfo = getLoginInfo()
        if (url[0] != '/' && !url.contains("http")) reqUrl = "$localHost/$url"
        if (url[0] == '/' && !url.contains("http")) reqUrl = "$localHost$url"
        query += mapOf("jsessionid" to loginInfo.first, "mgeSession" to loginInfo.first)
        headers["cookie"] = "JSESSIONID=${loginInfo.second}"
    }
    val httpBuilder: HttpUrl.Builder =
        HttpUrl.parse(reqUrl)?.newBuilder() ?: throw IllegalStateException("URL invalida")
    query.forEach { (name, value) ->
        httpBuilder.addQueryParameter(name, value)
    }
    val urlWithQueryParams = httpBuilder.build()

    // Instância o client
    val client = OkHttpClient()

    val requestBuild = Request.Builder().url(urlWithQueryParams).get()
    headers.forEach { (name, value) ->
        requestBuild.addHeader(name, value)
    }
    val request = requestBuild.build()
    client.newCall(request).execute().use { response ->
        assert(response.body() != null)
        return Triple(response.body()!!.string(), response.headers(), response.headers().values("Set-Cookie"))
    }
}

class JSONprop(val obj: Any?) {
    inline fun <reified T : Any> toArray(): MutableList<T> {
        val jsonArray = JSONArray(obj.toString())
        val array = mutableListOf<T>()
        for (i in 0 until jsonArray.length()) {
            val item = gson.fromJson(jsonArray[i].toString(), T::class.java)
            array.add(item)
        }
        return array
    }

    @JvmName("toArrayAny")
    fun toArray(): MutableList<Any> {
        val jsonArray = JSONArray(obj.toString())
        val array = mutableListOf<Any>()
        for (i in 0 until jsonArray.length()) {
            val item = gson.fromJson(jsonArray[i].toString(), Any::class.java)
            array.add(item)
        }
        return array
    }


    override fun toString(): String {
        return obj.toString()
    }
}


typealias QueryCallback = (n: NativeSql?) -> NativeSql?

/**
 * Classe para realizar consulta SQL
 * @author Luis Ricardo Alves Santos
 * @param  query: [String]: Consulta SQL a ser executada
 * @param closeStream: [Boolean]: - Default - [true] - Fecha a conexão automaticamente após a leitura dos dados do [resultSet]
 * @param callBack:  [QueryCallback] - Permite adição de novas propriedades ao objeto [NativeSql]
 * @return [String]
 */

class RunQuery(query: String, private val closeStream: Boolean = true, callBack: QueryCallback? = null) {
    private val jdbc: JdbcWrapper?
    private var sql: NativeSql?
    private val hnd: SessionHandle? = JapeSession.open()
    private val resultSet: ResultSet?

    init {
        try {
            hnd?.findersMaxRows = -1
            val entity = EntityFacadeFactory.getDWFFacade()
            jdbc = entity.jdbcWrapper
            jdbc.openSession()
            sql = NativeSql(jdbc)
            sql!!.appendSql(query)
            callBack?.let {
                val newNativeSQL = it(sql)
                sql = newNativeSQL
            }
            resultSet = sql?.executeQuery()
        } catch (e: Exception) {
            e.printStackTrace()
            throw Exception("Error durante execução da query:${e.message}")
        }
    }

    /**
     * Percorre os resultados da consulta.
     * OBS: Após a execução, será fechada a conexão, o que impede que o ResultSet seja usado novamente.
     * Esse comportamento pode ser evitado passando o parametro [closeStream] como `[false]` no ato de instância da classe
     * @author Luis Ricardo Alves Santos
     * @return [Unit]
     */
    fun forEach(callback: (it: ResultSet?) -> Unit) {
        if (resultSet == null) return
        while (resultSet.next()) {
            callback(resultSet)
        }
        if (this.closeStream) {
            this.close()
        }
    }

    fun getMetaData(): ResultSetMetaData? {

        return resultSet?.metaData
    }
    /**
     * Fecha a conexão com o banco de dados e limpa os dados do ResultSet
     * @return [Unit]
     */
    fun close() {
        JdbcUtils.closeResultSet(resultSet)
        NativeSql.releaseResources(sql)
        JdbcWrapper.closeSession(jdbc)
        JapeSession.close(hnd)
    }
}

/**
 * Método para realizar consulta SQL
 * @author Luis Ricardo Alves Santos
 * @param  query: [String]: Consulta SQL a ser executada
 * @param callBack:  [QueryCallback] - Default - emptyMap(): Cabeçalhos adicionais
 * @return [Boolean]
 */
fun runQuery(
    query: String,
    callBack: QueryCallback? = null,
    update: Boolean = false
): Boolean? {
    val jdbc: JdbcWrapper?
    var sql: NativeSql?
    val hnd: SessionHandle? = JapeSession.open()
    hnd?.findersMaxRows = -1
    val entity = EntityFacadeFactory.getDWFFacade()
    jdbc = entity.jdbcWrapper
    jdbc.openSession()
    sql = NativeSql(jdbc)
    sql.appendSql(query)
    callBack?.let {
        val newNativeSQL = it(sql)
        sql = newNativeSQL
    }

    return sql?.executeUpdate()
}

/**
 * Método para criar o cláusula IN do SQL
 * @author Luis Ricardo Alves Santos
 * @param  array: [Array<String>]: Valores a serem usados no IN
 * @return [String]
 */
fun toSqlInClause(array: Array<String>): String {
    return array.joinToString(prefix = "(", postfix = ")") {
        "'$it'".trim().toUpperCase()
    }
}

/**
 * Método para realizar substituição de caracteres coringa
 * @author Luis Ricardo Alves Santos
 * @param  text: [String]: Texto que possui caracteres a serem substituidos
 * @param  text: [HashMap<String, String>]: Valores a serem substituidos dentro do texto
 * @return [String]
 */
fun replaceTemplate(text: String, values: HashMap<String, String>): String {
    var replacedText = text
    values.forEach { (fieldName, value) ->
        val regex = "#\\{$fieldName+\\}".toRegex()
        replacedText = regex.replace(replacedText, value)
    }
    return replacedText
}


fun novaLinhaVo(instancia: String, hnd: SessionHandle): FluidCreateVO {
    val instanciaDAO = JapeFactory.dao(instancia);
    val create = instanciaDAO.create();
    return create;
}

/**
 * Método para criar um novo registro na instância informada.
 * @author Luis Ricardo Alves Santos
 * @param  values: [HashMap]<[String],[Any]>: Nomes e valores dos campos.
 * @param  instance: [String]: instancia a ser criado o novo registro
 * @return [String]
 */
fun createNewLine(values: HashMap<String, Any>, instance: String? = getInstancia()): DynamicVO {
    var hnd: JapeSession.SessionHandle? = null
    var listValues = ""
    try {
        hnd = JapeSession.open()
        val instanciaDAO = JapeFactory.dao(instance)
        val fluidCreate = instanciaDAO.create()
        values.forEach { (name, value) ->
            fluidCreate.set(name, value)
            listValues += "$name= $value\n"
        }
        return fluidCreate.save()
    } catch (e: Exception) {
        throw MGEModelException("createNewLine Error(Values->$listValues:${e.message}")
    } finally {
        JapeSession.close(hnd)
    }
}


/**
 * Método para Salvar campo do tipo arquivo.
 * @author Luis Ricardo Alves Santos
 * @param  values: [HashMap]<[String],[Any]>: Nomes e valores dos campos.
 * @param  instance: [String]: instancia a ser criado o novo registro
 * @return [Boolean]
 */
fun saveFileField(field: String, fileName: String, filePath: String, where: String, instance: String): Boolean {

    val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
        .addFormDataPart(
            "arquivo", filePath,
            RequestBody.create(
                MediaType.parse("application/octet-stream"),
                File(filePath)
            )
        )
        .build()

    post("/mge/sessionUpload.mge?sessionkey=SanhkyaZIP2&fitem=S&salvar=S&useCache=N", body)

    val tempFile = SessionFile.getTempViewerFileDir()
    if (!tempFile.isDirectory) return false
    val file = tempFile.walk().find { item -> item.name == fileName } ?: return false
    val size = file.length()
    val format = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")
    val lastModified = LocalDateTime.ofInstant(Instant.ofEpochMilli(size), ZoneId.systemDefault()).format(format)
    val json = """
    __start_fileinformation__ {
    "name": "${tempFile.name}",
    "size": $size,
    "type": "application/x-zip-compressed",
        "lastModifiedDate": "$lastModified"
}
__end_fileinformation__""".trimIndent()

    setCampo(field, "$json${file.readBytes()}", where, instance)

    return true;
}

@Throws(java.lang.Exception::class)
fun setCampoAfter(campo: String?, value: Any?, evt: PersistenceEvent, useOld: Boolean = false) {
    val json = ServiceContext.getCurrent().jsonRequestBody.toString()
    val entity = getPropFromJSON("entityName", json)
    val inst = (if (!useOld) evt.vo else evt.oldVO) as DynamicVO
    var hnd: SessionHandle? = null
    try {
        hnd = JapeSession.open()
        val instanciaDAO = JapeFactory.dao(entity)
        val fluidupdate = instanciaDAO.prepareToUpdateByPK(inst.primaryKey)
        fluidupdate[campo] = value
        fluidupdate.update()
    } catch (e: java.lang.Exception) {
        throw MGEModelException("Erro setCampoAfter:${e.message}")
    } finally {
        JapeSession.close(hnd)
    }
}


