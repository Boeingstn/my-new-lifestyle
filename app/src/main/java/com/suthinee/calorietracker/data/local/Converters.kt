package com.suthinee.calorietracker.data.local

import androidx.room.TypeConverter
import com.suthinee.calorietracker.domain.model.MealType
import com.suthinee.calorietracker.domain.model.ReminderType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let { LocalDateTime.parse(it) }

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let { LocalTime.parse(it) }

    @TypeConverter
    fun fromMealType(value: MealType?): String? = value?.name

    @TypeConverter
    fun toMealType(value: String?): MealType? = value?.let { MealType.valueOf(it) }

    @TypeConverter
    fun fromReminderType(value: ReminderType?): String? = value?.name

    @TypeConverter
    fun toReminderType(value: String?): ReminderType? = value?.let { ReminderType.valueOf(it) }
}
