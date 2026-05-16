// engine/module/ModuleEngine.kt
package com.pookie.octfis.engine.module

import com.pookie.octfis.data.remote.ZohoApiService
import com.pookie.octfis.data.remote.dto.ZohoModule
import javax.inject.Inject
import javax.inject.Singleton

data class ActiveModule(
    val apiName      : String,
    val pluralLabel  : String,
    val singularLabel: String,
    val sequence     : Int,
)

@Singleton
class ModuleEngine @Inject constructor(
    private val api: ZohoApiService,
) {
    companion object {
        const val MAX_NAV_TABS = 5

        private val ACTIVITY_MODULE_NAMES = setOf(
            "Tasks", "Events", "Calls", "Activities"
        )
    }

    private var cachedModules: List<ActiveModule>? = null

    suspend fun getActiveModules(forceRefresh: Boolean = false): Result<List<ActiveModule>> {
        if (!forceRefresh) {
            cachedModules?.let { return Result.success(it) }
        }
        return try {
            val response = api.getModules()
            val modules = response.modules
                ?.filter { it.isNavCandidate() }
                ?.sortedBy { it.sequenceNumber }
                ?.map { it.toActiveModule() }
                ?: emptyList()

            cachedModules = modules
            Result.success(modules)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun invalidate() { cachedModules = null }

    private fun ZohoModule.isNavCandidate(): Boolean =
        apiSupported &&
                viewable &&
                generatedType != "activity" &&       // standard Zoho filter
                apiName !in ACTIVITY_MODULE_NAMES    // belt-and-suspenders for null generatedType

    private fun ZohoModule.toActiveModule() = ActiveModule(
        apiName       = apiName,
        pluralLabel   = pluralLabel   ?: moduleName,
        singularLabel = singularLabel ?: moduleName.removeSuffix("s"),
        sequence      = sequenceNumber,
    )
}