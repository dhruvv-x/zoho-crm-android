package com.pookie.octfis.engine.form

import com.pookie.octfis.engine.metadata.FieldMetadata
import com.pookie.octfis.engine.metadata.FieldType

// ── Result type ──────────────────────────────────────────────────────────────

sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
}

val ValidationResult.isValid get() = this is ValidationResult.Valid
val ValidationResult.errorMessage get() = (this as? ValidationResult.Invalid)?.message

// ── Engine ───────────────────────────────────────────────────────────────────

object ValidationEngine {

    /**
     * Validate [rawValue] against [field] rules.
     * [rawValue] is always the string the user typed/selected —
     * numeric fields get parsed here, booleans arrive as "true"/"false".
     */
    fun validate(field: FieldMetadata, rawValue: String): ValidationResult {

        val trimmed = rawValue.trim()

        // 1. Required check (skip for BOOLEAN — a false toggle is still a value)
        if (field.required && field.type != FieldType.BOOLEAN && trimmed.isBlank()) {
            return ValidationResult.Invalid("${field.label} is required")
        }

        // 2. If blank and not required — always valid beyond this point
        if (trimmed.isBlank()) return ValidationResult.Valid

        // 3. Type-specific checks
        return when (field.type) {

            FieldType.EMAIL -> {
                if (android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches())
                    ValidationResult.Valid
                else
                    ValidationResult.Invalid("Enter a valid email address")
            }

            FieldType.PHONE -> {
                // Allow digits, spaces, +, -, (, )  — min 7 digits
                val digits = trimmed.filter { it.isDigit() }
                if (digits.length >= 7)
                    ValidationResult.Valid
                else
                    ValidationResult.Invalid("Enter a valid phone number")
            }

            FieldType.URL -> {
                if (android.util.Patterns.WEB_URL.matcher(trimmed).matches())
                    ValidationResult.Valid
                else
                    ValidationResult.Invalid("Enter a valid URL (e.g. https://example.com)")
            }

            FieldType.INTEGER -> {
                // Zoho sometimes returns integers as "255.0" — accept both forms
                val asLong   = trimmed.toLongOrNull()
                val asDouble = trimmed.toDoubleOrNull()?.let {
                    if (it == kotlin.math.floor(it)) it.toLong() else null
                }
                if (asLong != null || asDouble != null)
                    ValidationResult.Valid
                else
                    ValidationResult.Invalid("Enter a whole number")
            }

            FieldType.DECIMAL, FieldType.CURRENCY, FieldType.PERCENT -> {
                trimmed.toDoubleOrNull()
                    ?.let { ValidationResult.Valid }
                    ?: ValidationResult.Invalid("Enter a valid number")
            }

            FieldType.DATE -> {
                // Accepts YYYY-MM-DD
                val dateRegex = Regex("""^\d{4}-\d{2}-\d{2}$""")
                if (dateRegex.matches(trimmed))
                    ValidationResult.Valid
                else
                    ValidationResult.Invalid("Use format YYYY-MM-DD")
            }

            FieldType.DATETIME -> {
                // Accept: YYYY-MM-DD, YYYY-MM-DDTHH:MM, or full ISO with offset/Z
                val dtRegex = Regex("""^\d{4}-\d{2}-\d{2}(T\d{2}:\d{2}(:\d{2}([+-]\d{2}:\d{2}|Z)?)?)?$""")
                if (dtRegex.matches(trimmed))
                    ValidationResult.Valid
                else
                    ValidationResult.Invalid("Use format YYYY-MM-DD or YYYY-MM-DDTHH:MM")
            }

            FieldType.TEXT, FieldType.TEXTAREA, FieldType.RICH_TEXT -> {
                val max = field.maxLength
                if (max != null && trimmed.length > max)
                    ValidationResult.Invalid("Maximum $max characters allowed")
                else
                    ValidationResult.Valid
            }

            // PICKLIST / MULTI_SELECT — value already constrained by UI, just check not blank
            FieldType.PICKLIST, FieldType.MULTI_SELECT -> {
                if (trimmed.isNotBlank()) ValidationResult.Valid
                else ValidationResult.Invalid("${field.label} is required")
            }

            // BOOLEAN, LOOKUP, OWNER, FORMULA, UNKNOWN — no extra validation
            else -> ValidationResult.Valid
        }
    }

    /**
     * Convenience: validate an entire form map at once.
     * Returns a map of apiName → error message (only for invalid fields).
     */
    fun validateAll(
        fields: List<FieldMetadata>,
        values: Map<String, String>,
    ): Map<String, String> {
        return fields
            .mapNotNull { field ->
                val raw = values[field.apiName] ?: ""
                val result = validate(field, raw)
                if (result is ValidationResult.Invalid)
                    field.apiName to result.message
                else
                    null
            }
            .toMap()
    }
}