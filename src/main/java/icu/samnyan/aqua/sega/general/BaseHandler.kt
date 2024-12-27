package icu.samnyan.aqua.sega.general

import com.fasterxml.jackson.core.JsonProcessingException
import ext.long
import ext.parsing
import jakarta.servlet.http.HttpServletRequest


fun interface BaseHandler {
    @Throws(JsonProcessingException::class)
    fun handle(request: Map<String, Any>): Any?
}

data class RequestContext(
    val req: HttpServletRequest,
    val data: Map<String, Any>,
) {
    val uid by lazy { parsing { data["userId"]!!.long } }
}

typealias SpecialHandler = RequestContext.() -> Any?
fun BaseHandler.toSpecial() = { ctx: RequestContext -> handle(ctx.data) }

// A very :3 way of declaring APIs
abstract class MeowApi(val serialize: (String, Any?) -> String) {
    val initH = mutableMapOf<String, SpecialHandler>()
    infix operator fun String.invoke(fn: SpecialHandler) = initH.set("${this}Api", fn)
    infix fun List<String>.all(fn: SpecialHandler) = forEach { it(fn) }
    infix fun String.static(fn: () -> Any) = serialize(this, fn()).let { resp -> this { resp } }
}
