package com.klee.sapio.model

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.klee.sapio.R
import java.util.Date

data class StrapiAnswer(
    @JsonProperty("data") val data: ArrayList<StrapiElement>,
    @JsonProperty("meta") val meta: StrapiMeta
)

data class StrapiMeta(
    @JsonProperty("pagination") val pagination: StrapiPagination
)

data class StrapiPagination(
    @JsonProperty("page") val page:Int,
    @JsonProperty("pageSize") val pageSize:Int,
    @JsonProperty("pageCount") val pageCount:Int,
    @JsonProperty("total") val total:Int
)

data class StrapiElement(
    @JsonProperty("id") val id: Int,
    @JsonProperty("attributes") val attributes: RemoteApplication
)

data class RemoteApplication(
    @JsonProperty("name") val name: String,
    @JsonProperty("packageName") val packageName: String,
    @JsonProperty("icon") val iconUrl: Drawable?,
    @JsonProperty("rating") val rating: Int,
    @JsonProperty("microg") val microg: Int,
    @JsonProperty("rooted") val rooted: Int,
    @JsonProperty("updatedAt") val updatedAt: Date,
    @JsonProperty("createdAt") val createdAt: Date,
    @JsonProperty("publishedAt") val publishedAt: Date
)

data class InstalledApplication(
    val name: String,
    val packageName: String,
    val icon: Drawable?
)

data class Label(val text: String, val color: Int) {

    companion object {
        const val MICROG = 1
        const val BARE_AOSP = 2
        const val USER = 3
        const val ROOTED = 4

        @RequiresApi(Build.VERSION_CODES.M)
        fun create(context: Context, label: Int): Label {
            return when (label) {
                MICROG -> Label(
                    context.getString(R.string.microg_label), context.getColor(R.color.teal_200)
                )
                BARE_AOSP -> Label(
                    context.getString(R.string.bare_aosp_label), context.getColor(R.color.teal_700)
                )
                USER -> Label(
                    context.getString(R.string.user_label), context.getColor(R.color.purple_200)
                )
                ROOTED -> Label(
                    context.getString(R.string.rooted_label), context.getColor(R.color.purple_500)
                )
                else -> Label(" Empty label ", context.getColor(R.color.black))
            }
        }
    }
}

data class Rating(val value: Int, val text: String) {

    companion object {

        const val GOOD = 1
        const val AVERAGE = 2
        const val BAD = 3

        fun create(rating: Int): Rating {
            return when (rating) {
                GOOD -> Rating(GOOD, "\uD83D\uDFE2 \uD83E\uDD47")
                AVERAGE -> Rating(AVERAGE, "\uD83D\uDFE0 \uD83D\uDE10")
                BAD -> Rating(BAD, "\uD83D\uDD34 \uD83D\uDC4E")
                else -> Rating(BAD, "\uD83D\uDD34 \uD83D\uDC4E")
            }
        }
    }
}
