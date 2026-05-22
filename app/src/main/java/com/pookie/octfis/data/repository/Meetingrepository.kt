package com.pookie.octfis.data.repository

import com.pookie.octfis.data.model.Meeting
import com.pookie.octfis.data.remote.ZohoApiService

class MeetingRepository(private val api: ZohoApiService) {

    companion object {
        val cache = mutableListOf<Meeting>()
    }

    private fun map(z: com.pookie.octfis.data.remote.dto.ZohoEvent) = Meeting(
        zohoId        = z.id,
        title         = z.title.orEmpty().ifEmpty { "(No Title)" },
        // FIX: take(19) strips the timezone offset Zoho appends (e.g. "+05:30").
        // Zoho's write API expects exactly "yyyy-MM-ddTHH:mm:ss" — sending the
        // offset back causes "invalid data" on update.
        startDateTime = z.startDateTime?.take(19).orEmpty(),
        endDateTime   = z.endDateTime?.take(19).orEmpty(),
        description   = z.description.orEmpty(),
        location      = z.location.orEmpty(),
        ownerName     = z.owner?.name.orEmpty(),
        ownerId       = z.owner?.id.orEmpty(),
        allDay        = z.allDay ?: false,
        participants  = z.participants
            ?.mapNotNull { it.name?.ifEmpty { it.email } }
            ?.joinToString(", ") ?: "",
    )

    suspend fun getMeetings(page: Int = 1): Result<Pair<List<Meeting>, Boolean>> = runCatching {
        val r = api.getEvents(page = page, perPage = 50, sortOrder = "desc")
        val items = r.data?.map { map(it) } ?: emptyList()
        if (page == 1) cache.clear()
        cache.addAll(items)
        Pair(items, r.info?.moreRecords ?: false)
    }

    suspend fun getMeetingById(zohoId: String): Result<Meeting> = runCatching {
        val z = api.getEventById(zohoId).data?.firstOrNull()
            ?: error("Meeting not found: $zohoId")
        map(z)
    }

    suspend fun createMeeting(
        title        : String,
        startDateTime: String,
        endDateTime  : String,
        description  : String,
        location     : String,
        ownerId      : String,
    ): Result<String> = runCatching {
        val record = buildMap<String, Any> {
            put("Event_Title",    title)
            put("Start_DateTime", startDateTime)
            put("End_DateTime",   endDateTime)
            if (description.isNotBlank()) put("Description", description)
            if (location.isNotBlank())    put("Location",    location)
            if (ownerId.isNotBlank())     put("Owner",       mapOf("id" to ownerId))
        }
        val r = api.createEvent(mapOf("data" to listOf(record)))
        val result = r.data?.firstOrNull()
        if (result?.status == "success") result.details?.id ?: "created"
        else error(result?.message ?: "Create failed")
    }

    suspend fun updateMeeting(
        zohoId       : String,
        title        : String,
        startDateTime: String,
        endDateTime  : String,
        description  : String,
        location     : String,
        ownerId      : String,
    ): Result<Unit> = runCatching {
        val record = buildMap<String, Any> {
            put("Event_Title",    title)
            put("Start_DateTime", startDateTime)
            put("End_DateTime",   endDateTime)
            // FIX: guard optional fields with isNotBlank() — consistent with createMeeting
            if (description.isNotBlank()) put("Description", description)
            if (location.isNotBlank())    put("Location",    location)
            if (ownerId.isNotBlank())     put("Owner",       mapOf("id" to ownerId))
        }
        val r = api.updateEvent(zohoId, mapOf("data" to listOf(record)))
        if (r.data?.firstOrNull()?.status != "success")
            error(r.data?.firstOrNull()?.message ?: "Update failed")
        val idx = cache.indexOfFirst { it.zohoId == zohoId }
        if (idx >= 0) cache[idx] = cache[idx].copy(
            title = title, startDateTime = startDateTime,
            endDateTime = endDateTime, description = description, location = location,
        )
    }

    suspend fun deleteMeeting(zohoId: String): Result<Unit> = runCatching {
        val r = api.deleteEvent(zohoId)
        if (r.data?.firstOrNull()?.status != "success")
            error(r.data?.firstOrNull()?.message ?: "Delete failed")
        cache.removeAll { it.zohoId == zohoId }
    }
}