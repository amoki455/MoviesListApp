package com.example.movielist.data.sources.local.database

import androidx.room.TypeConverter
import com.example.movielist.data.models.Genre
import com.example.movielist.data.models.ProductionCompany
import com.example.movielist.data.models.ProductionCountry
import com.example.movielist.data.models.SpokenLanguage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// converters for room database to use to save lists as json string

class Converters {
    companion object {
        private val gson = Gson()
    }

    @TypeConverter
    fun fromIntListToJsonString(intList: List<Int>): String = gson.toJson(intList)

    @TypeConverter
    fun fromJsonStringToIntList(string: String): List<Int> {
        val type = TypeToken.getArray(Integer::class.java).type
        val array: Array<Int> = gson.fromJson(string, type)
        return array.toList()
    }

    @TypeConverter
    fun fromGenreListToJsonString(genres: List<Genre>): String = gson.toJson(genres)

    @TypeConverter
    fun fromJsonStringToGenreList(string: String): List<Genre> {
        val type = TypeToken.getArray(Genre::class.java).type
        val array: Array<Genre> = gson.fromJson(string, type)
        return array.toList()
    }

    @TypeConverter
    fun fromProductionCompanyListToJsonString(productionCompanies: List<ProductionCompany>): String = gson.toJson(productionCompanies)

    @TypeConverter
    fun fromJsonStringToProductionCompanyList(string: String): List<ProductionCompany> {
        val type = TypeToken.getArray(ProductionCompany::class.java).type
        val array: Array<ProductionCompany> = gson.fromJson(string, type)
        return array.toList()
    }

    @TypeConverter
    fun fromProductionCountryListToJsonString(productionCountries: List<ProductionCountry>): String = gson.toJson(productionCountries)

    @TypeConverter
    fun fromJsonStringToProductionCountryList(string: String): List<ProductionCountry> {
        val type = TypeToken.getArray(ProductionCountry::class.java).type
        val array: Array<ProductionCountry> = gson.fromJson(string, type)
        return array.toList()
    }

    @TypeConverter
    fun fromSpokenLanguageListToJsonString(spokenLanguages: List<SpokenLanguage>): String = gson.toJson(spokenLanguages)

    @TypeConverter
    fun fromJsonStringToSpokenLanguageList(string: String): List<SpokenLanguage> {
        val type = TypeToken.getArray(SpokenLanguage::class.java).type
        val array: Array<SpokenLanguage> = gson.fromJson(string, type)
        return array.toList()
    }
}