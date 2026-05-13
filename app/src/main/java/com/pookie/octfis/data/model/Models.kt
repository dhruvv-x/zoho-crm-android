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

data class Deal(
    val id              : Int,
    val name            : String,
    val phone           : String,
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
    val brand       : String = "Brand",
    val productName : String = "Product name",
    val description : String = "Description",
    val quantity    : Int    = 1,
    val price       : Double = 0.0,
)

data class Quote(
    val id          : Int,
    val name        : String,
    val phone       : String,
    val subject     : String = "",
    val accountName : String = "",
    val contactName : String = "",
    val validUntil  : String = "10/05/2026",
    val quoteStage  : String = "Draft",
    val description : String = "",
    val items       : List<QuoteItem> = listOf(
        QuoteItem(1, price = 10.99),
        QuoteItem(2, price = 8.99),
    ),
)

data class CallActivity(val time: String, val subject: String)
data class MeetingActivity(val time: String, val subject: String)
data class TaskActivity(val time: String, val subject: String)

object FakeData {
    val accounts = (1..8).map { i ->
        Account(id = i, name = "starryskies23", phone = "+91-9845787415")
    }
    val deals = (1..8).map { i ->
        Deal(id = i, name = "starryskies23", phone = "+91-9845787415")
    }
    val quotes = (1..8).map { i ->
        Quote(id = i, name = "starryskies23", phone = "+91-9845787415")
    }

    val todayCalls    : List<CallActivity>    = emptyList()
    val todayMeetings : List<MeetingActivity> = emptyList()
    val todayTasks    : List<TaskActivity>    = emptyList()
    val upcomingCalls : List<CallActivity>    = emptyList()
    val thisWeekCalls : List<CallActivity>    = emptyList()
}