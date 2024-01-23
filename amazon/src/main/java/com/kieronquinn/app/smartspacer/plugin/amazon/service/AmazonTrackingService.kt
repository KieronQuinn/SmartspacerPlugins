package com.kieronquinn.app.smartspacer.plugin.amazon.service

import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.TrackingData
import com.kieronquinn.app.smartspacer.plugin.amazon.model.api.TrackingStatus
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AmazonTrackingService {

    @FormUrlEncoded
    @POST("https://www.{domain}/progress-tracker/package/actions/map-tracking-deans-proxy")
    fun getTrackingData(
        @Path("domain")
        domain: String,
        @Header("Cookie")
        cookie: String,
        @Field("trackingId")
        trackingId: String,
        @Field("csrfToken")
        csrfToken: String
    ): Call<TrackingData>

    @FormUrlEncoded
    @POST("https://www.{domain}/progress-tracker/package/actions/map-tracking/memoize")
    fun getTrackingStatus(
        @Path("domain")
        domain: String,
        @Header("Cookie")
        cookie: String,
        @Field("status")
        status: String,
        @Field("stops")
        stops: String,
        @Field("timezone")
        timezone: String,
        @Field("customerId")
        customerId: String,
        @Field("trackingId")
        trackingId: String,
        @Field("csrfToken")
        csrfToken: String
    ): Call<TrackingStatus>

}