package com.travelplanner.model.api

import com.google.gson.annotations.SerializedName

// Response from /gb-nr/location or /rtt/location
data class LocationResponse(
    @SerializedName("services") val services: List<LocationLineUpObject>? = null,
    @SerializedName("reasons") val reasons: List<ReasonBlock>? = null
)

// Response from /gb-nr/service or /rtt/service
data class ServiceResponse(
    @SerializedName("service") val service: ServiceDetail? = null
)

data class ServiceDetail(
    @SerializedName("scheduleMetadata") val scheduleMetadata: ScheduleMetadata? = null,
    @SerializedName("locations") val locations: List<ServiceLocation>? = null,
    @SerializedName("reasons") val reasons: List<ReasonBlock>? = null
)

data class ServiceLocation(
    @SerializedName("location") val location: GeographicLocation? = null,
    @SerializedName("temporalData") val temporalData: LocationTemporalData? = null,
    @SerializedName("locationMetadata") val locationMetadata: LocationMetadata? = null
)

data class GeographicLocation(
    @SerializedName("description") val description: String? = null,
    @SerializedName("shortCodes") val shortCodes: List<String>? = null
)

data class LocationLineUpObject(
    @SerializedName("temporalData") val temporalData: LocationTemporalData? = null,
    @SerializedName("locationMetadata") val locationMetadata: LocationMetadata? = null,
    @SerializedName("scheduleMetadata") val scheduleMetadata: ScheduleMetadata? = null,
    @SerializedName("origin") val origin: List<LocationPair>? = null,
    @SerializedName("destination") val destination: List<LocationPair>? = null,
    @SerializedName("reasons") val reasons: List<ReasonBlock>? = null
)

data class LocationTemporalData(
    @SerializedName("arrival") val arrival: IndividualTemporalData? = null,
    @SerializedName("departure") val departure: IndividualTemporalData? = null,
    @SerializedName("displayAs") val displayAs: String? = null, // e.g. "CALL", "PASS", "CANCELLED"
    @SerializedName("status") val status: String? = null // e.g. "APPROACHING", "ARRIVING", "AT_PLATFORM"
)

data class IndividualTemporalData(
    @SerializedName("scheduleAdvertised") val scheduleAdvertised: String? = null, // "13:45:00" or ISO-8601
    @SerializedName("realtimeForecast") val realtimeForecast: String? = null,
    @SerializedName("realtimeActual") val realtimeActual: String? = null,
    @SerializedName("realtimeNoReport") val realtimeNoReport: Boolean? = null,
    @SerializedName("realtimeAdvertisedLateness") val realtimeAdvertisedLateness: Int? = null,
    @SerializedName("isCancelled") val isCancelled: Boolean? = null
)

data class LocationMetadata(
    @SerializedName("platform") val platform: PlannedActualData? = null
)

data class PlannedActualData(
    @SerializedName("planned") val planned: String? = null,
    @SerializedName("actual") val actual: String? = null
)

data class ScheduleMetadata(
    @SerializedName("uniqueIdentity") val uniqueIdentity: String? = null,
    @SerializedName("identity") val identity: String? = null,
    @SerializedName("departureDate") val departureDate: String? = null,
    @SerializedName("operator") val operator: Operator? = null,
    @SerializedName("modeType") val modeType: String? = null, // e.g. "TRAIN", "BUS"
    @SerializedName("inPassengerService") val inPassengerService: Boolean? = null
)

data class Operator(
    @SerializedName("code") val code: String? = null,
    @SerializedName("name") val name: String? = null
)

data class LocationPair(
    @SerializedName("location") val location: GeographicLocation? = null,
    @SerializedName("temporalData") val temporalData: IndividualTemporalData? = null
)

data class ReasonBlock(
    @SerializedName("type") val type: String? = null, // "DELAY" or "CANCEL"
    @SerializedName("code") val code: String? = null,
    @SerializedName("shortText") val shortText: String? = null,
    @SerializedName("longText") val longText: String? = null
)
