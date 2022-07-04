package com.android.sapio.model

import android.graphics.drawable.Drawable
import java.util.Date

data class Application(
    val name: String,
    val packageName: String,
    val iconUrl: String?,
    val rating: Int,
    val microg: Int,
    val rooted: Int,
    val updatedAt: Date
)

data class InstalledApplication(
    val name:String,
    val packageName: String,
    val icon: Drawable?
)

