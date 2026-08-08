package com.travelplanner.api

import com.travelplanner.model.api.LocationResponse
import com.travelplanner.model.api.ServiceResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RealtimeTrainsApi {

    @GET("gb-nr/location")
    suspend fun getLocationServices(
        @Query("code") code: String,
        @Query("timeFrom") timeFrom: String? = null,
        @Query("detailed") detailed: Boolean = false
    ): LocationResponse

    @GET("gb-nr/service")
    suspend fun getServiceDetails(
        @Query("uniqueIdentity") uniqueIdentity: String,
        @Query("detailed") detailed: Boolean = false
    ): ServiceResponse
}
