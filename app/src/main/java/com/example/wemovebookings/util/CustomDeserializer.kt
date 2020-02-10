package com.example.wemovebookings.util

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.*
import java.lang.reflect.Type

class CustomDeserializer<T>(val clasz: Class<T>) : JsonDeserializer<T> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): T {
        if (json?.isJsonArray == true) {
            return (Gson().fromJson<T>(json.asJsonArray.get(0), clasz))
        }
        return Gson().fromJson(json?.asJsonObject, clasz)
    }

}