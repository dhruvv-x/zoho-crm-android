package com.pookie.octfis.engine.metadata

enum class FieldType {
    // Text variants
    TEXT,
    EMAIL,
    PHONE,
    URL,
    TEXTAREA,
    RICH_TEXT,

    // Numeric
    INTEGER,
    DECIMAL,
    CURRENCY,
    PERCENT,

    // Selection
    PICKLIST,
    MULTI_SELECT,
    BOOLEAN,

    // Date/Time
    DATE,
    DATETIME,

    // Relations
    LOOKUP,
    OWNER,

    // Special
    FORMULA,    // always read-only, server computed
    UNKNOWN,    // fallback for unmapped types
}