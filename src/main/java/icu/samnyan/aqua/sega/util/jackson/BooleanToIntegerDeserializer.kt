package icu.samnyan.aqua.sega.util.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import ext.int

class BooleanToIntegerDeserializer : JsonDeserializer<Int>() {
    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Int {
        return when (p.currentToken) {
            JsonToken.VALUE_STRING -> when (val str = p.valueAsString.lowercase()) {
                "true" -> 1
                "false" -> 0
                else -> str.int
            }
            JsonToken.VALUE_NUMBER_INT -> p.intValue
            JsonToken.VALUE_TRUE -> 1
            JsonToken.VALUE_FALSE -> 0
            else -> throw UnsupportedOperationException("Cannot deserialize to boolean int")
        }
    }
}
