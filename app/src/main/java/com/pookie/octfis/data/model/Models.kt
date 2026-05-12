package com.pookie.octfis.data.model

data class Account(
    val id           : Int,
    val name         : String,
    val phone        : String,
    val website      : String = "",
    val industry     : String = "-None-",
    val gstTreatment : String = "-None-",
    val gstin        : String = "",
    val leadSource   : String = "-None-",
    val accountOwner : String = "-None-",
    val description  : String = "",
    val billingStreet : String = "",
    val billingStreet2: String = "",
    val billingCity   : String = "",
    val billingState  : String = "",
    val billingCode   : String = "",
    val billingCountry: String = "",
)

data class CallActivity(
    val time   : String,
    val subject: String,
)

data class MeetingActivity(
    val time   : String,
    val subject: String,
)

data class TaskActivity(
    val time   : String,
    val subject: String,
)

object FakeData {
    val accounts = (1..8).map { i ->
        Account(
            id    = i,
            name  = "starryskies23",
            phone = "+91-9845787415",
        )
    }

    val todayCalls    : List<CallActivity>    = emptyList()
    val todayMeetings : List<MeetingActivity> = emptyList()
    val todayTasks    : List<TaskActivity>    = emptyList()
    val upcomingCalls : List<CallActivity>    = emptyList()
    val thisWeekCalls : List<CallActivity>    = emptyList()
}