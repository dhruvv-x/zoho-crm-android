package com.pookie.octfis.data.model

data class Account(
    val id            : Int,
    val zohoId        : String = "",
    val accountNo     : String = "",
    val name          : String,
    val phone         : String,
    val website       : String = "",
    val industry      : String = "-None-",
    val gstTreatment  : String = "-None-",
    val gstin         : String = "",
    val leadSource    : String = "-None-",
    val accountOwner  : String = "-None-",
    val description   : String = "",
    val billingStreet : String = "",
    val billingStreet2: String = "",
    val billingCity   : String = "",
    val billingState  : String = "",
    val billingCode   : String = "",
    val billingCountry: String = "",
)

data class Contact(
    val id            : Int,
    val zohoId        : String = "",
    val firstName     : String = "",
    val lastName      : String = "",
    val fullName      : String,
    val phone         : String = "",
    val mobile        : String = "",
    val email         : String = "",
    val accountName   : String = "",
    val title         : String = "",
    val department    : String = "",
    val leadSource    : String = "-None-",
    val contactOwner  : String = "-None-",
    val description   : String = "",
    val mailingStreet : String = "",
    val mailingCity   : String = "",
    val mailingState  : String = "",
    val mailingZip    : String = "",
    val mailingCountry: String = "",
)

data class Deal(
    val id              : Int,
    val zohoId          : String = "",
    val name            : String,
    val phone           : String = "",
    val dealName        : String = "",
    val accountName     : String = "",
    val contactName     : String = "",
    val amount          : String = "",
    val closingDate     : String = "",
    val type            : String = "-None-",
    val email           : String = "",
    val dealOwner       : String = "-None-",
    val description     : String = "",
    val stage           : String = "-None-",
    val leadSource      : String = "-None-",
    val leadSourceDrill : String = "",
)

data class QuoteItem(
    val sNo         : Int,
    val brand       : String = "",
    val productName : String = "Product name",
    val description : String = "",
    val quantity    : Int    = 1,
    val price       : Double = 0.0,
)

data class Quote(
    val id          : Int,
    val zohoId      : String = "",
    val name        : String,
    val phone       : String = "",
    val subject     : String = "",
    val accountName : String = "",
    val contactName : String = "",
    val validUntil  : String = "",
    val quoteStage  : String = "Draft",
    val description : String = "",
    val grandTotal  : Double = 0.0,
    val subTotal    : Double = 0.0,
    val discount    : Double = 0.0,
    val tax         : Double = 0.0,
    val items       : List<QuoteItem> = emptyList(),
)

data class CallActivity(val time: String, val subject: String)
data class MeetingActivity(val time: String, val subject: String)
data class TaskActivity(val time: String, val subject: String)

object FakeData {
    val todayCalls    : List<CallActivity>    = emptyList()
    val todayMeetings : List<MeetingActivity> = emptyList()
    val todayTasks    : List<TaskActivity>    = emptyList()
    val upcomingCalls : List<CallActivity>    = emptyList()
    val thisWeekCalls : List<CallActivity>    = emptyList()
}
data class ActivityTask(
    val id       : String,
    val subject  : String,
    val dueDate  : String,
    val status   : String,
    val priority : String,
)

data class ActivityMeeting(
    val id            : String,
    val title         : String,
    val startDateTime : String,
    val endDateTime   : String,
)

data class ActivityCall(
    val id        : String,
    val subject   : String,
    val startTime : String,
    val duration  : String,
    val callType  : String,
    val status    : String,
)