package com.pookie.octfis.data.repository

import com.pookie.octfis.data.model.CrmCall
import com.pookie.octfis.data.remote.ZohoApiService

class CallRepository(private val api: ZohoApiService) {

    companion object {
        val cache = mutableListOf<CrmCall>()
    }

    private fun map(z: com.pookie.octfis.data.remote.dto.ZohoCall) = CrmCall(
        zohoId        = z.id,
        subject       = z.subject.orEmpty().ifEmpty { "(No Subject)" },
        callStartTime = z.callStartTime.orEmpty(),
        duration      = z.duration.orEmpty(),
        callType      = z.callType.orEmpty().ifEmpty { "Outbound" },
        status        = z.status.orEmpty().ifEmpty { "Scheduled" },
        description   = z.description.orEmpty(),
        ownerName     = z.owner?.name.orEmpty(),
        ownerId       = z.owner?.id.orEmpty(),
        contactName   = z.whoId?.name.orEmpty(),
        relatedTo     = z.whatId?.name.orEmpty(),
        direction     = z.direction.orEmpty(),
    )

    suspend fun getCalls(page: Int = 1): Result<Pair<List<CrmCall>, Boolean>> = runCatching {
        val r = api.getCalls(page = page, perPage = 50)
        val items = r.data?.map { map(it) } ?: emptyList()
        if (page == 1) cache.clear()
        cache.addAll(items)
        Pair(items, r.info?.moreRecords ?: false)
    }

    suspend fun getCallById(zohoId: String): Result<CrmCall> = runCatching {
        val z = api.getCallById(zohoId).data?.firstOrNull()
            ?: error("Call not found: $zohoId")
        map(z)
    }

    suspend fun createCall(
        subject      : String,
        callStartTime: String,
        duration     : String,
        callType     : String,
        status       : String,
        description  : String,
        ownerId      : String,
    ): Result<String> = runCatching {
        val record = buildMap<String, Any> {
            put("Subject",         subject)
            put("Call_Type",       callType)
            put("Call_Status",     status)
            if (callStartTime.isNotBlank()) put("Call_Start_Time", callStartTime)
            if (duration.isNotBlank())      put("Call_Duration",   duration)
            if (description.isNotBlank())   put("Description",     description)
            if (ownerId.isNotBlank())        put("Owner",           mapOf("id" to ownerId))
        }
        val r = api.createCall(mapOf("data" to listOf(record)))
        val result = r.data?.firstOrNull()
        if (result?.status == "success") result.details?.id ?: "created"
        else error(result?.message ?: "Create failed")
    }

    suspend fun updateCall(
        zohoId       : String,
        subject      : String,
        callStartTime: String,
        duration     : String,
        callType     : String,
        status       : String,
        description  : String,
        ownerId      : String,
    ): Result<Unit> = runCatching {
        val record = buildMap<String, Any> {
            put("Subject",         subject)
            put("Call_Type",       callType)
            put("Call_Status",     status)
            put("Call_Start_Time", callStartTime)
            put("Call_Duration",   duration)
            put("Description",     description)
            if (ownerId.isNotBlank()) put("Owner", mapOf("id" to ownerId))
        }
        val r = api.updateCall(zohoId, mapOf("data" to listOf(record)))
        if (r.data?.firstOrNull()?.status != "success")
            error(r.data?.firstOrNull()?.message ?: "Update failed")
        val idx = cache.indexOfFirst { it.zohoId == zohoId }
        if (idx >= 0) cache[idx] = cache[idx].copy(
            subject = subject, callStartTime = callStartTime, duration = duration,
            callType = callType, status = status, description = description,
        )
    }

    suspend fun deleteCall(zohoId: String): Result<Unit> = runCatching {
        val r = api.deleteCall(zohoId)
        if (r.data?.firstOrNull()?.status != "success")
            error(r.data?.firstOrNull()?.message ?: "Delete failed")
        cache.removeAll { it.zohoId == zohoId }
    }
}