package com.pookie.octfis.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TasksResponse(
    @SerializedName("data") val data: List<ZohoTask>?,
    @SerializedName("info") val info: PageInfo?,
)
data class ZohoTask(
    @SerializedName("id")       val id: String,
    @SerializedName("Subject")  val subject: String?,
    @SerializedName("Due_Date") val dueDate: String?,
    @SerializedName("Status")   val status: String?,
    @SerializedName("Priority") val priority: String?,
)

data class EventsResponse(
    @SerializedName("data") val data: List<ZohoEvent>?,
    @SerializedName("info") val info: PageInfo?,
)
data class ZohoEvent(
    @SerializedName("id")             val id: String,
    @SerializedName("Event_Title")    val title: String?,
    @SerializedName("Start_DateTime") val startDateTime: String?,
    @SerializedName("End_DateTime")   val endDateTime: String?,
)

data class CallsResponse(
    @SerializedName("data") val data: List<ZohoCall>?,
    @SerializedName("info") val info: PageInfo?,
)
data class ZohoCall(
    @SerializedName("id")              val id: String,
    @SerializedName("Subject")         val subject: String?,
    @SerializedName("Call_Start_Time") val callStartTime: String?,
    @SerializedName("Call_Duration")   val duration: String?,
    @SerializedName("Call_Type")       val callType: String?,
    @SerializedName("Call_Status")     val status: String?,
)