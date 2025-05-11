package com.klee.sapio.data

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import com.fasterxml.jackson.annotation.JsonProperty
import com.klee.sapio.R
import java.util.Date

data class StrapiAnswer(
    @JsonProperty("data") val data: ArrayList<StrapiElement>,
    @JsonProperty("meta") val meta: StrapiMeta
)

data class StrapiMeta(
    @JsonProperty("pagination") val pagination: StrapiPagination?
)

data class StrapiPagination(
    @JsonProperty("page") val page: Int,
    @JsonProperty("pageSize") val pageSize: Int,
    @JsonProperty("pageCount") val pageCount: Int,
    @JsonProperty("total") val total: Int
)

data class StrapiImageElement(
    @JsonProperty("id") val id: Int,
    @JsonProperty("attributes") val attributes: RemoteImage
)

data class StrapiElement(
    @JsonProperty("id") val id: Int,
    @JsonProperty("attributes") val attributes: Evaluation
)

data class Evaluation(
    @JsonProperty("name") val name: String,
    @JsonProperty("packageName") val packageName: String,
    @JsonProperty("icon") var icon: Icon?,
    @JsonProperty("rating") val rating: Int,
    @JsonProperty("microg") val microg: Int,
    @JsonProperty("rooted") val secure: Int,
    @JsonProperty("updatedAt") val updatedAt: Date?,
    @JsonProperty("createdAt") val createdAt: Date?,
    @JsonProperty("publishedAt") val publishedAt: Date?,
    @JsonProperty("versionName") val versionName: String?
)

data class Icon(
    @JsonProperty("data") val data: StrapiImageElement?
)

data class RemoteImage(
    @JsonProperty("name") val name: String,
    @JsonProperty("alternativeText") val alternativeText: String?,
    @JsonProperty("caption") val caption: String?,
    @JsonProperty("width") val width: Int,
    @JsonProperty("height") val height: Int,
    @JsonProperty("formats") val formats: RemoteImageFormats?,
    @JsonProperty("hash") val hash: String,
    @JsonProperty("ext") val ext: String,
    @JsonProperty("mime") val mime: String,
    @JsonProperty("size") val size: Int,
    @JsonProperty("url") val url: String,
    @JsonProperty("previewUrl") val previewUrl: String?,
    @JsonProperty("provider") val provider: String?,
    @JsonProperty("provider_metadata") val provider_metadata: String?,
    @JsonProperty("createdAt") val createdAt: Date,
    @JsonProperty("updatedAt") val updatedAt: Date
)

data class RemoteImageFormats(
    @JsonProperty("thumbnail") val thumbnail: Image,
    @JsonProperty("large") val large: Image?,
    @JsonProperty("medium") val medium: Image?,
    @JsonProperty("small") val small: Image?
)

data class Image(
    @JsonProperty("name") val name: String,
    @JsonProperty("hash") val hash: String,
    @JsonProperty("ext") val ext: String,
    @JsonProperty("mime") val mime: String,
    @JsonProperty("path") val path: String?,
    @JsonProperty("width") val width: Int,
    @JsonProperty("height") val height: Int,
    @JsonProperty("size") val size: Int,
    @JsonProperty("url") val url: String
)

data class UploadEvaluation(
    @JsonProperty("name") val name: String,
    @JsonProperty("packageName") val packageName: String,
    @JsonProperty("icon") var icon: Int?,
    @JsonProperty("rating") val rating: Int,
    @JsonProperty("microg") val microg: Int,
    @JsonProperty("rooted") val rooted: Int
)

data class UploadAnswer(
    @JsonProperty("data") val data: StrapiElement,
    @JsonProperty("meta") val meta: StrapiMeta?
)

data class UploadEvaluationHeader(
    @JsonProperty("data") var data: UploadEvaluation
)

data class IconAnswer(
    @JsonProperty("id") val id: Int,
    @JsonProperty("name") val name: String,
    @JsonProperty("alternativeText") val alternativeText: String?,
    @JsonProperty("caption") val caption: String?,
    @JsonProperty("width") val width: Int,
    @JsonProperty("height") val height: Int,
    @JsonProperty("formats") val formats: RemoteImageFormats?,
    @JsonProperty("hash") val hash: String,
    @JsonProperty("ext") val ext: String,
    @JsonProperty("mime") val mime: String,
    @JsonProperty("size") val size: Int,
    @JsonProperty("url") val url: String,
    @JsonProperty("previewUrl") val previewUrl: String?,
    @JsonProperty("provider") val provider: String?,
    @JsonProperty("provider_metadata") val provider_metadata: String?,
    @JsonProperty("createdAt") val createdAt: Date,
    @JsonProperty("updatedAt") val updatedAt: Date
)

data class InstalledApplication(
    val name: String,
    val packageName: String,
    val icon: Drawable,
)

object GmsType {
    const val MICROG = 1
    const val BARE_AOSP = 2
    const val GOOGLE_PLAY_SERVICES = 3
}

object UserType {
    const val SECURE = 3
    const val RISKY = 4
}

data class Label(val text: String, val color: Int) {

    companion object {
        const val MICROG = GmsType.MICROG
        const val BARE_AOSP = GmsType.BARE_AOSP
        const val SECURE = UserType.SECURE
        const val RISKY = UserType.RISKY

        @RequiresApi(Build.VERSION_CODES.M)
        fun create(context: Context, label: Int): Label {
            return when (label) {
                MICROG -> Label(
                    context.getString(R.string.microg_label), context.getColor(R.color.blue_200)
                )
                BARE_AOSP -> Label(
                    context.getString(R.string.bare_aosp_label), context.getColor(R.color.blue_700)
                )
                SECURE -> Label(
                    context.getString(R.string.secure_label), context.getColor(R.color.purple_200)
                )
                RISKY -> Label(
                    context.getString(R.string.risky_label), context.getColor(R.color.purple_700)
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

        private const val GREEN_CIRCLE_EMOJI =  0x1F7E2
        private const val YELLOW_CIRCLE_EMOJI = 0x1F7E1
        private const val RED_CIRCLE_EMOJI = 0x1F534

        fun create(rating: Int): Rating {
            return when (rating) {
                GOOD -> Rating(GOOD, String(Character.toChars(GREEN_CIRCLE_EMOJI)))
                AVERAGE -> Rating(AVERAGE, String(Character.toChars(YELLOW_CIRCLE_EMOJI)))
                BAD -> Rating(BAD, String(Character.toChars(RED_CIRCLE_EMOJI)))
                else -> Rating(BAD, String(Character.toChars(RED_CIRCLE_EMOJI)))
            }
        }
    }
}
