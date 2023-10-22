package com.kieronquinn.app.smartspacer.plugins.googlemaps.repositories

import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.gson.Gson
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepository.TrafficLevel
import com.kieronquinn.app.smartspacer.plugin.googlemaps.repositories.GoogleMapsRepositoryImpl
import com.kieronquinn.app.smartspacer.plugin.googlemaps.test.R
import com.kieronquinn.app.smartspacer.plugins.googlemaps.utils.BaseTest
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GoogleMapsRepositoryTests: BaseTest<GoogleMapsRepository>() {
    
    private val gsonMock = mock<Gson>()

    override val sut by lazy {
        GoogleMapsRepositoryImpl(contextMock, gsonMock, scope, Dispatchers.Main)
    }

    @Test
    fun testCalculateTrafficImageOne() = runTest {
        assertTrue(sut.getTrafficState() == null)
        val map = ContextCompat.getDrawable(testContext, R.drawable.map)!!.toBitmap()
        val zoomedIn = sut.calculateTrafficLevel(map, GoogleMapsRepository.ZoomMode.IN)
        val zoomedOut = sut.calculateTrafficLevel(map, GoogleMapsRepository.ZoomMode.OUT)
        assertTrue(zoomedIn == TrafficLevel.MID_TRAFFIC)
        assertTrue(zoomedOut == TrafficLevel.HEAVY_TRAFFIC)
    }

    @Test
    fun testCalculateTrafficImageTwo() = runTest {
        assertTrue(sut.getTrafficState() == null)
        val map = ContextCompat.getDrawable(testContext, R.drawable.map2)!!.toBitmap()
        val zoomedIn = sut.calculateTrafficLevel(map, GoogleMapsRepository.ZoomMode.IN)
        val zoomedOut = sut.calculateTrafficLevel(map, GoogleMapsRepository.ZoomMode.OUT)
        assertTrue(zoomedIn == TrafficLevel.LIGHT_TRAFFIC)
        assertTrue(zoomedOut == TrafficLevel.LIGHT_TRAFFIC)
    }

    @Test
    fun testCalculateTrafficImageThree() = runTest {
        assertTrue(sut.getTrafficState() == null)
        val map = ContextCompat.getDrawable(testContext, R.drawable.map3)!!.toBitmap()
        val zoomedIn = sut.calculateTrafficLevel(map, GoogleMapsRepository.ZoomMode.IN)
        val zoomedOut = sut.calculateTrafficLevel(map, GoogleMapsRepository.ZoomMode.OUT)
        assertTrue(zoomedIn == TrafficLevel.NO_TRAFFIC)
        assertTrue(zoomedOut == TrafficLevel.MID_TRAFFIC)
    }

    @Test
    fun testCalculateTrafficImageFour() = runTest {
        assertTrue(sut.getTrafficState() == null)
        val map = ContextCompat.getDrawable(testContext, R.drawable.map4)!!.toBitmap()
        val zoomedIn = sut.calculateTrafficLevel(map, GoogleMapsRepository.ZoomMode.IN)
        val zoomedOut = sut.calculateTrafficLevel(map, GoogleMapsRepository.ZoomMode.OUT)
        assertTrue(zoomedIn == TrafficLevel.HEAVY_TRAFFIC)
        assertTrue(zoomedOut == TrafficLevel.HEAVY_TRAFFIC)
    }

    @Test
    fun testCalculateTrafficImageFive() = runTest {
        assertTrue(sut.getTrafficState() == null)
        val map = ContextCompat.getDrawable(testContext, R.drawable.map5)!!.toBitmap()
        val zoomedIn = sut.calculateTrafficLevel(map, GoogleMapsRepository.ZoomMode.IN)
        val zoomedOut = sut.calculateTrafficLevel(map, GoogleMapsRepository.ZoomMode.OUT)
        assertTrue(zoomedIn == TrafficLevel.NO_TRAFFIC)
        assertTrue(zoomedOut == TrafficLevel.NO_TRAFFIC)
    }

}