package com.pookie.octfis.data.repository

import com.pookie.octfis.data.model.ActivityCall
import com.pookie.octfis.data.model.ActivityMeeting
import com.pookie.octfis.data.model.ActivityTask
import com.pookie.octfis.data.remote.ZohoApiService

data class DashboardData(
    val tasks    : List<ActivityTask>,
    val meetings : List<ActivityMeeting>,
    val calls    : List<ActivityCall>,
)

class ActivityRepository(private val api: ZohoApiService) {

    suspend fun getDashboardData(): Result<DashboardData> = runCatching {
        val tasks = api.getTasks().data?.map {
            ActivityTask(
                id       = it.id,
                subject  = it.subject.orEmpty().ifEmpty { "(No Subject)" },
                dueDate  = it.dueDate.orEmpty().take(10),
                status   = it.status.orEmpty(),
                priority = it.priority.orEmpty(),
            )
        } ?: emptyList()

        val meetings = api.getEvents().data?.map {
            ActivityMeeting(
                id            = it.id,
                title         = it.title.orEmpty().ifEmpty { "(No Title)" },
                startDateTime = it.startDateTime.orEmpty(),
                endDateTime   = it.endDateTime.orEmpty(),
            )
        } ?: emptyList()

        val calls = api.getCalls().data?.map {
            ActivityCall(
                id        = it.id,
                subject   = it.subject.orEmpty().ifEmpty { "(No Subject)" },
                startTime = it.callStartTime.orEmpty(),
                duration  = it.duration.orEmpty(),
                callType  = it.callType.orEmpty(),
                status    = it.status.orEmpty(),
            )
        } ?: emptyList()

        DashboardData(tasks, meetings, calls)
    }
}