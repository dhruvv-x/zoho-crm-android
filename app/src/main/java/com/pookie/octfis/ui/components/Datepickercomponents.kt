package com.pookie.octfis.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

// ─────────────────────────────────────────────────────────────────────────────
// DatePickerField
//
// Use for date-only fields: Deals → Closing_Date, Tasks → Due_Date
// value / onValueChange format: "yyyy-MM-dd"
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label        : String,
    value        : String,
    modifier     : Modifier = Modifier,
    required     : Boolean  = false,
    onValueChange: (String) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }

    val initialMillis = remember(value) {
        if (value.isBlank()) System.currentTimeMillis()
        else runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(value)?.time
        }.getOrNull() ?: System.currentTimeMillis()
    }
    val pickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value         = value,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(if (required) "$label *" else label) },
            placeholder   = { Text("yyyy-MM-dd") },
            trailingIcon  = {
                Icon(
                    imageVector        = Icons.Default.CalendarToday,
                    contentDescription = "Pick date",
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        // Transparent overlay — captures taps anywhere on the field
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showPicker = true }
        )
    }

    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onValueChange(
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                .format(Date(millis))
                        )
                    }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            },
        ) { DatePicker(state = pickerState) }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DateTimePickerField
//
// Use for datetime fields: Meetings → Start_DateTime / End_DateTime,
//                          Calls    → Call_Start_Time
//
// value / onValueChange format: "yyyy-MM-dd'T'HH:mm:ss"
// Flow: date picker opens → user confirms → time picker opens → confirm
//       → "$date'T'$HH:$mm:00" is emitted
// Display in field: T is replaced with a space, seconds dropped ("yyyy-MM-dd HH:mm")
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerField(
    label        : String,
    value        : String,
    modifier     : Modifier = Modifier,
    required     : Boolean  = false,
    onValueChange: (String) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pendingDate    by remember { mutableStateOf("") }

    // Friendly display: "2025-06-15T09:30:00" → "2025-06-15 09:30"
    val displayValue = value.replace("T", " ").take(16)

    // Pre-seed date picker from existing value
    val initialMillis = remember(value) {
        if (value.isBlank()) System.currentTimeMillis()
        else runCatching {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(value)?.time
        }.getOrNull() ?: System.currentTimeMillis()
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    // Pre-seed time picker from existing value
    val (initHour, initMinute) = remember(value) {
        if (value.length >= 16) {
            val h = value.substring(11, 13).toIntOrNull() ?: 9
            val m = value.substring(14, 16).toIntOrNull() ?: 0
            h to m
        } else 9 to 0
    }
    val timePickerState = rememberTimePickerState(
        initialHour   = initHour,
        initialMinute = initMinute,
        is24Hour      = true,
    )

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value         = displayValue,
            onValueChange = {},
            readOnly      = true,
            label         = { Text(if (required) "$label *" else label) },
            placeholder   = { Text("Select date & time") },
            trailingIcon  = {
                Icon(
                    imageVector        = Icons.Default.CalendarToday,
                    contentDescription = "Pick date and time",
                )
            },
            modifier = Modifier.fillMaxWidth(),
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDatePicker = true }
        )
    }

    // Step 1 — date picker
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton    = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        pendingDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(Date(millis))
                    }
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("Next →") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) { DatePicker(state = datePickerState) }
    }

    // Step 2 — time picker (shown after date is confirmed)
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title            = { Text("Select time") },
            text             = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val result = "%sT%02d:%02d:00".format(
                        pendingDate,
                        timePickerState.hour,
                        timePickerState.minute,
                    )
                    onValueChange(result)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
        )
    }
}