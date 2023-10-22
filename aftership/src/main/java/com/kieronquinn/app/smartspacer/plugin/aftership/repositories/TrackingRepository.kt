package com.kieronquinn.app.smartspacer.plugin.aftership.repositories

import android.content.Context
import android.location.Geocoder
import com.kieronquinn.app.smartspacer.plugin.aftership.model.database.Package.Tracking
import com.kieronquinn.app.smartspacer.plugin.aftership.utils.extensions.getFromLocationName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

interface TrackingRepository {

    suspend fun getTrackingInfo(trackingUrl: String): Tracking?

}

class TrackingRepositoryImpl(context: Context): TrackingRepository {

    private val geocoder = Geocoder(context)

    override suspend fun getTrackingInfo(trackingUrl: String): Tracking? {
        return withContext(Dispatchers.IO) {
            try {
                getTrackingInfoForUrl(trackingUrl)
            }catch (e: Exception) {
                null
            }
        }
    }

    /**
     *  Scrapes the latest tracking location, date and title from Aftership's (public) web interface
     *
     *  This interface needs no authentication, once you have a full tracking URL (same as courier
     *  websites). It has obfuscated CSS classes, so instead we use static tag positions. The
     *  location is then reverse-geocoded into a (hopefully usable) LatLng, if available.
     */
    private suspend fun getTrackingInfoForUrl(trackingUrl: String): Tracking? {
        val document = Jsoup.connect(trackingUrl).get()
        val trackingSection = document.select("section").getOrNull(2) ?: return null
        val latestTracking = trackingSection.select("li").firstOrNull() ?: return null
        val date = latestTracking.select("p").getOrNull(0)?.text() ?: return null
        val time = latestTracking.select("p").getOrNull(1)?.text() ?: return null
        val title = latestTracking.select("span").getOrNull(0)?.text() ?: return null
        val location = latestTracking.select("p").getOrNull(3)?.text()
        val latLng = location?.let {
            geocoder.getFromLocationName(it)
        }
        return Tracking(title, date, time, latLng?.latitude, latLng?.longitude)
    }

}