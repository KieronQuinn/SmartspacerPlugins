package com.kieronquinn.app.smartspacer.plugin.youtube.service

import com.kieronquinn.app.smartspacer.plugin.youtube.model.YouTubeStatisticsResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeService {

    companion object {
        private const val BASE_URL = "https://www.googleapis.com/youtube/v3/"

        fun createService(): YouTubeService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(YouTubeService::class.java)
        }
    }

    @GET("channels?part=statistics,snippet")
    fun getStatistics(
        @Query("key") apiKey: String,
        @Query("id") channelId: String
    ): Call<YouTubeStatisticsResponse>

}