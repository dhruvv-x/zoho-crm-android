package com.pookie.octfis.data.model

data class Task(
    val zohoId      : String,
    val subject     : String,
    val dueDate     : String = "",
    val status      : String = "Not Started",
    val priority    : String = "Normal",
    val description : String = "",
    val ownerName   : String = "",
    val ownerId     : String = "",
    val relatedTo   : String = "",   // whatId name
    val contactName : String = "",   // whoId name
    val remindAt    : String = "",
    val closedTime  : String = "",
) {
    // Shims used by list/detail/form screens
    val id    : String get() = zohoId
    val owner : String get() = ownerName
}

data class Meeting(
    val zohoId        : String,
    val title         : String,
    val startDateTime : String = "",
    val endDateTime   : String = "",
    val description   : String = "",
    val location      : String = "",
    val ownerName     : String = "",
    val ownerId       : String = "",
    val allDay        : Boolean = false,
    val participants  : String = "",  // comma-joined display names
) {
    val id    : String get() = zohoId
    val owner : String get() = ownerName
}

data class CrmCall(
    val zohoId        : String,
    val subject       : String,
    val callStartTime : String = "",
    val duration      : String = "",   // display format "MM:SS" — for list/detail only
    val durationRaw   : String = "",   // original "HH:MM" from Zoho — used when saving back
    val callType      : String = "Outbound",
    val status        : String = "Scheduled",
    val description   : String = "",
    val ownerName     : String = "",
    val ownerId       : String = "",
    val contactName   : String = "",  // whoId
    val relatedTo     : String = "",  // whatId
    val direction     : String = "",
) {
    val id        : String get() = zohoId
    val owner     : String get() = ownerName
    val startTime : String get() = callStartTime
}