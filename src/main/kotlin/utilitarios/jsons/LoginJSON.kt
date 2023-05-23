package utilitarios.jsons

import com.google.gson.annotations.SerializedName

data class LoginJSON(
    val serviceName: String,
    val requestBody: RequestBody
)


data class RequestBody(
    @SerializedName("NOMUSU")
    val nomusu: Nomusu,
    @SerializedName("INTERNO")
    val interno: Interno,
    @SerializedName("KEEPCONNECTED")
    val keepconnected: Keepconnected
)

data class Nomusu(
    @SerializedName("$")
    val field: String
)

data class Interno(
    @SerializedName("$")
    val field: String
)

data class Keepconnected(
    @SerializedName("$")
    val field: String
)


data class LoginResponseJSON(
    val serviceName: String,
    val status: String,
    val pendingPrinting: String,
    val transactionId: String,
    val tsError: TsError,
    val statusMessage: String,
    val responseBody: ResponseBody
)

data class TsError(
    val tsErrorCode: String,
    val tsErrorLevel: String
)

data class ResponseBody(
    @SerializedName("callID")
    val callId: CallId,
    val jsessionid: Jsessionid,
    @SerializedName("kID")
    val kId: KId,
    val idusu: Idusu
)

data class CallId(
    @SerializedName("$")
    val field: String
)

data class Jsessionid(
    @SerializedName("$")
    val field: String
)

data class KId(
    @SerializedName("$")
    val field: String
)

data class Idusu(
    @SerializedName("$")
    val field: String
)



