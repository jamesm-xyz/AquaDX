package icu.samnyan.aqua.net.db

import ext.arr
import icu.samnyan.aqua.net.utils.ApiException
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Timer
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.TimeSource
import kotlin.time.toJavaDuration
import io.micrometer.core.instrument.Metrics as MMetrics

operator fun Counter.unaryPlus() = increment()

class APICounter(val api: String, val metrics: APIMetrics) {
    operator fun unaryPlus() = +metrics["api_count", arr("api", api)]

    operator fun rem(err: Exception) = also {
        val e = if (err is ApiException) err.code.toString() else err.javaClass.simpleName
        +metrics["api_error_count", arr("api", api, "error", e)]
    }

    operator fun <T> invoke(fn: () -> T): T {
        val start = TimeSource.Monotonic.markNow()
        try { return fn().also { +this } }
        catch (e: Exception) { throw e.also { this % e } }
        finally {
            metrics
                .timer("api_latency", arr("api", api))
                .record(start.elapsedNow().toJavaDuration())
        }
    }
}

class APIMetrics(val domain: String) {
    val cache = ConcurrentHashMap<Array<String>, Any>()
    val reg = MMetrics.globalRegistry

    operator fun get(name: String, vararg pairs: Pair<String, Any>) =
        get(name, pairs.flatMap { listOf(it.first, it.second.toString()) }.toTypedArray())

    operator fun get(name: String, tag: Array<String>) = cache.computeIfAbsent(tag) {
        Counter
            .builder("aquadx_${domain}_$name")
            .tags(*tag)
            .register(reg)
    } as Counter

    fun timer(name: String, tag: Array<String>) = cache.computeIfAbsent(tag) {
        Timer
            .builder("aquadx_${domain}_$name")
            .tags(*tag)
            .publishPercentiles(0.5, 0.75, 0.90, 0.95, 0.99)
            .register(reg)
    } as Timer

    operator fun get(api: String) = APICounter(api, this)
    operator fun set(api: String, value: APICounter) {}
}
