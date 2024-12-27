package icu.samnyan.aqua.sega.util.jackson

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

interface IMapper {
    fun write(o: Any?): String
}

@Component
class BasicMapper: IMapper {
    companion object {
        val BASIC_MAPPER = jacksonObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(SerializationFeature.WRITE_ENUMS_USING_INDEX, true)
            findAndRegisterModules()
            registerModule(SimpleModule().apply {
                addSerializer(
                    LocalDateTime::class.java,
                    LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.0"))
                )
                addDeserializer(
                    LocalDateTime::class.java,
                    LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.0"))
                )
            })
        }
    }

    override fun write(o: Any?) =
        BASIC_MAPPER.writeValueAsString(o)

    fun <T> read(jsonStr: String?, toClass: Class<T>?) =
        BASIC_MAPPER.readValue(jsonStr, toClass)

    fun <T> read(jsonStr: String?, toValueTypeRef: TypeReference<T>?) =
        BASIC_MAPPER.readValue(jsonStr, toValueTypeRef)

    fun <T> convert(map: Any?, toClass: Class<T>?) =
        BASIC_MAPPER.convertValue(map, toClass)

    fun <T> convert(map: Any?, toValueTypeRef: TypeReference<T>?) =
        BASIC_MAPPER.convertValue(map, toValueTypeRef)

    fun toMap(obj: Any?): LinkedHashMap<String, Any?> =
        BASIC_MAPPER.convertValue(obj, object : TypeReference<LinkedHashMap<String, Any?>>() {})
}
