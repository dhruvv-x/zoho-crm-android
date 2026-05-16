package com.pookie.octfis.engine.form

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pookie.octfis.engine.metadata.FieldMetadata

/**
 * Manages form field values, dirty state, and validation errors.
 * Designed to live inside a ViewModel — create with `remember { FormStateManager() }`
 * inside a Composable only for simple one-off forms.
 */
class FormStateManager {

    // ── Field values ──────────────────────────────────────────────────────────
    // apiName → current string value
    private val _values = mutableStateMapOf<String, String>()
    val values: Map<String, String> get() = _values

    // ── Dirty tracking ────────────────────────────────────────────────────────
    // apiName → true if the user has touched/changed this field
    private val _dirty = mutableStateMapOf<String, Boolean>()

    // ── Validation errors ─────────────────────────────────────────────────────
    // apiName → error message (only present when invalid)
    private val _errors = mutableStateMapOf<String, String>()
    val errors: Map<String, String> get() = _errors

    // ── Submit attempted flag ─────────────────────────────────────────────────
    // Once true, errors show on ALL fields (not just dirty ones)
    var submitAttempted by mutableStateOf(false)
        private set

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Seed the form with initial values (e.g. when editing an existing record).
     * Does NOT mark fields as dirty.
     */
    fun initialize(initial: Map<String, String>) {
        _values.clear()
        _values.putAll(initial)
        _dirty.clear()
        _errors.clear()
        submitAttempted = false
    }

    /**
     * Called by DynamicFormRenderer whenever a field changes.
     * Validates immediately so the user gets live feedback after first touch.
     */
    fun onFieldChange(
        apiName: String,
        newValue: String,
        field: FieldMetadata,
    ) {
        _values[apiName] = newValue
        _dirty[apiName]  = true

        // Live validation — only show error after field is dirty
        val result = ValidationEngine.validate(field, newValue)
        if (result is ValidationResult.Invalid) {
            _errors[apiName] = result.message
        } else {
            _errors.remove(apiName)
        }
    }

    /**
     * Call on form submit. Validates ALL fields regardless of dirty state.
     * Returns true if the form is valid and ready to submit.
     */
    fun validateAll(fields: List<FieldMetadata>): Boolean {
        submitAttempted = true
        _errors.clear()

        fields.forEach { field ->
            val raw    = _values[field.apiName] ?: ""
            val result = ValidationEngine.validate(field, raw)
            if (result is ValidationResult.Invalid) {
                _errors[field.apiName] = result.message
            }
        }

        return _errors.isEmpty()
    }

    /**
     * The errors to actually display — respects dirty/submitAttempted state
     * so we don't yell at the user before they've touched anything.
     */
    fun visibleErrors(): Map<String, String> {
        return if (submitAttempted) {
            _errors
        } else {
            _errors.filter { (apiName, _) -> _dirty[apiName] == true }
        }
    }

    /**
     * Returns the current values as a clean Map ready to POST to Zoho API.
     * Strips blank values for optional fields.
     */
    fun toPayload(fields: List<FieldMetadata>): Map<String, String> {
        return fields
            .mapNotNull { field ->
                val value = _values[field.apiName]?.trim() ?: ""
                if (value.isBlank() && !field.required) null
                else field.apiName to value
            }
            .toMap()
    }

    /** True if any field has been touched by the user. */
    val isDirty: Boolean get() = _dirty.values.any { it }

    /** Reset everything back to empty. */
    fun reset() {
        _values.clear()
        _dirty.clear()
        _errors.clear()
        submitAttempted = false
    }
}