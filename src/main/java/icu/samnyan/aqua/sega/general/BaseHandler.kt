package icu.samnyan.aqua.sega.general

import com.fasterxml.jackson.core.JsonProcessingException
import jakarta.servlet.http.HttpServletRequest

/**
 * @author samnyan (privateamusement@protonmail.com)
 */
fun interface BaseHandler {
    @Throws(JsonProcessingException::class)
    fun handle(request: Map<String, Any>): Any?
}

data class RequestContext(
    val req: HttpServletRequest,
    val data: Map<String, Any>,
)

typealias SpecialHandler = (RequestContext) -> Any?
fun BaseHandler.toSpecial() = { ctx: RequestContext -> handle(ctx.data) }
