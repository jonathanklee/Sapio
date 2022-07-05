package com.klee.sapio

import android.os.Build
import com.klee.sapio.model.ApplicationsRepository
import com.parse.ParseFile
import com.parse.ParseObject
import com.parse.ParseQuery
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [Build.VERSION_CODES.N])
class ApplicationsRepositoryTest {

    private val repository = ApplicationsRepository()

    @Mock
    val mockedQuery = ParseQuery<ParseObject>("LibreApps")

    @Mock
    private lateinit var mockedParseObjectOne: ParseObject

    @Mock
    private lateinit var mockedParseObjectTwo: ParseObject

    @Mock
    private lateinit var mockedParseObjectThree: ParseObject

    @Mock
    private lateinit var mockedParseObjectFour: ParseObject

    @Mock
    private lateinit var mockedParseFile: ParseFile

    private lateinit var fakeList: List<ParseObject>

    @Before
    fun setUp() {

        MockitoAnnotations.openMocks(this)

        mockBehavior(mockedParseObjectOne, "ApplicationOne")
        mockBehavior(mockedParseObjectTwo, "ApplicationTwo")
        mockBehavior(mockedParseObjectThree, "ApplicationThree")
        mockBehavior(mockedParseObjectFour, "ApplicationFour")

        fakeList = arrayListOf(
            mockedParseObjectOne,
            mockedParseObjectTwo,
            mockedParseObjectThree,
            mockedParseObjectFour)

        Mockito.`when`(mockedQuery.find()).thenReturn(fakeList)

        repository.query = mockedQuery
    }

    private fun mockBehavior(mockedParseObject: ParseObject, appName: String) {
        Mockito.`when`(mockedParseObject.createdAt).thenReturn(getCurrentDate())
        Mockito.`when`(mockedParseObject.updatedAt).thenReturn(getCurrentDate())
        Mockito.`when`(mockedParseObject.getString("name")).thenReturn(appName)
        Mockito.`when`(mockedParseObject.getString("package")).thenReturn("fake.application")
        Mockito.`when`(mockedParseObject.getParseFile("icon")).thenReturn(mockedParseFile)
        Mockito.`when`(mockedParseObject.getInt("rating")).thenReturn(0)
        Mockito.`when`(mockedParseObject.getInt("microg")).thenReturn(0)
        Mockito.`when`(mockedParseObject.getInt("rooted")).thenReturn(0)
    }

    private fun getCurrentDate(): Date = Calendar.getInstance().time

    @Test
    fun `get feed applications and check the number of application received`() = runTest {
        val result = repository.getFeedApplications()
        assertEquals("Check list size", fakeList.size, result.size)
    }

    @Test
    fun `get feed applications and check the first element name`() = runTest {
        val result = repository.getFeedApplications()
        assertEquals("Check first application name", "ApplicationOne", result[0].name)
    }

    @Test
    fun `geet feed applications and check the third element name`() = runTest {
        val result = repository.getFeedApplications()
        assertEquals("Check first application name", "ApplicationThree", result[2].name)
    }

    @Test
    fun `search applications and check whereMatches() is called`() = runTest {
        repository.searchApplications("One")
        Mockito.verify(mockedQuery, times(1)).whereMatches(eq("name"), eq("One"), eq("i"))
    }

    @Test
    fun `search applications and check results`() = runTest {
        Mockito.`when`(mockedQuery.find()).thenReturn(arrayListOf(mockedParseObjectOne))

        val result = repository.searchApplications("One")
        assertEquals("Check search results", 1, result.size)
    }

    @Test
    fun `search applications and check empty results`() = runTest {
        Mockito.`when`(mockedQuery.find()).thenReturn(arrayListOf())

        val result = repository.searchApplications("Five")
        assertEquals("Check search results", 0, result.size)
    }
}
