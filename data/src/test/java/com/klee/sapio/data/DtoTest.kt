package com.klee.sapio.data

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.klee.sapio.data.dto.Image
import com.klee.sapio.data.dto.RemoteImageFormats
import com.klee.sapio.data.dto.StrapiAnswer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DtoTest {

    // Mirrors the mapper the Retrofit client is built with.
    private val mapper = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    @Test
    fun `an image is read from its json payload`() {
        val image = mapper.readValue(IMAGE_JSON, Image::class.java)

        assertEquals("thumbnail_icon.png", image.name)
        assertEquals("thumbnail_icon_hash", image.hash)
        assertEquals(".png", image.ext)
        assertEquals("image/png", image.mime)
        assertEquals(156, image.width)
        assertEquals(156, image.height)
        assertEquals(4437, image.size)
        assertEquals("/uploads/thumbnail_icon.png", image.url)
        assertNull(image.path)
    }

    @Test
    fun `an unknown image field is ignored`() {
        val image = mapper.readValue(IMAGE_JSON.dropLast(1) + ""","sizeInBytes": 10}""", Image::class.java)

        assertEquals("thumbnail_icon.png", image.name)
    }

    @Test
    fun `image formats keep only the thumbnail when the others are missing`() {
        val formats = mapper.readValue("""{"thumbnail":$IMAGE_JSON}""", RemoteImageFormats::class.java)

        assertEquals("thumbnail_icon.png", formats.thumbnail.name)
        assertNull(formats.large)
        assertNull(formats.medium)
        assertNull(formats.small)
    }

    @Test
    fun `image formats expose every declared size`() {
        val json =
            """{"thumbnail":$IMAGE_JSON,"large":$IMAGE_JSON,"medium":$IMAGE_JSON,"small":$IMAGE_JSON}"""

        val formats = mapper.readValue(json, RemoteImageFormats::class.java)

        assertEquals("thumbnail_icon.png", formats.large?.name)
        assertEquals("thumbnail_icon.png", formats.medium?.name)
        assertEquals("thumbnail_icon.png", formats.small?.name)
    }

    @Test
    fun `a strapi answer carries its pagination`() {
        val json =
            """{"data":[],"meta":{"pagination":{"page":2,"pageSize":20,"pageCount":5,"total":94}}}"""

        val answer = mapper.readValue(json, StrapiAnswer::class.java)
        val pagination = answer.meta.pagination

        assertTrue(answer.data.isEmpty())
        assertEquals(2, pagination?.page)
        assertEquals(20, pagination?.pageSize)
        assertEquals(5, pagination?.pageCount)
        assertEquals(94, pagination?.total)
    }

    @Test
    fun `a strapi answer without pagination is still read`() {
        val answer = mapper.readValue("""{"data":[],"meta":{}}""", StrapiAnswer::class.java)

        assertNull(answer.meta.pagination)
    }

    @Test
    fun `a strapi element exposes the evaluation attributes`() {
        val json = """
            {"data":[{"id":7,"attributes":{"name":"Sapio","packageName":"com.klee.sapio",
            "icon":null,"rating":1,"microg":1,"rooted":3,"updatedAt":null,"createdAt":null,
            "publishedAt":null,"versionName":"2.9.1","brokenFeatures":null}}],"meta":{}}
        """.trimIndent().replace("\n", "")

        val element = mapper.readValue(json, StrapiAnswer::class.java).data.single()

        assertEquals(7, element.id)
        assertEquals("Sapio", element.attributes.name)
        assertEquals("com.klee.sapio", element.attributes.packageName)
        assertEquals("2.9.1", element.attributes.versionName)
        assertNull(element.attributes.icon)
    }

    private companion object {
        const val IMAGE_JSON = """{"name":"thumbnail_icon.png","hash":"thumbnail_icon_hash",""" +
            """"ext":".png","mime":"image/png","path":null,"width":156,"height":156,""" +
            """"size":4437,"url":"/uploads/thumbnail_icon.png"}"""
    }
}
