package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.model.ActionItem
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromActionItemList(items: List<ActionItem>): String {
        val jsonArray = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("title", item.title)
                put("dateOrTime", item.dateOrTime)
                put("details", item.details)
                put("category", item.category)
                put("isChecked", item.isChecked)
            }
            jsonArray.put(obj)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toActionItemList(jsonStr: String?): List<ActionItem> {
        if (jsonStr.isNullOrBlank()) return emptyList()
        return try {
            val list = mutableListOf<ActionItem>()
            val jsonArray = JSONArray(jsonStr)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    ActionItem(
                        id = obj.optString("id"),
                        title = obj.optString("title"),
                        dateOrTime = obj.optString("dateOrTime", "Today"),
                        details = obj.optString("details", ""),
                        category = obj.optString("category", "General"),
                        isChecked = obj.optBoolean("isChecked", false)
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
