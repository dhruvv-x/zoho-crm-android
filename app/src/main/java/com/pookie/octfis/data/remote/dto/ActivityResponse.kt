package com.pookie.octfis.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Tasks ─────────────────────────────────────────────────────────────────────

data class TasksResponse(
    @SerializedName("data") val data: List<ZohoTask>?,
    @SerializedName("info") val info: PageInfo?,
)

data class ZohoTask(
    @SerializedName("id")           val id: String,
    @SerializedName("Subject")      val subject: String?,
    @SerializedName("Due_Date")     val dueDate: String?,
    @SerializedName("Status")       val status: String?,
    @SerializedName("Priority")     val priority: String?,
    @SerializedName("Description")  val description: String?,
    @SerializedName("Owner")        val owner: ZohoOwner?,
    @SerializedName("What_Id")      val whatId: ZohoTaskRelated?,
    @SerializedName("Who_Id")       val whoId: ZohoTaskRelated?,
    @SerializedName("Remind_At")    val remindAt: FlexibleReminder?,
    @SerializedName("Closed_Time")  val closedTime: String?,
)

data class ZohoTaskRelated(
    @SerializedName("name") val name: String?,
    @SerializedName("id")   val id: String?,
)

// ── Events / Meetings ─────────────────────────────────────────────────────────

data class EventsResponse(
    @SerializedName("data") val data: List<ZohoEvent>?,
    @SerializedName("info") val info: PageInfo?,
)

data class ZohoEvent(
    @SerializedName("id")               val id: String,
    @SerializedName("Event_Title")      val title: String?,
    @SerializedName("Start_DateTime")   val startDateTime: String?,
    @SerializedName("End_DateTime")     val endDateTime: String?,
    @SerializedName("Description")      val description: String?,
    @SerializedName("Location")         val location: String?,
    @SerializedName("Owner")            val owner: ZohoOwner?,
    @SerializedName("All_day")          val allDay: Boolean?,
    @SerializedName("Remind_At")        val remindAt: FlexibleReminder?,
    @SerializedName("Participants")     val participants: List<ZohoParticipant>?,
)

// Shared reminder class — handles both Task and Event Remind_At
data class FlexibleReminder(
    @SerializedName("period") val period: String?,
    @SerializedName("unit")   val unit: String?,
)

data class ZohoParticipant(
    @SerializedName("name")  val name: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("type")  val type: String?,
)

// ── Calls ─────────────────────────────────────────────────────────────────────

data class CallsResponse(
    @SerializedName("data") val data: List<ZohoCall>?,
    @SerializedName("info") val info: PageInfo?,
)

/**
 * ISSUE 4 FIX:
 * "Call_Duration_In_Seconds" is mapped to [durationSeconds].
 * CallRepository.map() converts it to "MM:SS" format.
 * "Call_Duration" (HH:MM string from Zoho) is kept as fallback.
 */
data class ZohoCall(
    @SerializedName("id")                       val id: String,
    @SerializedName("Subject")                  val subject: String?,
    @SerializedName("Call_Start_Time")          val callStartTime: String?,
    @SerializedName("Call_Duration")            val duration: String?,
    @SerializedName("Call_Duration_In_Seconds") val durationSeconds: Int?,
    @SerializedName("Call_Type")                val callType: String?,
    @SerializedName("Call_Status")              val status: String?,
    @SerializedName("Description")              val description: String?,
    @SerializedName("Owner")                    val owner: ZohoOwner?,
    @SerializedName("Who_Id")                   val whoId: ZohoTaskRelated?,
    @SerializedName("What_Id")                  val whatId: ZohoTaskRelated?,
    @SerializedName("Direction")                val direction: String?,
)