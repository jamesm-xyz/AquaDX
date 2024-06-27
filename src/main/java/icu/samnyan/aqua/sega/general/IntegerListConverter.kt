package icu.samnyan.aqua.sega.general

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class IntegerListConverter : AttributeConverter<List<Int>, String> {
    override fun convertToDatabaseColumn(lst: List<Int>?) = lst?.joinToString(";") ?: ""
    override fun convertToEntityAttribute(str: String?) = if (str.isNullOrBlank()) mutableListOf() else
        str.split(';').map { it.toInt() }.toMutableList()
}
