package com.pookie.octfis.data.repository

import android.util.Log
import com.pookie.octfis.data.model.CrmCall
import com.pookie.octfis.data.remote.ZohoApiService

class CallRepository(
    private val api: ZohoApiService
) {

    companion object {
        val cache = mutableListOf<CrmCall>()
    }

    /**
     * duration      → display-only "MM:SS" built from Call_Duration_In_Seconds (or raw fallback)
     * durationRaw   → original "HH:MM" string from Zoho, sent back unchanged on create/update
     *
     * Zoho's write API expects Call_Duration in "HH:MM" format.
     * The edit form pre-fills from durationRaw so the round-trip is lossless.
     */
    private fun map(
        z: com.pookie.octfis.data.remote.dto.ZohoCall
    ) = CrmCall(

        zohoId = z.id,

        subject = z.subject
            .orEmpty()
            .ifEmpty { "(No Subject)" },

        callStartTime = z.callStartTime.orEmpty(),

        duration = run {
            val secs = z.durationSeconds
            if (secs != null && secs > 0) {
                // Prefer seconds field → display as "MM:SS"
                String.format("%02d:%02d", secs / 60, secs % 60)
            } else {
                // Fallback to "HH:MM" string Zoho returns
                z.duration.orEmpty()
            }
        },

        // FIX: preserve the raw "HH:MM" value Zoho sends so the edit form can
        // send it back unchanged — avoids "invalid data" on update.
        durationRaw = z.duration.orEmpty(),

        callType = z.callType
            .orEmpty()
            .ifEmpty { "Outbound" },

        status = z.status
            .orEmpty()
            .ifEmpty { "Scheduled" },

        description = z.description.orEmpty(),

        ownerName = z.owner?.name.orEmpty(),

        ownerId = z.owner?.id.orEmpty(),

        contactName = z.whoId?.name.orEmpty(),

        relatedTo = z.whatId?.name.orEmpty(),

        direction = z.direction.orEmpty()
    )

    suspend fun getCalls(
        page: Int = 1
    ): Result<Pair<List<CrmCall>, Boolean>> = runCatching {

        Log.d("ZOHO_CALLS", "Fetching calls page=$page")

        val r = api.getCalls(
            page = page,
            perPage = 50
        )

        Log.d("ZOHO_CALLS", r.toString())

        val items = r.data?.map {
            map(it)
        } ?: emptyList()

        if (page == 1) {
            cache.clear()
        }

        cache.addAll(items)

        Pair(
            items,
            r.info?.moreRecords ?: false
        )
    }

    suspend fun getCallsForContact(
        contactId: String,
        page: Int = 1,
    ): Result<List<CrmCall>> = runCatching {
        val r = api.getCallsForContact(contactId, page)
        r.data?.map { map(it) } ?: emptyList()
    }

    suspend fun getCallById(
        zohoId: String
    ): Result<CrmCall> = runCatching {

        Log.d("ZOHO_CALL_DETAIL", "Loading id=$zohoId")

        val z = api.getCallById(zohoId)
            .data
            ?.firstOrNull()
            ?: error("Call not found: $zohoId")

        map(z)
    }

    suspend fun createCall(
        subject: String,
        callStartTime: String,
        duration: String,
        callType: String,
        status: String,
        description: String,
        ownerId: String,
        whoId: String = "",
    ): Result<String> = runCatching {

        val record = buildMap<String, Any> {

            put("Subject", subject)

            put("Call_Type", callType)

            put("Call_Status", status)

            put("Outgoing_Call_Status", "Completed")

            if (description.isNotBlank()) {
                put("Description", description)
            }

            if (ownerId.isNotBlank()) {
                put(
                    "Owner",
                    mapOf("id" to ownerId)
                )
            }

            if (callStartTime.isNotBlank()) {
                put("Call_Start_Time", callStartTime)
            }

            if (duration.isNotBlank()) {
                put("Call_Duration", duration)
            }

            if (whoId.isNotBlank()) {
                put(
                    "Who_Id",
                    mapOf("id" to whoId)
                )
                put(
                    "\$se_module",
                    "Contacts"
                )
            }
        }

        Log.d(
            "ZOHO_CREATE",
            "REQUEST = $record"
        )

        val r = api.createCall(
            mapOf("data" to listOf(record))
        )

        Log.d(
            "ZOHO_CREATE",
            "RESPONSE = $r"
        )

        val result = r.data?.firstOrNull()

        Log.d(
            "ZOHO_CREATE",
            "status=${result?.status} message=${result?.message}"
        )

        if (
            result?.status.equals(
                "success",
                true
            )
        ) {

            result?.details?.id ?: "created"

        } else {

            error(
                result?.message
                    ?: "Create failed"
            )
        }
    }

    suspend fun updateCall(
        zohoId: String,
        subject: String,
        callStartTime: String,
        duration: String,
        callType: String,
        status: String,
        description: String,
        ownerId: String,
    ): Result<Unit> = runCatching {

        val record = buildMap<String, Any> {

            // zohoId goes in the URL path via @PUT("Calls/{id}"), not in the body

            put("Subject", subject)

            put("Call_Type", callType)

            put("Call_Status", status)

            put("Outgoing_Call_Status", "Completed")

            if (description.isNotBlank()) {
                put("Description", description)
            }

            if (ownerId.isNotBlank()) {
                put(
                    "Owner",
                    mapOf("id" to ownerId)
                )
            }

            if (callStartTime.isNotBlank()) {
                put(
                    "Call_Start_Time",
                    callStartTime
                )
            }

            if (duration.isNotBlank()) {
                put(
                    "Call_Duration",
                    duration
                )
            }
        }

        val payload = mapOf(
            "data" to listOf(record)
        )

        android.util.Log.d(
            "ZOHO_UPDATE",
            "PAYLOAD = $payload"
        )

        val r = api.updateCall(zohoId, payload)

        android.util.Log.d(
            "ZOHO_UPDATE",
            "RESPONSE = $r"
        )

        android.util.Log.d(
            "ZOHO_FULL_RESPONSE",
            r.toString()
        )

        val result = r.data?.firstOrNull()

        android.util.Log.d(
            "ZOHO_STATUS",
            "status=${result?.status}"
        )

        android.util.Log.d(
            "ZOHO_MESSAGE",
            "message=${result?.message}"
        )

        android.util.Log.d(
            "ZOHO_DETAILS",
            "details=${result?.details}"
        )

        if (
            !result?.status.equals(
                "success",
                true
            )
        ) {

            throw Exception(
                "Zoho Error -> ${result?.message}"
            )
        }

        val idx = cache.indexOfFirst {
            it.zohoId == zohoId
        }

        if (idx >= 0) {

            cache[idx] = cache[idx].copy(

                subject = subject,

                callStartTime = callStartTime,

                duration = duration,

                callType = callType,

                status = status,

                description = description
            )
        }
    }

    suspend fun deleteCall(
        zohoId: String
    ): Result<Unit> = runCatching {

        Log.d(
            "ZOHO_DELETE",
            "Deleting id=$zohoId"
        )

        val r = api.deleteCall(zohoId)

        Log.d(
            "ZOHO_DELETE",
            r.toString()
        )

        val result = r.data?.firstOrNull()

        if (
            !result?.status.equals(
                "success",
                true
            )
        ) {

            error(
                result?.message
                    ?: "Delete failed"
            )
        }

        cache.removeAll {
            it.zohoId == zohoId
        }
    }
}