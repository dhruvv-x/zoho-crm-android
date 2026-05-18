// engine/metadata/MetadataEngine.kt
package com.pookie.octfis.engine.metadata

import com.pookie.octfis.data.remote.ZohoApiService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MetadataEngine @Inject constructor(
    private val api: ZohoApiService,
) {
    private val cache = mutableMapOf<String, List<FieldMetadata>>()

    suspend fun getModuleMetadata(module: String): Result<List<FieldMetadata>> {
        cache[module]?.let { return Result.success(it) }

        return try {
            // ── Parallel fetch: fields + layouts ─────────────────────────────
            val (fieldsResponse, sectionMap) = coroutineScope {
                val fieldsDeferred  = async { api.getFields(module) }
                val layoutsDeferred = async {
                    runCatching { api.getLayouts(module) }.getOrNull()
                }

                val fields  = fieldsDeferred.await()
                val layouts = layoutsDeferred.await()

                // Build apiName → sectionName from the first active layout
                // (status == 0 means active in Zoho; fallback to first layout)
                val activeLayout = layouts?.layouts
                    ?.firstOrNull { it.status == 0 }
                    ?: layouts?.layouts?.firstOrNull()

                val map = mutableMapOf<String, String>()
                activeLayout?.sections?.forEach { section ->
                    section.fields?.forEach { layoutField ->
                        map[layoutField.apiName] = section.name
                    }
                }

                fields to map
            }

            // ── Build FieldMetadata list ──────────────────────────────────────
            val fields = fieldsResponse.fields
                ?.map { zohoField ->
                    FieldMetadata(
                        apiName        = zohoField.apiName,
                        label          = zohoField.fieldLabel,
                        type           = FieldTypeResolver.resolve(
                            zohoField.dataType,
                            zohoField.jsonType,
                        ),
                        required       = zohoField.mandatory,
                        readOnly       = zohoField.readOnly,
                        maxLength      = zohoField.length,
                        sequence       = zohoField.sequenceNumber,
                        sectionName    = sectionMap[zohoField.apiName] ?: "Details",
                        pickListValues = zohoField.pickListValues ?: emptyList(),
                        lookupModule   = zohoField.lookup?.module,
                        defaultValue   = zohoField.defaultValue,
                        tooltip        = zohoField.tooltip?.name,
                    )
                }
                ?.filter { field ->
                    when {
                        field.type == FieldType.FORMULA           -> false
                        field.type == FieldType.UNKNOWN           -> false
                        field.readOnly && !field.required         -> false
                        else                                      -> true
                    }
                }
                ?.sortedBy { it.sequence }
                ?: emptyList()

            cache[module] = fields
            Result.success(fields)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun invalidate(module: String) { cache.remove(module) }
    fun invalidateAll() { cache.clear() }
}