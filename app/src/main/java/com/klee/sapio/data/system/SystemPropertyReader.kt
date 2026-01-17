package com.klee.sapio.data.system

import android.annotation.SuppressLint
import android.util.Log

@SuppressLint("PrivateApi")
class SystemPropertyReader {

    private val getMethod by lazy {
        val clazz = Class.forName("android.os.SystemProperties")
        clazz.getMethod("get", String::class.java, String::class.java)
    }

    fun read(propertyName: String): String {
        return try {
            val value = getMethod.invoke(null, propertyName, "") as String
            value.trim()
        } catch (exception: ReflectiveOperationException) {
            Log.w(TAG, "Unable to read property $propertyName", exception)
            ""
        } catch (exception: IllegalArgumentException) {
            Log.w(TAG, "Invalid arguments supplied for $propertyName", exception)
            ""
        }
    }

    private companion object {
        private const val TAG = "SystemPropertyReader"
    }
}
